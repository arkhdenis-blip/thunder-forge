package com.groma.forge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

abstract class ForgeBase(ctx: Context) : SurfaceView(ctx), SurfaceHolder.Callback, Runnable {

    protected val dp = resources.displayMetrics.density
    protected var W = 0f
    protected var H = 0f
    protected var zoom = 1f
    protected var camX = 5.5f
    protected var camY = 8.5f
    protected var thread: Thread? = null
    @Volatile protected var running = false
    @Volatile protected var paused = false

    protected val P = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val PL = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val PT = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val path = Path()
    protected val rect = RectF()

    protected val sfx = Sfx()
    protected val vib = ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    var soundOn = true
    var vibOn = true
    var quality = 2

    protected val terrs = arrayOf(Terr(0, 1f), Terr(1, 15f), Terr(2, 29f))
    protected val actors = ArrayList<Actor>()
    protected val spots = ArrayList<Spot>()
    protected val parts = ArrayList<Part>()
    protected val texts = ArrayList<Ftxt>()
    protected val piles = ArrayList<Pile>()

    protected var px = 5.5f
    protected var py = 9.0f
    protected var pKind = 0
    protected var pCarry = 0
    protected var pTier = 0
    protected var pFace = 1f
    protected var pPhase = 0f
    protected var pSwing = 0f
    protected var pAct = 0f
    protected var capLv = 1
    protected var spdLv = 1
    protected var carLv = 1

    protected var coins = 0f
    protected var shownCoins = 0f
    protected var earned = 0f
    protected var rep = 100f
    protected var quest = 0
    protected var playT = 0f
    protected var won = false
    protected var winShown = false
    protected var served = 0
    protected var angryN = 0
    protected var mined = 0

    protected var shake = 0f
    protected var flash = 0f
    protected var flashC = Color.WHITE
    protected var saveT = 0f
    protected var time = 0f

    protected var joyId = -1
    protected var jx = 0f
    protected var jy = 0f
    protected var jdx = 0f
    protected var jdy = 0f
    protected var pinch = false
    protected var pinchD = 0f
    protected var pinchZ = 1f
    protected var downT = 0L
    protected var downX = 0f
    protected var downY = 0f
    protected val uiLock = Any()
    protected val btns = ArrayList<UiBtn>()
    protected val btnBack = ArrayList<UiBtn>()
    protected var panel = 0
    protected var offlineAmt = 0f
    protected var offlineSec = 0f
    protected var tipSpot: Spot? = null
    protected var tipT = 0f
    protected var toastS = ""
    protected var toastT = 0f
    protected var crash: String? = null

    protected var floorBmp: Bitmap? = null
    protected val BOX = -720f
    protected val BOY = -140f
    protected val BW = 2140
    protected val BH = 1240

    protected fun walkable(x: Float, y: Float): Boolean {
        if (y < 1.2f || y > 16.8f) return false
        for (t in terrs) if (t.open && x >= t.ox + 0.2f && x <= t.ox + 10.8f) return true
        if (terrs[1].open && x > 11.5f && x < 15.5f && y > 6.2f && y < 8.8f) return true
        if (terrs[2].open && x > 25.5f && x < 29.5f && y > 6.2f && y < 8.8f) return true
        return false
    }

    protected fun s2wx(sx: Float, sy: Float): Float {
        val a = (sx - W / 2f) / zoom + ix(camX, camY)
        val b = (sy - H / 2f) / zoom + iy(camX, camY)
        return (a / (TW / 2f) + b / (TH / 2f)) / 2f
    }

    protected fun s2wy(sx: Float, sy: Float): Float {
        val a = (sx - W / 2f) / zoom + ix(camX, camY)
        val b = (sy - H / 2f) / zoom + iy(camX, camY)
        return (b / (TH / 2f) - a / (TW / 2f)) / 2f
    }

    protected fun pCap() = 6 + (capLv - 1) * 3
    protected fun pSpeed() = 4.3f + (spdLv - 1) * 0.32f
    protected fun carCap() = 5 + (carLv - 1) * 2
    protected fun yieldN(t: Terr) = 1 + (t.vein.lv - 1)
    protected fun mineTime(t: Terr) = 0.55f / (1f + 0.07f * (t.vein.lv - 1))
    protected fun furnYield(t: Terr) = 1 + (t.furn.lv - 1) / 2
    protected fun anvYield(t: Terr) = 1 + (t.anv.lv - 1) / 2
    protected fun smithsAt(t: Terr, role: Int): Int {
        var n = 0
        for (a in actors) if (a.tier == t.i && a.job == 2 && a.post == role) n++
        return n
    }
    protected fun furnTime(t: Terr) = 1.15f / (1f + 0.14f * (t.furn.lv - 1)) / (1f + 0.85f * smithsAt(t, 1))
    protected fun anvTime(t: Terr) = 1.35f / (1f + 0.14f * (t.anv.lv - 1)) / (1f + 0.85f * smithsAt(t, 2))
    protected fun sellTime(t: Terr) = 1.7f / (1f + 0.80f * t.sellers)
    protected fun repMul() = 0.78f + rep / 100f * 0.44f
    protected fun price(t: Terr) = ITEM_BASE[t.i] * (1f + 0.26f * (t.cnt.lv - 1)) * repMul()

