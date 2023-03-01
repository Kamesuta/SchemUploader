# SchemUploader

WorldEditのschemファイルをDiscordにアップロード/ダウンロードできるようにします。
![2023-03-01_15h05_33](https://user-images.githubusercontent.com/16362824/222058389-8fa598f7-990a-45ad-bb42-8a78609e9212.png)

## 使い方

- アップロード
`/schem_upload <schem名> [メッセージ]`
- ダウンロード
`/schem_download <schem名> <schemファイルのURL>`

### schemファイルのURL取得方法
添付ファイルを右クリックし、「リンクをコピー」をクリックします  
![2023-03-01_15h06_38](https://user-images.githubusercontent.com/16362824/222058621-5f81e3fd-d3e1-408c-ae4b-41366c481684.png)

## 設定

このプラグインは導入時にconfig.ymlの設定が必要です。

### 最低限必要な設定

- `upload.webhook-url`: アップロード用のWebhookのURLです。チャンネル設定からWebhookを登録後、取得できます。

### ダウンロード元のURLを制限する場合
この機能をONにすることで、指定したDiscordチャンネル、または指定したサイトからのみダウンロードさせるといった事ができます。

- `download.url-restriction.enabled`: 機能の有効化
  - true にしてください。
- `download.url-restriction.prefix`: ダウンロードするURLのプレフィックス
  - `https://cdn.discordapp.com/attachments/<チャンネルID>/` を指定すれば特定のDiscordチャンネルのみからダウンロードさせることができます。
- `download.url-restriction.name`: ダウンロード場所の名前
  - 上記のプレフィックス以外からダウンロードした場合に表示されるエラーにnameが使用されます。
  - `#schem置き場` と設定した場合、`#schem置き場 にあるファイルのみダウンロードできます` とエラーメッセージが表示されます。

### 全設定
```yaml
# アップロード用設定
upload:
  # アップロード機能の有効化
  enabled: true
  # アップロード用のDiscord Webhook URL
  webhook-url: https://discord.com/api/webhooks/xxxxxxxxxxxxxxxxxx/xxxxxxxxxxxxxxxxxxxxxxxxxxxx-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
# ダウンロード用設定
download:
  # ダウンロード機能の有効化
  enabled: true
  # ダウンロードファイルの最大サイズ (バイト) (-1で無制限)
  max-size: 8388608 # 8MB
  # ダウンロード元制限機能設定
  url-restriction:
    # ダウンロード元制限機能の有効化
    enabled: true
    # ダウンロード用のURL制限のプレフィックス (Discordの添付ファイルURLの場合、cdn.から始まっていること、末尾に/を付けることに注意する)
    # 基本的に↓の xxxxxxxxxxxxxxxxxx の部分を自分のチャンネルIDに変えるだけでOK
    prefix: https://cdn.discordapp.com/attachments/xxxxxxxxxxxxxxxxxx/
    # ダウンロード用のURL制限場所の名前 (エラー時に案内が表示される)
    name: Discord内の「#schem置き場」チャンネル
```
