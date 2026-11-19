package com.groma.forge

import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

abstract class ForgeWorkers(ctx: android.content.Context) : ForgeSim(ctx) {

    protected fun syncActors() {
        for (t in terrs) {
            if (!t.open) continue
            fun count(job: Int, post: Int): Int {
                var n = 0
                for (a in actors) if (a.tier == t.i && a.job == job && a.post == post) n++
                return n
            }
            while (count(0, 0) < t.miners) {
                val a = Actor(t.i, 0, 0)
                a.x = t.vein.x + (Math.random().toFloat() - 0.5f) * 1.6f
                a.y = t.vein.y + 1.3f + Math.random().toFloat() * 0.5f
                actors.add(a)
            }
            while (count(1, 0) < t.carriers) {
                val a = Actor(t.i, 1, 0)
                a.x = t.ox + 5.2f
                a.y = 6.4f
                actors.add(a)
            }
            while (count(2, 1) + count(2, 2) < t.smiths) {
                val post = if (count(2, 1) <= count(2, 2)) 1 else 2
                val s = t.st(post)
                val a = Actor(t.i, 2, post)
                a.x = s.x + 1.2f
                a.y = s.y + 0.9f
                actors.add(a)
            }
            while (count(3, 0) < t.sellers) {
                val a = Actor(t.i, 3, 0)
                a.x = t.cnt.x - 1.1f
                a.y = t.cnt.y - 0.9f
                actors.add(a)
            }
        }
    }

    override fun worker(a: Actor, dt: Float) {
        val t = terrs[a.tier]
        a.swing = max(0f, a.swing - dt * 3.2f)
        when (a.job) {
            0 -> {
                val s = t.vein
                if (s.stock < s.cap) {
                    a.timer += dt
                    if (a.timer >= mineTime(t) * 1.7f) {
                        a.timer = 0f
                        s.stock = min(s.cap, s.stock + yieldN(t))
                        a.swing = 1f
                        s.pop = 0.7f
                        if (abs(a.x - px) + abs(a.y - py) < 14f) sfx.mine()
                        burst(s.x, s.y - 0.3f, TIER_ORE[t.i], 4, 2.0f, 2.6f)
                    }
                }
                if (a.swing > 0f) a.phase += dt * 8f
            }
            2 -> {
                a.phase += dt * 7f
                if (Math.random() < dt * 1.2f) a.swing = 1f
            }
            3 -> { a.phase += dt * 3f }
            1 -> carrier(a, t, dt)
        }
    }

    protected fun carrier(a: Actor, t: Terr, dt: Float) {
        val sp = 3.1f + carLv * 0.16f
        if (a.st == 0) {
            var src = -1
            var dst = -1
            if (t.anv.outN > 0 && t.cnt.stock < t.cnt.cap) { src = 2; dst = 3 }
            else if (t.furn.outN > 0 && t.anv.inN < t.anv.cap) { src = 1; dst = 2 }
            else if (t.vein.stock > 0 && t.furn.inN < t.furn.cap) { src = 0; dst = 1 }
            if (src < 0) {
                moveTo(a, t.ox + 5.2f, 6.4f, sp * 0.6f, dt)
                return
            }
            a.srcRole = src
            a.dstRole = dst
            a.st = 1
        }
        if (a.st == 1) {
            val s = t.st(a.srcRole)
            if (moveTo(a, s.x + 0.9f, s.y + 0.8f, sp, dt)) a.st = 2
        } else if (a.st == 2) {
            a.timer += dt
            if (a.timer > 0.09f) {
                a.timer = 0f
                val s = t.st(a.srcRole)
                val avail = if (a.srcRole == 0) s.stock else s.outN
                if (avail <= 0) { a.st = 0; a.carry = 0; a.kind = 0; return }
                if (a.srcRole == 0) s.stock-- else s.outN--
                a.carry++
                a.kind = a.srcRole + 1
                a.cTier = t.i
                s.pop = 0.5f
                if (a.carry >= carCap()) a.st = 3
            }
        } else if (a.st == 3) {
            val d = t.st(a.dstRole)
            if (moveTo(a, d.x + 0.9f, d.y + 0.8f, sp, dt)) a.st = 4
        } else if (a.st == 4) {
            a.timer += dt
            if (a.timer > 0.09f) {
                a.timer = 0f
                val d = t.st(a.dstRole)
                if (a.dstRole == 3) {
                    if (d.stock < d.cap) d.stock++
                } else {
                    if (d.inN < d.cap) d.inN++
                }
                a.carry--
                d.pop = 0.5f
                if (a.carry <= 0) { a.carry = 0; a.kind = 0; a.st = 0 }
            }
        }
    }

