package com.adam.shareofshelf.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.adam.shareofshelf.R
import com.adam.shareofshelf.ui.data.BranchDataModel

class BranchAdapter(
    private val branches : ArrayList<BranchDataModel>,
    private val onBranchClickListener: OnBranchClickListener
) :
    RecyclerView.Adapter<BranchAdapter.BranchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BranchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.branch_item, parent, false)
        return BranchViewHolder(view)
    }

    override fun onBindViewHolder(holder: BranchViewHolder, position: Int) {
        val branch = branches[position]
        holder.bind(branch, onBranchClickListener)
    }

    override fun getItemCount(): Int {
        return branches.size
    }

    class BranchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val parentView: RelativeLayout = itemView.findViewById(R.id.layoutParent)
        private val tvBrandName: TextView =
            itemView.findViewById(R.id.tvBrandName)
        private val tvProductName: TextView =
            itemView.findViewById(R.id.tvProductName)

        fun bind(branch: BranchDataModel, onBranchClickListener: OnBranchClickListener) {
            tvBrandName.text = branch.brandName ?: "N/A"
            tvProductName.text = branch.productName ?: "N/A"
            parentView.setOnClickListener {
                it.setBackgroundResource(R.drawable.selected_branch)
                onBranchClickListener.onBranchClick(branch)
            }
        }
    }
}

interface  OnBranchClickListener{

    fun onBranchClick(model : BranchDataModel)
}