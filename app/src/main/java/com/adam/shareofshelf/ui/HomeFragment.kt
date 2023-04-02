package com.adam.shareofshelf.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adam.shareofshelf.R
import com.adam.shareofshelf.retrofit.DaeemServiceInterface
import com.adam.shareofshelf.retrofit.RetrofitClient
import com.adam.shareofshelf.ui.adapter.CustomerAdapter
import com.adam.shareofshelf.ui.data.CustomerDataModel
import com.adam.shareofshelf.utils.Constants.INTENT_ID
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var id = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var progress: ProgressBar
    private var customerDataModel: ArrayList<CustomerDataModel> = arrayListOf()

    companion object {
        fun newInstance(id: String): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle().apply {
                putString(INTENT_ID, id)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
         id = arguments?.getString(INTENT_ID) ?: ""
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        progress = view.findViewById(R.id.progress)
        recyclerView = view.findViewById(R.id.rvCustomerList)

        fetchCustomerList()
        return view
    }

    private fun fetchCustomerList() {
        val retrofit = RetrofitClient.getInstance()
        val apiInterface = retrofit.create(DaeemServiceInterface::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiInterface.fetchListOfCustomers(id, "branch").enqueue(
                    object : Callback<ArrayList<CustomerDataModel>> {
                        override fun onResponse(
                            call: Call<ArrayList<CustomerDataModel>>,
                            response: Response<ArrayList<CustomerDataModel>>
                        ) {
                            progress.visibility = View.GONE
                            response.body()?.let {
                                customerDataModel = it
                                setAdapter()
                            }
                        }

                        override fun onFailure(
                            call: Call<ArrayList<CustomerDataModel>>,
                            t: Throwable
                        ) {
                            progress.visibility = View.GONE
                            Toast.makeText(requireContext(), t.toString(), Toast.LENGTH_LONG).show()
                        }

                    }
                )
            } catch (Ex: Exception) {
                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE
                }
                Ex.localizedMessage?.let { Log.e("Error", it) }
            }
        }
    }

    private fun setAdapter() {

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CustomerAdapter(customerDataModel)
    }

}