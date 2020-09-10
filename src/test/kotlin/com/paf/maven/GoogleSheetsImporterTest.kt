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

import com.google.api.client.googleapis.testing.auth.oauth2.MockGoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.LowLevelHttpRequest
import com.google.api.client.http.LowLevelHttpResponse
import com.google.api.client.testing.http.MockHttpTransport
import com.google.api.client.testing.http.MockLowLevelHttpRequest
import com.google.api.client.testing.http.MockLowLevelHttpResponse
import org.junit.Test
import kotlin.test.assertEquals


class GoogleSheetsImporterTest {
    @Test
    fun testImport() {
        val transport: HttpTransport = object : MockHttpTransport() {
            override fun buildRequest(method: String, url: String): LowLevelHttpRequest {
                return object : MockLowLevelHttpRequest() {
                    override fun execute(): LowLevelHttpResponse {
                        val result = MockLowLevelHttpResponse()
                        result.setContent(when (url) {
                            "https://sheets.googleapis.com/v4/spreadsheets/abcd" -> "{\"properties\":{\"autoRecalc\":\"ON_CHANGE\",\"defaultFormat\":{\"backgroundColor\":{\"blue\":1.0,\"green\":1.0,\"red\":1.0},\"backgroundColorStyle\":{\"rgbColor\":{\"blue\":1.0,\"green\":1.0,\"red\":1.0}},\"padding\":{\"bottom\":2,\"left\":3,\"right\":3,\"top\":2},\"textFormat\":{\"bold\":false,\"fontFamily\":\"arial,sans,sans-serif\",\"fontSize\":10,\"foregroundColor\":{},\"foregroundColorStyle\":{\"rgbColor\":{}},\"italic\":false,\"strikethrough\":false,\"underline\":false},\"verticalAlignment\":\"BOTTOM\",\"wrapStrategy\":\"OVERFLOW_CELL\"},\"locale\":\"en_US\",\"spreadsheetTheme\":{\"primaryFontFamily\":\"Arial\",\"themeColors\":[{\"color\":{\"rgbColor\":{\"blue\":1.0,\"green\":1.0,\"red\":1.0}},\"colorType\":\"BACKGROUND\"},{\"color\":{\"rgbColor\":{\"blue\":0.003921569,\"green\":0.42745098,\"red\":1.0}},\"colorType\":\"ACCENT5\"},{\"color\":{\"rgbColor\":{}},\"colorType\":\"TEXT\"},{\"color\":{\"rgbColor\":{\"blue\":0.20784314,\"green\":0.2627451,\"red\":0.91764706}},\"colorType\":\"ACCENT2\"},{\"color\":{\"rgbColor\":{\"blue\":0.7764706,\"green\":0.7411765,\"red\":0.27450982}},\"colorType\":\"ACCENT6\"},{\"color\":{\"rgbColor\":{\"blue\":0.95686275,\"green\":0.52156866,\"red\":0.25882354}},\"colorType\":\"ACCENT1\"},{\"color\":{\"rgbColor\":{\"blue\":0.3254902,\"green\":0.65882355,\"red\":0.20392157}},\"colorType\":\"ACCENT4\"},{\"color\":{\"rgbColor\":{\"blue\":0.015686275,\"green\":0.7372549,\"red\":0.9843137}},\"colorType\":\"ACCENT3\"},{\"color\":{\"rgbColor\":{\"blue\":0.8,\"green\":0.33333334,\"red\":0.06666667}},\"colorType\":\"LINK\"}]},\"timeZone\":\"Europe/Helsinki\",\"title\":\"Test spreadsheet\"},\"sheets\":[{\"properties\":{\"gridProperties\":{\"columnCount\":26,\"rowCount\":1000},\"index\":0,\"sheetId\":0,\"sheetType\":\"GRID\",\"title\":\"Capitals\"}},{\"properties\":{\"gridProperties\":{\"columnCount\":26,\"rowCount\":1000},\"index\":1,\"sheetId\":1269572715,\"sheetType\":\"GRID\",\"title\":\"Translations\"}}],\"spreadsheetId\":\"1ctJhzmb0BYxGfAhumU_TErBQFKmei3qquqevEruRDeo\",\"spreadsheetUrl\":\"https://docs.google.com/spreadsheets/d/1ctJhzmb0BYxGfAhumU_TErBQFKmei3qquqevEruRDeo/edit\"}"
                            "https://sheets.googleapis.com/v4/spreadsheets/abcd/values/Capitals" -> "{\"majorDimension\":\"ROWS\",\"range\":\"Capitals!A1:Z1000\",\"values\":[[\"Country\",\"Capital\"],[\"Spain\",\"Madrid\"],[\"Finland\",\"Helsinki\"],[\"Sweden\",\"Stockholm\"],[\"France\",\"Paris\"]]}"
                            "https://sheets.googleapis.com/v4/spreadsheets/abcd/values/Translations" -> "{\"majorDimension\":\"ROWS\",\"range\":\"Translations!A1:Z1000\",\"values\":[[\"Key\",\"EN\",\"ES\",\"FI\"],[\"apple\",\"Apple\",\"Manzana\",\"Omena\"],[\"orange\",\"Orange\",\"Naranja\",\"Oranssi\"],[\"strawberry\",\"Strawberry\",\"Fresa\",\"Mansikka\"]]}"
                            else -> ""
                        })
                        return result
                    }
                }
            }
        }

        val credentials = MockGoogleCredential(MockGoogleCredential.Builder())

        val importedData = GoogleSheetsImporter.import("abcd", transport, credentials)
        val expectation = mapOf(
                "Capitals" to listOf(
                        mapOf("Country" to "Spain", "Capital" to "Madrid"),
                        mapOf("Country" to "Finland", "Capital" to "Helsinki"),
                        mapOf("Country" to "Sweden", "Capital" to "Stockholm"),
                        mapOf("Country" to "France", "Capital" to "Paris")
                ),
                "Translations" to listOf(
                        mapOf("Key" to "apple", "EN" to "Apple", "ES" to "Manzana", "FI" to "Omena"),
                        mapOf("Key" to "orange", "EN" to "Orange", "ES" to "Naranja", "FI" to "Oranssi"),
                        mapOf("Key" to "strawberry", "EN" to "Strawberry", "ES" to "Fresa", "FI" to "Mansikka")
                )
        )
        assertEquals(expectation, importedData)
    }
}