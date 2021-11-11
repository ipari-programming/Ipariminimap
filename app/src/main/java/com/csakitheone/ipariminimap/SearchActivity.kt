package com.csakitheone.ipariminimap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_QUERY = "query"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchEditFilter.addTextChangedListener {
            searchCardImage.visibility = View.GONE
        }

        if (!intent.getStringExtra(EXTRA_QUERY).isNullOrEmpty()) searchEditFilter.text = SpannableStringBuilder(intent.getStringExtra(EXTRA_QUERY))
        else searchEditFilter.requestFocus()
    }

    fun onBtnCancelClick(view: View) {
        if (searchEditFilter.text.isNullOrBlank()) finish()
        else searchEditFilter.text.clear()
    }
}