package com.groma.forge

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

abstract class ForgeDraw(ctx: android.content.Context) : ForgeFloor(ctx) {

    protected fun shadow(c: Canvas, x: Float, y: Float, r: Float) {
        if (quality == 0) return
        val X = ix(x, y)
        val Y = iy(x, y)
        P.color = Color.parseColor("#66000000")
        c.drawOval(X - r, Y - r * 0.5f, X + r, Y + r * 0.5f, P)
    }

    protected fun drawStack(c: Canvas, X: Float, Y: Float, kind: Int, n: Int, tier: Int, sc: Float) {
        if (kind == 0 || n <= 0) return
        val col = when (kind) {
            R_ORE -> TIER_ORE[tier]
            R_ING -> TIER_ING[tier]
            else -> TIER_ITEM[tier]
        }
        val shown = min(n, 7)
        for (i in 0 until shown) {
            val yy = Y - i * 6f * sc
            P.color = Color.parseColor("#3A2E22")
            c.drawRoundRect(RectF(X - 11f * sc, yy - 5f * sc, X + 11f * sc, yy + 4f * sc), 3f * sc, 3f * sc, P)
            P.color = col
            if (kind == R_ORE) {
                path.reset()
                path.moveTo(X - 8f * sc, yy + 2f * sc)
                path.lineTo(X - 3f * sc, yy - 5f * sc)
                path.lineTo(X + 4f * sc, yy - 4f * sc)
                path.lineTo(X + 8f * sc, yy + 2f * sc)
                path.close()
                c.drawPath(path, P)
            } else if (kind == R_ING) {
                c.drawRoundRect(RectF(X - 8f * sc, yy - 4f * sc, X + 8f * sc, yy + 2f * sc), 2f * sc, 2f * sc, P)
                P.color = Color.parseColor("#66FFFFFF")
                c.drawRect(X - 7f * sc, yy - 3.5f * sc, X + 6f * sc, yy - 2f * sc, P)
            } else {
                if (tier == 0) {
                    c.drawRect(X - 1.6f * sc, yy - 8f * sc, X + 1.6f * sc, yy + 2f * sc, P)
                    c.drawRect(X - 6f * sc, yy - 1f * sc, X + 6f * sc, yy + 1.4f * sc, P)
                } else if (tier == 1) {
                    path.reset()
                    path.moveTo(X, yy - 8f * sc)
                    path.lineTo(X + 7f * sc, yy - 4f * sc)
                    path.lineTo(X + 5f * sc, yy + 3f * sc)
                    path.lineTo(X, yy + 5f * sc)
                    path.lineTo(X - 5f * sc, yy + 3f * sc)
                    path.lineTo(X - 7f * sc, yy - 4f * sc)
                    path.close()
                    c.drawPath(path, P)
                } else {
                    path.reset()
                    path.moveTo(X - 8f * sc, yy + 2f * sc)
                    path.lineTo(X - 8f * sc, yy - 6f * sc)
                    path.lineTo(X - 4f * sc, yy - 1f * sc)
                    path.lineTo(X, yy - 8f * sc)
                    path.lineTo(X + 4f * sc, yy - 1f * sc)
                    path.lineTo(X + 8f * sc, yy - 6f * sc)
                    path.lineTo(X + 8f * sc, yy + 2f * sc)
                    path.close()
                    c.drawPath(path, P)
                }
            }
        }
        if (n > 7) {
            PT.textAlign = Paint.Align.CENTER
            PT.textSize = 13f * sc
            PT.typeface = TF_BLACK
            PT.color = Color.parseColor("#000000")
            c.drawText("×$n", X + 1f, Y - shown * 6f * sc - 3f, PT)
            PT.color = Color.parseColor(C_PARCH)
            c.drawText("×$n", X, Y - shown * 6f * sc - 4f, PT)
        }
    }

