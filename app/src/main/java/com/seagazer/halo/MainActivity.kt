package com.seagazer.halo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val listener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v!!.animate().scaleX(1.1f).scaleY(1.1f).start()
            } else {
                v!!.animate().scaleX(1f).scaleY(1f).start()
            }
        }
        findViewById<Halo>(R.id.halo1).onFocusChangeListener = listener
        findViewById<Halo>(R.id.halo2).onFocusChangeListener = listener
        findViewById<Halo>(R.id.halo3).onFocusChangeListener = listener
    }
}