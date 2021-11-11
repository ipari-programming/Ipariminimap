package com.csakitheone.ipariminimap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.csakitheone.ipariminimap.data.DB
import com.csakitheone.ipariminimap.data.Data
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.layout_search_result.view.*

class SearchActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_QUERY = "query"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    override fun onResume() {
        super.onResume()

        checkAndDownloadData()
    }

    private fun checkAndDownloadData() {
        if (!Data.getIsLoaded()) {
            DB.downloadBuildingData {
                if (it) initSearch()
                else finish()
            }
        }
        else {
            initSearch()
        }
    }

    private fun initSearch() {
        searchEdit.isEnabled = true

        searchEdit.addTextChangedListener {
            searchLayoutRooms.removeAllViews()
            if (it.isNullOrBlank() || it.length < 2) return@addTextChangedListener
            
            searchCardImage.visibility = View.GONE
            searchScroll.visibility = View.VISIBLE

            for (room in Data.rooms.filter { r -> r.toString().contains(searchEdit.text, true) }) {
                val place = Data.places.find { r -> r.name == room.placeName }

                val v = layoutInflater.inflate(R.layout.layout_search_result, null, false)
                v.searchResultTitle.text = "${place?.buildingName} • ${room.placeName} • ${room.id}"
                v.searchResultDesc.text = room.tags.joinToString()
                v.setOnClickListener { 
                    startActivity(Intent(this, RoomActivity::class.java).apply { putExtra("room_sign", room.id) })
                }
                searchLayoutRooms.addView(v)
            }
        }

        if (!intent.getStringExtra(EXTRA_QUERY).isNullOrEmpty()) searchEdit.text = SpannableStringBuilder(intent.getStringExtra(EXTRA_QUERY))

        searchEdit.requestFocus()
        val imm: InputMethodManager = getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(searchEdit, InputMethodManager.SHOW_IMPLICIT)
    }

    fun onBtnCancelClick(view: View) {
        if (searchEdit.text.isNullOrBlank()) finish()
        else searchEdit.text.clear()
    }
}