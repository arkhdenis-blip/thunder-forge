package com.groma.forge

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

abstract class ForgeFloor(ctx: android.content.Context) : ForgeSave(ctx) {

    override fun bake() {
        try {
            val old = floorBmp
            val b = Bitmap.createBitmap(BW, BH, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            c.translate(-BOX, -BOY)
            for (t in terrs) drawTerrFloor(c, t)
            if (terrs[1].open) drawBridge(c, 11.6f, 15.4f)
            if (terrs[2].open) drawBridge(c, 25.6f, 29.4f)
            floorBmp = b
            old?.recycle()
        } catch (e: Throwable) {}
    }

    protected fun tile(c: Canvas, x: Float, y: Float, col: Int) {
        val X = ix(x, y)
        val Y = iy(x, y)
        path.reset()
        path.moveTo(X, Y - TH / 2f)
        path.lineTo(X + TW / 2f, Y)
        path.lineTo(X, Y + TH / 2f)
        path.lineTo(X - TW / 2f, Y)
        path.close()
        P.style = Paint.Style.FILL
        P.color = col
        c.drawPath(path, P)
    }

    protected fun drawTerrFloor(c: Canvas, t: Terr) {
        val open = t.open
        val base = if (open) Color.parseColor(C_STONE) else Color.parseColor("#191410")
        val alt = if (open) Color.parseColor(C_STONE2) else Color.parseColor("#1E1813")
        val x0 = t.ox.toInt()
        val x1 = (t.ox + 11f).toInt()
        P.style = Paint.Style.FILL
        for (x in x0..x1) {
            val X = ix(x.toFloat(), 17f)
            val Y = iy(x.toFloat(), 17f)
            path.reset()
            path.moveTo(X - TW / 2f, Y)
            path.lineTo(X, Y + TH / 2f)
            path.lineTo(X, Y + TH / 2f + 40f)
            path.lineTo(X - TW / 2f, Y + 40f)
            path.close()
            P.color = if (open) Color.parseColor("#241B15") else Color.parseColor("#120D0A")
            c.drawPath(path, P)
        }
        for (y in 1..17) {
            val X = ix(x1.toFloat(), y.toFloat())
            val Y = iy(x1.toFloat(), y.toFloat())
            path.reset()
            path.moveTo(X, Y + TH / 2f)
            path.lineTo(X + TW / 2f, Y)
            path.lineTo(X + TW / 2f, Y + 40f)
            path.lineTo(X, Y + TH / 2f + 40f)
            path.close()
            P.color = if (open) Color.parseColor("#1B1410") else Color.parseColor("#0E0A08")
            c.drawPath(path, P)
        }
        for (x in x0..x1) for (y in 1..17) {
            val n = ((x * 73856093) xor (y * 19349663)) and 255
            val cc = if ((x + y) % 2 == 0) base else alt
            tile(c, x.toFloat(), y.toFloat(), cc)
            if (open && n < 26) {
                val X = ix(x.toFloat(), y.toFloat())
                val Y = iy(x.toFloat(), y.toFloat())
                P.color = Color.parseColor("#2F2822")
                c.drawCircle(X + (n % 11) - 5f, Y + (n % 7) - 3f, 2.2f + n % 3, P)
            }
            if (open && n > 240) {
                val X = ix(x.toFloat(), y.toFloat())
                val Y = iy(x.toFloat(), y.toFloat())
                P.color = Color.parseColor("#3D5230")
                c.drawCircle(X + (n % 13) - 6f, Y + (n % 5) - 2f, 3.4f, P)
            }
        }
        if (open) {
            val cx = t.ox + 5.5f
            val cy = 8.6f
            val X = ix(cx, cy)
            val Y = iy(cx, cy)
            PL.color = Color.parseColor(if (t.i == 0) "#5A4632" else if (t.i == 1) "#4A5A68" else "#6A5A2E")
            PL.strokeWidth = 3f
            c.drawOval(X - 74f, Y - 37f, X + 74f, Y + 37f, PL)
            c.drawOval(X - 52f, Y - 26f, X + 52f, Y + 26f, PL)
        }
        PL.strokeWidth = 2.5f
        PL.color = if (open) Color.parseColor("#5C4A38") else Color.parseColor("#241C16")
        for (x in x0..x1) {
            val X = ix(x.toFloat(), 1f)
            val Y = iy(x.toFloat(), 1f)
            c.drawLine(X - TW / 2f, Y, X, Y - TH / 2f, PL)
        }
    }

    protected fun drawBridge(c: Canvas, xa: Float, xb: Float) {
        var x = xa
        while (x < xb) {
            for (y in 6..8) {
                tile(c, x, y.toFloat(), Color.parseColor("#4A3A28"))
                val X = ix(x, y.toFloat())
                val Y = iy(x, y.toFloat())
                PL.color = Color.parseColor("#2E2418")
                PL.strokeWidth = 2f
                c.drawLine(X - TW / 2f, Y, X + TW / 2f, Y, PL)
            }
            x += 1f
        }
        PL.color = Color.parseColor("#6B563A")
        PL.strokeWidth = 3f
        for (y in intArrayOf(6, 9)) {
            val X1 = ix(xa, y.toFloat())
            val Y1 = iy(xa, y.toFloat())
            val X2 = ix(xb, y.toFloat())
            val Y2 = iy(xb, y.toFloat())
            c.drawLine(X1, Y1 - 26f, X2, Y2 - 26f, PL)
        }
    }
}
