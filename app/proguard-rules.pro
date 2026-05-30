# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Koin
-keep class org.koin.** { *; }
-keepnames class * { @org.koin.core.annotation.* <methods>; }

# kotlinx-datetime / serialization
-keepclassmembers class kotlinx.datetime.** { *; }
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    *** Companion;
}
-dontwarn kotlinx.serialization.**
