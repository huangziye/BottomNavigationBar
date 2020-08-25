package com.hzy.bottomnavigationbar

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.hzy.bottomnavigationbar.fragment.ContactFragment
import com.hzy.bottomnavigationbar.fragment.FindFragment
import com.hzy.bottomnavigationbar.fragment.MeFragment
import com.hzy.bottomnavigationbar.fragment.WechatFragment
import com.hzy.navigation.BottomNavigationBar
import com.hzy.navigation.badge.Badge
import com.hzy.navigation.badge.BadgeView
import com.hzy.navigation.badge.DragState
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bar = BottomNavigationBar.Companion.Builder().with(this)
            .bottomNavigationView(bottom_navigation)
            .viewpager(viewpager)
//            .addMenuItem(R.id.action_wechat, getString(R.string.wechat), R.mipmap.ic_wechat)
//            .addMenuItem(R.id.action_contact, getString(R.string.contact), R.mipmap.ic_contact)
//            .addMenuItem(R.id.action_find, getString(R.string.find), R.mipmap.ic_find)
//            .addMenuItem(R.id.action_me, getString(R.string.me), R.mipmap.ic_me)
            .addMenuItem(
                R.id.action_wechat,
                getString(R.string.wechat),
                R.mipmap.ic_wechat, R.mipmap.ic_contact
            )
            .addMenuItem(
                R.id.action_contact,
                getString(R.string.contact),
                R.mipmap.ic_contact, R.mipmap.ic_find
            )
            .addMenuItem(
                R.id.action_find,
                getString(R.string.find),
                R.mipmap.ic_find, R.mipmap.ic_me
            )
            .addMenuItem(
                R.id.action_me,
                getString(R.string.me),
                R.mipmap.ic_me, R.mipmap.ic_wechat
            )
            .notCanScroll(false)
            .itemBackground(ContextCompat.getDrawable(this@MainActivity, android.R.color.white)!!)
//            .itemIconTint(getColorStateList(R.color.menu_item_selector))
//            .itemTextColor(getColorStateList(R.color.menu_item_selector))
            .setMenuItemTextColor(
                ContextCompat.getColor(this, R.color.navigation_item_selected_color),
                ContextCompat.getColor(this, android.R.color.black)
            )
            .addFragment(WechatFragment())
            .addFragment(ContactFragment())
            .addFragment(FindFragment())
            .addFragment(MeFragment())
            .setSelectedItem(3)
            .build()


        val menuView = bottom_navigation.getChildAt(0) as BottomNavigationMenuView
        val itemView = menuView.getChildAt(2) as BottomNavigationItemView

        BadgeView(this).bindTargetView(itemView).setBadgeCount(120)
            .setOnDragStateChangedListener(object : Badge.OnDragStateChangedListener {
                override fun onDragStateChanged(
                    dragState: DragState,
                    badge: Badge,
                    targetView: View
                ) {
                    if (dragState == DragState.STATE_SUCCEED) {
                        Toast.makeText(this@MainActivity, "success", Toast.LENGTH_SHORT).show()
                    }
                }
            })


        Handler().postDelayed(Runnable { bar.setSelectedItem(1) }, 10000)
    }

}
