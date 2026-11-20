package com.groma.forge

import android.graphics.Canvas
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

abstract class ForgeSim(ctx: android.content.Context) : ForgeBase(ctx) {

    protected fun questText(): String = when (quest) {
        0 -> "Добудь руду: встань у жилы"
        1 -> "Отнеси руду в горн"
        2 -> "Забери слитки и неси на наковальню"
        3 -> "Отнеси изделия на прилавок"
        4 -> "Собери 120 монет"
        5 -> "Найми первого работника"
        6 -> "Открой вторую террасу"
        7 -> "Заработай " + fmt(GOAL) + " — восстанови Кузню"
        else -> "Кузня Грома восстановлена. Куй дальше!"
    }

    protected fun questDone(reward: Float) {
        coins += reward
        earned += reward
        ftxt(px, py - 1.2f, "+" + reward.toInt(), Color.parseColor(C_GOLD))
        sfx.done()
        buzz(30)
        quest++
        toast("ЗАДАНИЕ ВЫПОЛНЕНО  +" + reward.toInt())
        if (quest == 8 && !winShown) {
            winShown = true
            won = true
            panel = 3
            sfx.fanfare()
        }
    }

    protected fun checkQuests() {
        when (quest) {
            0 -> if (mined >= 4) questDone(25f)
            1 -> if (terrs[0].furn.inN > 0 || terrs[0].furn.outN > 0) questDone(30f)
            2 -> if (terrs[0].anv.inN > 0 || terrs[0].anv.outN > 0) questDone(40f)
            3 -> if (served > 0) questDone(60f)
            4 -> if (earned >= 120f) questDone(80f)
            5 -> if (actors.size > 0) questDone(150f)
            6 -> if (terrs[1].open) questDone(600f)
            7 -> if (earned >= GOAL) questDone(5000f)
        }
    }

    override fun update(dt: Float) {
        time += dt
        shake = max(0f, shake - dt * 42f)
        flash = max(0f, flash - dt * 2.6f)
        toastT = max(0f, toastT - dt)
        tipT = max(0f, tipT - dt)
        shownCoins += (coins - shownCoins) * min(1f, dt * 9f)
        if (abs(coins - shownCoins) < 0.6f) shownCoins = coins

        if (panel != 0 || paused) { sfx.amb = 0.05f; return }
        playT += dt
        saveT += dt
        if (saveT > 8f) { saveT = 0f; save() }

        var mx = jdx
        var my = jdy
        val mag = sqrt(mx * mx + my * my)
        if (mag > 1f) { mx /= mag; my /= mag }
        if (mag > 0.06f) {
            val wx = (mx + my * 2f) * 0.5f
            val wy = (my * 2f - mx) * 0.5f
            val l = sqrt(wx * wx + wy * wy)
            val sp = pSpeed() * min(1f, mag)
            if (l > 0.001f) {
                val nx = px + wx / l * sp * dt
                val ny = py + wy / l * sp * dt
                if (walkable(nx, ny)) { px = nx; py = ny }
                else if (walkable(nx, py)) px = nx
                else if (walkable(px, ny)) py = ny
                pFace = if (mx >= 0f) 1f else -1f
                pPhase += dt * 11f * min(1f, mag)
                if (quality > 0 && Math.random() < dt * 9f && parts.size < 200)
                    parts.add(Part(px, py, (Math.random().toFloat() - .5f) * .3f, -0.2f,
                        0.35f, 0.35f, 1.6f, Color.parseColor("#6B5A48"), 0.4f))
            }
        } else pPhase = lerp(pPhase, 0f, min(1f, dt * 8f))

        camX = lerp(camX, px, min(1f, dt * 4.2f))
        camY = lerp(camY, py, min(1f, dt * 4.2f))

        playerWork(dt)
        for (t in terrs) if (t.open) terrace(t, dt)
        for (a in actors) worker(a, dt)
        fundSpots(dt)

        var i = piles.size - 1
        while (i >= 0) {
            val p = piles[i]
            p.t += dt
            val d = sqrt((p.x - px) * (p.x - px) + (p.y - py) * (p.y - py))
            if (d < 3.0f || p.t > 14f) p.fly += dt * 3.4f
            if (p.fly > 0f) {
                p.x = lerp(p.x, px, min(1f, dt * 7f))
                p.y = lerp(p.y, py, min(1f, dt * 7f))
                if (p.fly > 0.55f || d < 0.5f) {
                    coins += p.amt
                    earned += p.amt
                    sfx.coin()
                    burst(px, py - 0.6f, Color.parseColor(C_GOLD), 5, 2.2f, 1.2f)
                    piles.removeAt(i)
                    checkQuests()
                }
            }
            i--
        }

        i = parts.size - 1
        while (i >= 0) {
            val p = parts[i]
            p.life -= dt
            if (p.life <= 0f) { parts.removeAt(i); i--; continue }
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += p.grav * dt
            p.vx *= Math.pow(0.25, dt.toDouble()).toFloat()
            i--
        }
        i = texts.size - 1
        while (i >= 0) {
            val t = texts[i]
            t.life -= dt * 0.95f
            t.y -= dt * 1.1f
            if (t.life <= 0f) texts.removeAt(i)
            i--
        }

        rep = min(100f, rep + dt * 0.55f)
        var amb = 0f
        for (t in terrs) if (t.open) {
            val d = abs(t.vein.x - px) + abs(t.vein.y - py)
            if (d < 16f) amb = max(amb, (1f - d / 16f) * (0.4f + 0.6f * t.furn.hot))
        }
        sfx.amb = amb
        checkQuests()
    }

