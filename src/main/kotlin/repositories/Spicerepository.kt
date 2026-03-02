package org.delcom.repositories

import org.delcom.dao.SpiceDAO
import org.delcom.entities.Spice
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.SpiceTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class SpiceRepository : ISpiceRepository {

    private fun daoToModel(dao: SpiceDAO) = Spice(
        id = dao.id.value.toString(),
        nama = dao.nama,
        jenis = dao.jenis,
        pathGambar = dao.pathGambar,
        createdAt = dao.createdAt,
        updatedAt = dao.updatedAt,
    )

    override suspend fun getSpices(search: String): List<Spice> = suspendTransaction {
        if (search.isBlank()) {
            SpiceDAO.all()
                .orderBy(SpiceTable.createdAt to SortOrder.DESC)
                .limit(20)
                .map(::daoToModel)
        } else {
            val keyword = "%${search.lowercase()}%"
            SpiceDAO
                .find { SpiceTable.nama.lowerCase() like keyword }
                .orderBy(SpiceTable.nama to SortOrder.ASC)
                .limit(20)
                .map(::daoToModel)
        }
    }

    override suspend fun getSpiceById(id: String): Spice? = suspendTransaction {
        SpiceDAO
            .find { SpiceTable.id eq UUID.fromString(id) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun getSpiceByName(name: String): Spice? = suspendTransaction {
        SpiceDAO
            .find { SpiceTable.nama eq name }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun addSpice(spice: Spice): String = suspendTransaction {
        val spiceDAO = SpiceDAO.new {
            nama = spice.nama
            jenis = spice.jenis
            pathGambar = spice.pathGambar
            createdAt = spice.createdAt
            updatedAt = spice.updatedAt
        }
        spiceDAO.id.value.toString()
    }

    override suspend fun updateSpice(id: String, newSpice: Spice): Boolean = suspendTransaction {
        val spiceDAO = SpiceDAO
            .find { SpiceTable.id eq UUID.fromString(id) }
            .limit(1)
            .firstOrNull()

        if (spiceDAO != null) {
            spiceDAO.nama = newSpice.nama
            spiceDAO.jenis = newSpice.jenis
            spiceDAO.pathGambar = newSpice.pathGambar
            spiceDAO.updatedAt = newSpice.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun removeSpice(id: String): Boolean = suspendTransaction {
        val rowsDeleted = SpiceTable.deleteWhere {
            SpiceTable.id eq UUID.fromString(id)
        }
        rowsDeleted == 1
    }
}