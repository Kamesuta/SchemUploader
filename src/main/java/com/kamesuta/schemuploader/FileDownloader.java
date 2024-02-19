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
 * ファイルをダウンロードするクラス
 */
public class FileDownloader {
    /**
     * 結果
     */
    public static class Result {
        /**
         * ダウンロードに成功したかどうか
         */
        public boolean success;

        /**
         * サイズが大きすぎるかどうか
         */
        public boolean exceededSize;

        /**
         * エラーメッセージ
         */
        public String error;
    }

    /**
     * ファイルをダウンロードする
     *
     * @param file    ダウンロードしたファイルを保存するファイル
     * @param url     ダウンロードするファイルのURL
     * @param maxSize ダウンロードするファイルの最大サイズ (-1で無制限)
     * @return ダウンロードに成功したかどうか
     */
    public static Result download(File file, String url, long maxSize) {
        // URLからファイルをダウンロードする処理
        // GETする先のURLを設定
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        // GETを実行
        Result result = new Result();
        HttpClient client = HttpClient.newHttpClient();
        try {
            // リクエストを送信し、レスポンスを受け取る
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            // レスポンスのステータスコードが200でなければエラー
            if (response.statusCode() != 200) {
                result.error = plugin.messages.getMessage("error_http_status", response.statusCode());
                return result;
            }

            // レスポンスヘッダーからContent-Lengthを取得
            var contentLength = response.headers().firstValue("Content-Length");
            // Content-Lengthが存在しない場合は-1にする
            var size = contentLength.map(Long::parseLong).orElse(-1L);

            // ファイルサイズが最大サイズを超えていたら失敗
            if (maxSize >= 0 && size > maxSize) {
                result.exceededSize = true;
                return result;
            }

            // レスポンスボディを取得
            byte[] data;
            try (InputStream body = response.body()) {
                // 一旦全部メモリに読み込む
                data = body.readAllBytes();
            }

            // Schematicファイルかどうかをチェックする
            if (!checkSchematic(data)) {
                result.error = plugin.messages.getMessage("error_not_schematic");
                return result;
            }

            // メモリ上のデータをファイルに書き込む
            Files.copy(new ByteArrayInputStream(data), file.toPath());

            // 結果を構築して返す
            result.success = true;
            return result;
        } catch (IOException | InterruptedException e) {
            result.error = plugin.messages.getMessage("error_io", e.getMessage());
            return result;
        }
    }

    /**
     * Schematicファイルかどうかをチェックする
     *
     * @param data ファイルのソース
     * @return チェック結果
     */
    private static boolean checkSchematic(byte[] data) {
        // Schematicファイルかどうかをチェックする
        try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(data))) {
            // ヘッダーを読み込む
            byte[] header = new byte[12];
            byte[] expected = {0x0a, 0x00, 0x09, 'S', 'c', 'h', 'e', 'm', 'a', 't', 'i', 'c'};
            // ヘッダーが一致しなかったら失敗
            if (input.read(header) < expected.length || !Arrays.equals(header, expected)) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
