# SchemUploader
![LogoArt](https://i.gyazo.com/b5b852f4e74c7d76377c7d5fb37ba17f.png)  
[![License: MIT](https://img.shields.io/github/license/Kamesuta/BungeePteroPower?label=License)](LICENSE)
[![Spigotmc Version](https://img.shields.io/spiget/version/115170?logo=spigotmc&label=Spigotmc%20Version)](https://www.spigotmc.org/resources/schemuploader-transfers-your-schematic-data-across-servers-with-discord.115170/)
[![Spigotmc Downloads](https://img.shields.io/spiget/downloads/115170?logo=spigotmc&label=Spigotmc%20Downloads)](https://www.spigotmc.org/resources/schemuploader-transfers-your-schematic-data-across-servers-with-discord.115170/)
[![bStats Servers](https://img.shields.io/bstats/servers/21061?label=bStats%20Servers)](https://bstats.org/plugin/bukkit/SchemUploader/21061)  

WorldEditのschemファイルをDiscordにアップロード/ダウンロードできるようにします。

## 使い方

- アップロード
`/schem_upload <schem名> [メッセージ]`
- ダウンロード
`/schem_download <schem名> <schemファイルのURL> [-f]`
![Video](https://i.gyazo.com/98c1e6c2565a580b6b3d1017228fb513.gif)

### schemファイルのURL取得方法
添付ファイルを右クリックし、「リンクをコピー」をクリックします  
![How to Get Link](https://i.gyazo.com/ded5a89d3618424bc29ecbf6a270bac2.png)

## 動作環境

- Java11以上
  - Java11の機能を使用してHTTP通信を行うため必要です。

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
  - `#schem置き場` と設定した場合、`Discord内の「#schem置き場」チャンネルにあるschematicファイルのみダウンロードできます` とエラーメッセージが表示されます。

### 全設定
```yaml
# 言語
language: en
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
    enabled: false
    # ダウンロード用のURL制限のプレフィックス (Discordの添付ファイルURLの場合、cdn.から始まっていること、末尾に/を付けることに注意する)
    # 基本的に↓の xxxxxxxxxxxxxxxxxx の部分を自分のチャンネルIDに変えるだけでOK
    prefix: https://cdn.discordapp.com/attachments/xxxxxxxxxxxxxxxxxx/
    # ダウンロード用のURL制限場所の名前 (エラー時に案内が表示される)
    # 上記のプレフィックス以外からダウンロードした場合に表示されるエラーにnameが使用されます。
    # `#schem置き場` と設定した場合、`#schem置き場 にあるファイルのみダウンロードできます` とエラーメッセージが表示されます。
    name: #schem置き場
```

## 権限

- `schemuploader.upload`: schemファイルのアップロード機能を使用する権限
- `schemuploader.download`: schemファイルのダウンロード機能を使用する権限

## 統計データについて

SchemUploaderは、[bStats](https://bstats.org/)を使用して匿名の統計データを収集しています。  
統計データは[こちら](https://bstats.org/plugin/bukkit/SchemUploader/21061)。

bStatsは、プラグインの使用状況を把握するために使用され、プラグインの改善に役立てられます。  
統計データの収集を無効にするには、`plugins/bStats/config.yml`の `enabled` を `false` に設定してください。