    protected fun moveTo(a: Actor, tx: Float, ty: Float, sp: Float, dt: Float): Boolean {
        val dx = tx - a.x
        val dy = ty - a.y
        val d = sqrt(dx * dx + dy * dy)
        if (d < 0.14f) { a.stuck = 0f; return true }
        var vx = dx / d
        var vy = dy / d
        if (a.stuck > 0.7f) { val tmp = vx; vx = -vy; vy = tmp }
        val nx = a.x + vx * sp * dt
        val ny = a.y + vy * sp * dt
        if (walkable(nx, ny)) { a.x = nx; a.y = ny }
        else if (walkable(nx, a.y)) a.x = nx
        else if (walkable(a.x, ny)) a.y = ny
        a.face = if (vx - vy >= 0f) 1f else -1f
        a.phase += dt * 10f
        val moved = abs(a.x - a.lx) + abs(a.y - a.ly)
        a.stuck = if (moved < 0.004f) a.stuck + dt else 0f
        if (a.stuck > 1.6f) a.stuck = 0f
        a.lx = a.x
        a.ly = a.y
        return false
    }

    override fun fundSpots(dt: Float) {
        for (s in spots) {
            s.pulse = max(0f, s.pulse - dt * 2f)
            if (s.done || spotMax(s)) continue
            if (s.kind == 8 && s.tier > 0 && !terrs[s.tier - 1].open) continue
            val d = sqrt((s.x - px) * (s.x - px) + (s.y - py) * (s.y - py))
            if (d > 1.25f) { s.hold = max(0f, s.hold - dt * 2f); continue }
            s.hold += dt
            if (s.hold < HOLD) continue
            val cost = spotCost(s)
            if (coins <= 0f) continue
            val rate = max(cost * 0.55f, 45f)
            val give = min(coins, rate * dt)
            coins -= give
            s.paid += give
            if (Math.random() < dt * 12f && quality > 0 && parts.size < 240)
                parts.add(Part(px, py - .5f, (s.x - px) * 1.6f, (s.y - py) * 1.6f - .6f,
                    .4f, .4f, 2.0f, Color.parseColor(C_GOLD), 0f))
            if (s.paid >= cost) {
                s.paid = 0f
                applySpot(s)
                s.pulse = 1f
                sfx.build()
                buzz(45)
                shake = max(shake, 5f)
                flash = 0.25f
                flashC = Color.parseColor(C_GOLD)
                burst(s.x, s.y - .3f, Color.parseColor(C_GOLD), 22, 3.4f, 2.2f)
                checkQuests()
            }
        }
    }

    protected fun applySpot(s: Spot) {
        val t = terrs[s.tier]
        when (s.kind) {
            0 -> { t.miners++; toast("ДОБЫТЧИК НАНЯТ") }
            1 -> { t.carriers++; toast("ПЕРЕНОСЧИК НАНЯТ") }
            2 -> { t.smiths++; toast("КУЗНЕЦ ПРИШЁЛ") }
            3 -> { t.sellers++; toast("ПРОДАВЕЦ НАНЯТ") }
            4 -> { t.vein.lv++; toast("ЖИЛА БОГАЧЕ · ур." + t.vein.lv) }
            5 -> { t.furn.lv++; toast("ГОРН ЖАРЧЕ · ур." + t.furn.lv) }
            6 -> { t.anv.lv++; toast("МОЛОТ СИЛЬНЕЕ · ур." + t.anv.lv) }
            7 -> { t.cnt.lv++; toast("ЦЕНА ВЫШЕ · ур." + t.cnt.lv) }
            8 -> {
                t.open = true
                s.done = true
                toast(TIER_NAME[s.tier] + " ОТКРЫТА")
                sfx.fanfare()
                shake = max(shake, 9f)
                bake()
            }
            9 -> { capLv++; toast("СПИНА КРЕПЧЕ · " + pCap()) }
            else -> { spdLv++; toast("НОГИ БЫСТРЕЕ · ур." + spdLv) }
        }
        if (s.kind == 1) carLv = max(carLv, 1 + (t.carriers / 2))
        syncActors()
        save()
    }

    protected abstract fun bake()
}
