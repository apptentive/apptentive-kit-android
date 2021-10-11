# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-keepnames class com.apptentive.android.sdk.** { *; }
-keep class apptentive.com.android.feedback.model.* { *; }
-keep class apptentive.com.android.feedback.engagement.interactions.InteractionData { *; }