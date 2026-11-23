# إعداد التحديث التلقائي داخل التطبيق

الريبو الأساسي `tpvkeov/clips` يفضل **خاص**. التطبيق بيفحص ملف صغير عام (`update.json`)
موجود في الريبو العام `tpvkeov/clips-updates` (اتعمل بالفعل على branch `main`).

## خطوة واحدة مطلوبة منك

1. افتح https://github.com/settings/tokens واعمل **Fine-grained token**:
   - **Repository access:** اختر بس `tpvkeov/clips-updates`.
   - **Permissions:** `Contents` = **Read and write**.
   - انسخ التوكن (هيظهر مرة واحدة بس).

2. افتح https://github.com/tpvkeov/clips/settings/secrets/actions واعمل **New repository secret**:
   - **Name:** `UPDATES_TOKEN`
   - **Value:** التوكن اللي نسخته.

## إيه اللي هيحصل بعد كده

لأي push على `main` (أو تشغيل الـ workflow يدوياً):
- الـ Actions يبني APK برقم إصدار متزايد (`1000 + run number`).
- يرفع `app-debug.apk` و `update.json` على `tpvkeov/clips-updates`.
- لما تفتح التطبيق، بيقارن رقم الإصدار، ولو في واحد أحدث يظهر مربع «يتوفر تحديث جديد»،
  ولما توافق بينزّل الـ APK ويفتح شاشة التثبيت (هتحتاج تسمح مرّة واحدة بتثبيت من مصدر غير المتجر).

لو حابب تتخطى تحديث معين، اضغط «لاحقاً» وهيتذكر اختيارك لحد ما يطلع إصدار أحدث.

> لو الـ secret مش متحط، الـ workflow بيطبع تحذير ويتجاهل نشر التحديث من غير ما يفشل البناء.
