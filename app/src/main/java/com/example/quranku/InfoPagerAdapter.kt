package com.example.quranku

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.quranku.databinding.FragmentAppDescBinding

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
