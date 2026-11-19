package com.groma.forge

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

abstract class ForgeModal(ctx: android.content.Context) : ForgeHud(ctx) {

    override fun modal(c: Canvas) {
        val u = dp
        P.color = Color.parseColor("#D80A0705")
        c.drawRect(0f, 0f, W, H, P)
        val mw = min(W - 24f * u, 340f * u)
        val mx = (W - mw) / 2f
        var mh = when (panel) {
            1 -> 330f * u
            2 -> 400f * u
            3 -> 300f * u
            4 -> 230f * u
            else -> 180f * u
        }
        mh = min(mh, H - 60f * u)
        val my = (H - mh) / 2f
        panelBox(c, mx, my, mw, mh, 16f * u)
        PT.textAlign = Paint.Align.LEFT
        val x0 = mx + 18f * u
        var y = my + 34f * u

        when (panel) {
            1 -> {
                PT.typeface = TF_BLACK
                PT.textSize = 22f * u
                PT.color = Color.parseColor(C_GOLD)
                c.drawText("ПАУЗА", x0, y, PT)
                y += 22f * u
                PT.typeface = TF_MONO
                PT.textSize = 11f * u
                PT.color = Color.parseColor(C_MUTE)
                c.drawText("в игре " + mmss(playT) + " · изделий " + served + " · руды " + mined, x0, y, PT)
                y += 16f * u
                c.drawText("ушли злыми " + angryN + " · работников " + actors.size, x0, y, PT)
                y += 22f * u
                val bw = mw - 36f * u
                val bh = 42f * u
                button(c, x0, y, bw, bh, "ПРОДОЛЖИТЬ", "resume", true)
                y += bh + 8f * u
                button(c, x0, y, bw / 2 - 4f * u, bh, if (soundOn) "ЗВУК ВКЛ" else "ЗВУК ВЫКЛ", "sound", false)
                button(c, x0 + bw / 2 + 4f * u, y, bw / 2 - 4f * u, bh, if (vibOn) "ВИБРО ВКЛ" else "ВИБРО ВЫКЛ", "vib", false)
                y += bh + 8f * u
                button(c, x0, y, bw, bh,
                    "КАЧЕСТВО: " + (if (quality == 2) "ВЫСОКОЕ" else if (quality == 1) "СРЕДНЕЕ" else "НИЗКОЕ"),
                    "qual", false)
                y += bh + 8f * u
                button(c, x0, y, bw, bh, "СБРОСИТЬ ПРОГРЕСС", "reset", false)
            }
            2 -> {
                PT.typeface = TF_BLACK
                PT.textSize = 20f * u
                PT.color = Color.parseColor(C_GOLD)
                c.drawText("КАК КОВАТЬ", x0, y, PT)
                y += 24f * u
                val lines = arrayOf(
                    "1 · Веди пальцем в любом месте — дворф бежит.",
                    "2 · Встань у ЖИЛЫ: кирка бьёт сама, руда копится на спине.",
                    "3 · Неси руду в ГОРН — он плавит слитки.",
                    "4 · Забери слитки, неси на НАКОВАЛЬНУ — получишь изделие.",
                    "5 · Изделия — на ПРИЛАВОК. Клиенты платят монетами.",
                    "6 · Монеты подбираются ногами. Не заставляй клиентов ждать: слава падает, цена вместе с ней.",
                    "7 · Пунктирные круги — покупки. Встань в круг, монеты польются сами.",
                    "8 · Добытчик копает, переносчик возит, кузнец ускоряет, продавец торгует.",
                    "9 · Открывай террасы: железо дороже меди, золото дороже железа.",
                    "10 · Прогресс сохраняется сам. Работники трудятся и без тебя."
                )
                PT.typeface = TF_BOLD
                PT.textSize = 10.5f * u
                for (l in lines) {
                    PT.color = Color.parseColor(C_PARCH)
                    val words = l.split(" ")
                    var line = ""
                    for (w in words) {
                        val test = if (line.isEmpty()) w else line + " " + w
                        if (PT.measureText(test) > mw - 36f * u) {
                            c.drawText(line, x0, y, PT)
                            y += 14f * u
                            line = w
                        } else line = test
                    }
                    if (line.isNotEmpty()) {
                        c.drawText(line, x0, y, PT)
                        y += 14f * u
                    }
                    y += 4f * u
                }
                val bh = 42f * u
                button(c, x0, my + mh - bh - 16f * u, mw - 36f * u, bh, "ПОНЯТНО", "resume", true)
            }
            3 -> {
                PT.typeface = TF_BLACK
                PT.textSize = 21f * u
                PT.color = Color.parseColor(C_GOLD)
                c.drawText("КУЗНЯ ГРОМА", x0, y, PT)
                y += 24f * u
                PT.color = Color.parseColor(C_EMBER)
                c.drawText("ВОССТАНОВЛЕНА", x0, y, PT)
                y += 26f * u
                PT.typeface = TF_BOLD
                PT.textSize = 11.5f * u
                PT.color = Color.parseColor(C_PARCH)
                c.drawText("Клан снова кует гром. Заработано " + fmt(earned) + ".", x0, y, PT)
                y += 18f * u
                PT.typeface = TF_MONO
                PT.textSize = 11f * u
                PT.color = Color.parseColor(C_MUTE)
                c.drawText("время " + mmss(playT) + " · изделий " + served, x0, y, PT)
                y += 16f * u
                c.drawText("работников " + actors.size + " · слава " + rep.toInt(), x0, y, PT)
                val bh = 42f * u
                button(c, x0, my + mh - bh - 16f * u, mw - 36f * u, bh, "КОВАТЬ ДАЛЬШЕ", "resume", true)
            }
            4 -> {
                PT.typeface = TF_BLACK
                PT.textSize = 19f * u
                PT.color = Color.parseColor(C_GOLD)
                c.drawText("ПОКА ТЕБЯ НЕ БЫЛО", x0, y, PT)
                y += 24f * u
                PT.typeface = TF_BOLD
                PT.textSize = 11.5f * u
                PT.color = Color.parseColor(C_PARCH)
                c.drawText("Работники ковали " + mmss(offlineSec) + ".", x0, y, PT)
                y += 30f * u
                PT.typeface = TF_MONO
                PT.textSize = 30f * u
                PT.color = Color.parseColor(C_GOLD)
                c.drawText("+" + fmt(offlineAmt), x0, y, PT)
                y += 20f * u
                PT.typeface = TF_BOLD
                PT.textSize = 10f * u
                PT.color = Color.parseColor(C_MUTE)
                c.drawText("монеты уже в кассе", x0, y, PT)
                val bh = 42f * u
                button(c, x0, my + mh - bh - 16f * u, mw - 36f * u, bh, "ЗА РАБОТУ", "resume", true)
            }
            else -> {
                PT.typeface = TF_BLACK
                PT.textSize = 18f * u
                PT.color = Color.parseColor(C_RED)
                c.drawText("СТЕРЕТЬ ВСЁ?", x0, y, PT)
                y += 24f * u
                PT.typeface = TF_BOLD
                PT.textSize = 11.5f * u
                PT.color = Color.parseColor(C_PARCH)
                c.drawText("Монеты, работники и террасы исчезнут.", x0, y, PT)
                y += 16f * u
                c.drawText("Начнёшь с первой жилы.", x0, y, PT)
                val bh = 42f * u
                val bw = mw - 36f * u
                button(c, x0, my + mh - bh * 2 - 24f * u, bw / 2 - 4f * u, bh, "ОТМЕНА", "cancel", false)
                button(c, x0 + bw / 2 + 4f * u, my + mh - bh * 2 - 24f * u, bw / 2 - 4f * u, bh, "СТЕРЕТЬ", "doreset", true)
            }
        }
    }

