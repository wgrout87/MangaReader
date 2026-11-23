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

}

