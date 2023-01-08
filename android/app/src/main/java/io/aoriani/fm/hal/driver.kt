package io.aoriani.fm.hal

import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.lang.Exception


fun starting(context: Context) {
    try {
        val usbManager = context.getSystemService(UsbManager::class.java)
        val customProbeTable = ProbeTable().apply {
            addProduct(9114, 32971, CdcAcmSerialDriver::class.java)
        }
        val prober = UsbSerialProber(customProbeTable)
        val availableDrivers = prober.findAllDrivers(usbManager)
        val driver = availableDrivers[0]
        val connection = usbManager.openDevice(driver.device)
        val port = driver.ports[0] // Most devices have just one port (port 0)

        port.open(connection)
        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        port.dtr = true

        Log.d("ORIANI","Perm = ${usbManager.hasPermission(driver.device)}")

//        port.setBreak()


        val ioManager = SerialInputOutputManager(port, object : SerialInputOutputManager.Listener{
            override fun onNewData(data: ByteArray?) {
                val text = data?.let { String(data, Charsets.US_ASCII) }.orEmpty()
                Log.d("ORIANI", text.replace('\n', '#').replace(' ', '='))
            }

            override fun onRunError(e: Exception?) {
                Log.d("ORIANI", e?.message.orEmpty())
            }

        })

        ioManager.start()
        ioManager.writeAsync("s911".toByteArray(Charsets.US_ASCII))
        //port.write("hello".encodeToByteArray(), 1000);
        Log.d("ORIANI","Hi")
    } catch (e: Exception) {
        Log.d("ORIANI","Fez merda: ${e.message}", e)
    }


}
