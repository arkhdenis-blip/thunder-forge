package com.groma.forge

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.max
import kotlin.math.sin

class Sfx {
    private val sr = 22050
    private var track: AudioTrack? = null
    private var th: Thread? = null

    @Volatile var on = true
    @Volatile private var running = false
    @Volatile var amb = 0f

    private val lock = Any()
    private var ambPh = 0.0
    private val vs = ArrayList<Voice>()

    private class Voice(
        var f: Float, var f2: Float,
        var dur: Float, var vol: Float, var noise: Boolean
    ) {
        var t = 0f
        var ph = 0.0
    }

    fun start() {
        if (running) return
        try {
            val min = AudioTrack.getMinBufferSize(
                sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            val t = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sr)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(max(min * 2, 4096))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            t.play()
            track = t
            running = true
            val worker = Thread {
                val buf = ShortArray(512)
                while (running) {
                    if (on) mix(buf) else java.util.Arrays.fill(buf, 0.toShort())
                    try {
                        t.write(buf, 0, buf.size)
                    } catch (e: Exception) {
                        break
                    }
                }
            }
            worker.priority = Thread.MIN_PRIORITY
            worker.isDaemon = true
            worker.start()
            th = worker
        } catch (e: Exception) {
            track = null
            running = false
        }
    }

    fun stop() {
        running = false
        try { th?.join(250) } catch (e: Exception) {}
        th = null
        try { track?.pause() } catch (e: Exception) {}
        try { track?.flush() } catch (e: Exception) {}
        try { track?.stop() } catch (e: Exception) {}
        try { track?.release() } catch (e: Exception) {}
        track = null
        synchronized(lock) { vs.clear() }
    }

    private fun mix(buf: ShortArray) {
        synchronized(lock) {
            for (i in buf.indices) {
                var s = 0f
                if (amb > 0.002f) {
                    ambPh += 2.0 * Math.PI * 54.0 / sr
                    s += (sin(ambPh) * 0.7 + sin(ambPh * 1.503) * 0.3).toFloat() * amb * 0.10f
                }
                var k = 0
                while (k < vs.size) {
                    val v = vs[k]
                    v.t += 1f / sr
                    if (v.t >= v.dur) {
                        vs.removeAt(k)
                        continue
                    }
                    if (v.t < 0f) { k++; continue }
                    val p = v.t / v.dur
                    val atk = if (v.t < 0.006f) v.t / 0.006f else 1f
                    val env = atk * (1f - p) * (1f - p)
                    val f = v.f + (v.f2 - v.f) * p
                    v.ph += 2.0 * Math.PI * f / sr
                    if (v.ph > 6.283185) v.ph -= 6.283185
                    val w = if (v.noise) (Math.random().toFloat() * 2f - 1f)
                    else sin(v.ph).toFloat()
                    s += w * env * v.vol
                    k++
                }
                buf[i] = (cl(s, -1f, 1f) * 29000f).toInt().toShort()
            }
        }
    }

    private fun add(f: Float, f2: Float, dur: Float, vol: Float, noise: Boolean, delay: Float) {
        if (!on || !running) return
        synchronized(lock) {
            if (vs.size > 22) return
            val v = Voice(f, f2, dur, vol, noise)
            v.t = -delay
            vs.add(v)
        }
    }

    fun pick() { add(620f, 900f, 0.07f, 0.16f, false, 0f) }
    fun drop() { add(300f, 200f, 0.08f, 0.14f, false, 0f) }
    fun hammer() {
        add(1250f, 420f, 0.13f, 0.20f, false, 0f)
        add(0f, 0f, 0.09f, 0.10f, true, 0f)
    }
    fun mine() {
        add(180f, 90f, 0.12f, 0.18f, true, 0f)
        add(760f, 300f, 0.08f, 0.10f, false, 0f)
    }
    fun coin() {
        add(1050f, 1500f, 0.09f, 0.13f, false, 0f)
        add(1560f, 2100f, 0.08f, 0.09f, false, 0.04f)
    }
    fun build() {
        add(220f, 440f, 0.30f, 0.18f, false, 0f)
        add(0f, 0f, 0.25f, 0.09f, true, 0f)
    }
    fun done() {
        add(523f, 523f, 0.16f, 0.16f, false, 0f)
        add(784f, 784f, 0.20f, 0.15f, false, 0.10f)
        add(1046f, 1046f, 0.30f, 0.14f, false, 0.20f)
    }
    fun angry() { add(300f, 120f, 0.28f, 0.16f, false, 0f) }
    fun ui() { add(880f, 660f, 0.05f, 0.09f, false, 0f) }
    fun fanfare() {
        val n = floatArrayOf(523f, 659f, 784f, 1046f, 1318f)
        for (i in n.indices) add(n[i], n[i], 0.45f, 0.15f, false, i * 0.11f)
    }
}
