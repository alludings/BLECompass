// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

class BLECompassClient(
    private val context: Context,
    private val onHeadingUpdate: (Float) -> Unit
) {

    private val SERVICE_UUID =
        UUID.fromString("12345678-1234-5678-1234-56789abcdef0")
    private val HEADING_CHAR_UUID =
        UUID.fromString("12345678-1234-5678-1234-56789abcdef1")
    private val CCCD_UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val bluetoothAdapter: BluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private val scanner = bluetoothAdapter.bluetoothLeScanner
    private var gatt: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!bluetoothAdapter.isEnabled) return

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(listOf(filter), settings, scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            scanner.stopScan(this)
            gatt = result.device.connectGatt(
                context,
                false,
                gattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                g.discoverServices()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            val service = g.getService(SERVICE_UUID) ?: return
            val headingChar = service.getCharacteristic(HEADING_CHAR_UUID) ?: return

            g.setCharacteristicNotification(headingChar, true)

            val descriptor = headingChar.getDescriptor(CCCD_UUID) ?: return
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            g.writeDescriptor(descriptor)
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == HEADING_CHAR_UUID) {
                val data = characteristic.value
                if (data != null && data.size >= 4) {
                    val heading = ByteBuffer.wrap(data)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .float

                    onHeadingUpdate(heading)
                }
            }
        }
    }
}