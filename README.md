# halo
[![](https://www.jitpack.io/v/seagazer/halo.svg)](https://www.jitpack.io/#seagazer/halo)
* A view container can play halo animation when get focused like HUAWEI TV.


* How to use:
```kotlin
// step1. add config in build.gradle of the root project
allprojects {
    repositories {
        maven { url 'https://www.jitpack.io' }
        google()
        jcenter()
    }
}

// step2. add the library in your app module
implementation 'com.github.seagazer:halo:latestVersion'

// step3. wrap a sub widget in xml like this:
    <com.seagazer.halo.Halo
        android:id="@+id/halo1"
        android:layout_width="230dp"
        android:layout_height="150dp"
        android:layout_marginStart="30dp"
        app:haloColor="#F3F6F6"
        app:haloDuration="5000"
        app:haloInsertEdge="8dp"
        app:haloShape="rect"
        app:haloWidth="3dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="@color/halo_card"
            android:focusable="true"
            android:gravity="center"
            android:text="Rect"
            android:textColor="@color/white"
            android:textSize="18sp" />
    </com.seagazer.halo.Halo>

```

![](https://upload-images.jianshu.io/upload_images/4420407-d0fd9e58f7bd3775.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1920)