    protected fun drawDwarf(c: Canvas, x: Float, y: Float, phase: Float, face: Float,
                            kind: Int, n: Int, tier: Int, job: Int, swing: Float) {
        val X = ix(x, y)
        val Y = iy(x, y)
        shadow(c, x, y, 15f)
        val bob = if (phase > 0.01f) abs(sin(phase)) * 3f else sin(time * 2f + x) * 1.1f
        val by = Y - 18f - bob
        val cloak = when (job) {
            -1 -> Color.parseColor("#2F5D8C")
            0 -> Color.parseColor("#6B4A2E")
            1 -> Color.parseColor("#3F6B3A")
            2 -> Color.parseColor("#7A3A2A")
            else -> Color.parseColor("#5A4A7A")
        }
        val leg = sin(phase) * 5f
        P.color = Color.parseColor("#2A211A")
        c.drawRoundRect(RectF(X - 7f + leg, by + 12f, X - 1f + leg, by + 22f), 3f, 3f, P)
        c.drawRoundRect(RectF(X + 1f - leg, by + 12f, X + 7f - leg, by + 22f), 3f, 3f, P)
        P.color = cloak
        path.reset()
        path.moveTo(X - 11f, by + 14f)
        path.lineTo(X - 8f, by - 6f)
        path.lineTo(X + 8f, by - 6f)
        path.lineTo(X + 11f, by + 14f)
        path.close()
        c.drawPath(path, P)
        P.color = Color.parseColor("#22FFFFFF")
        c.drawRect(X - 8f, by - 6f, X + 8f, by - 3f, P)
        P.color = Color.parseColor("#C98F5E")
        c.drawCircle(X, by - 12f, 8f, P)
        P.color = Color.parseColor("#E8D8B8")
        path.reset()
        path.moveTo(X - 7f, by - 10f)
        path.lineTo(X + 7f, by - 10f)
        path.lineTo(X + 3f, by + 2f)
        path.lineTo(X - 3f, by + 2f)
        path.close()
        c.drawPath(path, P)
        P.color = if (job == -1) Color.parseColor(C_GOLD) else Color.parseColor("#8A8F96")
        path.reset()
        path.moveTo(X - 9f, by - 13f)
        path.lineTo(X + 9f, by - 13f)
        path.lineTo(X + 7f, by - 21f)
        path.lineTo(X - 7f, by - 21f)
        path.close()
        c.drawPath(path, P)
        P.color = Color.parseColor("#33FFFFFF")
        c.drawRect(X - 7f, by - 21f, X + 7f, by - 19f, P)
        P.color = Color.parseColor("#241C16")
        c.drawCircle(X + face * 3f, by - 13.5f, 1.5f, P)
        c.drawCircle(X + face * 6.5f, by - 13.5f, 1.5f, P)
        if (job == 0 || job == 2 || (job == -1 && swing > 0f)) {
            val a = -0.7f + swing * 1.5f
            val hx = X + face * (11f + cos(a) * 9f)
            val hy = by - 2f + sin(a) * 9f
            PL.color = Color.parseColor("#6B4A2E")
            PL.strokeWidth = 3f
            c.drawLine(X + face * 7f, by + 2f, hx, hy, PL)
            P.color = Color.parseColor("#9AA3AB")
            c.save()
            c.translate(hx, hy)
            c.rotate(a * 57f + 20f)
            c.drawRect(-6f, -3.5f, 6f, 3.5f, P)
            c.restore()
        }
        drawStack(c, X, by - 26f, kind, n, tier, 1f)
    }

