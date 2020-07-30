package com.kodetani.onoffled

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val ACTION_USB = "com.kodetani.onoffled.OnOffUSB"
    }

    var on: Boolean = false

    private val usbBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context, p1: Intent) {
            if (p1.action == ACTION_USB) {
                synchronized(this) {
                    val usbOTG: UsbDevice? = p1.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if(p1.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        usbOTG?.apply {
                            connectDevice()
                        }
                    } else {
                        Toast.makeText(
                                p0,
                                "Gagal mendapatkan permission USB",
                                Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectDevice()
    }

    private fun connectDevice() {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)

        if (availableDrivers.isEmpty()) {
            textViewStatus.text = "Ups... tidak ada USB terhubung..."
            textViewStatus.setTextColor(Color.RED)
            return
        }
        // kalau ada device terhubung
        val driver = availableDrivers.first()
        val connection = manager.openDevice(driver.device)
        if (connection == null) {
            textViewStatus.text = "Ups... ada koneksi oatau permission..."
            textViewStatus.setTextColor(Color.RED)
            val permissionIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(ACTION_USB),
                    0
            )
            val filter = IntentFilter(ACTION_USB)
            registerReceiver(usbBroadcastReceiver, filter)
            manager.requestPermission(driver.device, permissionIntent)
            return
        }

        // kalau semua OK
        val port = driver.ports.firstOrNull()
        port?.open(connection)
        port?.setParameters(
                115200,
                8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
        )
        textViewStatus.text = "Connected to device...."
        textViewStatus.setTextColor(Color.GREEN)

        button.setOnClickListener {
            port?.write((if (on) "0" else "1").toByteArray(), 100)
            var bytes = ByteArray(50)
            port?.read(bytes, 200)
            textViewStatus.text = String(bytes)
            textViewStatus.setTextColor(Color.BLUE)
            on = !on
        }
    }
}