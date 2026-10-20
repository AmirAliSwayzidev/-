# کجا API Key اضافه کنم؟

## نسخه وب (PlanoraAI-Web)

1. برنامه را باز کنید
2. بروید به **پروفایل** (تب پایین)
3. بخش **🔑 تنظیمات API**
4. فیلدها:
   - **ارائه‌دهنده**: OpenAI / xAI Grok / سفارشی
   - **Base URL**:
     - OpenAI: `https://api.openai.com/v1`
     - Grok (xAI): `https://api.x.ai/v1`
   - **API Key**: کلید خودتان (مثلاً `sk-...` یا کلید xAI)
   - **مدل**: مثلاً `gpt-4o-mini` یا `grok-2-latest`
5. دکمه **ذخیره تنظیمات API**

کلید فقط در `localStorage` مرورگر شما ذخیره می‌شود.

کد مربوطه: `js/store.js` → `getApiConfig` / `saveApiConfig`  
فراخوانی: `js/planner.js` → `chatOnline`

## نسخه اندروید (PlanoraAI)

### در رابط کاربری
تنظیمات / پروفایل → فیلدهای:
- `apiBaseUrl`
- `apiKey`
- `apiModel`

ذخیره در جدول Room: `user_preferences`

### در کد (مرجع)

| فایل | نقش |
|------|-----|
| `UserPreferencesEntity.kt` | فیلدهای `apiKey`, `apiBaseUrl`, `apiModel` |
| `AiRepository.kt` | خواندن کلید از preferences و ارسال `Authorization: Bearer ...` |
| `AiApiService.kt` | اینترفیس Retrofit |

**هرگز** کلید را داخل `build.gradle` یا سورس به‌صورت ثابت نگذارید.

### نمونه مقداردهی موقت (فقط دیباگ)

```kotlin
database.preferencesDao().insert(
    UserPreferencesEntity(
        apiBaseUrl = "https://api.openai.com/v1",
        apiKey = "sk-YOUR_KEY_HERE",
        apiModel = "gpt-4o-mini"
    )
)
```

## رفتار آفلاین

- اینترنت قطع باشد → پیام واضح «آفلاین — هیچ برنامه‌ای تولید نمی‌شود»
- کلید نباشد → راهنمای وارد کردن API Key
- آنلاین + کلید معتبر → گفتگو با مدل و پیشنهاد برنامه (فقط بعد از تأیید شما ذخیره می‌شود)
