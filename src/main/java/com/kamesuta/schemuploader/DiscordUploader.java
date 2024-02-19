package com.kamesuta.schemuploader;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;

import static com.kamesuta.schemuploader.SchemUploader.plugin;

/**
 * Class for uploading files to Discord
 */
public class DiscordUploader {
    /**
     * Result
     */
    public static class Result {
        /**
         * Whether the upload was successful or not
         */
        public boolean success;

        /**
         * URL of the file
         */
        public String url;

        /**
         * Error message
         */
        public String error;
    }

    /**
     * Message to send to Discord
     */
    private static class WebhookPayload {
        /**
         * Content of the message
         */
        public String content;
    }

    /**
     * Response from Discord
     */
    private static class WebhookResponse {
        /**
         * Attachment list
         */
        public Attachment[] attachments;

        /**
         * Attachment
         */
        public static class Attachment {
            /**
             * URL of the file
             */
            public String url;
        }
    }

    /**
     * Gson
     */
    private static final Gson gson = new Gson();

    /**
     * Upload a file to Discord
     *
     * @param senderName Name of the sender
     * @param senderUUID UUID of the sender
     * @param file       File to upload
     * @param message    Message
     * @return Upload result
     */
    public static Result upload(String senderName, UUID senderUUID, File file, String message) {
        // Sanitize the string
        String sanitizedFileName = file.getName().replaceAll("[^A-Za-z0-9_.]", "");

        // Create JSON for Discord webhook
        WebhookPayload payload = new WebhookPayload();
        String msg = message == null ? "" : message + "\n";
        payload.content = plugin.messages.getMessage("upload_message", msg, senderName, senderUUID, sanitizedFileName);
        String json = gson.toJson(payload);

        // Execute POST
        Result result = new Result();
        try {
            // Multipart form data boundary
            String boundary = "--------------------------" + System.currentTimeMillis();

            // Upload to Discord using webhook
            // Set the content to POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PluginConfig.uploadWebhookUrl))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(ofMimeMultipartData(file, json, boundary))
                    .build();

            // Create HttpClient
            HttpClient client = HttpClient.newHttpClient();
            // Send the request and receive the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If the response status code is not 200, there is an error
            if (response.statusCode() != 200) {
                result.error = plugin.messages.getMessage("error_http_status", response.statusCode());
                return result;
            }

            // Get the response body
            WebhookResponse responseData = gson.fromJson(response.body(), WebhookResponse.class);
            if (responseData.attachments.length == 0) {
                result.error = "No attachments (unexpected error)";
                return result;
            }

            // Build and return the result
            result.success = true;
            result.url = responseData.attachments[0].url;
            return result;
        } catch (IOException | InterruptedException e) {
            result.error = plugin.messages.getMessage("error_io", e.getMessage());
            return result;
        }
    }

    // Create multipart form data
    private static HttpRequest.BodyPublisher ofMimeMultipartData(File file, String json, String boundary) throws IOException {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8);

        // payload_json part
        byteArrays.add(separator);
        byteArrays.add("\"payload_json\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        byteArrays.add(json.getBytes(StandardCharsets.UTF_8));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));

        // files[0] part
        byteArrays.add(separator);
        byteArrays.add("\"files[0]\";filename=\"".getBytes(StandardCharsets.UTF_8));
        byteArrays.add(file.getName().getBytes(StandardCharsets.UTF_8));
        byteArrays.add("\"\r\nContent-Type: application/octet-stream\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        byteArrays.add(Files.readAllBytes(file.toPath()));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));

        // Last boundary
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}
