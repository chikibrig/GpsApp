package com.android.gpsapplication.ui.screens.statusscreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.gpsapplication.R
import com.android.gpsapplication.databinding.StatusRowItemBinding
import com.android.gpsapplication.model.SatelliteStatus

class SatelliteStatusListAdapter : RecyclerView.Adapter<SatelliteStatusListAdapter.ViewHolder>() {

    private val satelliteList = mutableListOf<SatelliteStatus>()

    class ViewHolder(val binding: StatusRowItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun setSatelliteList(list: List<SatelliteStatus>) {
        satelliteList.clear()
        satelliteList.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<StatusRowItemBinding>(
            inflater,
            R.layout.status_row_item,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            with(holder.binding) {
                svId.text = "Id"
                signal.text = "C/N0"
                elevation.text = "Elev"
                azimuth.text = "Azim"
            }
        } else {
            val satellite = satelliteList[position - 1]
            with(holder.binding) {
                svId.text = satellite.svid.toString()
                signal.text = satellite.cn0DbHz.toString()
                elevation.text = satellite.elevationDegrees.toString()
                azimuth.text = satellite.azimuthDegrees.toString()
//                val flags = CharArray(3)
//                flags[0] = if (!satellite.hasAlmanac) ' ' else 'A'
//                flags[1] = if (!satellite.hasEphemeris) ' ' else 'E'
//                flags[2] = if (!satellite.usedInFix) ' ' else 'U'
            }
        }
    }

    override fun getItemCount(): Int = satelliteList.size + 1

}