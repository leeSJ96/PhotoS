package com.sjkorea.photos.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Flow

// 문지열이 제이슨 형태인지, 제이슨 배열형태인지
fun String?.isJsonObject():Boolean = this?.startsWith("{") == true && this.endsWith("}")


//문자열이 제이슨 배열인지
fun String?.isJsonArray(): Boolean{
    if (this?.startsWith("[") == true && this.endsWith("]")){
        return true
    }else{
        return false
    }

}
//날짜 포맷

fun Date.toSimpleString() : String{
    val format = SimpleDateFormat("HH시mm분ss초")
    return format.format(this)
}



// 에딧 텍스트에 대한 익스텐션
fun EditText.onMyTextChanged(completion: (Editable?) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(editable: Editable?) {
            completion(editable)
        }


    })


}
// 에딧텍스트 텍스트 변경을 flow로 받기
fun EditText.textChangesToFlow(): kotlinx.coroutines.flow.Flow<CharSequence?> {

    // flow 콜백 받기
    return callbackFlow<CharSequence?> {

        val listener= object  : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)  = Unit

            override fun afterTextChanged(s: Editable?) {
                UInt
            }
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                offer(text)
            }



        }
        //위에서 설정한 리스너 달아주기
        addTextChangedListener(listener)

        // 콜백이 사라질때 실행됨
        awaitClose {

            removeTextChangedListener(listener)

        }

    }.onStart {
        // Rx 에ㅓ onNext 와 동일
        // emit 으로 이벤트를 전달
        emit(text)
    }




}