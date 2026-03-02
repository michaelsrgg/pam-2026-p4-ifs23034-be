package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.datetime.Clock
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.SpiceRequest
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.ISpiceRepository
import java.io.File
import java.util.*

class SpiceService(private val spiceRepository: ISpiceRepository) {

    // Mengambil semua data rempah
    suspend fun getAllSpices(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""
        val spices = spiceRepository.getSpices(search)
        call.respond(
            DataResponse("success", "Berhasil mengambil daftar rempah", mapOf("spices" to spices))
        )
    }

    // Mengambil data rempah berdasarkan id
    suspend fun getSpiceById(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID rempah tidak boleh kosong!")

        val spice = spiceRepository.getSpiceById(id)
            ?: throw AppException(404, "Data rempah tidak tersedia!")

        call.respond(
            DataResponse("success", "Berhasil mengambil data rempah", mapOf("spice" to spice))
        )
    }

    // Ambil data request dari multipart
    private suspend fun getSpiceRequest(call: ApplicationCall): SpiceRequest {
        val spiceReq = SpiceRequest()
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)

        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "nama" -> spiceReq.nama = part.value.trim()
                        "jenis" -> spiceReq.jenis = part.value.trim()
                    }
                }
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/spices/$fileName"

                    val file = File(filePath)
                    file.parentFile.mkdirs()

                    part.provider().copyAndClose(file.writeChannel())
                    spiceReq.pathGambar = filePath
                }
                else -> {}
            }
            part.dispose()
        }

        return spiceReq
    }

    // Validasi request
    private fun validateSpiceRequest(spiceReq: SpiceRequest) {
        val validator = ValidatorHelper(spiceReq.toMap())
        validator.required("nama", "Nama rempah tidak boleh kosong")
        validator.required("jenis", "Jenis rempah tidak boleh kosong")
        validator.required("pathGambar", "Gambar rempah tidak boleh kosong")
        validator.validate()

        val file = File(spiceReq.pathGambar)
        if (!file.exists()) {
            throw AppException(400, "Gambar rempah gagal diupload!")
        }
    }

    // Menambahkan data rempah
    suspend fun createSpice(call: ApplicationCall) {
        val spiceReq = getSpiceRequest(call)
        validateSpiceRequest(spiceReq)

        val existSpice = spiceRepository.getSpiceByName(spiceReq.nama)
        if (existSpice != null) {
            File(spiceReq.pathGambar).takeIf { it.exists() }?.delete()
            throw AppException(409, "Rempah dengan nama ini sudah terdaftar!")
        }

        val spiceId = spiceRepository.addSpice(spiceReq.toEntity())
        call.respond(
            DataResponse("success", "Berhasil menambahkan data rempah", mapOf("spiceId" to spiceId))
        )
    }

    // Mengubah data rempah
    suspend fun updateSpice(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID rempah tidak boleh kosong!")

        val oldSpice = spiceRepository.getSpiceById(id)
            ?: throw AppException(404, "Data rempah tidak tersedia!")

        val spiceReq = getSpiceRequest(call)

        if (spiceReq.pathGambar.isEmpty()) {
            spiceReq.pathGambar = oldSpice.pathGambar
        }

        validateSpiceRequest(spiceReq)

        if (spiceReq.nama != oldSpice.nama) {
            val existSpice = spiceRepository.getSpiceByName(spiceReq.nama)
            if (existSpice != null) {
                File(spiceReq.pathGambar).takeIf { it.exists() }?.delete()
                throw AppException(409, "Rempah dengan nama ini sudah terdaftar!")
            }
        }

        if (spiceReq.pathGambar != oldSpice.pathGambar) {
            File(oldSpice.pathGambar).takeIf { it.exists() }?.delete()
        }

        val entity = spiceReq.toEntity().copy(updatedAt = Clock.System.now())
        val isUpdated = spiceRepository.updateSpice(id, entity)
        if (!isUpdated) throw AppException(400, "Gagal memperbarui data rempah!")

        call.respond(DataResponse("success", "Berhasil mengubah data rempah", null))
    }

    // Menghapus data rempah
    suspend fun deleteSpice(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID rempah tidak boleh kosong!")

        val oldSpice = spiceRepository.getSpiceById(id)
            ?: throw AppException(404, "Data rempah tidak tersedia!")

        val oldFile = File(oldSpice.pathGambar)
        val isDeleted = spiceRepository.removeSpice(id)
        if (!isDeleted) throw AppException(400, "Gagal menghapus data rempah!")

        if (oldFile.exists()) oldFile.delete()

        call.respond(DataResponse("success", "Berhasil menghapus data rempah", null))
    }

    // Mengambil gambar rempah
    suspend fun getSpiceImage(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest)

        val spice = spiceRepository.getSpiceById(id)
            ?: return call.respond(HttpStatusCode.NotFound)

        val file = File(spice.pathGambar)
        if (!file.exists()) return call.respond(HttpStatusCode.NotFound)

        call.respondFile(file)
    }
}