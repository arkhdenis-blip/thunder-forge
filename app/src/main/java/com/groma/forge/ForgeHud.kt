package com.groma.forge

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

abstract class ForgeHud(ctx: android.content.Context) : ForgeDraw(ctx) {

    override fun render(c: Canvas) {
        P.style = Paint.Style.FILL
        P.shader = LinearGradient(0f, 0f, 0f, H,
            intArrayOf(Color.parseColor(C_BG1), Color.parseColor(C_BG0), Color.parseColor("#2A0F06")),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, W, H, P)
        P.shader = null

        val lavaY = H * 0.86f
        P.shader = LinearGradient(0f, lavaY - 90f, 0f, H,
            intArrayOf(Color.parseColor("#00000000"), Color.parseColor("#57160A"), Color.parseColor(C_LAVA)),
            floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP)
        P.alpha = 90
        c.drawRect(0f, lavaY - 90f, W, H, P)
        P.alpha = 255
        P.shader = null
        if (quality > 0) {
            for (i in 0 until 14) {
                val t = time * 0.25f + i * 1.7f
                val ex = (i * 137.5f + sin(t) * 40f) % W
                val ey = H - ((time * 26f + i * 90f) % (H * 0.5f))
                P.color = Color.parseColor(if (i % 3 == 0) C_GOLD else C_EMBER)
                P.alpha = (70 + 60 * sin(t * 2f)).toInt().coerceIn(0, 255)
                c.drawCircle(ex, ey, 1.6f * dp, P)
            }
            P.alpha = 255
        }

        c.save()
        c.translate(W / 2f + (Math.random().toFloat() - .5f) * shake,
            H / 2f + (Math.random().toFloat() - .5f) * shake)
        c.scale(zoom, zoom)
        c.translate(-ix(camX, camY), -iy(camX, camY))

        floorBmp?.let { c.drawBitmap(it, BOX, BOY, null) }
        drawSpots(c)

        val items = ArrayList<Pair<Float, () -> Unit>>()
        for (t in terrs) {
            if (!t.open) continue
            items.add(Pair(t.vein.x + t.vein.y) { drawVein(c, t) })
            items.add(Pair(t.furn.x + t.furn.y) { drawFurnace(c, t) })
            items.add(Pair(t.anv.x + t.anv.y) { drawAnvil(c, t) })
            items.add(Pair(t.cnt.x + t.cnt.y) { drawCounter(c, t) })
            for (cu in t.custs) items.add(Pair(cu.x + cu.y) { drawCust(c, cu, t) })
            drawTorches(c, t)
        }
        for (a in actors) items.add(Pair(a.x + a.y) {
            drawDwarf(c, a.x, a.y, a.phase, a.face, a.kind, a.carry, a.cTier, a.job, a.swing)
        })
        items.add(Pair(px + py) { drawDwarf(c, px, py, pPhase, pFace, pKind, pCarry, pTier, -1, pSwing) })
        for (p in piles) items.add(Pair(p.x + p.y + 0.1f) { drawPile(c, p) })
        items.sortBy { it.first }
        for (it2 in items) it2.second()

        P.style = Paint.Style.FILL
        for (p in parts) {
            val a = cl(p.life / p.max, 0f, 1f)
            P.color = p.c
            P.alpha = (a * 235).toInt().coerceIn(0, 255)
            c.drawCircle(ix(p.x, p.y), iy(p.x, p.y) - 18f, p.r * a, P)
        }
        P.alpha = 255

        PT.textAlign = Paint.Align.CENTER
        for (t in texts) {
            val a = cl(t.life, 0f, 1f)
            PT.textSize = 20f
            PT.typeface = TF_BLACK
            val X = ix(t.x, t.y)
            val Y = iy(t.x, t.y) - 46f - (1f - t.life) * 26f
            PT.color = Color.parseColor("#000000")
            PT.alpha = (a * 150).toInt()
            c.drawText(t.s, X + 1.5f, Y + 1.5f, PT)
            PT.color = t.c
            PT.alpha = (a * 255).toInt()
            c.drawText(t.s, X, Y, PT)
        }
        PT.alpha = 255
        c.restore()

        if (flash > 0.002f) {
            P.color = flashC
            P.alpha = (flash * 70).toInt().coerceIn(0, 255)
            c.drawRect(0f, 0f, W, H, P)
            P.alpha = 255
        }
        hud(c)
    }

