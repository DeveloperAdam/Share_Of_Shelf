package com.adam.shareofshelf.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adam.shareofshelf.R
import com.adam.shareofshelf.ui.data.CustomerDataModel

class CustomerAdapter(private val customers: ArrayList<CustomerDataModel>) :
    RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.customer_item, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customers[position]
        holder.bind(customer)
    }

    override fun getItemCount(): Int {
        return customers.size
    }

    class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBranchName: TextView =
            itemView.findViewById(R.id.tvBranchName)
        private val tvCustomerName: TextView =
            itemView.findViewById(R.id.tvCustomerName)

        fun bind(customer: CustomerDataModel) {
            tvBranchName.text = customer.branchName ?: "N/A"
            tvCustomerName.text = customer.customerName ?: "N/A"
        }
    }
}
