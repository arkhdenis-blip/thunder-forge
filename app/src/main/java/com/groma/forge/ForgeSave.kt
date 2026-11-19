package com.groma.forge

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.min

abstract class ForgeSave(ctx: android.content.Context) : ForgeWorkers(ctx) {

    override fun save() {
        try {
            val o = JSONObject()
            o.put("v", 1)
            o.put("coins", coins.toDouble())
            o.put("earned", earned.toDouble())
            o.put("rep", rep.toDouble())
            o.put("quest", quest)
            o.put("capLv", capLv)
            o.put("spdLv", spdLv)
            o.put("carLv", carLv)
            o.put("px", px.toDouble())
            o.put("py", py.toDouble())
            o.put("playT", playT.toDouble())
            o.put("won", won)
            o.put("winShown", winShown)
            o.put("served", served)
            o.put("angry", angryN)
            o.put("mined", mined)
            o.put("sound", soundOn)
            o.put("vib", vibOn)
            o.put("q", quality)
            o.put("ts", System.currentTimeMillis())
            val arr = JSONArray()
            for (t in terrs) {
                val j = JSONObject()
                j.put("open", t.open)
                j.put("miners", t.miners)
                j.put("carriers", t.carriers)
                j.put("smiths", t.smiths)
                j.put("sellers", t.sellers)
                j.put("vl", t.vein.lv)
                j.put("fl", t.furn.lv)
                j.put("al", t.anv.lv)
                j.put("cl", t.cnt.lv)
                j.put("vs", t.vein.stock)
                j.put("fi", t.furn.inN)
                j.put("fo", t.furn.outN)
                j.put("ai", t.anv.inN)
                j.put("ao", t.anv.outN)
                j.put("cs", t.cnt.stock)
                arr.put(j)
            }
            o.put("terr", arr)
            val sp = JSONArray()
            for (s in spots) { sp.put(s.paid.toDouble()); sp.put(s.done) }
            o.put("spots", sp)
            val f = File(context.filesDir, "forge.json")
            val tmp = File(context.filesDir, "forge.tmp")
            tmp.writeText(o.toString())
            if (f.exists()) {
                try { f.copyTo(File(context.filesDir, "forge.bak"), true) } catch (e: Exception) {}
            }
            tmp.renameTo(f)
        } catch (e: Exception) {}
    }

    protected fun load() {
        try {
            val f = File(context.filesDir, "forge.json")
            if (!f.exists()) return
            val o = JSONObject(f.readText())
            coins = o.optDouble("coins", 0.0).toFloat()
            shownCoins = coins
            earned = o.optDouble("earned", 0.0).toFloat()
            rep = o.optDouble("rep", 100.0).toFloat()
            quest = o.optInt("quest", 0)
            capLv = o.optInt("capLv", 1)
            spdLv = o.optInt("spdLv", 1)
            carLv = o.optInt("carLv", 1)
            px = o.optDouble("px", 5.5).toFloat()
            py = o.optDouble("py", 9.0).toFloat()
            playT = o.optDouble("playT", 0.0).toFloat()
            won = o.optBoolean("won", false)
            winShown = o.optBoolean("winShown", false)
            served = o.optInt("served", 0)
            angryN = o.optInt("angry", 0)
            mined = o.optInt("mined", 0)
            soundOn = o.optBoolean("sound", true)
            vibOn = o.optBoolean("vib", true)
            quality = o.optInt("q", 2)
            val arr = o.optJSONArray("terr")
            if (arr != null) for (i in 0 until min(arr.length(), 3)) {
                val j = arr.getJSONObject(i)
                val t = terrs[i]
                t.open = j.optBoolean("open", i == 0)
                t.miners = j.optInt("miners", 0)
                t.carriers = j.optInt("carriers", 0)
                t.smiths = j.optInt("smiths", 0)
                t.sellers = j.optInt("sellers", 0)
                t.vein.lv = j.optInt("vl", 1)
                t.furn.lv = j.optInt("fl", 1)
                t.anv.lv = j.optInt("al", 1)
                t.cnt.lv = j.optInt("cl", 1)
                t.vein.stock = j.optInt("vs", 0)
                t.furn.inN = j.optInt("fi", 0)
                t.furn.outN = j.optInt("fo", 0)
                t.anv.inN = j.optInt("ai", 0)
                t.anv.outN = j.optInt("ao", 0)
                t.cnt.stock = j.optInt("cs", 0)
            }
            val sp = o.optJSONArray("spots")
            if (sp != null) for (i in spots.indices) {
                val k = i * 2
                if (k + 1 < sp.length()) {
                    spots[i].paid = sp.optDouble(k, 0.0).toFloat()
                    spots[i].done = sp.optBoolean(k + 1, false)
                }
            }
            if (!walkable(px, py)) { px = 5.5f; py = 9.0f }
            camX = px
            camY = py
            val ts = o.optLong("ts", System.currentTimeMillis())
            val el = (System.currentTimeMillis() - ts) / 1000f
            if (el > 90f) offline(el)
        } catch (e: Exception) {}
    }

    protected fun offline(sec: Float) {
        val capped = min(sec, 8f * 3600f)
        var total = 0f
        for (t in terrs) {
            if (!t.open || t.miners + t.carriers == 0) continue
            val mineR = t.miners * yieldN(t) / (mineTime(t) * 1.7f)
            val furnR = 1f / furnTime(t)
            val anvR = 1f / anvTime(t)
            val sellR = 1f / sellTime(t) * 2f
            val flow = min(min(mineR, furnR), min(anvR, sellR))
            total += flow * price(t) * capped * 0.42f
        }
        if (total > 1f) {
            offlineAmt = total
            offlineSec = capped
            coins += total
            earned += total
            shownCoins = coins
            panel = 4
        }
    }

    override fun resetAll() {
        coins = 0f
        shownCoins = 0f
        earned = 0f
        rep = 100f
        quest = 0
        capLv = 1
        spdLv = 1
        carLv = 1
        px = 5.5f
        py = 9.0f
        camX = px
        camY = py
        pKind = 0
        pCarry = 0
        pTier = 0
        playT = 0f
        won = false
        winShown = false
        served = 0
        angryN = 0
        mined = 0
        actors.clear()
        parts.clear()
        texts.clear()
        piles.clear()
        for (t in terrs) {
            t.open = t.i == 0
            t.miners = 0
            t.carriers = 0
            t.smiths = 0
            t.sellers = 0
            t.custs.clear()
            t.spawnT = 2f
            for (r in 0..3) {
                val s = t.st(r)
                s.lv = 1
                s.stock = 0
                s.inN = 0
                s.outN = 0
                s.prog = 0f
            }
        }
        buildSpots()
        bake()
        panel = 0
        save()
        toast("НОВЫЙ ЗАБЕГ")
    }
}
