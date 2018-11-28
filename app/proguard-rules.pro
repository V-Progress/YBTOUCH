# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interfaces
# class:
#-keepclassmembers class fqcn.of.javascript.interfaces.for.webview {
#   public *;
#}
#忽略警告，避免打包时某些警告出现
-ignorewarnings
#引用v4,v7 包不被混淆
-keep class android.support.v4.** {*;}
#-keep class android.support.v7.** {*;}
-dontwarn android.support.**
#避免混淆泛型
-keepattributes Signature
#指定代码的压缩级别
-optimizationpasses 5
#包明不混合大小写
-dontusemixedcaseclassnames
#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses
 #优化  不优化输入的类文件
-dontoptimize
 #预校验
-dontpreverify
 #混淆时是否记录日志
-verbose
 # 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#保护注解
-keepattributes *Annotation*

-keep class com.lidroid.xutils.**

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
    public <fields>;
}

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

#百度基础地图
-keep class com.baidu.** {*;}
-keep class vi.com.** {*;}
-dontwarn com.baidu.**

-keep class com.ideafactory.client.xmpp.** {*;}

#bean包下的所有.class文件不进行混淆编译
-keep class com.ideafactory.client.business.draw.layout.bean.** {*;}
-keep class com.ideafactory.client.business.touchQuery.bean.** {*;}
-keep class com.ideafactory.client.business.weichat.bean.** {*;}
-keep class com.ideafactory.client.business.machine.** {*;}

#小百合HDMI IN
-keep class com.hisilicon.android.tvapi.** {*;}

-keep class cn.trinea.android.** { *; }
-keepclassmembers class cn.trinea.android.** { *; }
-dontwarn cn.trinea.android.**

-keep class org.xwalk.core.** { *;}
-keep class org.chromium.** { *;}
-keepattributes **
#以上是其官网提高，但打包时仍存在问题，添加下方后解决
-keep  class  junit.framework.**{*;}

#-keep class com.lidroid.xutils.**

################### region for xUtils
-keep public class org.xutils.** {
    public protected *;
}
-keep public interface org.xutils.** {
    public protected *;
}
-keepclassmembers class * extends org.xutils.** {
    public protected *;
}
-keepclassmembers @org.xutils.db.annotation.* class * {*;}
-keepclassmembers @org.xutils.http.annotation.* class * {*;}
-keepclassmembers class * {
    @org.xutils.view.annotation.Event <methods>;
}
#################### end region

-keep class com.google.protobuf.micro.** { *; }

#联屏的混淆
-keep class com.ideafactory.client.business.unicomscreen.** { *; }

#greenDao混淆
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties
# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**
# If you do not use Rx:
-dontwarn rx.**


