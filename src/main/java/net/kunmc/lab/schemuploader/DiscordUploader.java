package net.kunmc.lab.schemuploader;

import com.google.gson.Gson;
import com.squareup.okhttp.*;

import java.io.File;
import java.io.IOException;
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

        // Webhookを使ってDiscordにアップロードする処理
        // POSTする内容を設定
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("payload_json", json)
                .addFormDataPart("files[0]", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();
        // POSTする先のURLを設定
        Request request = new Request.Builder()
                .url(PluginConfig.uploadWebhookUrl)
                .post(requestBody)
                .build();

        // POSTを実行
        Result result = new Result();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                result.error = String.format("HTTPステータスコード: %d", response.code());
                return result;
            }
            // レスポンスボディを取得
            try (ResponseBody body = response.body()) {
                if (body == null) {
                    result.error = "レスポンスボディなし (想定外のエラー)";
                    return result;
                }
                // レスポンスボディを表示
                WebhookResponse responseData = gson.fromJson(body.string(), WebhookResponse.class);
                if (responseData.attachments.length == 0) {
                    result.error = "添付ファイルなし (想定外のエラー)";
                    return result;
                }

                // 結果を構築して返す
                result.success = true;
                result.url = responseData.attachments[0].url;
                return result;
            }
        } catch (IOException e) {
            result.error = String.format("IOエラー: %s", e.getMessage());
            return result;
        }
    }
}
