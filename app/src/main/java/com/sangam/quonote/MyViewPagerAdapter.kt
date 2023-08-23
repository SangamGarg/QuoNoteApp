package com.sangam.quonote

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sangam.quonote.explore.Imagefragment
import com.sangam.quonote.explore2.QuoteFragment

class MyViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            Imagefragment()
        } else{
            QuoteFragment()
//        } else {
//            ShareFragment()
        }
    }
}