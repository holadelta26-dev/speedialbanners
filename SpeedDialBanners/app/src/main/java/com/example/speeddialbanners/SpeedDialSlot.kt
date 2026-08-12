package com.example.speeddialbanners

/**
 * Representa una casilla de marcado rápido.
 * - slotId: posición fija (0..7)
 * - contactName: nombre a mostrar sobre el banner
 * - phoneNumber: número al que se llama al mantener presionado
 * - bannerUri: URI de la imagen elegida por el usuario como fondo/banner
 */
data class SpeedDialSlot(
    val slotId: Int,
    var contactName: String? = null,
    var phoneNumber: String? = null,
    var bannerUri: String? = null
) {
    val isEmpty: Boolean
        get() = phoneNumber.isNullOrEmpty()
}
