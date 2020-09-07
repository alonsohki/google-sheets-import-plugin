package com.paf.maven

import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.File
import java.io.InputStream

class GoogleSheetsImporter {
    companion object {
        fun import(sheetId: String, credentialsPath: String) =
                import(sheetId, File(credentialsPath).inputStream())

        fun import(sheetId: String, credentialsStream: InputStream) =
                import(sheetId,
                        NetHttpTransport(),
                        getCredentials(credentialsStream)
                )

        fun import(sheetId: String,
                   transport: HttpTransport,
                   requestInitializer: HttpRequestInitializer
        ): Map<String, List<Map<String, Any>>> {
            val api = getSheetsAPI(transport, requestInitializer)
            val valuesApi = api.spreadsheets().values()
            val document = api.spreadsheets().get(sheetId).execute()

            return document.sheets
                    .map {
                        val properties = it.properties
                        val valuesData = valuesApi.get(sheetId, properties.title).execute()
                        val values = valuesData.getValues()
                        val headers = values.first().map { header -> header.toString() }
                        val rows = values.drop(1) as List<List<Any>>
                        val zipped = rows.map { row -> headers.zip(row).toMap() }
                        Pair(properties.title, zipped)
                    }
                    .toMap()
        }

        private fun getCredentials(credentialsStream: InputStream) =
            HttpCredentialsAdapter(
                    GoogleCredentials
                    .fromStream(credentialsStream)
                    .createScoped(listOf(SheetsScopes.SPREADSHEETS_READONLY))
            )

        private fun getSheetsAPI(transport: HttpTransport, requestInitializer: HttpRequestInitializer): Sheets {
            return Sheets
                    .Builder(transport, JacksonFactory(), requestInitializer)
                    .setApplicationName("paf-google-sheets-importer")
                    .build()
        }
    }
}
