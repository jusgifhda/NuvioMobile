package com.nuvio.app.features.telegram

import com.nuvio.app.BuildConfig

actual object TelegramConfig {
    actual val API_ID: Int
        get() = BuildConfig.TELEGRAM_API_ID

    actual val API_HASH: String
        get() = BuildConfig.TELEGRAM_API_HASH
}