    protected fun drawTorches(c: Canvas, t: Terr) {
        val pts = arrayOf(
            floatArrayOf(t.ox + 0.6f, 1.6f), floatArrayOf(t.ox + 10.4f, 1.6f),
            floatArrayOf(t.ox + 0.6f, 16.4f), floatArrayOf(t.ox + 10.4f, 16.4f)
        )
        for (p in pts) {
            val X = ix(p[0], p[1])
            val Y = iy(p[0], p[1])
            P.color = Color.parseColor("#2A211A")
            c.drawRect(X - 3f, Y - 40f, X + 3f, Y, P)
            val fl = 0.7f + 0.3f * sin(time * 7f + p[0])
            P.shader = RadialGradient(X, Y - 44f, 46f * fl,
                intArrayOf(Color.parseColor("#88FF7A1A"), Color.parseColor("#00FF7A1A")),
                null, Shader.TileMode.CLAMP)
            c.drawCircle(X, Y - 44f, 46f * fl, P)
            P.shader = null
            P.color = Color.parseColor(C_EMBER)
            c.drawCircle(X, Y - 44f, 5.5f * fl, P)
            P.color = Color.parseColor(C_GOLD)
            c.drawCircle(X, Y - 45f, 3f * fl, P)
        }
    }

    protected fun drawSpots(c: Canvas) {
        for (s in spots) {
            if (s.kind == 8 && s.tier > 0 && !terrs[s.tier - 1].open) continue
            if (s.done) continue
            if (s.kind != 8 && !terrs[s.tier].open) continue
            val X = ix(s.x, s.y)
            val Y = iy(s.x, s.y)
            val maxed = spotMax(s)
            val cost = spotCost(s)
            val can = coins >= cost && !maxed
            val col = if (maxed) Color.parseColor("#4A4136")
            else if (can) Color.parseColor(C_GOLD) else Color.parseColor("#7A6A55")
            PL.color = col
            PL.strokeWidth = 2.6f
            PL.pathEffect = DashPathEffect(floatArrayOf(9f, 7f), (time * 22f) % 16f)
            c.drawOval(X - 30f, Y - 15f, X + 30f, Y + 15f, PL)
            PL.pathEffect = null
            if (can) {
                P.shader = RadialGradient(X, Y, 34f,
                    intArrayOf(Color.parseColor("#3AFFC23D"), Color.parseColor("#00FFC23D")),
                    null, Shader.TileMode.CLAMP)
                c.drawOval(X - 34f, Y - 17f, X + 34f, Y + 17f, P)
                P.shader = null
            }
            if (s.paid > 0f) {
                PL.color = Color.parseColor(C_EMBER)
                PL.strokeWidth = 4f
                rect.set(X - 30f, Y - 15f, X + 30f, Y + 15f)
                c.drawArc(rect, -90f, 360f * cl(s.paid / cost, 0f, 1f), false, PL)
            }
            drawSpotIcon(c, X, Y - 4f, s.kind, col)
            PT.textAlign = Paint.Align.CENTER
            PT.typeface = TF_MONO
            PT.textSize = 14f
            val label = if (maxed) "МАКС" else fmt(cost)
            PT.color = Color.parseColor("#AA000000")
            c.drawText(label, X + 1f, Y + 27f, PT)
            PT.color = col
            c.drawText(label, X, Y + 26f, PT)
            if (s.pulse > 0f) {
                PL.color = Color.parseColor(C_GOLD)
                PL.strokeWidth = 3f
                PL.alpha = (s.pulse * 200).toInt()
                val r = 30f + (1f - s.pulse) * 40f
                c.drawOval(X - r, Y - r * .5f, X + r, Y + r * .5f, PL)
                PL.alpha = 255
            }
        }
    }

