package com.sjkorea.photos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.sjkorea.photos.R
import com.sjkorea.photos.retrofit.RetrofitManager
import com.sjkorea.photos.util.RESPONSE_STATUS
import com.sjkorea.photos.util.SEARCH_TUPE
import com.sjkorea.photos.util.onMyTextChanged
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_button_search.*

class MainActivity : AppCompatActivity() {


    private var currentSearchType : SEARCH_TUPE = SEARCH_TUPE.PHOTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        search_term_raadio_group.setOnCheckedChangeListener { group, checkedId ->

            when(checkedId) {
                R.id.photo_search_radio_btn -> {
                    search_term_text_layout.hint = "사진검색"
                    search_term_text_layout.startIconDrawable = resources.getDrawable(R.drawable.ic_baseline_broken_image_24,resources.newTheme())
                    this.currentSearchType = SEARCH_TUPE.PHOTO

                }
                R.id.user_search_radio_btn ->{
                    search_term_text_layout.hint = "사용자 검색"
                    search_term_text_layout.startIconDrawable = resources.getDrawable(R.drawable.ic_baseline_supervised_user_circle_24,resources.newTheme())
                    this.currentSearchType = SEARCH_TUPE.USER

                }
            }
        }

        search_term_edit_text.onMyTextChanged{

            if (it.toString().count() >0){
                frame_search_btn.visibility = View.VISIBLE
                search_term_text_layout.helperText = " "
                main_scrollview.scrollTo(0,400)
            }else{
                frame_search_btn.visibility = View.INVISIBLE

            }

            if (it.toString().count() == 12) {
                Snackbar.make(main_scrollview, "검색어는 최대 12자 이하로 작성해야 합니다,", Snackbar.LENGTH_SHORT).show()
            }

        }
        // 검색버튼
        btn_search.setOnClickListener {
            RetrofitManager.instance.searchPhotos(searchTerm = search_term_edit_text.text.toString(), completion = {
                responseState, responseDataArrayList ->

                val userSearchInput = search_term_edit_text.text.toString()

                when(responseState){
                    RESPONSE_STATUS.OKAY -> {

                        val intent = Intent(this, PhotoCollectionActivity::class.java)

                        val bundle = Bundle()


                        bundle.putSerializable("photo_array_list", responseDataArrayList)

                        intent.putExtra("array_bundle", bundle)

                        intent.putExtra("search_term", userSearchInput)

                        startActivity(intent)

                    }
                    RESPONSE_STATUS.FAIL -> {
                        Snackbar.make(main_scrollview, "api 호출 에러입니다.", Snackbar.LENGTH_SHORT).show()
                    }

                    RESPONSE_STATUS.NO_CONTENT ->{
                        Snackbar.make(main_scrollview, "검색결과가 없습니다다", Snackbar.LENGTH_SHORT).show()
                    }


                }
                btn_progress.visibility = View.INVISIBLE
                btn_search.text = "검색"
                search_term_edit_text.setText("")

            })


            this.handleSearchButtonUi()
        }



    }

    private fun handleSearchButtonUi(){
        btn_progress.visibility = View.VISIBLE
        btn_search.text = ""

        Handler().postDelayed({
            btn_progress.visibility =View.INVISIBLE
            btn_search.text = "검색"

        },1500)

    }


}