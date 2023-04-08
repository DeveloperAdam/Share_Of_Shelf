package com.adam.shareofshelf.ui

import android.content.Intent
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
import com.adam.shareofshelf.ui.adapter.OnItemClickListener
import com.adam.shareofshelf.ui.data.CustomerDataModel
import com.adam.shareofshelf.utils.Constants.INTENT_CUSTOMER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment(), OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progress: ProgressBar
    private var customerDataModel: ArrayList<CustomerDataModel> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        progress = view.findViewById(R.id.progress)
        recyclerView = view.findViewById(R.id.rvBranchList)

        fetchCustomerList()
        return view
    }

    private fun fetchCustomerList() {
        val retrofit = RetrofitClient.getInstance()
        val apiInterface = retrofit.create(DaeemServiceInterface::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiInterface.fetchListOfCustomers((activity as HostActivity).id).enqueue(
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
        recyclerView.adapter = CustomerAdapter(customerDataModel, this)
    }

    override fun onItemClick(model: CustomerDataModel) {

        startActivity(Intent(requireActivity(),DashboardActivity::class.java).apply {
            putExtra(INTENT_CUSTOMER, model)
        })
    }

}