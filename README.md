# SchemUploader

Allows you to upload/download WorldEdit schem files to/from Discord.
![2023-03-01_15h05_33](https://user-images.githubusercontent.com/16362824/222058389-8fa598f7-990a-45ad-bb42-8a78609e9212.png)

## Usage

- Upload
`/schem_upload <schem_name> [message]`
- Download
`/schem_download <schem_name> <schem_file_URL> [-f]`

### How to get schem file URL
Right-click on the attached file and click "Copy link"
![2023-03-01_15h06_38](https://user-images.githubusercontent.com/16362824/222058621-5f81e3fd-d3e1-408c-ae4b-41366c481684.png)

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
  - If set to `#schem-storage`, the error message will display as "You can only download files from #schem-storage channel".

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
file:
  # Path to the schematics folder
  folder-path: plugins/WorldEdit/schematics
```

## Permissions

- `schemuploader.upload`: Permission to use the schem file upload feature
- `schemuploader.download`: Permission to use the schem file download feature
