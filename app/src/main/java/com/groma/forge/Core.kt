package com.groma.forge

import android.graphics.Color
import android.graphics.RectF
import android.graphics.Typeface

const val TW = 64f
const val TH = 32f

fun ix(x: Float, y: Float) = (x - y) * (TW * 0.5f)
fun iy(x: Float, y: Float) = (x + y) * (TH * 0.5f)
fun cl(v: Float, a: Float, b: Float) = if (v < a) a else if (v > b) b else v
fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

const val C_BG0 = "#120C09"
const val C_BG1 = "#1E1310"
const val C_LAVA = "#FF4E12"
const val C_EMBER = "#FF7A1A"
const val C_GOLD = "#FFC23D"
const val C_COPPER = "#E07A3C"
const val C_STEEL = "#8FA6B8"
const val C_MOSS = "#79B04A"
const val C_RED = "#E0453A"
const val C_PARCH = "#F5E6CA"
const val C_MUTE = "#A89684"
const val C_STONE = "#3E362D"
const val C_STONE2 = "#4A4136"

val TIER_ORE = intArrayOf(
    Color.parseColor("#E07A3C"),
    Color.parseColor("#9FB6C8"),
    Color.parseColor("#FFD45E")
)
val TIER_ING = intArrayOf(
    Color.parseColor("#FF9A55"),
    Color.parseColor("#C9DCEA"),
    Color.parseColor("#FFE999")
)
val TIER_ITEM = intArrayOf(
    Color.parseColor("#FFC23D"),
    Color.parseColor("#8FA6B8"),
    Color.parseColor("#FFD45E")
)
val TIER_NAME = arrayOf("МЕДНАЯ ТЕРРАСА", "ЖЕЛЕЗНАЯ ТЕРРАСА", "ЗОЛОТАЯ ТЕРРАСА")
val ITEM_NAME = arrayOf("меч", "щит", "корону")
val ITEM_BASE = floatArrayOf(7f, 30f, 135f)
val TIER_COST_MUL = floatArrayOf(1f, 7f, 48f)

const val GOAL = 300000f
const val R_ORE = 1
const val R_ING = 2
const val R_ITEM = 3
const val HOLD = 0.9f

val TF_BOLD: Typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
val TF_BLACK: Typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
val TF_MONO: Typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

class Station(val tier: Int, val role: Int, val x: Float, val y: Float) {
    var stock = 0
    var inN = 0
    var outN = 0
    var prog = 0f
    var lv = 1
    var hot = 0f
    var pop = 0f
    val cap = 16
}

class Cust(val tier: Int, var slot: Int) {
    var qty = 1
    var pat = 1f
    var patMax = 26f
    var x = 0f
    var y = 0f
    var state = 0
    var t = 0f
    var face = 0
    var bob = 0f
}

class Actor(val tier: Int, val job: Int, val post: Int) {
    var x = 0f
    var y = 0f
    var kind = 0
    var carry = 0
    var cTier = 0
    var st = 0
    var timer = 0f
    var phase = 0f
    var face = 1f
    var srcRole = -1
    var dstRole = -1
    var stuck = 0f
    var lx = 0f
    var ly = 0f
    var swing = 0f
}

class Spot(val tier: Int, val kind: Int, val x: Float, val y: Float) {
    var paid = 0f
    var pulse = 0f
    var done = false
    var hold = 0f
    var labelText = ""
    var labelW = 0f
}

class Terr(val i: Int, val ox: Float) {
    var open = i == 0
    val vein = Station(i, 0, ox + 2.5f, 3.2f)
    val furn = Station(i, 1, ox + 7.6f, 3.2f)
    val anv = Station(i, 2, ox + 7.6f, 7.6f)
    val cnt = Station(i, 3, ox + 2.5f, 11.2f)
    var miners = 0
    var carriers = 0
    var smiths = 0
    var sellers = 0
    var spawnT = 2f
    val custs = ArrayList<Cust>()
    fun st(role: Int): Station = when (role) {
        0 -> vein
        1 -> furn
        2 -> anv
        else -> cnt
    }
}

class Part(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var life: Float, val max: Float,
    val r: Float, val c: Int, val grav: Float
)

class Ftxt(var x: Float, var y: Float, val s: String, val c: Int, var life: Float)

class Pile(var x: Float, var y: Float, var amt: Float, var t: Float, var fly: Float)

class UiBtn(val r: RectF, val tag: String)
