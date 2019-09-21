# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

-keepnames class com.apptentive.android.sdk.** { *; }

-keep class * implements com.apptentive.android.sdk.serialization.SerializableObject { *; }
-keep class com.apptentive.android.sdk.** implements java.io.Serializable { *; }