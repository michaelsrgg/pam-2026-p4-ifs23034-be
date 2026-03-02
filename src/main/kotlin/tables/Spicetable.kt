package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object SpiceTable : UUIDTable("spices") {
    val nama = varchar("nama", 100)
    val jenis = varchar("jenis", 100)
    val pathGambar = varchar("path_gambar", 255)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}