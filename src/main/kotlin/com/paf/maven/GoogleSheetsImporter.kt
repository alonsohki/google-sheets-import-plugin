/*
 * Copyright 2020 Paf
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
