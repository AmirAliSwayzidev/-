# Planora AI — هوشمند برنامه‌ریزی شخصی (آفلاین + آنلاین)

**Planora AI** یک اپلیکیشن اندروید مدرن و کامل برای برنامه‌ریزی شخصی است که هم به صورت **آفلاین** کاملاً کاربردی است و هم با اتصال به اینترنت از دستیار هوش مصنوعی محاوره‌ای بهره می‌برد.

طراحی‌شده با تمرکز بر کاربران ایرانی: پشتیبانی کامل از **فارسی (RTL)**، فونت‌های مناسب فارسی، و تقویم محلی.

## ویژگی‌های اصلی

### حالت آفلاین
- ایجاد، ویرایش، تکمیل و حذف کارها
- برنامه‌ریزی روزانه و هفتگی
- اهداف و پروژه‌های شخصی
- ردیاب عادت با streak
- تقویم و لیست کارها
- اعلان‌های محلی (WorkManager)
- جستجوی محلی
- موتور برنامه‌ریزی قاعده‌محور محلی
- تاریخچه گفتگو و برنامه‌های ذخیره‌شده روی دستگاه

### حالت آنلاین (AI)
- چت محاوره‌ای شبیه ChatGPT
- تبدیل درخواست‌های طبیعی به برنامه ساختاریافته
- بازبینی و تأیید قبل از ذخیره کارها
- Fallback خودکار به موتور آفلاین در صورت قطع اینترنت
- هیچ کلید API در کلاینت ذخیره نمی‌شود

## معماری

```
MVVM + Clean Architecture
├── ui/          (Jetpack Compose + Material 3)
├── domain/      (Planner engine, models, use-cases)
├── data/
│   ├── local/   (Room DB, DAOs, entities)
│   ├── remote/  (Retrofit AI API)
│   └── repository/
├── notification/ (WorkManager + BroadcastReceivers)
└── di/          (Manual / ready for Hilt)
```

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **Room** برای ذخیره‌سازی محلی
- **DataStore / Room Preferences** برای تنظیمات
- **WorkManager** برای یادآورها
- **Navigation Compose**
- **Coroutines + Flow**
- **Retrofit + Moshi** برای ارتباط امن با بک‌اند AI

## طراحی UI

- تم تاریک و روشن
- پس‌زمینه Deep Navy با اکسنت بنفش و آبی
- تایپوگرافی تمیز با پشتیبانی عالی فارسی
- کارت‌های گرد، سایه‌های ملایم
- انیمیشن‌های محدود و حرفه‌ای
- پشتیبانی کامل RTL

## راه‌اندازی پروژه

### پیش‌نیاز
- Android Studio Ladybug (2024.2+) یا جدیدتر
- JDK 17
- Android SDK 35

### اجرا
```bash
git clone <repo>
cd PlanoraAI
./gradlew assembleDebug
```

یا در Android Studio: Open project → Sync → Run.

### پیکربندی بک‌اند AI

1. یک سرور بک‌اند (Node/Python/Go) بسازید که درخواست‌ها را به LLM مورد نظر (OpenAI, Grok, Claude, …) پروکسی کند.
2. در `AiRepository` و `AiApiService` آدرس پایه را تنظیم کنید:
   ```kotlin
   // مثال
   private const val BASE_URL = "https://your-backend.example.com/"
   ```
3. هرگز کلید API را داخل اپ قرار ندهید.

اگر بک‌اند در دسترس نباشد، اپ همچنان با موتور آفلاین کاملاً کار می‌کند و پیام مناسب نمایش می‌دهد.

## GitHub Actions

فایل `.github/workflows/android.yml` شامل:
- بیلد APK دیباگ
- اجرای تست‌های واحد
- آپلود آرتیفکت

با هر push به `main` یا `develop` اجرا می‌شود.

## تست‌ها

```bash
./gradlew testDebugUnitTest
```

تست‌های موجود:
- موتور برنامه‌ریزی آفلاین (عدم همپوشانی، اولویت‌بندی، برنامه مطالعه)

## ساختار پکیج

```
com.planora.ai
├── data.local.entity / dao / converter
├── data.remote
├── data.repository
├── domain.planner
├── ui.theme / components / navigation / screens.*
├── notification
└── util
```

## حریم خصوصی

- تمام داده‌های ضروری روی دستگاه ذخیره می‌شوند.
- قبل از ارسال پیام به سرویس AI، اطلاع‌رسانی شفاف انجام می‌شود.
- گزینه حذف کامل داده‌ها در تنظیمات وجود دارد.
- امکان Export/Import محلی.

## مجوزها

- `INTERNET` / `ACCESS_NETWORK_STATE` — فقط برای AI
- `POST_NOTIFICATIONS` — یادآورها
- `SCHEDULE_EXACT_ALARM` / `RECEIVE_BOOT_COMPLETED` — یادآورهای دقیق

## محدودیت‌های فعلی و گام‌های بعدی

- بک‌اند AI باید توسط شما پیکربندی شود (اینترفیس آماده است).
- فونت سفارشی Vazirmatn را می‌توانید در `res/font` اضافه کنید و در `Type.kt` جایگزین کنید.
- تقویم جلالی پیشرفته با کتابخانه PersianDate قابل گسترش است.
- نمودارهای پیشرفته با MPAndroidChart قابل افزودن است.

---

ساخته‌شده با ❤️ برای کاربران فارسی‌زبان  
**Planora AI** — برنامه‌ریز هوشمند آفلاین و آنلاین
