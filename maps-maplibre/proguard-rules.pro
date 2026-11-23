# Add project specific ProGuard rules here.
-keepnames interface org.tinylog.**
-keepnames class * implements org.tinylog.**
-keepclassmembers class * implements org.tinylog.** { <init>(...); }
-dontwarn dalvik.system.VMStack
-dontwarn java.lang.**
