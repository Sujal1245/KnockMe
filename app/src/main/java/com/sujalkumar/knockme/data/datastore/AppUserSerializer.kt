package com.sujalkumar.knockme.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.sujalkumar.knockme.data.model.AppUser
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object AppUserSerializer : Serializer<AppUser?> {
    override val defaultValue: AppUser? = null

    override suspend fun readFrom(input: InputStream): AppUser? {
        try {
            val jsonString = input.bufferedReader().use { it.readText() }
            if (jsonString.isEmpty()) {
                return defaultValue
            }
            return Json.decodeFromString<AppUser>(jsonString)
        } catch (e: SerializationException) {
            throw CorruptionException("Cannot read AppUser from JSON. Error: ${e.message}", e)
        }
    }

    override suspend fun writeTo(t: AppUser?, output: OutputStream) {
        try {
            val jsonString = t?.let { Json.encodeToString(it) } ?: ""
            output.bufferedWriter().use { it.write(jsonString) }
        } catch (e: SerializationException) {
            throw CorruptionException("Cannot write AppUser to JSON. Error: ${e.message}", e)
        }
    }
}