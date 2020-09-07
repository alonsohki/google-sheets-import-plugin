# google-sheets-import-plugin
Maven plugin that automatically imports data from Google Sheets into the project.

Given a Google Spreadsheet ID, it will create one JSON file for each sheet of the document on the specified output path.
This plugin assumes that the spreadsheets are data grids where the first row are the column headers.

## Configuration
### Google API service account
In order to be able to access your spreadsheet, you need to create a Google APIs service account.
- Create a project at https://console.developers.google.com/apis/dashboard
- Make sure that the Google Sheets API is enabled by clicking on "Enable APIs and services"
- Go to Credentials -> Create credentials -> Service Account and create a new one.
- Once the service account has been created, create a new key and download the JSON file.
You can use this JSON file to configure the `googleApiCredentialsFile` configuration property of this plugin.

### Plugin setup
Include the following configuration into your `pom.xml` file:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.paf.maven</groupId>
            <artifactId>google-sheets-import-plugin</artifactId>
            <version>1.0</version>
            <executions>
                <execution>
                    <id>compile</id>
                    <phase>generate-resources</phase>
                    <goals>
                        <goal>import-sheets</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <outputDirectory>${project.build.directory}/resources</outputDirectory>
                <googleSheetId>${your_google_sheet_id}</googleSheetId>
                <googleApiCredentialsFile>${project.basedir}/credentials.json</googleApiCredentialsFile>
                <prettyPrint>true</prettyPrint>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Granting access to your spreadsheet
Make sure that your spreadsheet is either publicly visible or your service account has access to it. The Google APIs service accounts are created with an email address associated to it in the format `service-account-name@project-id.iam.gserviceaccount.com`. Sharing the document with that address with read-only permissions will suffice.
