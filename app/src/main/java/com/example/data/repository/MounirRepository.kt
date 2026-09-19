package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.model.AccessCode
import com.example.data.model.Recipe
import com.example.data.model.Subscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.SecureRandom

class MounirRepository(
    private val database: AppDatabase,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mounir_formule_prefs", Context.MODE_PRIVATE)

    private val recipeDao = database.recipeDao()
    private val codeDao = database.accessCodeDao()
    private val subscriptionDao = database.subscriptionDao()

    val allRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()
    val allCodes: Flow<List<AccessCode>> = codeDao.getAllCodes()
    val allSubscriptions: Flow<List<Subscription>> = subscriptionDao.getAllSubscriptions()

    fun getRecipesByCategory(category: String): Flow<List<Recipe>> =
        recipeDao.getRecipesByCategory(category)

    fun searchRecipes(query: String): Flow<List<Recipe>> =
        recipeDao.searchRecipes(query)

    suspend fun insertRecipe(recipe: Recipe): Long = withContext(Dispatchers.IO) {
        recipeDao.insertRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: Recipe): Int = withContext(Dispatchers.IO) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun deleteRecipe(recipe: Recipe): Int = withContext(Dispatchers.IO) {
        recipeDao.deleteRecipe(recipe)
    }

    suspend fun deleteRecipeById(id: String): Int = withContext(Dispatchers.IO) {
        recipeDao.deleteRecipeById(id)
    }

    suspend fun validateAccessCode(code: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = code.trim().uppercase()
        if (trimmed == "ADMIN123") return@withContext true
        val count = codeDao.countCode(trimmed)
        count > 0
    }

    suspend fun generateNewCode(note: String = ""): String = withContext(Dispatchers.IO) {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = SecureRandom()
        val sb = StringBuilder(8)
        for (i in 0 until 8) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        val newCode = sb.toString()
        codeDao.insertCode(AccessCode(code = newCode, note = note))
        newCode
    }

    suspend fun addAccessCode(code: String, note: String = "") = withContext(Dispatchers.IO) {
        codeDao.insertCode(AccessCode(code = code.trim().uppercase(), note = note))
    }

    suspend fun deleteAccessCode(code: String) = withContext(Dispatchers.IO) {
        codeDao.deleteCode(code)
    }

    suspend fun addSubscription(contact: String) = withContext(Dispatchers.IO) {
        if (contact.isNotBlank()) {
            subscriptionDao.insertSubscription(Subscription(contact = contact.trim()))
        }
    }

    suspend fun saveSubscriptionsBulk(lines: List<String>) = withContext(Dispatchers.IO) {
        subscriptionDao.clearAll()
        lines.filter { it.isNotBlank() }.forEach { line ->
            subscriptionDao.insertSubscription(Subscription(contact = line.trim()))
        }
    }

    suspend fun deleteSubscription(id: Long) = withContext(Dispatchers.IO) {
        subscriptionDao.deleteSubscription(id)
    }

    // Access & Admin Session State in Preferences
    fun isAccessUnlocked(): Boolean {
        return prefs.getBoolean("is_access_unlocked", false)
    }

    fun setAccessUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean("is_access_unlocked", unlocked).apply()
    }

    fun verifyAdminPassword(password: String): Boolean {
        val stored = prefs.getString("admin_password", "mounirath1977@") ?: "mounirath1977@"
        return password == stored
    }

    fun updateAdminPassword(newPassword: String) {
        prefs.edit().putString("admin_password", newPassword).apply()
    }

    // Ensure initial data seeded even if DB was pre-created
    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (codeDao.getCodeCount() == 0) {
            codeDao.insertCodes(
                listOf(
                    AccessCode(code = "ADMIN123", note = "رمز الدخول الافتراضي"),
                    AccessCode(code = "MOUNIR77", note = "كود تجريبي إضافي"),
                    AccessCode(code = "FORMUL88", note = "كود تجريبي إضافي")
                )
            )
        }
        if (recipeDao.getRecipeCount() == 0) {
            recipeDao.insertRecipes(
                listOf(
                    Recipe(
                        id = "1",
                        category = "cleaners",
                        title = "مطهر أسطح متعدد الاستخدامات",
                        percentages = "ماء مقطر: 80%\nكحول إيثيلي: 19%\nزيت شجرة الشاي: 1%",
                        steps = "1. أضف الماء المقطر إلى وعاء زجاجي نظيف.\n2. أضف الكحول الإيثيلي وقلبه جيداً.\n3. أضف زيت شجرة الشاي واخلط حتى يتجانس.\n4. انقل الخليط إلى زجاجة بخاخ معقمة.",
                        warnings = "قابل للاشتعال، ابتعد عن مصادر اللهب والحرارة.\nتجنب ملامسة العين المباشرة.",
                        videoUrl = ""
                    ),
                    Recipe(
                        id = "2",
                        category = "cars",
                        title = "ملمع طلاء السيارة (Wax)",
                        percentages = "شمع كارنوبا: 30%\nسيليكون سائل: 40%\nمذيب بيترولي: 30%",
                        steps = "1. اخلط الشمع مع المذيب على حمام مائي دافئ (60 درجة).\n2. أضف السيليكون السائل تدريجياً مع التحريك المستمر.\n3. اترك الخليط يبرد تماماً قبل الاستخدام.",
                        warnings = "استخدم في مكان جيد التهوية. تجنب استنشاق الأبخرة.",
                        videoUrl = ""
                    ),
                    Recipe(
                        id = "3",
                        category = "cleaners",
                        title = "سائل غسيل الأطباق عالي الرغوة",
                        percentages = "ماء منقى: 70%\nحمض السلفونيك (LABSA): 10%\nصودا كاوية لمعادلة pH 7: 1.5%\nتكسابون (SLES 70%): 12%\nكمبرلان (معزز رغوة): 2%\nبيتائين: 2%\nملح طعام لضبط اللزوجة: 1.5%\nمادة حافظة وعطر ليمون ولون: 1%",
                        steps = "1. أذب السلفونيك في الماء ببطء مع التحريك في اتجاه واحد.\n2. عادل الخليط بالصودا الكاوية حتى يصبح الرقم الهيدروجيني pH = 7.\n3. حلل التكسابون في ماء دافئ منفصل ثم أضفه للخليط.\n4. أضف الكمبرلان والبيتائين لزيادة ثبات الرغوة.\n5. أضف محلول الملح تدريجياً لضبط القوام واللزوجة.\n6. أضف المادة الحافظة واللون والعطر واتركه 24 ساعة ليروق.",
                        warnings = "الصودا الكاوية مادة حارقة، ارتد قفازات ونظارات واقية أثناء التحضير.",
                        videoUrl = ""
                    ),
                    Recipe(
                        id = "4",
                        category = "cars",
                        title = "شامبو غسيل السيارات فائق اللمعان",
                        percentages = "ماء معالج: 75%\nتكسابون (SLES): 14%\nبيتائين (CAPB): 4%\nسيليكون مستحلب (60%): 4%\nجلسرين نقي: 1.5%\nعطر ولون ومادة حافظة: 1.5%",
                        steps = "1. أذب التكسابون في الماء المفلتر حتى يتجانس تماماً ويصبح رائقاً.\n2. أضف البيتائين مع التحريك الهادئ لتجنب تشكل فقاعات زائدة.\n3. أضف السيليكون المستحلب بالتدريج للحصول على لمعان وطبقة عازلة طاردة للماء.\n4. أضف الجلسرين لحماية طلاء السيارات والقطع المطاطية.\n5. أضف اللون والعطر والمادة الحافظة وعبئ في زجاجات معتمة.",
                        warnings = "لا تغسل السيارة تحت أشعة الشمس المباشرة أو عندما يكون سطح السيارة ساخناً.",
                        videoUrl = ""
                    ),
                    Recipe(
                        id = "5",
                        category = "cars",
                        title = "ملمع ومغذي إطارات السيارات (Tire Gel)",
                        percentages = "زيت سيليكون لزوجة 1000: 45%\nزيت سيليكون لزوجة 350: 15%\nمذيب سيكلوبنتاسيلوكسان: 38%\nمعطر مخصص للسيارات: 2%",
                        steps = "1. اخلط زيت السيليكون عالي اللزوجة مع زيت السيليكون الخفيف.\n2. أضف المذيب الحامل ببطء مع التقليب المستمر حتى تحصل على مظهر نقي تماماً.\n3. أضف المعطر واخلط لمدة 5 دقائق.\n4. يوزع على الجدران الجانبية للإطار بواسطة إسفنجة ناعمة.",
                        warnings = "يمنع رش المنتج على مداس الإطار أو على أقراص الفرامل منعاً لحدوث انزلاق.",
                        videoUrl = ""
                    ),
                    Recipe(
                        id = "6",
                        category = "cleaners",
                        title = "منظف وملمع الزجاج فائق النقاء",
                        percentages = "ماء مقطر: 82%\nكحول إيزوبروبيلي (IPA 99%): 12%\nبوتيل جليكول: 3%\nخل أبيض طبيعي (5%): 2%\nمادة سطحية غير أيونية: 0.5%\nلون أزرق شفاف: 0.5%",
                        steps = "1. اخلط الماء المقطر مع الكحول الإيزوبروبيلي في وعاء زجاجي نظيف.\n2. أضف البوتيل جليكول لمنع تشكل الضباب والخطوط أثناء المسح السريع.\n3. أضف الخل الأبيض لإزالة الرواسب الكلسية والتكلسات.\n4. أضف المنظف الخفيف واللون الأزرق ورج العبوة جيداً.",
                        warnings = "سريع التبخر وقابل للاشتعال، احفظه بعيداً عن أفران الغاز والشرر.",
                        videoUrl = ""
                    )
                )
            )
        }
    }
}
