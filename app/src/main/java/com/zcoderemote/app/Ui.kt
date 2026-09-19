package com.zcoderemote.app

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

fun Context.toast(resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

/**
 * 暗色沉浸式系统栏处理：
 * - API 30+：关闭 decorFitsSystemWindows，由 insets 监听器把系统栏 + 输入法高度转为内边距
 * - API 30 以下：系统栏直接着色为应用背景色，输入法由 adjustResize 处理
 */
fun Activity.setupZCodeInsets(root: View) {
    if (Build.VERSION.SDK_INT >= 30) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(0, bars.top, 0, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    } else {
        window.statusBarColor = ContextCompat.getColor(this, R.color.zr_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.zr_bg)
    }
}