    protected fun drawSpotIcon(c: Canvas, X: Float, Y: Float, kind: Int, col: Int) {
        P.color = col
        PL.color = col
        PL.strokeWidth = 2.4f
        when (kind) {
            0 -> { c.drawLine(X - 8f, Y + 6f, X + 7f, Y - 8f, PL); c.drawLine(X + 2f, Y - 11f, X + 11f, Y - 3f, PL) }
            1 -> { c.drawRect(X - 8f, Y - 6f, X + 8f, Y + 6f, PL); c.drawLine(X - 8f, Y - 6f, X + 8f, Y + 6f, PL) }
            2 -> { c.drawRect(X - 9f, Y - 8f, X + 9f, Y - 2f, P); c.drawRect(X - 2f, Y - 2f, X + 2f, Y + 8f, P) }
            3 -> {
                c.drawCircle(X, Y - 1f, 8f, PL)
                PT.textAlign = Paint.Align.CENTER
                PT.textSize = 13f
                PT.typeface = TF_BLACK
                PT.color = col
                c.drawText("$", X, Y + 4f, PT)
            }
            4 -> {
                path.reset(); path.moveTo(X, Y - 10f); path.lineTo(X + 8f, Y)
                path.lineTo(X, Y + 9f); path.lineTo(X - 8f, Y); path.close(); c.drawPath(path, P)
            }
            5 -> {
                path.reset(); path.moveTo(X, Y - 11f); path.lineTo(X + 7f, Y + 2f)
                path.lineTo(X + 2f, Y + 9f); path.lineTo(X - 6f, Y + 3f); path.close(); c.drawPath(path, P)
            }
            6 -> { c.drawRect(X - 9f, Y - 7f, X + 9f, Y - 1f, P); c.drawRect(X - 2.5f, Y - 1f, X + 2.5f, Y + 9f, P) }
            7 -> { c.drawLine(X, Y - 10f, X, Y + 9f, PL); c.drawCircle(X, Y - 2f, 7f, PL) }
            8 -> {
                c.drawLine(X - 9f, Y, X + 9f, Y, PL)
                c.drawLine(X + 3f, Y - 6f, X + 9f, Y, PL)
                c.drawLine(X + 3f, Y + 6f, X + 9f, Y, PL)
            }
            9 -> {
                c.drawRect(X - 8f, Y - 4f, X + 8f, Y + 8f, PL)
                c.drawLine(X - 5f, Y - 4f, X - 5f, Y - 9f, PL)
                c.drawLine(X + 5f, Y - 4f, X + 5f, Y - 9f, PL)
            }
            else -> {
                path.reset(); path.moveTo(X - 8f, Y + 8f); path.lineTo(X + 2f, Y - 2f)
                path.lineTo(X - 2f, Y - 4f); path.lineTo(X + 8f, Y - 10f); c.drawPath(path, PL)
            }
        }
    }

    protected fun panelBox(c: Canvas, x: Float, y: Float, w: Float, h: Float, r: Float) {
        P.color = Color.parseColor("#F0181210")
        c.drawRoundRect(RectF(x, y, x + w, y + h), r, r, P)
        PL.color = Color.parseColor("#4A3A2C")
        PL.strokeWidth = 1.5f * dp
        c.drawRoundRect(RectF(x, y, x + w, y + h), r, r, PL)
        P.shader = LinearGradient(x, y, x + w, y,
            intArrayOf(Color.parseColor(C_EMBER), Color.parseColor(C_GOLD), Color.parseColor(C_COPPER)),
            null, Shader.TileMode.CLAMP)
        c.drawRect(x + r * 0.5f, y, x + w - r * 0.5f, y + 2f * dp, P)
        P.shader = null
    }

    protected fun button(c: Canvas, x: Float, y: Float, w: Float, h: Float,
                         label: String, tag: String, primary: Boolean) {
        if (primary) {
            P.shader = LinearGradient(x, y, x + w, y + h,
                intArrayOf(Color.parseColor(C_EMBER), Color.parseColor(C_GOLD)),
                null, Shader.TileMode.CLAMP)
            c.drawRoundRect(RectF(x, y, x + w, y + h), 11f * dp, 11f * dp, P)
            P.shader = null
            PT.color = Color.parseColor("#20140A")
        } else {
            P.color = Color.parseColor("#241B15")
            c.drawRoundRect(RectF(x, y, x + w, y + h), 11f * dp, 11f * dp, P)
            PL.color = Color.parseColor("#4A3A2C")
            PL.strokeWidth = 1.2f * dp
            c.drawRoundRect(RectF(x, y, x + w, y + h), 11f * dp, 11f * dp, PL)
            PT.color = Color.parseColor(C_PARCH)
        }
        PT.textAlign = Paint.Align.CENTER
        PT.typeface = TF_BLACK
        PT.textSize = 13f * dp
        c.drawText(label, x + w / 2f, y + h / 2f + 4.5f * dp, PT)
        btnBack.add(UiBtn(RectF(x, y, x + w, y + h), tag))
    }

