-dontwarn com.fealrias.purchasehistorybackend.models.entities.Category
-dontwarn com.fealrias.purchasehistorybackend.models.entities.MonthlyLimit
-dontwarn com.fealrias.purchasehistorybackend.models.entities.ObservedUser
-dontwarn com.fealrias.purchasehistorybackend.models.entities.Purchase
-dontwarn com.fealrias.purchasehistorybackend.models.entities.ScheduledExpense
-dontwarn com.fealrias.purchasehistorybackend.models.entities.User
-dontwarn com.google.api.client.auth.oauth2
-dontwarn org.springframework.data.domain.Page
-dontwarn org.springframework.data.domain.PageRequest
-dontwarn org.springframework.data.domain.Sort$Direction
-dontwarn org.springframework.data.jpa.domain
-dontwarn org.springframework.http.ProblemDetail
-dontwarn org.springframework.data.jpa.domain.Specification
-dontwarn com.google.api.client.auth.oauth2.TokenResponse
-dontwarn com.google.api.client.util.Key
-dontwarn jakarta.persistence.criteria.CriteriaBuilder
-dontwarn jakarta.persistence.criteria.CriteriaQuery
-dontwarn jakarta.persistence.criteria.Expression
-dontwarn jakarta.persistence.criteria.Order
-dontwarn jakarta.persistence.criteria.Path
-dontwarn jakarta.persistence.criteria.Predicate
-dontwarn jakarta.persistence.criteria.Root
-dontwarn org.springframework.data.domain.Sort
-dontwarn org.springframework.lang.NonNull

-keep class com.fealrias.purchasehistorybackend.models.** { *; }
-keepclassmembers class com.fealrias.purchasehistorybackend.** {
    <init>(...);
    <fields>;
}
-keepattributes Signature,InnerClasses,EnclosingMethod
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# This line alone fixes 95% of the "Abstract classes can't be instantiated" errors
-keepclassmembers,allowobfuscation class * {
    <init>();
}