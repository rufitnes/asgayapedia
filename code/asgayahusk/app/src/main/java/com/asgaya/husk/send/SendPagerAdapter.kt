package com.asgaya.husk.send

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter for Send flow ViewPager2
 */
class SendPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SendAddressFragment()
            1 -> SendAmountFragment()
            2 -> SendConfirmFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
