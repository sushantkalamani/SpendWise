# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Koin
-keep class org.koin.** { *; }
-keepnames class * { @org.koin.core.annotation.* <methods>; }

# kotlinx-datetime / serialization
-keep class kotlinx.datetime.** { *; }
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    *** serializer(...);
}
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep class *$$serializer { *; }
-dontwarn kotlinx.serialization.**
