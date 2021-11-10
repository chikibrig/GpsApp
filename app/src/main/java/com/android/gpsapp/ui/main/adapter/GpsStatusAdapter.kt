package com.android.gpsapp.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.gpsapp.R
import com.android.gpsapp.data.model.ConstellationType
import com.android.gpsapp.databinding.StatusRowItemBinding

class GpsStatusAdapter : RecyclerView.Adapter<GpsStatusAdapter.ViewHolder>() {

    class ViewHolder(private val binding: StatusRowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = StatusRowItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return
    }

}