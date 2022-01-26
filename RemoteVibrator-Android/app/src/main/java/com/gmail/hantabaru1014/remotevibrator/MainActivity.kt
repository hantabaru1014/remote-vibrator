package com.gmail.hantabaru1014.remotevibrator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var serviceIntent: Intent
    private val logTextView: TextView by lazy {
        findViewById(R.id.textLog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()
        loadPref()
        findViewById<Button>(R.id.buttonDisconnect).isEnabled = false
    }

    private fun addLogMessage(message: String){
        runOnUiThread {
            logTextView.append("$message${System.lineSeparator()}")
        }
    }

    fun handleClickConnect(view: View){
        val editAddress = findViewById<EditText>(R.id.editAddress)
        val editDeviceName = findViewById<EditText>(R.id.editDeviceName)
        val address = editAddress.text.toString()
        val deviceName = editDeviceName.text.toString()
        serviceIntent = Intent(this, ForegroundService::class.java).apply {
            putExtra("url", address)
            putExtra("deviceName", deviceName)
        }
        applicationContext.startForegroundService(serviceIntent)
        savePref(address, deviceName)
        view.isEnabled = false
        findViewById<Button>(R.id.buttonDisconnect).isEnabled = true
        editAddress.isFocusable = false
        editDeviceName.isFocusable = false
        Log.i(javaClass.simpleName, "Start ForegroundService")
    }

    fun handleClickDisconnect(view: View){
        stopService(serviceIntent)
        view.isEnabled = false
        findViewById<Button>(R.id.buttonConnect).isEnabled = true
        val editAddress = findViewById<EditText>(R.id.editAddress)
        val editDeviceName = findViewById<EditText>(R.id.editDeviceName)
        editAddress.isFocusableInTouchMode = true
        editAddress.isFocusable = true
        editDeviceName.isFocusableInTouchMode = true
        editDeviceName.isFocusable = true
        Log.i(javaClass.simpleName, "Stop ForegroundService")
    }

    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            ForegroundService.NOTIFY_CHANNEL_ID,
            "ForegroundService",
            NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "ForegroundService Notification"
            }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun loadPref(){
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val address = sharedPref.getString("address", getString(R.string.edit_address_placeholder))
        val deviceName = sharedPref.getString("deviceName", getString(R.string.edit_devicename_placeholder))
        findViewById<EditText>(R.id.editAddress).setText(address)
        findViewById<EditText>(R.id.editDeviceName).setText(deviceName)
    }

    private fun savePref(address: String, deviceName: String){
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()){
            putString("address", address)
            putString("deviceName", deviceName)
            apply()
        }
    }
}