package com.sjkorea.photos.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sjkorea.photos.R
import com.sjkorea.photos.model.SearchData
import kotlinx.android.synthetic.main.layout_search_item.view.*

class SearchHistoryRecyclerViewAdapter(searchHistoryRecyclerViewInterface: ISearchHistoryRecyclerView)
    : RecyclerView.Adapter<SearchHistoryRecyclerViewAdapter.SearchItemViewHolder>() {

    private var searchHistoryList : ArrayList<SearchData> = ArrayList()

    private var iSearchHistoryRecyclerView : ISearchHistoryRecyclerView? = null

    init {
        this.iSearchHistoryRecyclerView = searchHistoryRecyclerViewInterface

    }

    inner class SearchItemViewHolder(itemView : View, searchRecyclerViewInterface: ISearchHistoryRecyclerView) : RecyclerView.ViewHolder(itemView),View.OnClickListener
    {
        private  var mySearchRecyclerViewInterface: ISearchHistoryRecyclerView

        private  val searchTermTextView = itemView.search_term_text
        private  val whenSearchedTextView = itemView.when_searched_text
        private  val deleteSearchBtn = itemView.delete_search_btn
        private  val constrainSearchItem =itemView.constraint_search_item

        init {


            //리스너 연결
            deleteSearchBtn.setOnClickListener(this)
            constrainSearchItem.setOnClickListener(this)
            this.mySearchRecyclerViewInterface = searchRecyclerViewInterface
        }

        // 데이터와 뷰를 묶는다.
        fun bindWithView(searchItem: SearchData){
            whenSearchedTextView.text = searchItem.timestamp
            searchTermTextView.text = searchItem.term

        }

        override fun onClick(view: View?) {

            when(view){
                deleteSearchBtn ->{
                    this.mySearchRecyclerViewInterface.onSearchItemDeleteClicked(adapterPosition)
                }
                constrainSearchItem ->{
                    this.mySearchRecyclerViewInterface.onSearchItemClicked(adapterPosition)

                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {

        val searchItemViewHolder = SearchItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_search_item,parent,false)
            , this.iSearchHistoryRecyclerView!!
        )

        return searchItemViewHolder

    }

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {

        val dataItem: SearchData = this.searchHistoryList[position]

        holder.bindWithView(dataItem)



    }

    fun submitList(searchHistoryList: ArrayList<SearchData>){
        this.searchHistoryList = searchHistoryList

    }

    override fun getItemCount(): Int {

        return searchHistoryList.size

    }
}