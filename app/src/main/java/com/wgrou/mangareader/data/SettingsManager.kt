package com.wgrou.mangareader.data

import android.content.Context

class SettingsManager(
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            "app_settings",
            Context.MODE_PRIVATE
        )

    companion object {
        const val DEFAULT_TEXTURE_STRENGTH = 0.5f
        const val DEFAULT_PAPER_WARMTH = 1f

        private const val KEY_TEXTURE_STRENGTH =
            "texture_strength"

        private const val KEY_PAPER_WARMTH =
            "paper_warmth"
    }

    fun getTextureStrength(): Float {
        return preferences.getFloat(
            KEY_TEXTURE_STRENGTH,
            DEFAULT_TEXTURE_STRENGTH
        ).coerceIn(0f, 1f)
    }

    fun setTextureStrength(value: Float) {
        preferences.edit()
            .putFloat(
                KEY_TEXTURE_STRENGTH,
                value.coerceIn(0f, 1f)
            )
            .apply()
    }

    fun getPaperWarmth(): Float {
        return preferences.getFloat(
            KEY_PAPER_WARMTH,
            DEFAULT_PAPER_WARMTH
        ).coerceIn(0f, 1f)
    }

    fun setPaperWarmth(value: Float) {
        preferences.edit()
            .putFloat(
                KEY_PAPER_WARMTH,
                value.coerceIn(0f, 1f)
            )
            .apply()
    }

}

