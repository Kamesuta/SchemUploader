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

/**
 * Discordにファイルをアップロードするクラス
 */
public class DiscordUploader {
    /**
     * 結果
     */
    public static class Result {
        /**
         * アップロードに成功したかどうか
         */
        public boolean success;

        /**
         * ファイルのURL
         */
        public String url;

        /**
         * エラーメッセージ
         */
        public String error;
    }

    /**
     * Discordに送信するメッセージ
     */
    private static class WebhookPayload {
        /**
         * メッセージの内容
         */
        public String content;
    }

    /**
     * Discordからのレスポンス
     */
    private static class WebhookResponse {
        /**
         * 添付ファイルリスト
         */
        public Attachment[] attachments;

        /**
         * 添付ファイル
         */
        public static class Attachment {
            /**
             * ファイルのURL
             */
            public String url;
        }
    }

    /**
     * Gson
     */
    private static final Gson gson = new Gson();

    /**
     * Discordにファイルをアップロードする
     *
     * @param senderName 送信者の名前
     * @param senderUUID 送信者のUUID
     * @param file       アップロードするファイル
     * @param message    メッセージ
     * @return アップロード結果
     */
    public static Result upload(String senderName, UUID senderUUID, File file, String message) {
        // 文字列をサニタイズする
        String sanitizedFileName = file.getName().replaceAll("[^A-Za-z0-9_.]", "");

        // DiscordのWebhook用のJSONを作成
        WebhookPayload payload = new WebhookPayload();
        String msg = message == null ? "" : message + "\n";
        payload.content = String.format("%s`%s` が `%s` をアップロードしました。", msg, senderName, sanitizedFileName);
        String json = gson.toJson(payload);

        // POSTを実行
        Result result = new Result();
        try {
            // マルチパートフォームデータの境界
            String boundary = "--------------------------" + System.currentTimeMillis();

            // Webhookを使ってDiscordにアップロードする処理
            // POSTする内容を設定
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PluginConfig.uploadWebhookUrl))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(ofMimeMultipartData(file, json, boundary))
                    .build();

            // HttpClientを作成
            HttpClient client = HttpClient.newHttpClient();
            // リクエストを送信し、レスポンスを受け取る
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // レスポンスのステータスコードが200でなければエラー
            if (response.statusCode() != 200) {
                result.error = String.format("HTTPステータスコード: %d", response.statusCode());
                return result;
            }

            // レスポンスボディを取得
            WebhookResponse responseData = gson.fromJson(response.body(), WebhookResponse.class);
            if (responseData.attachments.length == 0) {
                result.error = "添付ファイルなし (想定外のエラー)";
                return result;
            }

            // 結果を構築して返す
            result.success = true;
            result.url = responseData.attachments[0].url;
            return result;
        } catch (IOException | InterruptedException e) {
            result.error = String.format("IOエラー: %s", e.getMessage());
            return result;
        }
    }

    // マルチパートフォームデータを作成
    private static HttpRequest.BodyPublisher ofMimeMultipartData(File file, String json, String boundary) throws IOException {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8);

        // payload_jsonパート
        byteArrays.add(separator);
        byteArrays.add("\"payload_json\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        byteArrays.add(json.getBytes(StandardCharsets.UTF_8));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));

        // files[0]パート
        byteArrays.add(separator);
        byteArrays.add("\"files[0]\";filename=\"".getBytes(StandardCharsets.UTF_8));
        byteArrays.add(file.getName().getBytes(StandardCharsets.UTF_8));
        byteArrays.add("\"\r\nContent-Type: application/octet-stream\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        byteArrays.add(Files.readAllBytes(file.toPath()));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));

        // 最後の境界
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}
