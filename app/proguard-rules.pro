# PassGo ProGuard Rules

# Keep Hilt generated classes
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$ActivityContextWrapper { *; }

# Keep Room entities
-keep class com.passgo.app.core.database.entity.** { *; }

# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.passgo.app.**$$serializer { *; }
-keepclassmembers class com.passgo.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.passgo.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep AndroidX Security Crypto (EncryptedSharedPreferences)
-keep class androidx.security.crypto.** { *; }

# Keep SQLCipher classes used via reflection in Room
-keep class net.zetetic.database.sqlcipher.** { *; }
