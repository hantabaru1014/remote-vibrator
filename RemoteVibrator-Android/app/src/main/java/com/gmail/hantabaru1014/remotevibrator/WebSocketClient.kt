package com.gmail.hantabaru1014.remotevibrator

import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import android.util.Log
import kotlinx.serialization.json.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketClient(uri: URI, private val deviceName: String, private val vibrator: Vibrator, private val logHandler: ((String)->Unit)?) : WebSocketClient(uri) {
    private val json = Json { ignoreUnknownKeys = true }

    private fun log(message: String?) {
        if (message != null){
            Log.i(javaClass.simpleName, message)
            logHandler?.invoke(message)
        }
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        val cmd = buildJsonObject {
            put("cmd", "setDeviceName")
            put("name", deviceName)
        }
        send(cmd.toString())
        log("Connected to Server")
    }

    override fun onMessage(message: String?) {
        if (message != null){
            val cmd = json.parseToJsonElement(message).jsonObject
            if (cmd["cmd"]!!.jsonPrimitive.content.equals("vibrate", ignoreCase = true)){
                val ms = cmd["ms"]!!.jsonPrimitive.long
                val amp = cmd["amp"]!!.jsonPrimitive.intOrNull // optional
                val effect = VibrationEffect.createOneShot(ms, amp ?: DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            }
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        log("Disconnected")
    }

    override fun onError(ex: Exception?) {
        log("Error: ${ex?.message}")
    }
}