package com.example.quranku.ui.home

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class WaveformView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

	// ===== Paint =====
	private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		style = Paint.Style.FILL
		color = Color.parseColor("#5A5A5A") // bar kiri (masa lalu)
	}
	private val baselinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		style = Paint.Style.STROKE
		strokeWidth = dp(1.5f)
		color = Color.parseColor("#33000000")
	}
	private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		style = Paint.Style.FILL
		color = Color.parseColor("#33000000") // titik placeholder
	}

	// ===== State/Config =====
	private val levels: MutableList<Float> = ArrayList() // 0..1, index 0 = tertua
	private var halfSlots = 48                   // jumlah slot dari tengah ke kiri/kanan
	private var barWidthPx = dp(4f)              // lebar batang
	private var gapPx = dp(3f)                   // jarak antar batang/titik
	private val roundPx = dp(3f)                 // rounded rect radius
	private val dotRadius = dp(2.2f)             // radius titik
	private val smoothAlpha = 0.35f              // smoothing 0..1
	private var lastLevel = 0f

	private var isPlaying = false                // << mode yang kamu minta

	// ===== API =====
	/** panggil saat mulai/berhenti play/record */
	fun setPlaying(playing: Boolean) {
		isPlaying = playing
		invalidate()
	}

	/** amplitude mentah 0..32767 */
	fun addAmplitude(amplitude: Int) {
		val raw = (amplitude.coerceIn(0, 32767) / 32767f)
		val smooth = smoothAlpha * raw + (1 - smoothAlpha) * lastLevel
		lastLevel = smooth
		pushLevel(smooth)
	}

	/** reset konten */
	fun clear() {
		levels.clear()
		lastLevel = 0f
		invalidate()
	}

	/** tambah batch level (0..1) opsional */
	fun addLevels(batch: FloatArray) {
		batch.forEach { pushLevel(it.coerceIn(0f, 1f)) }
	}

	private fun pushLevel(lv: Float) {
		levels.add(lv)
		trimToMax()
		invalidate()
	}

	private fun trimToMax() {
		while (levels.size > halfSlots) levels.removeAt(0)
	}

	// ===== Render =====
	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		val per = barWidthPx + gapPx
		halfSlots = max(16, (w / 2f / per).toInt())
		trimToMax()
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		val w = width.toFloat()
		val h = height.toFloat()
		val cx = w / 2f
		val cy = h / 2f
		val per = barWidthPx + gapPx

		// baseline
		canvas.drawLine(0f, cy, w, cy, baselinePaint)

		// ------- PREVIEW (Layout Editor) -------
		if (isInEditMode && levels.isEmpty()) {
			val n = (halfSlots * 0.6f).toInt()
			val tmp = FloatArray(n) { i ->
				val t = i / (n - 1f)
				(0.25f + 0.75f * (1f - t * t)) * 0.9f
			}
			addLevels(tmp)
		}

		if (!isPlaying) {
			// ======= MODE: NOT PLAYING -> titik full sepanjang view =======
			// gambar dari kiri ke kanan dengan jarak per
			var x = (per / 2f)
			while (x < w) {
				canvas.drawCircle(x, cy, dotRadius, dotPaint)
				x += per
			}
			return
		}

		// ======= MODE: PLAYING =======
		// 1) BAR kiri (real-time), terbaru dekat center lalu bergeser ke kiri
		if (levels.isNotEmpty()) {
			val n = min(levels.size, halfSlots)
			val maxBarHeight = h * 0.85f
			for (i in 0 until n) {
				val lv = levels[levels.lastIndex - i] // i=0 => paling baru
				val barH = max(dp(4f), lv * maxBarHeight)
				val left = cx - (i + 1) * per
				val right = left + barWidthPx
				val top = cy - barH / 2f
				val bottom = cy + barH / 2f
				canvas.drawRoundRect(left, top, right, bottom, roundPx, roundPx, barPaint)
			}
		}

		// 2) TITIK kanan (to-be) SELALU PENUH dari tengah ke kanan
		for (i in 0 until halfSlots) {
			val x = cx + (i + 0.5f) * per
			canvas.drawCircle(x, cy, dotRadius, dotPaint)
		}
	}

	// ===== Util =====
	private fun dp(v: Float): Float = v * resources.displayMetrics.density
}
