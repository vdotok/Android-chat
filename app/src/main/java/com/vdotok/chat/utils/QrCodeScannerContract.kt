package com.vdotok.chat.utils

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class QrCodeScannerContract: ActivityResultContract<IntentIntegrator, IntentResult>() {

    override fun createIntent(context: Context, input: IntentIntegrator): Intent {
        return input.apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("Point the Scanner at the QR Code")
            setBeepEnabled(false)
            setBarcodeImageEnabled(false)
            setOrientationLocked(false)
        }.createScanIntent()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): IntentResult {
        return IntentIntegrator.parseActivityResult(resultCode, intent)
    }
}