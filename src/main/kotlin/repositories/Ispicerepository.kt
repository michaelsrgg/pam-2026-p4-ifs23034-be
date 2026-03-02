package org.delcom.repositories

import org.delcom.entities.Spice

interface ISpiceRepository {
    suspend fun getSpices(search: String): List<Spice>
    suspend fun getSpiceById(id: String): Spice?
    suspend fun getSpiceByName(name: String): Spice?
    suspend fun addSpice(spice: Spice): String
    suspend fun updateSpice(id: String, newSpice: Spice): Boolean
    suspend fun removeSpice(id: String): Boolean
}