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
