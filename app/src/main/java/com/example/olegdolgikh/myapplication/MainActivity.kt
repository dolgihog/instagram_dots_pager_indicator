package com.example.olegdolgikh.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.*
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val content: List<String> = listOf(
        "text1",
        "text2",
        "text3",
        "text4")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pagerIndicatorView = PagerIndicatorView(this)
        frameLayout.addView(pagerIndicatorView)
        viewPagerContent.adapter = object : PagerAdapter() {

            override fun instantiateItem(container: ViewGroup?, position: Int): Any {
                val item = content[position]
                val textView = TextView(this@MainActivity).apply {
                    layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    tag = item
                    text = item
                    gravity = Gravity.CENTER
                    textSize = resources.getDimension(R.dimen.textSize)
                }
                container?.addView(textView)
                return item
            }

            override fun isViewFromObject(view: View?, item: Any?): Boolean = view?.tag == item ?: false

            override fun getCount(): Int = content.size

            override fun destroyItem(container: ViewGroup?, position: Int, item: Any?) {
                item as? String ?: return
                val view = container?.findViewWithTag<View>(item) ?: return
                container.removeView(view)
            }
        }
        pagerIndicatorView.setViewPager(viewPagerContent)
    }
}
