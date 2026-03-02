package org.delcom.dao

import org.delcom.tables.SpiceTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class SpiceDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, SpiceDAO>(SpiceTable)

    var nama by SpiceTable.nama
    var jenis by SpiceTable.jenis
    var pathGambar by SpiceTable.pathGambar
    var createdAt by SpiceTable.createdAt
    var updatedAt by SpiceTable.updatedAt
}