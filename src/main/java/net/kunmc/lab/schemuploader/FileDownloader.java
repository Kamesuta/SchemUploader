package net.kunmc.lab.schemuploader;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

import java.io.File;
import java.io.IOException;

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
                try (BufferedSink sink = Okio.buffer(Okio.sink(file))) {
                    sink.writeAll(body.source());

                    // 結果を構築して返す
                    result.success = true;
                    return result;
                }
            }
        } catch (IOException e) {
            result.error = String.format("IOエラー: %s", e.getMessage());
            return result;
        }
    }
}
