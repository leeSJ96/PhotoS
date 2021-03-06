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


    // ?????????
    private var photoList = ArrayList<Photo>()

    // ?????? ?????? ??????
    var searchHistoryList = ArrayList<SearchData>()


    // ?????????
    //lateinit??? ?????? ????????? ???????????? ???????????? ??????.
    private lateinit var photoGridRecyeclerViewAdapter: PhotoGridRecyeclerViewAdapter
    private lateinit var mySearchHistoryRecyclerViewAdapter: SearchHistoryRecyclerViewAdapter

    // ?????????
    private lateinit var mySearchView: SearchView

    // ????????? ?????? ?????????
    private lateinit var mySearchViewEditText: EditText
    //rx ?????? ??????
    // ???????????? ?????? ????????? ?????? CompositeDisposable
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

        // ?????????????????? ?????? ???????????? ???????????? ????????????.
        setSupportActionBar(top_app_bar)

        photoList = bundle?.getSerializable("photo_array_list") as ArrayList<Photo>




        search_history_mode_switch.setOnCheckedChangeListener(this)
        clear_search_history_buttton.setOnClickListener(this)

        top_app_bar.title = searchTerm

        // ?????????????????? ?????? ???????????? ???????????? ????????????.
        setSupportActionBar(top_app_bar)
        // ?????? ?????????????????? ??????
        this.photoCollectionRecyclerViewSetting(this.photoList)


        // ????????? ?????? ?????? ????????????
        this.searchHistoryList = SharedPrefManager.getSearchHistoryList() as ArrayList<SearchData>

        this.searchHistoryList.forEach {

        }
        handleSearchViewUi()

        //?????? ?????? ?????????????????? ??????
        this.searchHistoryRecyclerViewSetting(this.searchHistoryList)

        if (searchTerm!!.isNotEmpty()){
            val term = searchTerm?.let {
                it
            }?: ""
            this.insertSearchTermHistory(term)
        }


    } //

    override fun onDestroy() {
        //?????? ??????
        //?????? ??????
       this.myCompositeDisposable.clear()




        myCoroutinContext.cancel()
        super.onDestroy()
    }

    // ?????? ?????? ?????????????????? ??????
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

    // ????????? ?????? ?????????????????? ??????
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
            this.queryHint = "???????????? ??????????????????"

            this.setOnQueryTextListener(this@PhotoCollectionActivity)

            this.setOnQueryTextFocusChangeListener { _, hasExpaned ->
                when (hasExpaned) {
                    true -> {
                        //?????? ??????????????????
                       linear_search_history_view.visibility = View.VISIBLE
                        handleSearchViewUi()
                    }
                    false -> {

                        linear_search_history_view.visibility = View.INVISIBLE
                    }
                }
            }

            // ??????????????? ?????????????????? ????????????.
            mySearchViewEditText = this.findViewById(androidx.appcompat.R.id.search_src_text)

            //rx ?????? ??????
            // ??????????????? ????????????
            val editTextChangeObservable : InitialValueObservable<CharSequence> = mySearchViewEditText.textChanges()

            val searchEditTextSubscription : Disposable =
                // ??????????????? ?????????????????? ??????
                editTextChangeObservable
                    //????????? ?????? ?????? ?????? 0.8??? ?????? onNext ???????????? ???????????? ???????????????
                    .debounce(800, TimeUnit.MILLISECONDS)
                    //IO ??????????????? ????????????.
                    //Scheduler instance intended for IO_boundwork.
                        // ???????????? ?????? ?????? ??????,??????, ???????????? ???
                    .subscribeOn(Schedulers.io())
                        // ????????? ?????? ????????? ?????? ??????
                    .subscribeBy(
                        onNext = {
                                 //TODO:: ??????????????? ????????? ???????????? api ??????
                                 if (it.isNotEmpty()){
                                     searchPhotoApiCall(it.toString())
                                 }
                        },
                        onComplete = {

                        },
                        onError = {

                        }
                    )
            // compositeDisposable ??? ??????
            myCompositeDisposable.add(searchEditTextSubscription)



            //?????????????????????
        GlobalScope.launch (context = myCoroutinContext){

            // editText ??? ??????????????????
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


    // ????????? ????????? ?????? ?????????
    // ??????????????? ??????????????????
    override fun onQueryTextSubmit(query: String?): Boolean {


        if (!query.isNullOrEmpty()) {
            this.top_app_bar.title = query

            //TODO:: api ??????
            //TODO:: ????????? ??????

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
            Toast.makeText(this, "???????????? 12??? ????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show()
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
                //ui??????
                handleSearchViewUi()

            }
        }
    }

    // ?????? ??????????????? ?????? ?????????
    override fun onSearchItemDeleteClicked(position: Int) {
        //????????????
        this.searchHistoryList.removeAt(position)
        // ????????? ????????????
        SharedPrefManager.storeSearchHistoryList(this.searchHistoryList)
        //????????? ?????? ????????? ?????????
        this.mySearchHistoryRecyclerViewAdapter.notifyDataSetChanged()

        handleSearchViewUi()

    }

    // ?????? ????????? ?????? ?????????
    override fun onSearchItemClicked(position: Int) {

        val queryString = this.searchHistoryList[position].term

        searchPhotoApiCall(queryString)

        top_app_bar.title = queryString

        this.insertSearchTermHistory(searchTerm = queryString)

        this.top_app_bar.collapseActionView()

    }

    //?????? ?????? API ??????
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
                        "$query ??? ?????? ?????? ????????? ????????????.",
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
    // ????????? ??????
    private fun insertSearchTermHistory(searchTerm: String){

        if (SharedPrefManager.checkSearchHistoryMode()== true){

            // ?????? ????????? ??????
            var indexListToRemove = ArrayList<Int>()

            this.searchHistoryList.forEachIndexed { index, searchDataItem ->
                if (searchDataItem.term == searchTerm){
                    indexListToRemove.add(index)
                }
            }

            indexListToRemove.forEach {
                this.searchHistoryList.removeAt(it)
            }

            //???????????? ??????
            val newSearchData = SearchData(term = searchTerm, timestamp = Date().toSimpleString())
            this.searchHistoryList.add(newSearchData)

            //?????? ???????????? ????????????
            SharedPrefManager.storeSearchHistoryList(this.searchHistoryList)
            this.mySearchHistoryRecyclerViewAdapter.notifyDataSetChanged()
        }
    }


}
