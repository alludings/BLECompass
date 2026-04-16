# MBCompass - Bluetooth Compatibility with Raspberry Pi 5

This project is a modified version of **MBCompass** that adds **Bluetooth Low Energy (BLE)** support so an Android device can receive heading data from a **Raspberry Pi 5**.

The app is designed to work with a Raspberry Pi acting as a BLE server and sending heading values to the Android client. The heading can come from:
- IMU-only mode (magnetometer + accelerometer on the Pi)
- KrakenSDR DOA data
- fused IMU + KrakenSDR heading data

## Features

- Android compass app based on MBCompass
- BLE client support for Raspberry Pi 5
- Reads heading over BLE as a little-endian float
- Can use Raspberry Pi IMU heading
- Can use KrakenSDR `live_doa.json`
- Supports fallback behavior when Kraken data is unavailable
- Can be used in a portable / headless setup

## Project Structure

### Android App
- Modified MBCompass Android application
- BLE client added in `BLECompassClient.kt`
- Compass UI updated to use BLE heading values
