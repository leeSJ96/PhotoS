# PhotoS 포토폴리오
=============
      
간단한 unsplash api활용하여 사진검색 하는 앱입니다   
영상이 아닌 .jpg 로 소개하겠습니다    
   레트로핏 과rx 기본기를 배우기위해 공부했던 프로젝트입니다
      
개발언어 . Kotlin
개발 프로그램 . Android Studio
     
라이브러리 . retrofit2, gson , okhttp , glide , rxjava, rxkotlin, rxbinding,coroutines
        
레트로핏을 활용하여 unsplash api 호출
-------------   


1.[사진 검색] MainActivity -> PhotoCollectionActivity 
-------------

   

   
![image](https://im2.ezgif.com/tmp/ezgif-2-2d0899ea99d9.gif)   
editTest를 활용하여 검색버튼을     
api 데이터 호출 됩니다
     

      
2.검색기록 저장 PhotoCollectionActivity  
-------------         
![image](https://im2.ezgif.com/tmp/ezgif-2-58bc2f74bf22.gif)   

검색어 저장스위치가 체크 되어있을때만 검색어 저장됩니다
       
3.검색기록 삭제,전체삭제  PhotoCollectionActivity  
-------------          
![image](https://im2.ezgif.com/tmp/ezgif-2-9cb0b916f2b7.gif)       
x아이콘을 누르면 검색한 기록 지정삭제      
전체삭제 아이콘을 누르면 검색한 기록 전체삭제가 됩니다

4.실시간 검색  PhotoCollectionActivity  
-------------  
![image](https://im2.ezgif.com/tmp/ezgif-2-1b6d2ec85bfb.gif)    
rx옵저버블 사용하여 검색 버튼을 누르지 않아도 0.8초 뒤에 이벤트 데이터로 api가 호출됩니다

