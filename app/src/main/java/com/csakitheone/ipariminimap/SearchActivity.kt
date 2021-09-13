package com.csakitheone.ipariminimap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.csakitheone.ipariminimap.data.Data
import com.csakitheone.ipariminimap.data.Room
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.layout_search_result.view.*

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchEdit.addTextChangedListener {
            search(it.toString())
        }

        if (!intent.getStringExtra("query").isNullOrEmpty()) searchEdit.text = SpannableStringBuilder(intent.getStringExtra("query"))
        else searchEdit.requestFocus()
    }

    fun search(query: String) {
        val results = Data.rooms.filter { r -> r.containsQuery(query) }

        searchLayoutResults.removeAllViews()

        if (query.length < 3) return

        for (result in results) {
            val layoutSearchResult = layoutInflater.inflate(R.layout.layout_search_result, searchLayoutResults, false)
            var resultDescription = "${result.getBuildingName()} • ${result.placeName} • ${result.getSign()}"
            if (result.getRoomName().isNotEmpty()) resultDescription += " (${result.getRoomName()})"
            layoutSearchResult.searchResultTitle.text = resultDescription
            if (result.tags.isNotEmpty() || result.userTags.isNotEmpty()) {
                layoutSearchResult.searchResultTags.text =
                    if (result.userTags.isNotEmpty()) result.tags.joinToString() + " • " + result.userTags.joinToString()
                    else result.tags.joinToString()
            }
            else {
                layoutSearchResult.searchResultTags.visibility = View.GONE
            }

            layoutSearchResult.setOnClickListener {
                selectRoom(result.getSign())
            }
            
            searchLayoutResults.addView(layoutSearchResult)
        }
    }

    fun selectRoom(roomSign: String) {
        setResult(2, Intent().putExtra("room_sign", roomSign))
        finish()
    }

    fun btnCancelClick(view: View) {
        if (searchEdit.text.isNullOrEmpty()) finish()

        searchEdit.text = SpannableStringBuilder()
    }
}