    protected fun drawCust(c: Canvas, cu: Cust, t: Terr) {
        val X = ix(cu.x, cu.y)
        val Y = iy(cu.x, cu.y)
        shadow(c, cu.x, cu.y, 13f)
        val bob = if (cu.state != 1) abs(sin(cu.bob)) * 2.6f else sin(time * 2.4f + cu.slot) * 1.2f
        val by = Y - 16f - bob
        P.color = Color.parseColor("#4A3B52")
        path.reset()
        path.moveTo(X - 9f, by + 13f)
        path.lineTo(X - 6f, by - 5f)
        path.lineTo(X + 6f, by - 5f)
        path.lineTo(X + 9f, by + 13f)
        path.close()
        c.drawPath(path, P)
        P.color = Color.parseColor("#D9A87C")
        c.drawCircle(X, by - 11f, 7f, P)
        P.color = Color.parseColor("#3A2E44")
        c.drawRoundRect(RectF(X - 8f, by - 19f, X + 8f, by - 12f), 3f, 3f, P)
        P.color = Color.parseColor("#241C16")
        c.drawCircle(X - 2.5f, by - 11f, 1.3f, P)
        c.drawCircle(X + 2.5f, by - 11f, 1.3f, P)

        val byy = by - 34f
        val w = 30f
        val h = 26f
        P.color = Color.parseColor("#F2F0EA")
        c.drawRoundRect(RectF(X - w / 2f, byy - h / 2f, X + w / 2f, byy + h / 2f), 8f, 8f, P)
        path.reset()
        path.moveTo(X - 5f, byy + h / 2f - 1f)
        path.lineTo(X + 5f, byy + h / 2f - 1f)
        path.lineTo(X, byy + h / 2f + 6f)
        path.close()
        c.drawPath(path, P)
        if (cu.state == 1 && cu.pat / cu.patMax < 0.34f) {
            P.color = Color.parseColor(C_RED)
            c.drawCircle(X, byy - 1f, 8f, P)
            PL.color = Color.WHITE
            PL.strokeWidth = 2f
            c.drawLine(X - 4f, byy - 4f, X + 4f, byy + 2f, PL)
            c.drawLine(X + 4f, byy - 4f, X - 4f, byy + 2f, PL)
        } else {
            P.color = TIER_ITEM[t.i]
            if (t.i == 0) {
                c.drawRect(X - 1.5f, byy - 8f, X + 1.5f, byy + 3f, P)
                c.drawRect(X - 5f, byy - 1f, X + 5f, byy + 1.2f, P)
            } else if (t.i == 1) {
                path.reset()
                path.moveTo(X, byy - 8f)
                path.lineTo(X + 6f, byy - 4f)
                path.lineTo(X + 4f, byy + 3f)
                path.lineTo(X, byy + 6f)
                path.lineTo(X - 4f, byy + 3f)
                path.lineTo(X - 6f, byy - 4f)
                path.close()
                c.drawPath(path, P)
            } else {
                path.reset()
                path.moveTo(X - 7f, byy + 3f)
                path.lineTo(X - 7f, byy - 6f)
                path.lineTo(X - 3f, byy - 1f)
                path.lineTo(X, byy - 8f)
                path.lineTo(X + 3f, byy - 1f)
                path.lineTo(X + 7f, byy - 6f)
                path.lineTo(X + 7f, byy + 3f)
                path.close()
                c.drawPath(path, P)
            }
            PT.textAlign = Paint.Align.CENTER
            PT.textSize = 12f
            PT.typeface = TF_BLACK
            PT.color = Color.parseColor("#3A3028")
            c.drawText("×" + cu.qty, X, byy + 12f, PT)
        }
        if (cu.state == 1) {
            val f = cl(cu.pat / cu.patMax, 0f, 1f)
            P.color = Color.parseColor("#33000000")
            c.drawRoundRect(RectF(X - 15f, Y + 6f, X + 15f, Y + 10f), 2f, 2f, P)
            P.color = if (f > 0.5f) Color.parseColor(C_MOSS)
            else if (f > 0.25f) Color.parseColor(C_GOLD) else Color.parseColor(C_RED)
            c.drawRoundRect(RectF(X - 15f, Y + 6f, X - 15f + 30f * f, Y + 10f), 2f, 2f, P)
        }
    }

    protected fun badge(c: Canvas, X: Float, Y: Float, n: Int, col: Int) {
        if (n <= 0) return
        P.color = Color.parseColor("#CC120C09")
        c.drawRoundRect(RectF(X - 17f, Y - 11f, X + 17f, Y + 9f), 8f, 8f, P)
        PL.color = col
        PL.strokeWidth = 1.6f
        c.drawRoundRect(RectF(X - 17f, Y - 11f, X + 17f, Y + 9f), 8f, 8f, PL)
        PT.textAlign = Paint.Align.CENTER
        PT.textSize = 15f
        PT.typeface = TF_MONO
        PT.color = col
        c.drawText(n.toString(), X, Y + 5f, PT)
    }

