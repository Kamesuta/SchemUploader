package com.kamesuta.schemuploader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import static com.kamesuta.schemuploader.SchemUploader.plugin;

/**
 * Class for downloading files
 */
public class FileDownloader {
    /**
     * Result
     */
    public static class Result {
        /**
         * Whether the download was successful or not
         */
        public boolean success;

        /**
         * Whether the size exceeded the limit or not
         */
        public boolean exceededSize;

        /**
         * Error message
         */
        public String error;
    }

    /**
     * Download a file
     *
     * @param file    The file to save the downloaded file
     * @param url     The URL of the file to download
     * @param maxSize The maximum size of the file to download (-1 for unlimited)
     * @return Whether the download was successful or not
     */
    public static Result download(File file, String url, long maxSize) {
        // Code for downloading a file from a URL
        // Set the URL to GET
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        // Execute GET
        Result result = new Result();
        HttpClient client = HttpClient.newHttpClient();
        try {
            // Send the request and receive the response
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            // If the response status code is not 200, it's an error
            if (response.statusCode() != 200) {
                result.error = plugin.messages.getMessage("error_http_status", response.statusCode());
                return result;
            }

            // Get the Content-Length from the response headers
            var contentLength = response.headers().firstValue("Content-Length");
            // Set it to -1 if Content-Length doesn't exist
            var size = contentLength.map(Long::parseLong).orElse(-1L);

            // If the file size exceeds the maximum size, it's a failure
            if (maxSize >= 0 && size > maxSize) {
                result.exceededSize = true;
                return result;
            }

            // Get the response body
            byte[] data;
            try (InputStream body = response.body()) {
                // Read the entire body into memory
                data = body.readAllBytes();
            }

            // Check if it's a Schematic file
            if (!checkSchematic(data)) {
                result.error = plugin.messages.getMessage("error_not_schematic");
                return result;
            }

            // Write the data in memory to the file
            Files.copy(new ByteArrayInputStream(data), file.toPath());

            // Build and return the result
            result.success = true;
            return result;
        } catch (IOException | InterruptedException e) {
            result.error = plugin.messages.getMessage("error_io", e.getMessage());
            return result;
        }
    }

    /**
     * Check if it's a Schematic file
     *
     * @param data The source of the file
     * @return The check result
     */
    private static boolean checkSchematic(byte[] data) {
        // Check if it's a Schematic file
        try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(data))) {
            // Read the header
            byte[] header = new byte[12];
            byte[] expected = {0x0a, 0x00, 0x09, 'S', 'c', 'h', 'e', 'm', 'a', 't', 'i', 'c'};
            // If the header doesn't match, it's a failure
            if (input.read(header) < expected.length || !Arrays.equals(header, expected)) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
