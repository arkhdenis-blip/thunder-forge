package com.groma.forge

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager

class ForgeGame(ctx: android.content.Context) : ForgeModal(ctx) {
    init {
        holder.addCallback(this)
        isFocusable = true
        PL.style = android.graphics.Paint.Style.STROKE
        PL.strokeCap = android.graphics.Paint.Cap.ROUND
        PT.typeface = TF_BOLD
        buildSpots()
        load()
        bake()
        syncActors()
    }
}

class MainActivity : Activity() {
    private var view: ForgeGame? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val v = ForgeGame(this)
        view = v
        setContentView(v)
    }

    override fun onPause() {
        super.onPause()
        view?.onHostPause()
    }

    override fun onResume() {
        super.onResume()
        view?.onHostResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        view?.shutdown()
    }

    override fun onBackPressed() {
        view?.back()
    }
}