    protected fun iconBtn(c: Canvas, x: Float, y: Float, s: Float, tag: String, icon: Int, active: Boolean) {
        P.color = if (active) Color.parseColor("#33FF7A1A") else Color.parseColor("#CC181210")
        c.drawRoundRect(RectF(x, y, x + s, y + s), 11f * dp, 11f * dp, P)
        PL.color = if (active) Color.parseColor(C_EMBER) else Color.parseColor("#4A3A2C")
        PL.strokeWidth = 1.4f * dp
        c.drawRoundRect(RectF(x, y, x + s, y + s), 11f * dp, 11f * dp, PL)
        val cx = x + s / 2f
        val cy = y + s / 2f
        val u = dp
        P.color = if (active) Color.parseColor(C_GOLD) else Color.parseColor(C_MUTE)
        PL.color = P.color
        PL.strokeWidth = 2f * u
        when (icon) {
            0 -> {
                path.reset()
                path.moveTo(cx - 7 * u, cy - 3 * u); path.lineTo(cx - 3 * u, cy - 3 * u)
                path.lineTo(cx + 1 * u, cy - 7 * u); path.lineTo(cx + 1 * u, cy + 7 * u)
                path.lineTo(cx - 3 * u, cy + 3 * u); path.lineTo(cx - 7 * u, cy + 3 * u)
                path.close()
                c.drawPath(path, P)
                if (soundOn) c.drawArc(RectF(cx - 1 * u, cy - 6 * u, cx + 8 * u, cy + 6 * u), -55f, 110f, false, PL)
                else {
                    c.drawLine(cx + 3 * u, cy - 4 * u, cx + 8 * u, cy + 4 * u, PL)
                    c.drawLine(cx + 8 * u, cy - 4 * u, cx + 3 * u, cy + 4 * u, PL)
                }
            }
            1 -> {
                if (panel == 1 || paused) {
                    path.reset()
                    path.moveTo(cx - 4 * u, cy - 7 * u)
                    path.lineTo(cx + 7 * u, cy)
                    path.lineTo(cx - 4 * u, cy + 7 * u)
                    path.close()
                    c.drawPath(path, P)
                } else {
                    c.drawRect(cx - 6 * u, cy - 7 * u, cx - 2 * u, cy + 7 * u, P)
                    c.drawRect(cx + 2 * u, cy - 7 * u, cx + 6 * u, cy + 7 * u, P)
                }
            }
            else -> {
                c.drawCircle(cx, cy, 8 * u, PL)
                PT.textAlign = Paint.Align.CENTER
                PT.typeface = TF_BLACK
                PT.textSize = 12 * u
                PT.color = P.color
                c.drawText("?", cx, cy + 4.5f * u, PT)
            }
        }
        btnBack.add(UiBtn(RectF(x, y, x + s, y + s), tag))
    }

