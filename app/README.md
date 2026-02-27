# Internal Test Application

### Android SDK Environment Configuration

The Android SDK allows for a custom region configuration where the app can define a specific URL.
This feature is designed to support custom lower-environment URLs during development and testing.

### Stage Regions

In addition to custom URLs, we support standard stage region formats: `stage0`, `stage1`, and
`stage2`.

Historically, these were mapped to the following base URL template:
`https://${configuration.apptentiveKey}.api.use1.digital.${configuration.region.value}.alc-eng.com`

### Security Update

To keep our infrastructure internal, the base stage URL template has been removed from the source
code. Developers must now use System Variables on their local machines to support stage regions.

### Configuration Instructions

To enable stage region support, you must set the `INTERNAL_BASE_URL_OVERRIDE` environment variable.

#### On Windows:

1. Open **System Properties -> Environment Variables -> System Variables**
2. Under **System Variables**, click **New**
3. Enter the following: \
   variable: `INTERNAL_BASE_URL_OVERRIDE`\
   value: `https://<APPTENTIVE_KEY>.api.use1.digital.<REGION>.alc-eng.com`\
4. Restart Android Studio

#### On macOS

1. Open your terminal and edit your zsh configuration:
   ```nano ~/.zshrc```
2. Add the following line to the file:
   ```export INTERNAL_BASE_URL_OVERRIDE="https://<APPTENTIVE_KEY>.api.use1.digital.<REGION>.alc-eng.com"```
3. Save the file and apply the changes:
   ```source ~/.zshrc```
4. Restart Android Studio
