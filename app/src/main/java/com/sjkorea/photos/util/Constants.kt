package com.sjkorea.photos.util

object Constants {
    const val  TAG : String = "로그"
}

enum class SEARCH_TUPE {
    PHOTO, USER
}

enum class RESPONSE_STATUS {
    OKAY,
    FAIL,
    NO_CONTENT
}



object  API {
    const val BASE_URL = "https://api.unsplash.com/"
    const val CLIENT_ID = "ujJtZgSjpLLNNKU1UANoIIj3dzYvzzJU2duih5U3eb4"
    const val SEARCH_PHOTO = "search/photos"
    const val SEARCH_USERS = "search/users"
}