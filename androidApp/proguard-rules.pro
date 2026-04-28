# ============================================================
# Kotlinx Serialization
# ============================================================
# Keep serializer classes and their companion objects.
# The serialization plugin generates code but R8 can strip
# the companion $serializer classes if not told to keep them.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all @Serializable classes and their generated serializers
-keep,includedescriptorclasses class com.merkost.honq.**$$serializer { *; }
-keepclassmembers class com.merkost.honq.** {
    *** Companion;
}
-keepclasseswithmembers class com.merkost.honq.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Firestore DTOs end-to-end. R8's optimize pass can mangle the data classes
# even when the generated serializer survives, which makes GitLive Firebase KMP's
# `documents.map { it.data<Dto>() }` decode silently return empty in release.
# Covers both data/remote/dto/* and data/remote/api/*$ConfigDoc nested types.
-keep class com.merkost.honq.data.remote.** { *; }

# ============================================================
# Kotlin
# ============================================================
-dontwarn kotlin.MustUseReturnValue

# ============================================================
# Ktor / OkHttp
# ============================================================
-dontwarn org.slf4j.**

# ============================================================
# Play Integrity
# ============================================================
-keep class com.google.android.play.core.integrity.** { *; }