    protected fun progRing(c: Canvas, X: Float, Y: Float, f: Float, col: Int) {
        if (f <= 0.001f) return
        PL.color = Color.parseColor("#55000000")
        PL.strokeWidth = 4f
        c.drawCircle(X, Y, 13f, PL)
        PL.color = col
        PL.strokeWidth = 4f
        rect.set(X - 13f, Y - 13f, X + 13f, Y + 13f)
        c.drawArc(rect, -90f, 360f * cl(f, 0f, 1f), false, PL)
    }

    protected fun drawVein(c: Canvas, t: Terr) {
        val s = t.vein
        val X = ix(s.x, s.y)
        val Y = iy(s.x, s.y)
        shadow(c, s.x, s.y, 34f)
        val pop = 1f + s.pop * 0.06f
        c.save()
        c.translate(X, Y)
        c.scale(pop, pop)
        c.translate(-X, -Y)
        P.color = Color.parseColor("#3A332C")
        path.reset()
        path.moveTo(X - 34f, Y + 4f)
        path.lineTo(X - 22f, Y - 26f)
        path.lineTo(X - 2f, Y - 34f)
        path.lineTo(X + 20f, Y - 24f)
        path.lineTo(X + 34f, Y + 4f)
        path.lineTo(X + 12f, Y + 14f)
        path.lineTo(X - 14f, Y + 14f)
        path.close()
        c.drawPath(path, P)
        P.color = Color.parseColor("#4C443A")
        path.reset()
        path.moveTo(X - 22f, Y - 26f)
        path.lineTo(X - 2f, Y - 34f)
        path.lineTo(X + 6f, Y - 16f)
        path.lineTo(X - 14f, Y - 10f)
        path.close()
        c.drawPath(path, P)
        for (i in 0 until 5) {
            val a = i * 1.256f + 0.4f
            val cx = X + cos(a) * 17f
            val cy = Y - 12f + sin(a) * 9f
            P.color = TIER_ORE[t.i]
            path.reset()
            path.moveTo(cx, cy - 9f)
            path.lineTo(cx + 5f, cy)
            path.lineTo(cx, cy + 7f)
            path.lineTo(cx - 5f, cy)
            path.close()
            c.drawPath(path, P)
            P.color = Color.parseColor("#66FFFFFF")
            path.reset()
            path.moveTo(cx, cy - 9f)
            path.lineTo(cx + 5f, cy)
            path.lineTo(cx, cy - 1f)
            path.close()
            c.drawPath(path, P)
        }
        c.restore()
        if (s.stock > 0) drawStack(c, X + 34f, Y + 12f, R_ORE, s.stock, t.i, 0.85f)
        badge(c, X, Y - 48f, s.stock, TIER_ORE[t.i])
    }

