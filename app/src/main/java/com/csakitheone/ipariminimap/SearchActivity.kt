package com.csakitheone.ipariminimap

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import com.csakitheone.ipariminimap.data.DB
import com.csakitheone.ipariminimap.data.Data
import com.csakitheone.ipariminimap.data.Web
import com.csakitheone.ipariminimap.databinding.ActivitySearchBinding
import com.csakitheone.ipariminimap.databinding.LayoutSearchResultBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SearchActivity : AppCompatActivity() {

    lateinit var binding: ActivitySearchBinding

    companion object {
        const val EXTRA_QUERY = "query"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val quickChips = mutableListOf(
            "Mosdó", "Igazgatói iroda", "Gazdasági iroda"
        )
        quickChips.addAll(Data.buildings.map { it.name })
        quickChips.addAll(Data.getAllPlaces().map { it.name })
        binding.searchGroupChips.removeAllViews()
        quickChips.map {
            binding.searchGroupChips.addView(Chip(this).apply {
                text = it
                setOnClickListener { _ ->
                    binding.searchEdit.text = SpannableStringBuilder(it)
                }
            })
        }
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
            binding.searchBtnDownloadStudents.visibility = View.VISIBLE
        }
    }

    private fun initSearch() {
        binding.searchEdit.isEnabled = true
        binding.searchEdit.hint = "Keress teremre, linkre, diákra"

        binding.searchEdit.addTextChangedListener {
            binding.searchCardImage.visibility = View.GONE
            binding.searchScroll.visibility = View.VISIBLE

            binding.searchLayoutLinks.removeAllViews()
            binding.searchTextLinks.visibility = View.GONE
            binding.searchLayoutRooms.removeAllViews()
            binding.searchTextRooms.visibility = View.GONE
            binding.searchLayoutStudents.removeAllViews()
            binding.searchTextStudents.visibility = View.GONE
            if (it?.length ?: 0 < 3) return@addTextChangedListener

            //#region Links
            Data.links.filter { r -> r.toString().contains(binding.searchEdit.text, true) }.map { link ->
                binding.searchTextLinks.visibility = View.VISIBLE
                val v = layoutInflater.inflate(R.layout.layout_search_result, null, false)
                val vb = LayoutSearchResultBinding.bind(v)
                vb.searchResultTitle.text = link.key
                vb.searchResultDesc.text = link.value
                v.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.value)))
                }
                binding.searchLayoutLinks.addView(v)
            }
            //#endregion

            //#region Rooms
            Data.getAllRooms().filter { r -> "$r ${r.tags.joinToString()}".contains(binding.searchEdit.text, true) }.map { room ->
                binding.searchTextRooms.visibility = View.VISIBLE
                val v = layoutInflater.inflate(R.layout.layout_search_result, null, false)
                val vb = LayoutSearchResultBinding.bind(v)
                vb.searchResultTitle.text = room.toString()
                vb.searchResultDesc.text = room.tags.joinToString()
                v.setOnClickListener {
                    startActivity(Intent(this, RoomActivity::class.java).apply { putExtra("room_sign", room.id) })
                }
                binding.searchLayoutRooms.addView(v)
            }
            //#endregion

            //#region Students
            Web.getStudentsNoDownload().filter { r -> r.toString().contains(binding.searchEdit.text, true) }.map { student ->
                binding.searchTextStudents.visibility = View.VISIBLE
                val v = layoutInflater.inflate(R.layout.layout_search_result, null, false)
                val vb = LayoutSearchResultBinding.bind(v)
                vb.searchResultTitle.text = student.name
                vb.searchResultDesc.text = student.gradeMajor
                v.setOnClickListener {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(student.name)
                        .setMessage(student.gradeMajor)
                        .setPositiveButton("Osztálytársak mutatása") { _: DialogInterface, _: Int ->
                            binding.searchEdit.text = SpannableStringBuilder(student.gradeMajor)
                        }
                        .setNeutralButton("Bezárás")  { _: DialogInterface, _: Int -> }
                        .create().show()
                }
                binding.searchLayoutStudents.addView(v)
            }
            //#endregion
        }

        if (!intent.getStringExtra(EXTRA_QUERY).isNullOrEmpty()) {
            binding.searchEdit.text = SpannableStringBuilder(intent.getStringExtra(EXTRA_QUERY))
            return
        }

        binding.searchEdit.requestFocus()
        binding.searchEdit.postDelayed({
            val imm: InputMethodManager = getSystemService(InputMethodManager::class.java)
            imm.showSoftInput(binding.searchEdit, InputMethodManager.SHOW_IMPLICIT)
        }, 300)
    }

    fun onBtnCancelClick(view: View) {
        if (binding.searchEdit.text.isNullOrBlank()) finish()
        else binding.searchEdit.text.clear()
    }

    fun onBtnDownloadStudentsClick(view: View) {
        binding.searchBtnDownloadStudents.isEnabled = false
        binding.searchBtnDownloadStudents.text = "Diákok lekérése..."
        Web.getStudents {
            runOnUiThread {
                binding.searchBtnDownloadStudents.visibility = View.GONE
                initSearch()
            }
        }
    }
}