# Mercury Messenger Portal ProGuard Rules

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Mercury DTOs and domain models
-keep class com.mercury.messengerportal.data.** { *; }
-keep class com.mercury.messengerportal.domain.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
