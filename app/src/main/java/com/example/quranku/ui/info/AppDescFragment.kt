package com.example.quranku.ui.info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.quranku.R
import com.example.quranku.databinding.FragmentAppDescBinding

class AppDescFragment : Fragment() {
    private var _binding: FragmentAppDescBinding? = null
    private val binding: FragmentAppDescBinding by lazy { _binding!! }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAppDescBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set Cinzel font only for 'QuranKu' in the About title
        val tvAboutQuranKu = binding.tvAboutQuranKu
        val aboutText = tvAboutQuranKu.text.toString()
        val cinzel = ResourcesCompat.getFont(requireContext(), R.font.cinzelbold)
        if (cinzel != null) {
            val spannable = SpannableString(aboutText)
            val start = aboutText.indexOf("QuranKu")
            if (start >= 0) {
                val end = start + "QuranKu".length
                spannable.setSpan(CustomTypefaceSpan(cinzel), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            tvAboutQuranKu.text = spannable
        }
        val tvAppDesc = binding.tvAppDesc
        val originalText = tvAppDesc.text.toString()
        if (cinzel != null) {
            val spannable = SpannableString(originalText)
            var start = originalText.indexOf("QuranKu")
            while (start >= 0) {
                val end = start + "QuranKu".length
                spannable.setSpan(CustomTypefaceSpan(cinzel), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                start = originalText.indexOf("QuranKu", end)
            }
            tvAppDesc.text = spannable
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Helper class for custom font span
class CustomTypefaceSpan(private val typeface: Typeface) : TypefaceSpan("") {
    override fun updateDrawState(ds: android.text.TextPaint) {
        applyCustomTypeFace(ds, typeface)
    }
    override fun updateMeasureState(paint: android.text.TextPaint) {
        applyCustomTypeFace(paint, typeface)
    }
    private fun applyCustomTypeFace(paint: android.text.TextPaint, tf: Typeface) {
        val oldStyle: Int
        val old = paint.typeface
        oldStyle = old?.style ?: 0
        val fake = oldStyle and tf.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }
        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }
        paint.typeface = tf
    }
}