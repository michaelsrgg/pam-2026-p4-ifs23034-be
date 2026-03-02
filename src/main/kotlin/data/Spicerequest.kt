package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Spice

@Serializable
data class SpiceRequest(
    var nama: String = "",
    var jenis: String = "",
    var pathGambar: String = "",
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nama" to nama,
            "jenis" to jenis,
            "pathGambar" to pathGambar,
        )
    }

    fun toEntity(): Spice {
        return Spice(
            nama = nama,
            jenis = jenis,
            pathGambar = pathGambar,
        )
    }
}