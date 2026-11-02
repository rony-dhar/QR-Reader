package com.qrreader.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrUtils {

    // ---------- Creating ----------

    /** Renders [content] as a black-on-white QR bitmap (always high-contrast so it scans reliably). */
    fun encode(content: String, size: Int = 900): Bitmap {
        val hints = mapOf<EncodeHintType, Any>(
            EncodeHintType.MARGIN to 2,
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val w = matrix.width
        val h = matrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                pixels[y * w + x] = if (matrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
    }

    private fun escapeWifi(s: String): String = buildString {
        for (c in s) {
            if (c in "\\;,:\"") append('\\')
            append(c)
        }
    }

    /** security: 0 = WPA/WPA2, 1 = WEP, 2 = none */
    fun wifiPayload(ssid: String, password: String, security: Int, hidden: Boolean): String {
        val type = when (security) {
            0 -> "WPA"
            1 -> "WEP"
            else -> "nopass"
        }
        return buildString {
            append("WIFI:T:").append(type).append(";")
            append("S:").append(escapeWifi(ssid)).append(";")
            if (type != "nopass") append("P:").append(escapeWifi(password)).append(";")
            if (hidden) append("H:true;")
            append(";")
        }
    }

    fun vCard(name: String, org: String, phone: String, email: String): String = buildString {
        append("BEGIN:VCARD\n")
        append("VERSION:3.0\n")
        append("N:;").append(name).append(";;;\n")
        append("FN:").append(name).append("\n")
        if (org.isNotBlank()) append("ORG:").append(org).append("\n")
        if (phone.isNotBlank()) append("TEL:").append(phone).append("\n")
        if (email.isNotBlank()) append("EMAIL:").append(email).append("\n")
        append("END:VCARD")
    }

    // ---------- Reading ----------

    /** Parses "WIFI:T:WPA;S:name;P:pass;;" into its fields, or null if it isn't a Wi-Fi code. */
    fun parseWifi(raw: String): Map<String, String>? {
        if (!raw.startsWith("WIFI:", ignoreCase = true)) return null
        val body = raw.substring(5)
        val result = mutableMapOf<String, String>()
        val key = StringBuilder()
        val value = StringBuilder()
        var inKey = true
        var i = 0
        while (i < body.length) {
            val c = body[i]
            when {
                c == '\\' && i + 1 < body.length -> {
                    (if (inKey) key else value).append(body[i + 1])
                    i++
                }
                inKey && c == ':' -> inKey = false
                !inKey && c == ';' -> {
                    result[key.toString()] = value.toString()
                    key.setLength(0)
                    value.setLength(0)
                    inKey = true
                }
                inKey -> if (c != ';') key.append(c)
                else -> value.append(c)
            }
            i++
        }
        return result
    }

    fun kindOfText(raw: String): String {
        val t = raw.trim()
        return when {
            t.startsWith("http://", true) || t.startsWith("https://", true) -> "Link"
            t.startsWith("WIFI:", true) -> "Wi-Fi"
            t.startsWith("mailto:", true) -> "Email"
            t.startsWith("tel:", true) -> "Phone"
            t.startsWith("smsto:", true) || t.startsWith("sms:", true) -> "SMS"
            t.startsWith("BEGIN:VCARD", true) -> "Contact"
            t.startsWith("geo:", true) -> "Location"
            else -> "Text"
        }
    }

    fun formatName(format: Int): String = when (format) {
        Barcode.FORMAT_QR_CODE -> "QR Code"
        Barcode.FORMAT_AZTEC -> "Aztec"
        Barcode.FORMAT_CODABAR -> "Codabar"
        Barcode.FORMAT_CODE_39 -> "Code 39"
        Barcode.FORMAT_CODE_93 -> "Code 93"
        Barcode.FORMAT_CODE_128 -> "Code 128"
        Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
        Barcode.FORMAT_EAN_8 -> "EAN-8"
        Barcode.FORMAT_EAN_13 -> "EAN-13"
        Barcode.FORMAT_ITF -> "ITF"
        Barcode.FORMAT_PDF417 -> "PDF417"
        Barcode.FORMAT_UPC_A -> "UPC-A"
        Barcode.FORMAT_UPC_E -> "UPC-E"
        else -> "Barcode"
    }

    /** Turns an ML Kit result into a record we can show and store. */
    fun recordFrom(barcode: Barcode): ScanRecord? {
        val raw = barcode.rawValue ?: barcode.displayValue ?: return null
        if (raw.isEmpty()) return null
        val kind = if (barcode.valueType == Barcode.TYPE_PRODUCT || barcode.valueType == Barcode.TYPE_ISBN) {
            "Product"
        } else {
            kindOfText(raw)
        }
        val now = System.currentTimeMillis()
        return ScanRecord(now, raw, formatName(barcode.format), kind, Source.SCANNED, now)
    }

    /** Short one-line summary for lists. */
    fun summary(record: ScanRecord): String {
        val wifi = parseWifi(record.content)
        return if (wifi != null) "Wi-Fi · ${wifi["S"].orEmpty()}" else record.content.trim()
    }

    /** Looks for QR codes / barcodes in a picked image. */
    fun scanImage(
        context: Context,
        uri: Uri,
        onResult: (List<Barcode>) -> Unit,
        onError: () -> Unit,
    ) {
        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            onError()
            return
        }
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { onResult(it) }
            .addOnFailureListener { onError() }
            .addOnCompleteListener { scanner.close() }
    }
}