    override fun renderCrash(c: Canvas) {
        val u = dp
        P.color = Color.parseColor("#120C09")
        c.drawRect(0f, 0f, W, H, P)
        PT.textAlign = Paint.Align.LEFT
        PT.typeface = TF_BLACK
        PT.textSize = 18f * u
        PT.color = Color.parseColor(C_RED)
        c.drawText("ОШИБКА КУЗНИ", 16f * u, 40f * u, PT)
        PT.typeface = TF_MONO
        PT.textSize = 9f * u
        PT.color = Color.parseColor(C_PARCH)
        val txt = crash ?: ""
        var y = 66f * u
        for (raw in txt.split("\n")) {
            var line = raw
            while (line.isNotEmpty() && y < H - 90f * u) {
                var cut = line.length
                while (cut > 1 && PT.measureText(line.substring(0, cut)) > W - 32f * u) cut--
                c.drawText(line.substring(0, cut), 16f * u, y, PT)
                y += 12f * u
                line = line.substring(cut)
            }
            if (y >= H - 90f * u) break
        }
        btnBack.clear()
        button(c, 16f * u, H - 64f * u, W - 32f * u, 44f * u, "СБРОСИТЬ И ИГРАТЬ", "doreset", true)
        synchronized(uiLock) {
            btns.clear()
            btns.addAll(btnBack)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val act = e.actionMasked
        val idx = e.actionIndex
        val id = e.getPointerId(idx)
        val x = e.getX(idx)
        val y = e.getY(idx)
        when (act) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (act == MotionEvent.ACTION_DOWN) {
                    downT = System.currentTimeMillis()
                    downX = x
                    downY = y
                    var hit = false
                    synchronized(uiLock) {
                        for (i in btns.indices.reversed()) {
                            val b = btns[i]
                            if (b.r.contains(x, y)) { tap(b.tag); hit = true; break }
                        }
                    }
                    if (hit) return true
                    if (panel != 0) return true
                    joyId = id
                    jx = x
                    jy = y
                    jdx = 0f
                    jdy = 0f
                    sfx.start()
                } else {
                    val i2 = e.findPointerIndex(joyId)
                    if (i2 >= 0) {
                        val ax = e.getX(i2)
                        val ay = e.getY(i2)
                        pinchD = sqrt((ax - x) * (ax - x) + (ay - y) * (ay - y))
                        pinchZ = zoom
                        pinch = true
                        joyId = -1
                        jdx = 0f
                        jdy = 0f
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (pinch && e.pointerCount >= 2) {
                    val a = e.getX(0)
                    val b = e.getY(0)
                    val cx2 = e.getX(1)
                    val cy2 = e.getY(1)
                    val d = sqrt((a - cx2) * (a - cx2) + (b - cy2) * (b - cy2))
                    if (pinchD > 1f) zoom = cl(pinchZ * d / pinchD, 0.5f, 2.0f)
                } else if (id == joyId) {
                    val r = 52f * dp
                    jdx = cl((x - jx) / r, -1f, 1f)
                    jdy = cl((y - jy) / r, -1f, 1f)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (id == joyId) { joyId = -1; jdx = 0f; jdy = 0f }
                if (e.pointerCount <= 2) pinch = false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (id == joyId) {
                    joyId = -1
                    jdx = 0f
                    jdy = 0f
                    val dt = System.currentTimeMillis() - downT
                    if (dt < 260 && abs(x - downX) < 14f * dp && abs(y - downY) < 14f * dp && panel == 0) {
                        val wx = s2wx(x, y)
                        val wy = s2wy(x, y)
                        var best: Spot? = null
                        var bd = 1.6f
                        for (s in spots) {
                            if (s.done) continue
                            if (s.kind != 8 && !terrs[s.tier].open) continue
                            val d = sqrt((s.x - wx) * (s.x - wx) + (s.y - wy) * (s.y - wy))
                            if (d < bd) { bd = d; best = s }
                        }
                        if (best != null) { tipSpot = best; tipT = 4f; sfx.ui() }
                    }
                }
                pinch = false
            }
        }
        return true
    }

    protected fun tap(tag: String) {
        sfx.start()
        sfx.ui()
        when (tag) {
            "sound" -> { soundOn = !soundOn; sfx.on = soundOn; save() }
            "pause" -> { panel = if (panel == 1) 0 else 1; if (panel == 1) save() }
            "info" -> { panel = if (panel == 2) 0 else 2 }
            "resume" -> { panel = 0 }
            "vib" -> { vibOn = !vibOn; save(); if (vibOn) buzz(30) }
            "qual" -> { quality = (quality + 2) % 3; save() }
            "reset" -> { panel = 5 }
            "cancel" -> { panel = 1 }
            "doreset" -> { crash = null; resetAll() }
        }
    }

    fun back() {
        if (panel != 0) { panel = 0; return }
        panel = 1
        save()
    }

    fun onHostPause() {
        paused = true
        save()
        sfx.amb = 0f
        try { sfx.stop() } catch (e: Exception) {}
    }

    fun onHostResume() {
        paused = false
        sfx.on = soundOn
        sfx.start()
    }

    fun shutdown() {
        running = false
        save()
        sfx.stop()
    }
}
