package com.paf.maven

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

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

@Mojo(name = "import-sheets", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
class Plugin : AbstractMojo() {
    /**
     * Target directory for the generated JSON files.
     */
    @Parameter(name = "outputDirectory", required = true)
    private val outputDirectory: File? = null

    /**
     * ID of your Google Sheet. When you open your Google Sheet, this ID is that
     * part of the URL that looks like an ID.
     */
    @Parameter(name = "googleSheetId", required = true)
    private val googleSheetId: String? = null

    /**
     * Path to a credentials.json file containing the credentials required to use the
     * Google API. For more information about how to generate this file, refer to README.md.
     */
    @Parameter(name = "googleApiCredentialsFile")
    private val googleApiCredentialsFile: File? = null

    /**
     * Set to true if you want the output JSON to be well formatted.
     */
    @Parameter(name = "prettyPrint")
    private val prettyPrint: Boolean? = null

    @Throws(MojoExecutionException::class)
    override fun execute() {
        val targetDir = outputDirectory ?: error("Missing output directory. Configure it with the <outputDirectory> configuration option")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val sheetId = googleSheetId ?: error("Missing Google Sheet ID. Configure it with the <googleSheetId> configuration option")
        val credentials = googleApiCredentialsFile ?: error("Missing Google API credentials file location. Configure it with the <googleApiCredentialsFile> configuration option")
        val usePrettyPrint = prettyPrint ?: false

        val objectMapper = ObjectMapper()
        val writer = when (usePrettyPrint) {
            true -> objectMapper.writer(DefaultPrettyPrinter())
            false -> objectMapper.writer()
        }

        log.info("Parsing Google Sheet with id=${sheetId} using credentials from ${credentials}")

        val data = GoogleSheetsImporter.import(sheetId, credentials.inputStream())
        data.forEach {
            val file = File(targetDir, "${it.key}.json")
            if (file.exists()) {
                file.delete()
            }

            log.info("Saving sheet '${it.key}' to file ${file}")
            writer.writeValue(file, it.value)
        }
    }
}