package com.sangam.quonote.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.sangam.quonote.MyViewPagerAdapter
import com.sangam.quonote.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    //    private lateinit var tabLayout: TabLayout
//    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: MyViewPagerAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


        adapter = MyViewPagerAdapter(childFragmentManager, lifecycle)

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Images"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Quotes"))
//        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Feed"))
        binding.viewPager2.adapter = adapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    binding.viewPager2.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        binding.viewPager2.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}