    protected fun playerWork(dt: Float) {
        pSwing = max(0f, pSwing - dt * 3.2f)
        var near: Station? = null
        var nd = 1.65f
        for (t in terrs) {
            if (!t.open) continue
            for (r in 0..3) {
                val s = t.st(r)
                val d = sqrt((s.x - px) * (s.x - px) + (s.y - py) * (s.y - py))
                if (d < nd) { nd = d; near = s }
            }
        }
        if (near == null) { pAct = 0f; return }
        val t = terrs[near.tier]
        pAct += dt
        when (near.role) {
            0 -> {
                if ((pKind == 0 || (pKind == R_ORE && pTier == t.i)) && pCarry < pCap()) {
                    if (pAct >= mineTime(t)) {
                        pAct = 0f
                        val n = min(yieldN(t), pCap() - pCarry)
                        pKind = R_ORE; pTier = t.i; pCarry += n
                        mined += n
                        pSwing = 1f
                        near.pop = 1f
                        sfx.mine(); buzz(12)
                        burst(near.x, near.y - 0.3f, TIER_ORE[t.i], 6, 2.4f, 2.6f)
                        if (quality > 0) shake = max(shake, 1.6f)
                    }
                } else pAct = 0f
            }
            1 -> {
                if (pKind == R_ORE && pTier == t.i && pCarry > 0 && near.inN < near.cap) {
                    if (pAct > 0.075f) {
                        pAct = 0f; near.inN++; pCarry--
                        if (pCarry == 0) pKind = 0
                        sfx.drop(); near.pop = 0.6f
                    }
                } else if ((pKind == 0 || (pKind == R_ING && pTier == t.i)) && pCarry < pCap() && near.outN > 0) {
                    if (pAct > 0.075f) {
                        pAct = 0f; near.outN--
                        pKind = R_ING; pTier = t.i; pCarry++
                        sfx.pick()
                    }
                } else pAct = 0f
            }
            2 -> {
                if (pKind == R_ING && pTier == t.i && pCarry > 0 && near.inN < near.cap) {
                    if (pAct > 0.075f) {
                        pAct = 0f; near.inN++; pCarry--
                        if (pCarry == 0) pKind = 0
                        sfx.drop(); near.pop = 0.6f
                    }
                } else if ((pKind == 0 || (pKind == R_ITEM && pTier == t.i)) && pCarry < pCap() && near.outN > 0) {
                    if (pAct > 0.075f) {
                        pAct = 0f; near.outN--
                        pKind = R_ITEM; pTier = t.i; pCarry++
                        sfx.pick()
                    }
                } else pAct = 0f
            }
            3 -> {
                if (pKind == R_ITEM && pTier == t.i && pCarry > 0 && near.stock < near.cap) {
                    if (pAct > 0.075f) {
                        pAct = 0f; near.stock++; pCarry--
                        if (pCarry == 0) pKind = 0
                        sfx.drop(); near.pop = 0.6f
                    }
                } else pAct = 0f
            }
        }
    }

