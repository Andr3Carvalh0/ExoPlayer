package com.google.android.exoplayer2.ui.utilities

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.View
import androidx.core.content.ContextCompat

object SpannableUtilities {

    fun twoTone(
        firstColor: Int,
        secondColor: Int,
        separator: String,
        text: String,
        context: Context
    ): Spannable {
        val wordToSpan = SpannableString(text)
        val index = text.indexOf(separator)

        if (index == -1) {
            wordToSpan.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, firstColor)), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            wordToSpan.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, firstColor)), 0, index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            wordToSpan.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, secondColor)), index, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return wordToSpan
    }

    /**
     * text: the whole phrase including the text that you want to highligth
     * textToHighlight: the text that will be highligthed
     * */
    fun url(text: String, textToHighlight: String, url: String): SpannableStringBuilder {
        val urlStart = text.indexOf(textToHighlight)

        val spannable = SpannableStringBuilder(text)
        spannable.setSpan(URLSpan(url), urlStart, urlStart + textToHighlight.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        return spannable
    }

    /**
     * text: the whole phrase including the text that you want to highligth
     * textToHighlight: the text that will be highligthed
     * */
    fun url(text: String, textToHighlight: String, onClick: () -> Unit): SpannableStringBuilder {
        val urlStart = text.indexOf(textToHighlight)

        val spannable = SpannableStringBuilder(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClick.invoke()
            }
        }

        spannable.setSpan(clickableSpan, urlStart, urlStart + textToHighlight.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        return spannable
    }
}
