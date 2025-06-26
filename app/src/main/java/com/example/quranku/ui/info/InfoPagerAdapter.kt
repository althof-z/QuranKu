package com.example.quranku.ui.info

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class InfoPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AppDescFragment()
            1 -> AppTutorialFragment()
            else -> TajwidFragment()
        }
    }
}
