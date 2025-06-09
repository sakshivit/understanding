package org.example;

package com.chubb.na.domain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import com.chubb.na.domain.data.product.dto.MultipartBody;
import com.chubb.na.domain.data.product.restclient.impl.AdlsAPIAccessor;

/**
 * This Class is added to test ADLS upload and download functionality.
 */
@ApplicationScoped
public class Processor {

    private static final Logger logger = Logger.getLogger(Processor.class);

    @Inject
    AdlsAPIAccessor AdlsAPIAccessor;

    public void validateFileDownload() {
        downloadFileFrmAdls();
    }

    public void validateFileDownloadJson() {
        downloadFileFrmAdlsJson();
    }

    public void validateFileUpload() {
        uploadFileToAdls();
    }

    private void uploadFileToAdls() {
        MultipartBody body = new MultipartBody();
        String str = "{\"name\":\"AAA\",\"address\":\"add\",\"state\":\"st\",\"city\":\"city\"}";
        body.files = str.getBytes();
        body.eventSchema = "{\r\n"
                + "    \"source_context\": {\r\n"
                + "        \"submission_number\": \"Sub1234\",\r\n"
                + "        \"source_system_id\": \"10\",\r\n"
                + "        \"source_reference_number\": \"SourceNumber\",\r\n"
                + "        \"channel\": \"20\"\r\n"
                + "    },\r\n"
                + "    \"ingestion_context\": {\r\n"
                + "        \"use_case_id\": \"10\",\r\n"
                + "        \"status\": \"20\",\r\n"
                + "        \"stage\": \"400\",\r\n"
                + "        \"region\": \"21\",\r\n"
                + "        \"country\": \"US\",\r\n"
                + "        \"lob\": \"10\",\r\n"
                + "        \"product\": \"8901\",\r\n"
                + "        \"message\": \"Some message\",\r\n"
                + "        \"layer\": \"raw\",\r\n"
                + "        \"number_of_submitted_documents\": 4\r\n"
                + "    }\r\n"
                + "}";
        AdlsAPIAccessor.uploadFileToAdls(body);
        logger.info("---- File has been uploaded. ----");
    }

    private void downloadFileFrmAdlsJson() {
        byte[] fileByte = AdlsAPIAccessor.downloadFrmAdlsV1("https://stornau2dbsn001994102.dfs.core.windows.net/sit/Sub03192024-1%2Fraw%2F400%2FNGDS.json.gz");
        File outputFile = new File("test.json");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(fileByte);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("---- File has been downloaded. ----");
    }

    private void downloadFileFrmAdls() {
        byte[] fileByte = AdlsAPIAccessor.downloadFrmAdlsV1("sit/10-1708119116756-4FupyEDcPeWA-1/raw/400/10-1708119116756-4FupyEDcPeWA-1.eml.gz");
        File outputFile = new File("adlf-new.eml");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(fileByte);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("---- File has been downloaded. ----");
    }

    /** Need to test this method once get access to ADLS **/
    public byte[] convertFileToByteArray(String content) throws IOException {
        String fileName = "file_" + System.currentTimeMillis() + ".json";
        File file = new File(fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return Files.readAllBytes(file.toPath());
    }
}
