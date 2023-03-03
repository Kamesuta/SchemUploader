package net.kunmc.lab.schemuploader;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

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
        Request request = new Request.Builder()
                .url(url)
                .build();

        // GETを実行
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
                // ファイルサイズが最大サイズを超えていたら失敗
                if (maxSize >= 0 && body.contentLength() > maxSize) {
                    result.exceededSize = true;
                    return result;
                }
                // ダウンロードしたファイルを保存
                boolean shouldDelete = false;
                try (BufferedSink sink = Okio.buffer(Okio.sink(file))) {
                    sink.writeAll(body.source());

                    // Schematicファイルかどうかをチェックする
                    if (!checkSchematic(sink.buffer())) {
                        result.error = "Schematicファイルではありません";
                        shouldDelete = true;
                        return result;
                    }

                    // 結果を構築して返す
                    result.success = true;
                    return result;
                } finally {
                    // ダウンロードに失敗したらファイルを削除する
                    if (shouldDelete) {
                        file.delete();
                    }
                }
            }
        } catch (IOException e) {
            result.error = String.format("IOエラー: %s", e.getMessage());
            return result;
        }
    }

    /**
     * Schematicファイルかどうかをチェックする
     *
     * @param source ファイルのソース
     * @return チェック結果
     */
    private static boolean checkSchematic(BufferedSource source) {
        // Schematicファイルかどうかをチェックする
        try (GZIPInputStream input = new GZIPInputStream(source.inputStream())) {
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
