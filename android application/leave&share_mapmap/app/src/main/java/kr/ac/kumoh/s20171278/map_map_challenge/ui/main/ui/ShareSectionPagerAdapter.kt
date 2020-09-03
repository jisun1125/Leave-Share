package kr.ac.kumoh.s20171278.map_map_challenge.ui.main.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kr.ac.kumoh.s20171278.map_map_challenge.*
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumMapFragment
import kr.ac.kumoh.s20171278.map_map_challenge.album.main.AlbumMemoFragment
import kr.ac.kumoh.s20171278.map_map_challenge.main.MainMapFragment
import kr.ac.kumoh.s20171278.map_map_challenge.main.MainMemoFragment
import kr.ac.kumoh.s20171278.map_map_challenge.ui.main.SharedMapFragment
import kr.ac.kumoh.s20171278.map_map_challenge.ui.main.SharedMemoFragment


private val TAB_TITLES = arrayOf(
        R.string.tab_text_1,
        R.string.tab_text_2
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class ShareSectionsPagerAdapter(private val context: Context, fm: FragmentManager)
    : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when(position){
            0-> SharedMapFragment()
            else -> SharedMemoFragment()
        }
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        //return PlaceholderFragment.newInstance(position + 1)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}