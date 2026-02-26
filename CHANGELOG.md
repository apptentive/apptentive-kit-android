# 2026-02-26 - v7.0.0
### Fixes
* Attachment rotation in Message Center now works reliably.
* Email validation in Message Center/Profile correctly handles top‑level domains.
* Edge‑to‑edge layout issues have been resolved for the apps targeting Android 15 & 16.
* Dependency provider refactor ensures providers remain available; the new reboot listener helps the SDK recover if in‑memory providers are lost.
* Unread message count is properly tracked on the multiuser environment

### Improvements
* Added ETag / If‑None‑Match support for manifest endpoints.
* Compatibility updates for Gradle 9.0 and 10.0.
* Brings the SDK into full compliance with Android 15 and 16, updating interaction flows and background operations accordingly.
* Accessibility enhancements for Surveys and Message Center. Improved focus behavior, color contrast, screen‑reader announcements.
* Logging improvements - conversations are logged only when changed, preventing large repetitive dumps.
* Cleanup of deprecated code.
* Minimum SDK raised to 24 to better support modern apps while still accommodating apps targeting 21–23.
* Removed unnecessary cached‑interactions check.
* Full screen mode support with local customization for top and bottom bars for Surveys & Message center.

### New Features
* Reboot listener for SDK recover ensures the SDK can automatically restart itself when in-memory providers are lost, improving stability after process death or low‑memory events.
* Allows routing traffic to region‑specific endpoints, reducing latency and comply with regional data‑handling requirements. Now supporting EU along with US
* Allows reduced event traffic through a configurable setting

# 2025-12-15 - v6.9.10
### Fixes
* Device custom data is now reliably preserved across version upgrades.
* In survey, single select questions ensures mutual exclusivity

### Improvements
* Dialog creation is now deferred until the decor view is created, resulting in more consistent and stable behavior.
* A new status endpoint has been introduced to track updates to interactions and targeting. The SDK checks this endpoint frequently, ensuring interactions are updated promptly without waiting for cache expiration.

# 2025-03-19 - v6.9.3
### Fixes
* Addressed a race condition when showing coupled interactions that are engaged from activities without supportFragmentManager 

# 2025-03-05 - v6.9.2
### Improvements
* Activities without supportFragmentManager now can engage modal interactions.
* Enhanced external keyboard accessibility experience

# 2024-11-20 - v6.9.1
### Fixes
* Device updates are now accurately captured in the conversation metadata.
* Support for Android 15's edge-to-edge feature implemented via Apptentive theme override.
* Updated WebView configuration settings to address a security vulnerability.
* Resolved the profile screen freezing issue.

# 2024-09-18 - v6.9.0
### Fixes
* Draft messages are now saved correctly in the multiuser environment
* Prevent logout calls from being made when the SDK is not logged in
* Upgrade GSON dependnecy to latest to fix the security vulnerability

### New Features
* Added initiator to the kit to support Alchemer Workflow initiation from the SDKs

# 2024-07-31 - v6.8.1

### Fixes
* Alchemer Survey file upload now working in in-app mode
* Alchemer Survey video sentiment is now working in in-app mode
* Alchemer Survey title will display as the survey header in in-app mode
* Cache the Message Center's input field values for the next time if not updated

### Improvements
* Upgraded SDK target to Android 14 (API level 34)
* Upgraded Gradle version to 8.0
* Prevent the SDK from crashing after the dependencies are garbage collected

# 2024-05-29 - v6.8.0
### New Features
* Advanced Customer Research support to show Alchemer long form surveys through prompts

# 2024-04-12 - v6.7.0
### New Features
* Added rich text support through dashboard for Prompts and Surveys

# 2024-03-25 - v6.6.0
#### New Features
* Added Image support through dashboard for Prompts(previously called Notes)

# 2023-12-5 - v6.5.1
#### Improvements
* Updated google play libraries to support Android 14

# 2023-11-14 - v6.5.0
#### New Features
* Implemented Customer Authentication features from the legacy SDK in the new SDK (See Android [Integration guide](https://learn.apptentive.com/knowledge-base/android-integration-guide/)). This allows apps with sensitive data to be shared among multiple users on a single device
* Added the ability to work with multiple app key/signature pairs without deleting and reinstalling

#### Improvements
* Launch and exit events are standardized for app launches and exits
* Send callback failure if the SDK is registered already
* Expose a method to find if the SDK is already registered
* Confirm that the SDK only makes HTTPS requests

#### Fixes
* Resolved validation issues for free form question type

# 2023-07-20 - v6.1.0
#### New Features
* Survey skip logic

#### Improvements
* Survey terms and conditions can now be added from the dashboard
* Survey disclaimer can now be added from the dashboard
* Various accessibility improvements around Surveys
* Apptentive Logger service retrieval backup

#### Fixes
* SparseArray backwards compatibility

#### Known Issues and Limitations
* Client authentication (login/logout) is not yet supported

# 2023-05-17 - v6.0.5
#### Improvements
* Added Java interoperable support for push functions
* Improved error handling throughout the SDK
* Added session id to the payloads

#### Fixes
* Resolved internal observers and observables issue

#### Known Issues and Limitations
* Client authentication (login/logout) is not yet supported

# 2023-04-05 - v6.0.4
#### Improvements
* Expanded support for links in survey introduction

#### Fixes
* Resolved resource linking issues

#### Known Issues and Limitations
* Client authentication (login/logout) is not yet supported

# 2023-02-24 - v6.0.3
#### Improvements
* Support mParticleID collection
* Add `distributionName` and `distributionVersion` to `ApptentiveConfiguration`

#### Known Issues and Limitations
* Client authentication (login/logout) is not yet supported

# 2023-02-06 - v6.0.2

#### New Features
* Device storage encryption support
* Event observer support to listen for Apptentive events
* Message Center observer support to listen for Message Center updates

#### Improvements
* `canShowInteraction` function to check if an event will display an interaction

#### Known Issues and Limitations
* Client authentication (login/logout) is not yet supported

# 2023-01-11 - v6.0.1

#### New Features
* Add Push Notification support for Message Center

#### Improvements
* Add `getPersonName` and `getPersonEmail` functions
* Improve Note links to support email and phone numbers
* Improve `ApptentiveActivityCallback` to allow `unregisterApptentiveActivityInfoCallback` to be optional
* Extended localization support

#### Fixes
* Fix right-to-left language support in Message Center

#### Known Issues and Limitations
* Hidden attachments for Message Center size and type constraints to ensure proper usage
* Encryption is not yet supported
* Client authentication (login/logout) is not yet supported


# 2022-11-02 - v6.0.0

#### New Features
* Response Targeting
* Dark mode support

#### Improvements
* Dev supported interface customization
* WCAG compliant interactions
* Modernized theme based on Material Design

#### Known Issues and Limitations
* minSDKVersion is now 21
* Encryption is not yet supported
* Client authentication (login/logout) is not yet supported
* Push notifications for Message Center is not yet supported


#### Supporting Documentations
* [Migration guide](https://learn.apptentive.com/knowledge-base/android-sdk-5-x-to-6-0-migration-guide/)
* [Quick start guide](https://learn.apptentive.com/knowledge-base/android-quick-start-guide/)
* [Integration guide](https://learn.apptentive.com/knowledge-base/android-integration-guide/)
* [Interface customization guide](https://learn.apptentive.com/knowledge-base/android-interface-customization-2/)
* [Cookbook designs](https://learn.apptentive.com/knowledge-base/android-ui-cookbook-overview/)


#### Previous Releases
You can find versions 5 and earlier in our [legacy SDK repository](https://github.com/apptentive/apptentive-android)