    protected fun hud(c: Canvas) {
        btnBack.clear()
        val u = dp
        val pad = 10f * u

        val cw = 132f * u
        val ch = 38f * u
        panelBox(c, pad, pad, cw, ch, 12f * u)
        P.color = Color.parseColor(C_GOLD)
        c.drawCircle(pad + 20f * u, pad + ch / 2f, 10f * u, P)
        P.color = Color.parseColor("#B8860B")
        c.drawCircle(pad + 20f * u, pad + ch / 2f, 6.5f * u, P)
        PT.textAlign = Paint.Align.LEFT
        PT.typeface = TF_MONO
        PT.textSize = 19f * u
        PT.color = Color.parseColor(C_PARCH)
        c.drawText(fmt(shownCoins), pad + 36f * u, pad + ch / 2f + 7f * u, PT)

        val ry = pad + ch + 6f * u
        panelBox(c, pad, ry, cw, 22f * u, 9f * u)
        PT.textSize = 9f * u
        PT.typeface = TF_BOLD
        PT.color = Color.parseColor(C_MUTE)
        c.drawText("СЛАВА", pad + 9f * u, ry + 15f * u, PT)
        val bx = pad + 46f * u
        val bw = cw - 56f * u
        P.color = Color.parseColor("#2A211A")
        c.drawRoundRect(RectF(bx, ry + 8f * u, bx + bw, ry + 14f * u), 3f * u, 3f * u, P)
        P.color = if (rep > 55f) Color.parseColor(C_MOSS)
        else if (rep > 28f) Color.parseColor(C_GOLD) else Color.parseColor(C_RED)
        c.drawRoundRect(RectF(bx, ry + 8f * u, bx + bw * cl(rep / 100f, 0f, 1f), ry + 14f * u), 3f * u, 3f * u, P)

        val bs = 38f * u
        iconBtn(c, W - pad - bs, pad, bs, "sound", 0, soundOn)
        iconBtn(c, W - pad - bs * 2 - 6 * u, pad, bs, "pause", 1, panel == 1)
        iconBtn(c, W - pad - bs * 3 - 12 * u, pad, bs, "info", 2, panel == 2)

        val gy = pad + bs + 8f * u
        val gw = W - pad * 2
        panelBox(c, pad, gy, gw, 30f * u, 10f * u)
        PT.textAlign = Paint.Align.LEFT
        PT.typeface = TF_BOLD
        PT.textSize = 9.5f * u
        PT.color = Color.parseColor(C_MUTE)
        c.drawText("КУЗНЯ ГРОМА", pad + 10f * u, gy + 12f * u, PT)
        PT.textAlign = Paint.Align.RIGHT
        PT.typeface = TF_MONO
        PT.color = Color.parseColor(C_GOLD)
        c.drawText(fmt(earned) + " / " + fmt(GOAL), W - pad - 10f * u, gy + 12f * u, PT)
        val gx = pad + 10f * u
        val gww = gw - 20f * u
        P.color = Color.parseColor("#2A211A")
        c.drawRoundRect(RectF(gx, gy + 17f * u, gx + gww, gy + 24f * u), 4f * u, 4f * u, P)
        P.shader = LinearGradient(gx, 0f, gx + gww, 0f,
            intArrayOf(Color.parseColor(C_EMBER), Color.parseColor(C_GOLD)), null, Shader.TileMode.CLAMP)
        c.drawRoundRect(RectF(gx, gy + 17f * u, gx + gww * cl(earned / GOAL, 0f, 1f), gy + 24f * u), 4f * u, 4f * u, P)
        P.shader = null

        val qy = gy + 36f * u
        PT.textAlign = Paint.Align.LEFT
        PT.typeface = TF_BOLD
        PT.textSize = 11f * u
        val qt = questText()
        val qw = PT.measureText(qt) + 26f * u
        P.color = Color.parseColor("#D0181210")
        c.drawRoundRect(RectF(pad, qy, pad + qw, qy + 24f * u), 9f * u, 9f * u, P)
        P.color = Color.parseColor(C_EMBER)
        c.drawCircle(pad + 12f * u, qy + 12f * u, 4f * u, P)
        PT.color = Color.parseColor(C_PARCH)
        c.drawText(qt, pad + 21f * u, qy + 16f * u, PT)

        if (pCarry > 0) {
            val pw = 108f * u
            val ph = 42f * u
            val pxx = W - pad - pw
            val pyy = H - pad - ph - 74f * u
            panelBox(c, pxx, pyy, pw, ph, 12f * u)
            val col = when (pKind) {
                R_ORE -> TIER_ORE[pTier]
                R_ING -> TIER_ING[pTier]
                else -> TIER_ITEM[pTier]
            }
            P.color = col
            c.drawCircle(pxx + 20f * u, pyy + ph / 2f, 9f * u, P)
            PT.textAlign = Paint.Align.LEFT
            PT.typeface = TF_MONO
            PT.textSize = 15f * u
            PT.color = Color.parseColor(C_PARCH)
            c.drawText(pCarry.toString() + "/" + pCap(), pxx + 36f * u, pyy + ph / 2f + 2f * u, PT)
            PT.textSize = 8.5f * u
            PT.typeface = TF_BOLD
            PT.color = Color.parseColor(C_MUTE)
            val nm = when (pKind) {
                R_ORE -> "РУДА"
                R_ING -> "СЛИТКИ"
                else -> "ИЗДЕЛИЯ"
            }
            c.drawText(nm, pxx + 36f * u, pyy + ph / 2f + 14f * u, PT)
        }

        if (joyId >= 0 && panel == 0) {
            PL.color = Color.parseColor("#55F5E6CA")
            PL.strokeWidth = 2.5f * u
            c.drawCircle(jx, jy, 52f * u, PL)
            P.color = Color.parseColor("#22F5E6CA")
            c.drawCircle(jx, jy, 52f * u, P)
            P.color = Color.parseColor("#CCFF7A1A")
            c.drawCircle(jx + jdx * 40f * u, jy + jdy * 40f * u, 22f * u, P)
            PL.color = Color.parseColor(C_GOLD)
            PL.strokeWidth = 2f * u
            c.drawCircle(jx + jdx * 40f * u, jy + jdy * 40f * u, 22f * u, PL)
        } else if (panel == 0 && playT < 22f) {
            val a = 0.35f + 0.25f * sin(time * 3f)
            PL.color = Color.parseColor(C_PARCH)
            PL.alpha = (a * 160).toInt()
            PL.strokeWidth = 2f * u
            c.drawCircle(W / 2f, H - 96f * u, 40f * u, PL)
            PL.alpha = 255
            PT.textAlign = Paint.Align.CENTER
            PT.typeface = TF_BOLD
            PT.textSize = 10.5f * u
            PT.color = Color.parseColor(C_MUTE)
            c.drawText("ВЕДИ ПАЛЬЦЕМ — ДВОРФ БЕЖИТ", W / 2f, H - 40f * u, PT)
        }

        val ts = tipSpot
        if (ts != null && tipT > 0f) {
            val tw = min(W - pad * 2, 260f * u)
            val tx = (W - tw) / 2f
            val ty = H - 150f * u
            panelBox(c, tx, ty, tw, 66f * u, 12f * u)
            PT.textAlign = Paint.Align.LEFT
            PT.typeface = TF_BLACK
            PT.textSize = 13f * u
            PT.color = Color.parseColor(C_GOLD)
            c.drawText(spotName(ts), tx + 12f * u, ty + 22f * u, PT)
            PT.typeface = TF_BOLD
            PT.textSize = 10f * u
            PT.color = Color.parseColor(C_MUTE)
            c.drawText(spotDesc(ts), tx + 12f * u, ty + 39f * u, PT)
            PT.typeface = TF_MONO
            PT.textSize = 11f * u
            PT.color = if (spotMax(ts)) Color.parseColor(C_MOSS)
            else if (coins >= spotCost(ts)) Color.parseColor(C_GOLD) else Color.parseColor(C_RED)
            c.drawText(if (spotMax(ts)) "МАКСИМУМ" else ("ЦЕНА " + fmt(spotCost(ts)) + " · ВСТАНЬ В КРУГ"),
                tx + 12f * u, ty + 56f * u, PT)
        }

        if (toastT > 0f) {
            val a = cl(toastT / 0.5f, 0f, 1f)
            PT.textAlign = Paint.Align.CENTER
            PT.typeface = TF_BLACK
            PT.textSize = 12f * u
            val tw = PT.measureText(toastS) + 30f * u
            P.color = Color.parseColor("#E8181210")
            P.alpha = (a * 232).toInt()
            c.drawRoundRect(RectF((W - tw) / 2f, H - 108f * u, (W + tw) / 2f, H - 78f * u), 10f * u, 10f * u, P)
            P.alpha = 255
            PL.color = Color.parseColor(C_EMBER)
            PL.alpha = (a * 200).toInt()
            PL.strokeWidth = 1.3f * u
            c.drawRoundRect(RectF((W - tw) / 2f, H - 108f * u, (W + tw) / 2f, H - 78f * u), 10f * u, 10f * u, PL)
            PL.alpha = 255
            PT.color = Color.parseColor(C_PARCH)
            PT.alpha = (a * 255).toInt()
            c.drawText(toastS, W / 2f, H - 89f * u, PT)
            PT.alpha = 255
        }

        if (panel != 0) modal(c)
        synchronized(uiLock) {
            btns.clear()
            btns.addAll(btnBack)
        }
    }

    protected abstract fun modal(c: Canvas)
}
