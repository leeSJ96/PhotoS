package com.sjkorea.photos.activities

import android.app.SearchManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.InputFilter
import android.view.Menu
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.InitialValueObservable
import com.jakewharton.rxbinding4.widget.textChanges
import com.sjkorea.photos.R
import com.sjkorea.photos.model.Photo
import com.sjkorea.photos.model.SearchData
import com.sjkorea.photos.recyclerview.ISearchHistoryRecyclerView
import com.sjkorea.photos.recyclerview.PhotoGridRecyeclerViewAdapter
import com.sjkorea.photos.recyclerview.SearchHistoryRecyclerViewAdapter
import com.sjkorea.photos.retrofit.RetrofitManager
import com.sjkorea.photos.util.RESPONSE_STATUS
import com.sjkorea.photos.util.SharedPrefManager
import com.sjkorea.photos.util.textChangesToFlow
import com.sjkorea.photos.util.toSimpleString
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_photo_collection.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class PhotoCollectionActivity : AppCompatActivity(),
    SearchView.OnQueryTextListener,
    CompoundButton.OnCheckedChangeListener,
    View.OnClickListener,
    ISearchHistoryRecyclerView {


    // 데이터
    private var photoList = ArrayList<Photo>()

    // 검색 기록 배열
    var searchHistoryList = ArrayList<SearchData>()


    // 어답터
    //lateinit을 통해 나중에 메모리에 올라가도 된다.
    private lateinit var photoGridRecyeclerViewAdapter: PhotoGridRecyeclerViewAdapter
    private lateinit var mySearchHistoryRecyclerViewAdapter: SearchHistoryRecyclerViewAdapter

    // 서치뷰
    private lateinit var mySearchView: SearchView

    // 서치뷰 에딧 텍스트
    private lateinit var mySearchViewEditText: EditText
    //rx 적용 부분
    // 옵저버블 통한 제거를 위한 CompositeDisposable
    private var myCompositeDisposable = CompositeDisposable()

    private var myCoroutineJob : Job = Job()
    private val myCoroutinContext: CoroutineContext
    get() = Dispatchers.IO + myCoroutineJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_collection)

        val bundle = intent.getBundleExtra("array_bundle")

        val searchTerm = intent.getStringExtra("search_term")




        search_history_mode_switch.setOnCheckedChangeListener(this)
        clear_search_history_buttton.setOnClickListener(this)

        search_history_mode_switch.isChecked = SharedPrefManager.checkSearchHistoryMode()

        top_app_bar.title = searchTerm

        // 액티비티에서 어떤 액션바를 사용할지 설정한다.
        setSupportActionBar(top_app_bar)

        photoList = bundle?.getSerializable("photo_array_list") as ArrayList<Photo>




        search_history_mode_switch.setOnCheckedChangeListener(this)
        clear_search_history_buttton.setOnClickListener(this)

        top_app_bar.title = searchTerm

        // 액티비티에서 어떤 액션바를 사용할지 설정한다.
        setSupportActionBar(top_app_bar)
        // 사진 리사이클러뷰 세팅
        this.photoCollectionRecyclerViewSetting(this.photoList)


        // 저장된 검색 기록 가져오기
        this.searchHistoryList = SharedPrefManager.getSearchHistoryList() as ArrayList<SearchData>

        this.searchHistoryList.forEach {

        }
        handleSearchViewUi()

        //검색 기록 리사이클러뷰 준비
        this.searchHistoryRecyclerViewSetting(this.searchHistoryList)

        if (searchTerm!!.isNotEmpty()){
            val term = searchTerm?.let {
                it
            }?: ""
            this.insertSearchTermHistory(term)
        }


    } //

    override fun onDestroy() {
        //적용 부분
        //모두 삭제
       this.myCompositeDisposable.clear()




        myCoroutinContext.cancel()
        super.onDestroy()
    }

    // 검색 기록 리사이클러뷰 준비
    private fun searchHistoryRecyclerViewSetting(searchHistoryList: ArrayList<SearchData>) {

        this.mySearchHistoryRecyclerViewAdapter = SearchHistoryRecyclerViewAdapter(this)
        this.mySearchHistoryRecyclerViewAdapter.submitList(searchHistoryList)

        val myLinearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        myLinearLayoutManager.stackFromEnd = true

        search_history_recycler_view.apply {
            layoutManager = myLinearLayoutManager
            this.scrollToPosition(mySearchHistoryRecyclerViewAdapter.itemCount - 1)
            adapter = mySearchHistoryRecyclerViewAdapter
        }

    }

    // 그리드 사진 리사이클러뷰 세팅
    private fun photoCollectionRecyclerViewSetting(photoList: ArrayList<Photo>) {

        this.photoGridRecyeclerViewAdapter = PhotoGridRecyeclerViewAdapter()

        this.photoGridRecyeclerViewAdapter.submitList(photoList)

        my_photo_recycler_view.layoutManager = GridLayoutManager(
            this,
            2,
            GridLayoutManager.VERTICAL,
            false
        )
        my_photo_recycler_view.adapter = this.photoGridRecyeclerViewAdapter

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater

        inflater.inflate(R.menu.top_app_bar_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        this.mySearchView = menu?.findItem(R.id.search_menu_item)?.actionView as SearchView

        this.mySearchView.apply {
            this.queryHint = "검색어를 입력해주세요"

            this.setOnQueryTextListener(this@PhotoCollectionActivity)

            this.setOnQueryTextFocusChangeListener { _, hasExpaned ->
                when (hasExpaned) {
                    true -> {
                        //서치 리사이클러뷰
                       linear_search_history_view.visibility = View.VISIBLE
                        handleSearchViewUi()
                    }
                    false -> {

                        linear_search_history_view.visibility = View.INVISIBLE
                    }
                }
            }

            // 서치뷰에서 에딧텍스트를 가져온다.
            mySearchViewEditText = this.findViewById(androidx.appcompat.R.id.search_src_text)

            //rx 적용 부분
            // 에딧텍스트 옵저버블
            val editTextChangeObservable : InitialValueObservable<CharSequence> = mySearchViewEditText.textChanges()

            val searchEditTextSubscription : Disposable =
                // 옵저버블에 오퍼레이터들 추가
                editTextChangeObservable
                    //글자가 입력 되고 나서 0.8초 후에 onNext 이벤트로 데이터를 흘려보내기
                    .debounce(800, TimeUnit.MILLISECONDS)
                    //IO 쓰레드에서 돌리겠다.
                    //Scheduler instance intended for IO_boundwork.
                        // 네트워크 요청 파일 읽기,쓰기, 디비처리 등
                    .subscribeOn(Schedulers.io())
                        // 구독을 통해 이벤트 응답 받기
                    .subscribeBy(
                        onNext = {
                                 //TODO:: 흘러들어온 이벤트 데이터로 api 호출
                                 if (it.isNotEmpty()){
                                     searchPhotoApiCall(it.toString())
                                 }
                        },
                        onComplete = {

                        },
                        onError = {

                        }
                    )
            // compositeDisposable 에 추가
            myCompositeDisposable.add(searchEditTextSubscription)



            //글로벌스코프프
        GlobalScope.launch (context = myCoroutinContext){

            // editText 가 변경되었을때
            val editTextFlow  = mySearchViewEditText.textChangesToFlow()

            editTextFlow
                .debounce(2000)
                .filter {
                    it?.length!! > 0
                }
                .onEach {

                }
                .launchIn(this)
        }
      }




        this.mySearchViewEditText.apply {
            this.filters = arrayOf(InputFilter.LengthFilter(12))
            this.setTextColor(Color.WHITE)
            this.setHintTextColor(Color.WHITE)
        }


        return true
    }


    // 서치뷰 검색어 입력 이벤트
    // 검색버튼이 클릭되었을때
    override fun onQueryTextSubmit(query: String?): Boolean {


        if (!query.isNullOrEmpty()) {
            this.top_app_bar.title = query

            //TODO:: api 호출
            //TODO:: 검색어 저장

            this.insertSearchTermHistory(query)
            this.searchPhotoApiCall(query)
        }

//        this.mySearchView.setQuery("", false)
//        this.mySearchView.clearFocus()

        this.top_app_bar.collapseActionView()

        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {


//        val userInputText = newText ?: ""

        val userInputText = newText.let {
            it
        } ?: ""

        if (userInputText.count() == 12) {
            Toast.makeText(this, "검색어는 12자 까지만 입력 가능합니다.", Toast.LENGTH_SHORT).show()
        }

//        if (userInputText.length in 1..12){
//            searchPhotoApiCall(userInputText)
//        }


        return true
    }

    override fun onCheckedChanged(switch: CompoundButton?, isChecked: Boolean) {
        when (switch) {
            search_history_mode_switch -> {
                if (isChecked == true) {

                    SharedPrefManager.setSearchHistoryMode(isActivated = true )
                } else {
                    SharedPrefManager.setSearchHistoryMode(isActivated = false)
                }
            }

        }
    }

    override fun onClick(view: View?) {
        when (view) {
            clear_search_history_buttton -> {

                SharedPrefManager.clearSearchHistoryList()
                this.searchHistoryList.clear()
                //ui처리
                handleSearchViewUi()

            }
        }
    }

    // 검색 아이템삭제 버튼 이벤트
    override fun onSearchItemDeleteClicked(position: Int) {
        //해당요소
        this.searchHistoryList.removeAt(position)
        // 데이터 덮어쓰기
        SharedPrefManager.storeSearchHistoryList(this.searchHistoryList)
        //데이터 변경 됐다고 알려줌
        this.mySearchHistoryRecyclerViewAdapter.notifyDataSetChanged()

        handleSearchViewUi()

    }

    // 검색 아이템 버튼 이벤트
    override fun onSearchItemClicked(position: Int) {

        val queryString = this.searchHistoryList[position].term

        searchPhotoApiCall(queryString)

        top_app_bar.title = queryString

        this.insertSearchTermHistory(searchTerm = queryString)

        this.top_app_bar.collapseActionView()

    }

    //사진 검색 API 호출
    private fun searchPhotoApiCall(query: String) {

        RetrofitManager.instance.searchPhotos(searchTerm = query, completion = { status, list ->
            when (status) {
                RESPONSE_STATUS.OKAY -> {
                    if (list != null) {
                        this.photoList.clear()
                        this.photoList = list
                        this.photoGridRecyeclerViewAdapter.submitList(this.photoList)
                        this.photoGridRecyeclerViewAdapter.notifyDataSetChanged()

                    }
                }

                RESPONSE_STATUS.NO_CONTENT -> {
                    Snackbar.make(
                        collecton_layout,
                        "$query 에 대한 검색 결과가 없습니다.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        })

    }


    private fun handleSearchViewUi() {

        if (this.searchHistoryList.size > 0) {
            search_history_recycler_view.visibility = View.VISIBLE
            search_history_recycler_view_label.visibility = View.VISIBLE
            clear_search_history_buttton.visibility = View.VISIBLE
        } else {
            search_history_recycler_view.visibility = View.INVISIBLE
            search_history_recycler_view_label.visibility = View.INVISIBLE
            clear_search_history_buttton.visibility = View.INVISIBLE
        }
    }
    // 검색어 저장
    private fun insertSearchTermHistory(searchTerm: String){

        if (SharedPrefManager.checkSearchHistoryMode()== true){

            // 중복 아이템 삭제
            var indexListToRemove = ArrayList<Int>()

            this.searchHistoryList.forEachIndexed { index, searchDataItem ->
                if (searchDataItem.term == searchTerm){
                    indexListToRemove.add(index)
                }
            }

            indexListToRemove.forEach {
                this.searchHistoryList.removeAt(it)
            }

            //새아이템 넣기
            val newSearchData = SearchData(term = searchTerm, timestamp = Date().toSimpleString())
            this.searchHistoryList.add(newSearchData)

            //기존 데이터에 덮어쓰기
            SharedPrefManager.storeSearchHistoryList(this.searchHistoryList)
            this.mySearchHistoryRecyclerViewAdapter.notifyDataSetChanged()
        }
    }


}
