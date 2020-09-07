package com.paf.maven

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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