    protected fun drawFurnace(c: Canvas, t: Terr) {
        val s = t.furn
        val X = ix(s.x, s.y)
        val Y = iy(s.x, s.y)
        shadow(c, s.x, s.y, 32f)
        P.color = Color.parseColor("#39312A")
        path.reset()
        path.moveTo(X - 30f, Y + 10f)
        path.lineTo(X - 30f, Y - 30f)
        path.lineTo(X, Y - 44f)
        path.lineTo(X + 30f, Y - 30f)
        path.lineTo(X + 30f, Y + 10f)
        path.lineTo(X, Y + 24f)
        path.close()
        c.drawPath(path, P)
        P.color = Color.parseColor("#4A4137")
        path.reset()
        path.moveTo(X - 30f, Y - 30f)
        path.lineTo(X, Y - 44f)
        path.lineTo(X + 30f, Y - 30f)
        path.lineTo(X, Y - 16f)
        path.close()
        c.drawPath(path, P)
        P.color = Color.parseColor("#2A221C")
        c.drawRect(X - 6f, Y - 66f, X + 6f, Y - 40f, P)
        val glow = 0.25f + s.hot * 0.75f
        P.shader = RadialGradient(X, Y - 4f, 60f * glow,
            intArrayOf(Color.parseColor("#AAFF7A1A"), Color.parseColor("#00FF7A1A")),
            null, Shader.TileMode.CLAMP)
        c.drawCircle(X, Y - 4f, 60f * glow, P)
        P.shader = null
        P.color = Color.parseColor("#1A0E08")
        c.drawRoundRect(RectF(X - 16f, Y - 16f, X + 16f, Y + 10f), 8f, 8f, P)
        val fl = 0.75f + 0.25f * sin(time * 11f)
        P.color = Color.parseColor(C_LAVA)
        P.alpha = (255 * glow).toInt()
        c.drawRoundRect(RectF(X - 13f, Y - 12f * fl, X + 13f, Y + 8f), 6f, 6f, P)
        P.color = Color.parseColor(C_EMBER)
        c.drawRoundRect(RectF(X - 10f, Y - 8f * fl, X + 10f, Y + 7f), 5f, 5f, P)
        P.color = Color.parseColor(C_GOLD)
        c.drawRoundRect(RectF(X - 5f, Y - 4f * fl, X + 5f, Y + 6f), 3f, 3f, P)
        P.alpha = 255
        PL.color = Color.parseColor("#5C4A38")
        PL.strokeWidth = 2f
        c.drawLine(X - 30f, Y - 30f, X - 30f, Y + 10f, PL)
        c.drawLine(X + 30f, Y - 30f, X + 30f, Y + 10f, PL)
        if (s.inN > 0) drawStack(c, X - 40f, Y + 6f, R_ORE, s.inN, t.i, 0.7f)
        if (s.outN > 0) drawStack(c, X + 40f, Y + 6f, R_ING, s.outN, t.i, 0.7f)
        badge(c, X - 40f, Y - 34f, s.inN, TIER_ORE[t.i])
        badge(c, X + 40f, Y - 34f, s.outN, TIER_ING[t.i])
        progRing(c, X, Y - 74f, s.prog, Color.parseColor(C_EMBER))
    }

    protected fun drawAnvil(c: Canvas, t: Terr) {
        val s = t.anv
        val X = ix(s.x, s.y)
        val Y = iy(s.x, s.y)
        shadow(c, s.x, s.y, 28f)
        P.color = Color.parseColor("#4A3A28")
        c.drawRect(X - 14f, Y - 12f, X + 14f, Y + 12f, P)
        P.color = Color.parseColor("#5C4A34")
        c.drawOval(X - 14f, Y - 18f, X + 14f, Y - 6f, P)
        val pop = 1f + s.pop * 0.10f
        c.save()
        c.translate(X, Y - 20f)
        c.scale(pop, pop)
        c.translate(-X, -(Y - 20f))
        P.color = Color.parseColor("#6E7883")
        path.reset()
        path.moveTo(X - 26f, Y - 30f)
        path.lineTo(X + 20f, Y - 30f)
        path.lineTo(X + 30f, Y - 24f)
        path.lineTo(X + 20f, Y - 18f)
        path.lineTo(X + 8f, Y - 18f)
        path.lineTo(X + 12f, Y - 8f)
        path.lineTo(X - 12f, Y - 8f)
        path.lineTo(X - 8f, Y - 18f)
        path.lineTo(X - 26f, Y - 18f)
        path.close()
        c.drawPath(path, P)
        P.color = Color.parseColor("#9AA6B2")
        c.drawRect(X - 26f, Y - 30f, X + 20f, Y - 26f, P)
        P.color = Color.parseColor("#4E565F")
        c.drawRect(X - 12f, Y - 12f, X + 12f, Y - 8f, P)
        c.restore()
        if (s.inN > 0 && s.prog > 0.02f) {
            P.color = TIER_ING[t.i]
            P.alpha = 200
            c.drawRect(X - 9f, Y - 34f, X + 9f, Y - 30f, P)
            P.alpha = 255
        }
        if (s.inN > 0) drawStack(c, X - 38f, Y + 4f, R_ING, s.inN, t.i, 0.7f)
        if (s.outN > 0) drawStack(c, X + 38f, Y + 4f, R_ITEM, s.outN, t.i, 0.7f)
        badge(c, X - 38f, Y - 40f, s.inN, TIER_ING[t.i])
        badge(c, X + 38f, Y - 40f, s.outN, TIER_ITEM[t.i])
        progRing(c, X, Y - 62f, s.prog, Color.parseColor(C_STEEL))
    }

