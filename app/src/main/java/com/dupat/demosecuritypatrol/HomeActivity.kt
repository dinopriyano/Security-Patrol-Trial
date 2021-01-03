package com.dupat.demosecuritypatrol

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.dupat.demosecuritypatrol.fragment.ChatFragment
import com.dupat.demosecuritypatrol.fragment.ReportFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    var bottomNav: BottomNavigationView? = null
    private var activeFragment: Fragment? = null
    private var frgChat: Fragment? = null
    private var frgReport: Fragment? = null
    private var fragmentManager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        frgChat = ChatFragment()
        frgReport = ReportFragment()
        activeFragment = frgChat
        fragmentManager = supportFragmentManager
        setupFragment()

        bottomNav = findViewById<View>(R.id.bottomNav) as BottomNavigationView
        bottomNav!!.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var frg: Fragment? = null
        when(item.itemId){
            R.id.menu_chat -> {
                frg = frgChat
            }
            R.id.menu_report -> {
                frg = frgReport
            }
        }

        showFragment(frg)
        return true
    }

    private fun setupFragment()
    {
        fragmentManager!!.beginTransaction().add(R.id.fragmentContainer,frgChat as Fragment,"").hide(frgChat!!).commit()
        fragmentManager!!.beginTransaction().add(R.id.fragmentContainer,frgReport as Fragment,"").hide(frgReport!!).commit()
        showFragment(activeFragment)
    }

    private fun showFragment(frg: Fragment?){
        if(frg != null)
        {
            fragmentManager!!.beginTransaction()
                    .hide(activeFragment!!)
                    .show(frg)
                    .commit()
            activeFragment = frg
        }
    }
}