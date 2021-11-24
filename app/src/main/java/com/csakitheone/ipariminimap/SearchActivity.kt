package com.csakitheone.ipariminimap

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.csakitheone.ipariminimap.data.DB
import com.csakitheone.ipariminimap.data.Data
import com.csakitheone.ipariminimap.data.Web
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        checkStudents()
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

    private fun checkStudents() {
        if (Web.getStudentsNoDownload().isEmpty()) {
            searchBtnDownloadStudents.visibility = View.VISIBLE
        }
    }

    private fun initSearch() {
        searchEdit.isEnabled = true
        searchEdit.hint = "Keress teremre, linkre, diákra"

        searchEdit.addTextChangedListener {
            searchCardImage.visibility = View.GONE
            searchScroll.visibility = View.VISIBLE

            searchLayoutLinks.removeAllViews()
            searchTextLinks.visibility = View.GONE
            searchLayoutRooms.removeAllViews()
            searchTextRooms.visibility = View.GONE
            searchLayoutStudents.removeAllViews()
            searchTextStudents.visibility = View.GONE
            if (it?.length ?: 0 < 3) return@addTextChangedListener

            //#region Links
            Data.links.filter { r -> r.toString().contains(searchEdit.text, true) }.map { link ->
                searchTextLinks.visibility = View.VISIBLE
                val v = layoutInflater.inflate(R.layout.layout_search_result, null, false)
                v.searchResultTitle.text = link.key
                v.searchResultDesc.text = link.value
                v.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.value)))
                }
                searchLayoutLinks.addView(v)
            }
            //#endregion

            //#region Rooms
            Data.getAllRooms().filter { r -> "$r ${r.tags.joinToString()}".contains(searchEdit.text, true) }.map { room ->
                searchTextRooms.visibility = View.VISIBLE
                val v = layoutInflater.inflate(R.layout.layout_search_result, null, false)
                v.searchResultTitle.text = room.toString()
                v.searchResultDesc.text = room.tags.joinToString()
                v.setOnClickListener {
                    startActivity(Intent(this, RoomActivity::class.java).apply { putExtra("room_sign", room.id) })
                }
                searchLayoutRooms.addView(v)
            }
            //#endregion

            //#region Students
            Web.getStudentsNoDownload().filter { r -> r.toString().contains(searchEdit.text, true) }.map { student ->
                searchTextStudents.visibility = View.VISIBLE
                val v = layoutInflater.inflate(R.layout.layout_search_result, null, false)
                v.searchResultTitle.text = student.name
                v.searchResultDesc.text = student.gradeMajor
                v.setOnClickListener {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(student.name)
                        .setMessage(student.gradeMajor)
                        .setPositiveButton("Osztálytársak mutatása") { _: DialogInterface, _: Int ->
                            searchEdit.text = SpannableStringBuilder(student.gradeMajor)
                        }
                        .setNeutralButton("Bezárás")  { _: DialogInterface, _: Int -> }
                        .create().show()
                }
                searchLayoutStudents.addView(v)
            }
            //#endregion
        }

        if (!intent.getStringExtra(EXTRA_QUERY).isNullOrEmpty()) {
            searchEdit.text = SpannableStringBuilder(intent.getStringExtra(EXTRA_QUERY))
            return
        }

        searchEdit.requestFocus()
        searchEdit.postDelayed({
            val imm: InputMethodManager = getSystemService(InputMethodManager::class.java)
            imm.showSoftInput(searchEdit, InputMethodManager.SHOW_IMPLICIT)
        }, 300)
    }

    fun onBtnCancelClick(view: View) {
        if (searchEdit.text.isNullOrBlank()) finish()
        else searchEdit.text.clear()
    }

    fun onBtnDownloadStudentsClick(view: View) {
        searchBtnDownloadStudents.isEnabled = false
        searchBtnDownloadStudents.text = "Diákok lekérése..."
        Web.getStudents {
            runOnUiThread {
                searchBtnDownloadStudents.visibility = View.GONE
                initSearch()
            }
        }
    }
}