    protected fun drawCounter(c: Canvas, t: Terr) {
        val s = t.cnt
        val X = ix(s.x, s.y)
        val Y = iy(s.x, s.y)
        shadow(c, s.x, s.y, 36f)
        P.color = Color.parseColor("#5A4530")
        path.reset()
        path.moveTo(X - 36f, Y)
        path.lineTo(X, Y - 18f)
        path.lineTo(X + 36f, Y)
        path.lineTo(X, Y + 18f)
        path.close()
        c.drawPath(path, P)
        P.color = Color.parseColor("#3E2F20")
        path.reset()
        path.moveTo(X - 36f, Y)
        path.lineTo(X, Y + 18f)
        path.lineTo(X, Y + 30f)
        path.lineTo(X - 36f, Y + 12f)
        path.close()
        c.drawPath(path, P)
        P.color = Color.parseColor("#33261A")
        path.reset()
        path.moveTo(X + 36f, Y)
        path.lineTo(X, Y + 18f)
        path.lineTo(X, Y + 30f)
        path.lineTo(X + 36f, Y + 12f)
        path.close()
        c.drawPath(path, P)
        P.color = Color.parseColor("#7A5F42")
        path.reset()
        path.moveTo(X - 36f, Y)
        path.lineTo(X, Y - 18f)
        path.lineTo(X + 36f, Y)
        path.lineTo(X, Y + 6f)
        path.close()
        c.drawPath(path, P)
        for (i in intArrayOf(-1, 1)) {
            P.color = Color.parseColor("#4A3826")
            c.drawRect(X + i * 30f - 2.5f, Y - 56f, X + i * 30f + 2.5f, Y - 2f, P)
        }
        val aw = if (t.i == 0) Color.parseColor(C_COPPER)
        else if (t.i == 1) Color.parseColor(C_STEEL) else Color.parseColor(C_GOLD)
        for (k in 0 until 6) {
            P.color = if (k % 2 == 0) aw else Color.parseColor("#EFE2C6")
            val x0 = X - 34f + k * 11.4f
            path.reset()
            path.moveTo(x0, Y - 58f)
            path.lineTo(x0 + 11.4f, Y - 58f)
            path.lineTo(x0 + 11.4f, Y - 46f)
            path.lineTo(x0 + 5.7f, Y - 42f)
            path.lineTo(x0, Y - 46f)
            path.close()
            c.drawPath(path, P)
        }
        if (s.stock > 0) drawStack(c, X, Y - 22f, R_ITEM, s.stock, t.i, 0.8f)
        badge(c, X, Y - 74f, s.stock, TIER_ITEM[t.i])
        PT.textAlign = Paint.Align.CENTER
        PT.textSize = 14f
        PT.typeface = TF_MONO
        PT.color = Color.parseColor(C_GOLD)
        c.drawText(fmt(price(t)) + " ¤", X, Y + 44f, PT)
    }

    protected fun drawPile(c: Canvas, p: Pile) {
        val X = ix(p.x, p.y)
        val Y = iy(p.x, p.y)
        shadow(c, p.x, p.y, 12f)
        val bob = sin(time * 4f + p.x) * 2f
        for (i in 0 until min(5, 1 + (p.amt / 25f).toInt())) {
            val yy = Y - 4f - i * 4f + bob
            P.color = Color.parseColor("#B8860B")
            c.drawOval(X - 10f, yy - 5f, X + 10f, yy + 5f, P)
            P.color = Color.parseColor(C_GOLD)
            c.drawOval(X - 10f, yy - 6f, X + 10f, yy + 3f, P)
            P.color = Color.parseColor("#FFF0B8")
            c.drawOval(X - 6f, yy - 5f, X + 2f, yy - 1f, P)
        }
        PT.textAlign = Paint.Align.CENTER
        PT.textSize = 14f
        PT.typeface = TF_BLACK
        PT.color = Color.parseColor("#000000")
        c.drawText("+" + p.amt.toInt(), X + 1f, Y - 26f + bob, PT)
        PT.color = Color.parseColor(C_GOLD)
        c.drawText("+" + p.amt.toInt(), X, Y - 27f + bob, PT)
    }
}
