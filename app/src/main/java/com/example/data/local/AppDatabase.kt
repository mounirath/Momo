package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AccessCode
import com.example.data.model.Recipe
import com.example.data.model.Subscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Recipe::class, AccessCode::class, Subscription::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun accessCodeDao(): AccessCodeDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mounir_formule_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val codeDao = database.accessCodeDao()
            val recipeDao = database.recipeDao()

            codeDao.insertCodes(
                listOf(
                    AccessCode(code = "ADMIN123", note = "رمز الدخول الافتراضي"),
                    AccessCode(code = "MOUNIR77", note = "كود تجريبي إضافي"),
                    AccessCode(code = "FORMUL88", note = "كود تجريبي إضافي")
                )
            )

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