    protected fun terrace(t: Terr, dt: Float) {
        t.vein.pop = max(0f, t.vein.pop - dt * 3f)
        t.furn.pop = max(0f, t.furn.pop - dt * 3f)
        t.anv.pop = max(0f, t.anv.pop - dt * 3f)
        t.cnt.pop = max(0f, t.cnt.pop - dt * 3f)

        if (t.furn.inN > 0 && t.furn.outN < t.furn.cap) {
            t.furn.hot = min(1f, t.furn.hot + dt * 2f)
            t.furn.prog += dt / furnTime(t)
            if (quality > 0 && Math.random() < dt * 14f && parts.size < 240)
                parts.add(Part(t.furn.x + (Math.random().toFloat() - .5f) * .5f, t.furn.y - .5f,
                    (Math.random().toFloat() - .5f) * .3f, -0.9f - Math.random().toFloat(),
                    0.7f, 0.7f, 1.4f + Math.random().toFloat() * 1.8f,
                    Color.parseColor(if (Math.random() < .5) C_EMBER else C_GOLD), -0.2f))
            if (t.furn.prog >= 1f) {
                t.furn.prog = 0f; t.furn.inN--;t.furn.outN = min(t.furn.cap, t.furn.outN + furnYield(t)); t.furn.pop = 1f
            }
        } else t.furn.hot = max(0.12f, t.furn.hot - dt * 0.6f)

        if (t.anv.inN > 0 && t.anv.outN < t.anv.cap) {
            t.anv.prog += dt / anvTime(t)
            if (t.anv.prog >= 1f) {
                t.anv.prog = 0f; t.anv.inN--;t.anv.outN = min(t.anv.cap, t.anv.outN + anvYield(t))  ; t.anv.pop = 1f
                sfx.hammer()
                burst(t.anv.x, t.anv.y - 0.55f, Color.parseColor(C_GOLD), 8, 3.2f, 3.4f)
                if (abs(t.anv.x - px) + abs(t.anv.y - py) < 12f) shake = max(shake, 2.2f)
            }
        }

        t.spawnT -= dt
        val maxQ = 4 + (t.cnt.lv - 1) / 2  
        if (t.spawnT <= 0f && t.custs.size < maxQ) {
            t.spawnT = max(1.2f, 3.0f - t.i * 0.4f - (t.cnt.lv - 1) * 0.05f) + Math.random().toFloat() * 1.5f  
            val used = HashSet<Int>()
            for (c in t.custs) used.add(c.slot)
            var slot = -1
            for (k in 0 until maxQ) if (!used.contains(k)) { slot = k; break }
            if (slot >= 0) {
                val c = Cust(t.i, slot)
                val qb = 1 + t.i * 2 + (t.cnt.lv - 1) / 3; c.qty = qb + (Math.random() * (qb + 2)).toInt()
                c.patMax = 18f + c.qty * 3f  
                c.pat = c.patMax
                c.x = t.cnt.x - 5.5f
                c.y = 12.6f + slot * 1.18f
                t.custs.add(c)
            }
        }
        var i = t.custs.size - 1
        while (i >= 0) {
            val c = t.custs[i]
            val tx = t.cnt.x
            val ty = 12.6f + c.slot * 1.18f
            c.bob += dt * 6f
            if (c.state == 0) {
                c.x = lerp(c.x, tx, min(1f, dt * 2.2f))
                c.y = lerp(c.y, ty, min(1f, dt * 2.2f))
                if (abs(c.x - tx) < 0.15f) { c.x = tx; c.state = 1 }
            } else if (c.state == 1) {
                c.pat -= dt
                val nearMe = abs(px - t.cnt.x) + abs(py - t.cnt.y) < 2.2f
                if (c.slot == 0 && t.cnt.stock >= c.qty && (t.sellers > 0 || nearMe)) {
                    c.t += dt / sellTime(t)
                    if (c.t >= 1f) {
                        t.cnt.stock -= c.qty
                        val gain = price(t) * c.qty
                        if (t.sellers > 0) {
                            coins += gain
                            earned += gain
                            ftxt(c.x, c.y - 1.1f, "+" + gain.toInt(), Color.parseColor(C_GOLD))
                        } else {
                            piles.add(Pile(t.cnt.x + 0.9f, t.cnt.y + 0.4f, gain, 0f, 0f))
                        }
                        served++
                        rep = min(100f, rep + dt * 0.8f)  
                        c.state = 2; c.face = 1
                        sfx.done()
                        burst(c.x, c.y - 0.7f, Color.parseColor(C_MOSS), 8, 2.4f, 1.6f)
                    }
                }
                if (c.pat <= 0f) {
                    c.state = 2; c.face = -1
                    rep = max(0f, rep - 4f)
                    angryN++
                    sfx.angry()
                    burst(c.x, c.y - 0.7f, Color.parseColor(C_RED), 8, 2.2f, 1.6f)
                    ftxt(c.x, c.y - 1.1f, "УШЁЛ", Color.parseColor(C_RED))
                }
            } else {
                c.x -= dt * 2.6f
                if (c.x < t.cnt.x - 6.5f) { t.custs.removeAt(i); reindex(t) }
            }
            i--
        }
    }

    protected fun reindex(t: Terr) {
        val alive = ArrayList<Cust>()
        for (c in t.custs) if (c.state != 2) alive.add(c)
        alive.sortBy { it.slot }
        for (k in alive.indices) alive[k].slot = k
    }

    protected abstract fun worker(a: Actor, dt: Float)
    protected abstract fun fundSpots(dt: Float)
}
