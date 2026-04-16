// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mubarak.mbcompass.utils.Azimuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _azimuth = MutableStateFlow(Azimuth(0f))
    val azimuth: StateFlow<Azimuth> = _azimuth.asStateFlow()

    private val _strength = MutableStateFlow(0f)
    val strength: StateFlow<Float> = _strength.asStateFlow()

    private val bleClient = BLECompassClient(application) { heading ->
        updateAzimuth(Azimuth(heading))
    }

    init {
        bleClient.startScan()
    }

    fun updateAzimuth(azimuth: Azimuth) {
        _azimuth.value = azimuth
    }

    fun updateMagneticStrength(strengthInUt: Float) {
        _strength.value = strengthInUt
    }
}