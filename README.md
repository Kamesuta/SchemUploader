# SchemUploader
![LogoArt](https://i.gyazo.com/b5b852f4e74c7d76377c7d5fb37ba17f.png)  
[![License: MIT](https://img.shields.io/github/license/Kamesuta/BungeePteroPower?label=License)](LICENSE)
[![Spigotmc Version](https://img.shields.io/spiget/version/115170?logo=spigotmc&label=Spigotmc%20Version)](https://www.spigotmc.org/resources/schemuploader-transfers-your-schematic-data-across-servers-with-discord.115170/)
[![Spigotmc Downloads](https://img.shields.io/spiget/downloads/115170?logo=spigotmc&label=Spigotmc%20Downloads)](https://www.spigotmc.org/resources/schemuploader-transfers-your-schematic-data-across-servers-with-discord.115170/)
[![bStats Servers](https://img.shields.io/bstats/servers/21061?label=bStats%20Servers)](https://bstats.org/plugin/bukkit/SchemUploader/21061)  

Allows you to upload/download WorldEdit schem files to/from Discord.  

## Usage

- Upload
`/schem_upload <schem_name> [message]`
- Download
`/schem_download <schem_name> <schem_file_URL> [-f]`
![Video](https://i.gyazo.com/98c1e6c2565a580b6b3d1017228fb513.gif)

### How to get schem file URL
Right-click on the attached file and click "Copy link"  
![How to Get Link](https://i.gyazo.com/ded5a89d3618424bc29ecbf6a270bac2.png)

## Requirements

- Java 11 or higher
  - Required for performing HTTP communication using Java 11 features.

## Configuration

This plugin requires configuration in the config.yml file upon installation.

### Minimum required settings

- `upload.webhook-url`: URL of the webhook for uploading. After registering a webhook in the channel settings, you can obtain this URL.

### Restricting download URLs

You can restrict downloads to specific Discord channels or sites by enabling this feature.

- `download.url-restriction.enabled`: Enable the feature
  - Set to true.
- `download.url-restriction.prefix`: Prefix for download URLs
  - By specifying `https://cdn.discordapp.com/attachments/<channel_ID>/`, you can restrict downloads to specific Discord channels.
- `download.url-restriction.name`: Name of the download location
  - This name is used in error messages when downloading from locations other than the specified prefix.
  - If set to `#schem-storage`, the error message will display as "You can only download schematic files from the '#schem-storage' channel within Discord".

### Full configuration
```yaml
# Language
language: en
# Upload settings
upload:
  # Enable upload functionality
  enabled: true
  # Discord Webhook URL for uploads
  webhook-url: https://discord.com/api/webhooks/xxxxxxxxxxxxxxxxxx/xxxxxxxxxxxxxxxxxxxxxxxxxxxx-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
# Download settings
download:
  # Enable download functionality
  enabled: true
  # Maximum size of downloaded files (in bytes) (-1 for unlimited)
  max-size: 8388608 # 8MB
  # Download source restriction settings
  url-restriction:
    # Enable download source restriction
    enabled: false
    # Prefix for URL restrictions (Note: For Discord attachment URLs, make sure it starts with cdn. and ends with /)
    # Usually, you just need to replace the xxxxxxxxxxxxxxxxxx part with your own channel ID
    prefix: https://cdn.discordapp.com/attachments/xxxxxxxxxxxxxxxxxx/
    # The name of the download location (displayed in the error message)
    # This name is used in error messages when downloading from locations other than the specified prefix.
    # If set to #schem-storage, the error message will display as "You can only download schematic files from the '#schem-storage' channel within Discord"
    name: "#schem-storage"
file:
  # Path to the schematics folder
  folder-path: plugins/WorldEdit/schematics
```

## Permissions

- `schemuploader.upload`: Permission to use the schem file upload feature
- `schemuploader.download`: Permission to use the schem file download feature

## About Statistics Data

SchemUploader collects anonymous statistical data using [bStats](https://bstats.org/).  
You can find the statistics data [here](https://bstats.org/plugin/bukkit/SchemUploader/21061).

bStats is used to understand the usage of the plugin and help improve it.  
To disable the collection of statistical data, please set `enabled` to `false` in `plugins/bStats/config.yml`
