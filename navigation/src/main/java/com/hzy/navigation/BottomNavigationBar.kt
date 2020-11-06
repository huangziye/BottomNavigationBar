package com.hzy.navigation

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.hzy.navigation.util.ViewPagerUtil

/**
 * Created by ziye_huang on 2018/12/25.
 */

open class BottomNavigationBar {

    companion object {
        class Builder {
            private lateinit var mContext: Context
            private lateinit var mBottomNavigationView: BottomNavigationView
            private lateinit var mViewPager: ViewPager
            private lateinit var mAdapter: ViewPagerAdapter

            /**
             * 默认设置显示文字
             *
             * 默认 > 3 的选中效果会影响ViewPager的滑动切换时的效果，故利用反射去掉
             * -1 LABEL_VISIBILITY_AUTO 和 LABEL_VISIBILITY_SELECTED效果一样，没有看出来区别
             * 0 LABEL_VISIBILITY_SELECTED(选中的显示文字，没有选中的不显示文字) 默认动画
             * 1 LABEL_VISIBILITY_LABELED(显示文字) 或 布局中添加：app:labelVisibilityMode="labeled"
             * 2 LABEL_VISIBILITY_UNLABELED(所有都不显示文字)
             */
            private var mLabelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED

            /**
             * 默认可以滑动
             */
            private var mNotCanScroll = false

            /**
             * 预加载页面的个数
             */
            private var mPageLimit = 0

            /**
             * 默认选中的item
             */
            private var mSelectedIndex = 0

            fun with(context: Context): Builder {
                mContext = context
                if (mContext is AppCompatActivity) {
                    mAdapter =
                        ViewPagerAdapter((mContext as AppCompatActivity).supportFragmentManager)
                }
                return this
            }

            fun bottomNavigationView(bottomNavigationView: BottomNavigationView): Builder {
                mBottomNavigationView = bottomNavigationView
                return this
            }

            fun viewpager(viewpager: ViewPager): Builder {
                mViewPager = viewpager
                return this
            }

            fun labelVisibilityMode(labelVisibilityMode: Int): Builder {
                mLabelVisibilityMode = labelVisibilityMode
                return this
            }

            fun notCanScroll(notCanScroll: Boolean): Builder {
                mNotCanScroll = notCanScroll
                return this
            }

            fun offScreenPageLimit(pageLimit: Int): Builder {
                mPageLimit = pageLimit
                return this
            }

            fun itemBackground(background: Drawable): Builder {
                mBottomNavigationView.itemBackground = background
                return this
            }

            fun itemIconTint(tint: ColorStateList): Builder {
                mBottomNavigationView.itemIconTintList = tint
                return this
            }

            fun itemTextColor(tint: ColorStateList): Builder {
                mBottomNavigationView.itemTextColor = tint
                return this
            }

            fun itemIconSize(itemIconSize: Int): Builder {
                mBottomNavigationView.itemIconSize = itemIconSize
                return this
            }

            /**
             * 设置选中的item，下标是从0开始
             */
            fun setSelectedItem(position: Int): Builder {
                mSelectedIndex = position
                mBottomNavigationView.menu.getItem(position).isChecked = true
                val scroller = FixedSpeedScroller(mContext)
                scroller.setCanScroll(!mNotCanScroll)
                ViewPagerUtil.setViewPagerScrollSpeed(mViewPager, !mNotCanScroll)
                mViewPager.currentItem = position
                return this
            }

            fun addFragment(fragment: Fragment): Builder {
                mAdapter.addFragment(fragment)
                return this
            }

            fun addMenuItem(
                @IdRes itemId: Int,
                title: String,
                @DrawableRes itemIcon: Int
            ): Builder {
                /**
                 * 第一个int类型的group ID参数，代表的是组概念，你可以将几个菜单项归为一组，以便更好的以组的方式管理你的菜单按钮。
                 * 第二个int类型的item ID参数，代表的是项目编号。这个参数非常重要，一个item ID对应一个menu中的选项。在后面使用菜单的时候，就靠这个item ID来判断你使用的是哪个选项。
                 * 第三个int类型的order ID参数，代表的是菜单项的显示顺序。默认是0，表示菜单的显示顺序就是按照add的显示顺序来显示。
                 * 第四个String类型的title参数，表示选项中显示的文字。
                 */
                var menuItem = mBottomNavigationView.menu.add(
                    0,
                    itemId,
                    mBottomNavigationView.menu.size(),
                    title
                )
                menuItem.setIcon(itemIcon)
                return this
            }

            fun addMenuItem(
                @IdRes itemId: Int,
                title: String,
                itemIconDrawable: Drawable
            ): Builder {
                /**
                 * 第一个int类型的group ID参数，代表的是组概念，你可以将几个菜单项归为一组，以便更好的以组的方式管理你的菜单按钮。
                 * 第二个int类型的item ID参数，代表的是项目编号。这个参数非常重要，一个item ID对应一个menu中的选项。在后面使用菜单的时候，就靠这个item ID来判断你使用的是哪个选项。
                 * 第三个int类型的order ID参数，代表的是菜单项的显示顺序。默认是0，表示菜单的显示顺序就是按照add的显示顺序来显示。
                 * 第四个String类型的title参数，表示选项中显示的文字。
                 */
                var menuItem = mBottomNavigationView.menu.add(
                    0,
                    itemId,
                    mBottomNavigationView.menu.size(),
                    title
                )
                menuItem.icon = itemIconDrawable
                return this
            }

            /**
             *
             * @param itemCheckedIcon 选中的图片
             * @param itemNormalIcon 未选中的图片
             */
            fun addMenuItem(
                @IdRes itemId: Int,
                title: String,
                @DrawableRes itemCheckedIcon: Int,
                @DrawableRes itemNormalIcon: Int
            ): Builder {
                /**
                 * 第一个int类型的group ID参数，代表的是组概念，你可以将几个菜单项归为一组，以便更好的以组的方式管理你的菜单按钮。
                 * 第二个int类型的item ID参数，代表的是项目编号。这个参数非常重要，一个item ID对应一个menu中的选项。在后面使用菜单的时候，就靠这个item ID来判断你使用的是哪个选项。
                 * 第三个int类型的order ID参数，代表的是菜单项的显示顺序。默认是0，表示菜单的显示顺序就是按照add的显示顺序来显示。
                 * 第四个String类型的title参数，表示选项中显示的文字。
                 */
                var menuItem = mBottomNavigationView.menu.add(
                    0,
                    itemId,
                    mBottomNavigationView.menu.size(),
                    title
                )
                menuItem.icon = createItemIconDrawable(itemCheckedIcon, itemNormalIcon)
                //此处要设置为null，否则会显示默认颜色
                mBottomNavigationView.itemIconTintList = null
                return this
            }

            /**
             * 设置menuItem选中时文字及图片的颜色
             */
            fun setMenuItemTextWithIconColor(
                @ColorInt checkTextColor: Int,
                @ColorInt normalTextColor: Int
            ): Builder {
                mBottomNavigationView.itemTextColor =
                    createColorStateList(checkTextColor, normalTextColor)
                mBottomNavigationView.itemIconTintList =
                    createColorStateList(checkTextColor, normalTextColor)
                return this
            }

            /**
             * 设置menuItem 文字颜色
             */
            fun setMenuItemTextColor(
                @ColorInt checkTextColor: Int,
                @ColorInt normalTextColor: Int
            ): Builder {
                mBottomNavigationView.itemTextColor =
                    createColorStateList(checkTextColor, normalTextColor)
                return this
            }

            /**
             * 设置menuItem 图片颜色
             */
            fun setMenuItemIconColor(
                @ColorInt checkTextColor: Int,
                @ColorInt normalTextColor: Int
            ): Builder {
                mBottomNavigationView.itemIconTintList =
                    createColorStateList(checkTextColor, normalTextColor)
                return this
            }

            fun build(): Builder {
                init()
//                return BottomNavigationBar()
                return this
            }

            private fun init() {
                mBottomNavigationView.menu.getItem(mSelectedIndex).isChecked = true
                mBottomNavigationView.labelVisibilityMode = mLabelVisibilityMode
                mBottomNavigationView.setOnNavigationItemSelectedListener { item ->
                    mViewPager.currentItem = item.order
                    false
                }
                mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrollStateChanged(state: Int) {
                    }

                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                    }

                    override fun onPageSelected(position: Int) {
                        mBottomNavigationView.menu.getItem(position).isChecked = true
                    }
                })
                mViewPager.offscreenPageLimit = mPageLimit
                //设置ViewPager是否可以滑动 true 不可以 false 可以
                mViewPager.setOnTouchListener { v, event -> mNotCanScroll }
                mViewPager.adapter = mAdapter
                mViewPager.currentItem = mSelectedIndex
            }

            /**
             * 创建ColorStateList
             */
            private fun createColorStateList(
                @ColorInt checkedColor: Int,
                @ColorInt normalColor: Int
            ): ColorStateList {
                val colors = intArrayOf(checkedColor, normalColor)
                val states = arrayOfNulls<IntArray>(2)
                states[0] = intArrayOf(android.R.attr.state_checked)
                states[1] = intArrayOf(-android.R.attr.state_checked)
                return ColorStateList(states, colors)
            }

            /**
             * 创建选中及未选中图片Drawable selector
             */
            private fun createItemIconDrawable(
                @DrawableRes checkedIcon: Int,
                @DrawableRes normalIcon: Int
            ): Drawable {
                val sld = StateListDrawable()
                sld.addState(
                    intArrayOf(android.R.attr.state_checked),
                    ContextCompat.getDrawable(mContext, checkedIcon)
                )
                sld.addState(
                    intArrayOf(-android.R.attr.state_checked),
                    ContextCompat.getDrawable(mContext, normalIcon)
                )
                return sld
            }
        }
    }
}