    protected fun burst(x: Float, y: Float, col: Int, n: Int, sp: Float, grav: Float) {
        val cnt = if (quality == 0) n / 3 else if (quality == 1) n * 2 / 3 else n
        for (i in 0 until cnt) {
            if (parts.size > 260) return
            val a = Math.random().toFloat() * 6.283f
            val v = (0.3f + Math.random().toFloat() * 0.7f) * sp
            parts.add(Part(x, y, cos(a) * v, sin(a) * v * 0.55f - sp * 0.35f,
                0.35f + Math.random().toFloat() * 0.5f, 0.85f,
                1.6f + Math.random().toFloat() * 2.6f, col, grav))
        }
    }

    protected fun ftxt(x: Float, y: Float, s: String, col: Int) {
        if (texts.size > 26) texts.removeAt(0)
        texts.add(Ftxt(x, y, s, col, 1.1f))
    }

    protected fun toast(s: String) { toastS = s; toastT = 2.6f }

    protected fun buzz(ms: Long) {
        if (!vibOn) return
        try { vib?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)) } catch (e: Exception) {}
    }

    protected fun fmt(v: Float): String {
        val n = v.toLong()
        return when {
            n >= 1000000L -> String.format(Locale.US, "%.2fM", n / 1000000f)
            n >= 10000L -> String.format(Locale.US, "%.1fK", n / 1000f)
            else -> n.toString()
        }
    }

    protected fun mmss(s: Float): String {
        val m = (s / 60f).toInt()
        val ss = (s % 60f).toInt()
        return m.toString() + ":" + (if (ss < 10) "0" else "") + ss
    }

    protected fun spotCost(s: Spot): Float {
        val t = terrs[s.tier]
        val m = TIER_COST_MUL[s.tier]
        return when (s.kind) {
            0 -> 120f * Math.pow(2.15, t.miners.toDouble()).toFloat() * m
            1 -> 180f * Math.pow(2.10, t.carriers.toDouble()).toFloat() * m
            2 -> 220f * Math.pow(2.30, t.smiths.toDouble()).toFloat() * m
            3 -> 260f * Math.pow(2.30, t.sellers.toDouble()).toFloat() * m
            4 -> 60f * Math.pow(1.72, (t.vein.lv - 1).toDouble()).toFloat() * m
            5 -> 100f * Math.pow(1.78, (t.furn.lv - 1).toDouble()).toFloat() * m
            6 -> 140f * Math.pow(1.78, (t.anv.lv - 1).toDouble()).toFloat() * m
            7 -> 180f * Math.pow(1.85, (t.cnt.lv - 1).toDouble()).toFloat() * m
            8 -> if (s.tier == 1) 2600f else 48000f
            9 -> 110f * Math.pow(1.85, (capLv - 1).toDouble()).toFloat()
            else -> 90f * Math.pow(1.90, (spdLv - 1).toDouble()).toFloat()
        }
    }

    protected fun spotMax(s: Spot): Boolean = when (s.kind) {
        0 -> terrs[s.tier].miners >= 3
        1 -> terrs[s.tier].carriers >= 4
        2 -> terrs[s.tier].smiths >= 2
        3 -> terrs[s.tier].sellers >= 2
        4 -> terrs[s.tier].vein.lv >= 8
        5 -> terrs[s.tier].furn.lv >= 8
        6 -> terrs[s.tier].anv.lv >= 8
        7 -> terrs[s.tier].cnt.lv >= 8
        8 -> terrs[s.tier].open
        9 -> capLv >= 8
        else -> spdLv >= 6
    }

    protected fun spotName(s: Spot): String = when (s.kind) {
        0 -> "ДОБЫТЧИК"
        1 -> "ПЕРЕНОСЧИК"
        2 -> "КУЗНЕЦ"
        3 -> "ПРОДАВЕЦ"
        4 -> "БОГАТСТВО ЖИЛЫ"
        5 -> "ЖАР ГОРНА"
        6 -> "СИЛА МОЛОТА"
        7 -> "ЦЕНА ИЗДЕЛИЯ"
        8 -> "ОТКРЫТЬ ТЕРРАСУ"
        9 -> "ВМЕСТИМОСТЬ"
        else -> "СКОРОСТЬ БЕГА"
    }

    protected fun spotDesc(s: Spot): String {
        val t = terrs[s.tier]
        return when (s.kind) {
            0 -> "копает медленно, сам не тянет · " + t.miners + "/3"
            1 -> "возит руду, слитки и изделия · " + t.carriers + "/4"
            2 -> "ускоряет горн и наковальню · " + t.smiths + "/2"
            3 -> "торгует без тебя, деньги на счёт · " + t.sellers + "/2"
            4 -> "больше руды за удар · ур. " + t.vein.lv + "/8"
            5 -> "плавит ×" + furnYield(t) + " слитков за цикл · ур. " + t.furn.lv + "/8"
            6 -> "кует ×" + anvYield(t) + " изделий за цикл · ур. " + t.anv.lv + "/8"
            7 -> "цена выше, склад и очередь больше · ур. " + t.cnt.lv + "/8"
            8 -> TIER_NAME[s.tier] + " · " + ITEM_NAME[s.tier] + " по " + ITEM_BASE[s.tier].toInt()
            9 -> "носишь " + pCap() + " предметов · ур. " + capLv + "/8"
            else -> "бежишь " + String.format(Locale.US, "%.1f", pSpeed()) + " кл/с · ур. " + spdLv + "/6"
        }
    }

    protected fun buildSpots() {
        spots.clear()
        for (t in terrs) {
            val o = t.ox
            spots.add(Spot(t.i, 4, o + 4.4f, 2.0f))
            spots.add(Spot(t.i, 5, o + 9.2f, 2.0f))
            spots.add(Spot(t.i, 6, o + 9.2f, 6.3f))
            spots.add(Spot(t.i, 7, o + 4.4f, 10.0f))
            spots.add(Spot(t.i, 0, o + 1.5f, 4.6f))
            spots.add(Spot(t.i, 2, o + 9.2f, 8.9f))
            spots.add(Spot(t.i, 3, o + 1.5f, 12.4f))
            spots.add(Spot(t.i, 1, o + 5.2f, 5.6f))
        }
        spots.add(Spot(0, 9, 5.4f, 14.6f))
        spots.add(Spot(0, 10, 7.8f, 14.6f))
        spots.add(Spot(1, 8, 11.4f, 7.5f))
        spots.add(Spot(2, 8, 24.4f, 7.5f))
    }

    protected abstract fun update(dt: Float)
    protected abstract fun render(c: Canvas)
    protected abstract fun save()
    protected abstract fun resetAll()
    protected open fun renderCrash(c: Canvas) {}

    override fun surfaceCreated(h: SurfaceHolder) {
        if (running) return
        running = true
        val t = Thread(this)
        t.name = "forge"
        thread = t
        t.start()
        sfx.on = soundOn
        sfx.start()
    }

    override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, hgt: Int) {
        W = w.toFloat()
        H = hgt.toFloat()
        if (zoom == 1f) zoom = cl(W / (300f * dp), 0.9f, 1.8f)
    }

    override fun surfaceDestroyed(h: SurfaceHolder) {
        running = false
        try { thread?.join(600) } catch (e: Exception) {}
        thread = null
        save()
    }

    override fun run() {
        var last = System.nanoTime()
        while (running) {
            val now = System.nanoTime()
            var dt = (now - last) / 1e9f
            last = now
            if (dt > 0.05f) dt = 0.05f
            if (dt < 0f) dt = 0f
            if (W <= 0f || H <= 0f) {
                try { Thread.sleep(16) } catch (e: Exception) {}
                continue
            }
            var cv: Canvas? = null
            try {
                cv = holder.lockCanvas()
                if (cv != null) {
                    if (crash == null) { update(dt); render(cv) } else renderCrash(cv)
                }
            } catch (t: Throwable) {
                crash = Log.getStackTraceString(t)
                try { File(context.filesDir, "crash.txt").writeText(crash ?: "") } catch (e: Exception) {}
            } finally {
                if (cv != null) try { holder.unlockCanvasAndPost(cv) } catch (e: Exception) {}
            }
            val el = (System.nanoTime() - now) / 1000000L
            val sl = 16L - el
            if (sl > 0) try { Thread.sleep(sl) } catch (e: Exception) {}
        }
    }
}
