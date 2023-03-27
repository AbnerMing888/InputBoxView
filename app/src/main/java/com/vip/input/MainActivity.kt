package com.vip.input

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.vip.edit.InputBoxView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val ibView = findViewById<InputBoxView>(R.id.ib_view)

        ibView.inputChangeContent {
            //输入一个回调一下
            Log.e("输入回调======", it)
        }
        ibView.inputEndResult {
            //最终的结果值
            Log.e("最终结果======", it)
        }

        ibView.showKeyBoard {
            //启动自定义键盘
        }

    }
}