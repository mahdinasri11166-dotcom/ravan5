package com.therapist.assistant.util

import com.therapist.assistant.data.Client

/**
 * قالب‌های حرفه‌ای اسناد بالینی — خروجی فرم A5 جدولی.
 */
object DocumentTemplates {

    data class DocDef(
        val id: String,
        val title: String,
        val description: String,
        val category: String
    )

    val catalog: List<DocDef> = listOf(
        DocDef("consent", "رضایت‌نامه آگاهانه درمان", "رضایت کتبی دریافت خدمات روان‌شناختی و حدود محرمانگی", "حقوقی"),
        DocDef("intake", "فرم ارزیابی اولیه (Intake)", "شکایت اصلی، سابقه، دارو و اهداف درمان", "پذیرش"),
        DocDef("intake_extended", "فرم جامع پذیرش", "اطلاعات دموگرافیک، خانواده، حمایت اجتماعی و خطر", "پذیرش"),
        DocDef("session_note", "برگه ثبت جلسه (SOAP)", "ساختار S-O-A-P برای یک جلسه", "بالینی"),
        DocDef("treatment_plan", "برنامه درمان فردی", "اهداف کوتاه/بلندمدت، مداخلات و شاخص پیشرفت", "بالینی"),
        DocDef("progress", "گزارش پیشرفت درمان", "خلاصه دوره‌ای روند و تغییرات", "گزارش"),
        DocDef("discharge", "خلاصه پایان دوره / ترخیص", "وضعیت پایان درمان و توصیه‌های پیگیری", "گزارش"),
        DocDef("referral", "نامه ارجاع تخصصی", "ارجاع به روان‌پزشک یا متخصص دیگر", "مکاتبه"),
        DocDef("crisis", "فرم ارزیابی بحران و ایمنی", "افکار آسیب، حمایت و برنامه ایمنی", "ایمنی"),
        DocDef("confidentiality", "تعهدنامه محرمانگی", "تعهد درمانگر/مرکز نسبت به حفظ اطلاعات", "حقوقی"),
        DocDef("telehealth", "رضایت خدمات از راه دور", "شرایط جلسات آنلاین و محدودیت‌ها", "حقوقی"),
        DocDef("feedback", "فرم بازخورد مراجع", "رضایت از خدمات و پیشنهادها", "کیفیت"),
        DocDef("attendance", "گواهی حضور در جلسات", "تأیید حضور برای محل کار/دانشگاه", "مکاتبه"),
        DocDef("fee_agreement", "توافق‌نامه مالی جلسات", "تعرفه، لغو نوبت و بدهی", "مالی"),
        DocDef("homework", "برگه تکلیف بین‌جلسه‌ای", "تمرین CBT/رفتاری بین جلسات", "بالینی")
    )

    fun buildForm(
        id: String,
        client: Client,
        clinicName: String,
        clinicPhone: String,
        clinicAddress: String,
        sessionCount: Int = 0,
        nowMillis: Long = System.currentTimeMillis()
    ): DocumentFormPdf.FormSpec {
        val date = JalaliDate.formatDateLong(nowMillis)
        val name = client.name
        val phone = client.phone.ifBlank { "—" }
        val nationalId = client.nationalId.ifBlank { "—" }
        val cn = clinicName.ifBlank { "مرکز مشاوره" }

        return when (id) {
            "consent" -> DocumentFormPdf.FormSpec(
                docTitle = "رضایت‌نامه آگاهانه دریافت خدمات روان‌شناختی",
                docCode = "DOC-CON-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                intro = "این سند به‌منظور اخذ رضایت آگاهانه مراجع برای دریافت خدمات روان‌شناختی تنظیم شده و جزء پرونده بالینی محفوظ می‌ماند.",
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "۱. مشخصات طرفین",
                        fields = listOf(
                            DocumentFormPdf.Field("نام و نام خانوادگی مراجع", name),
                            DocumentFormPdf.Field("کد ملی", nationalId),
                            DocumentFormPdf.Field("شماره تماس", phone),
                            DocumentFormPdf.Field("نام مرکز / درمانگر", cn)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "۲. مفاد رضایت",
                        paragraphs = listOf(
                            "اینجانب با آگاهی نسبی از ماهیت خدمات روان‌شناختی، محدودیت‌های پیش‌بینی نتیجه درمان، و نقش فعال خود در فرایند درمان، رضایت می‌دهم خدمات مشاوره/روان‌درمانی دریافت کنم.",
                            "محرمانگی اطلاعات رعایت می‌شود مگر در موارد قانونی شامل خطر جدی برای خود یا دیگران، سوءاستفاده از کودک/سالمند، یا حکم مرجع قضایی صالح.",
                            "حق دارم در هر زمان درباره روش درمان سؤال کنم، درخواست تغییر برنامه دهم، یا با اطلاع قبلی درمان را متوقف کنم."
                        ),
                        checklist = listOf(
                            "توضیحات لازم درباره نوع خدمت و حدود محرمانگی به اینجانب داده شد.",
                            "فرصت پرسش و پاسخ فراهم بود و پاسخ‌ها را درک کردم.",
                            "رضایت خود را آزادانه و بدون اجبار اعلام می‌کنم."
                        )
                    )
                ),
                closing = listOf(
                    "تاریخ امضا: $date — محل تنظیم: $cn"
                ),
                signatureLabels = listOf("امضا و اثر انگشت مراجع", "امضا و مهر درمانگر")
            )

            "intake" -> DocumentFormPdf.FormSpec(
                docTitle = "فرم ارزیابی اولیه مراجع (Intake)",
                docCode = "DOC-INT-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "۱. اطلاعات هویتی",
                        fields = listOf(
                            DocumentFormPdf.Field("نام و نام خانوادگی", name),
                            DocumentFormPdf.Field("کد ملی", nationalId),
                            DocumentFormPdf.Field("تلفن", phone),
                            DocumentFormPdf.Field("تاریخ تولد", if (client.birthDateMillis > 0) JalaliDate.formatDate(client.birthDateMillis) else ""),
                            DocumentFormPdf.Field("مدرک تحصیلی", client.education),
                            DocumentFormPdf.Field("آدرس", client.address, lines = 2)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "۲. شکایت اصلی و سیر مشکل",
                        fields = listOf(
                            DocumentFormPdf.Field("شکایت اصلی (با کلمات مراجع)", lines = 3),
                            DocumentFormPdf.Field("شروع تقریبی علائم", lines = 1),
                            DocumentFormPdf.Field("عوامل تشدید / تسکین", lines = 2),
                            DocumentFormPdf.Field("تأثیر بر کار، تحصیل، روابط", lines = 2)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "۳. سابقه و درمان‌های قبلی",
                        fields = listOf(
                            DocumentFormPdf.Field("سابقه مراجعه به روان‌شناس/روان‌پزشک", lines = 2),
                            DocumentFormPdf.Field("داروهای روان‌پزشکی فعلی", lines = 2),
                            DocumentFormPdf.Field("بیماری جسمی مهم / بستری", lines = 2)
                        ),
                        checklist = listOf(
                            "سابقه اقدام به خودکشی یا آسیب به خود",
                            "سابقه خشونت یا خطر برای دیگران",
                            "مصرف مواد / الکل در ماه‌های اخیر"
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "۴. اهداف مراجع از درمان",
                        fields = listOf(
                            DocumentFormPdf.Field("اهداف کوتاه‌مدت (۴–۶ هفته)", lines = 2),
                            DocumentFormPdf.Field("اهداف بلندمدت", lines = 2)
                        )
                    )
                ),
                signatureLabels = listOf("امضای مراجع", "امضای درمانگر پذیرنده")
            )

            "intake_extended" -> DocumentFormPdf.FormSpec(
                docTitle = "فرم جامع پذیرش و ارزیابی چندبُعدی",
                docCode = "DOC-INT-02",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                intro = "این فرم برای جمع‌آوری نظام‌مند اطلاعات زمینه‌ای، حمایتی و بالینی در جلسه(های) ابتدایی طراحی شده است.",
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "الف) مشخصات",
                        fields = listOf(
                            DocumentFormPdf.Field("نام", name),
                            DocumentFormPdf.Field("کد ملی", nationalId),
                            DocumentFormPdf.Field("تلفن", phone),
                            DocumentFormPdf.Field("وضعیت تأهل / هم‌زیستی", lines = 1),
                            DocumentFormPdf.Field("شغل / وضعیت اشتغال", lines = 1),
                            DocumentFormPdf.Field("فرد تماس اضطراری (نام و تلفن)", lines = 1)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "ب) خانواده و حمایت اجتماعی",
                        fields = listOf(
                            DocumentFormPdf.Field("ترکیب خانوار", lines = 2),
                            DocumentFormPdf.Field("کیفیت روابط اصلی", lines = 2),
                            DocumentFormPdf.Field("منابع حمایت (افراد/نهادها)", lines = 2)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "ج) ارزیابی عملکرد و خطر",
                        fields = listOf(
                            DocumentFormPdf.Field("سطح کارکرد روزمره (۱ تا ۱۰)", lines = 1),
                            DocumentFormPdf.Field("خواب / اشتها / انرژی", lines = 2),
                            DocumentFormPdf.Field("افکار آسیب به خود یا دیگران (جزئیات)", lines = 3)
                        ),
                        checklist = listOf(
                            "نیاز به ارجاع فوری روان‌پزشکی",
                            "نیاز به برنامه ایمنی مکتوب",
                            "هماهنگی با خانواده با رضایت مراجع"
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "د) جمع‌بندی بالینی اولیه",
                        fields = listOf(
                            DocumentFormPdf.Field("فرضیه‌های کاری اولیه", lines = 3),
                            DocumentFormPdf.Field("اولویت مداخله در ۴ جلسه اول", lines = 2)
                        )
                    )
                ),
                signatureLabels = listOf("امضای مراجع", "امضای درمانگر")
            )

            "session_note" -> DocumentFormPdf.FormSpec(
                docTitle = "برگه ثبت جلسه درمان (ساختار SOAP)",
                docCode = "DOC-SOAP-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "اطلاعات جلسه",
                        fields = listOf(
                            DocumentFormPdf.Field("مراجع", name),
                            DocumentFormPdf.Field("شماره / تاریخ جلسه", date),
                            DocumentFormPdf.Field("مدت جلسه (دقیقه)", lines = 1),
                            DocumentFormPdf.Field("نوع جلسه (حضوری / آنلاین)", lines = 1)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "S — Subjective (گفته‌ها و تجربه مراجع)",
                        fields = listOf(DocumentFormPdf.Field("شرح", lines = 4))
                    ),
                    DocumentFormPdf.Section(
                        title = "O — Objective (مشاهدات درمانگر)",
                        fields = listOf(DocumentFormPdf.Field("خلق، ظاهر، رفتار، بینش", lines = 3))
                    ),
                    DocumentFormPdf.Section(
                        title = "A — Assessment (ارزیابی و مفهوم‌پردازی)",
                        fields = listOf(DocumentFormPdf.Field("تحلیل بالینی جلسه", lines = 3))
                    ),
                    DocumentFormPdf.Section(
                        title = "P — Plan (برنامه و تکلیف)",
                        fields = listOf(
                            DocumentFormPdf.Field("مداخلات انجام‌شده در جلسه", lines = 2),
                            DocumentFormPdf.Field("تکلیف بین‌جلسه‌ای", lines = 2),
                            DocumentFormPdf.Field("برنامه جلسه بعد", lines = 2)
                        )
                    )
                ),
                signatureLabels = listOf("امضای درمانگر")
            )

            "treatment_plan" -> DocumentFormPdf.FormSpec(
                docTitle = "برنامه درمان فردی (Individual Treatment Plan)",
                docCode = "DOC-TP-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                intro = "برنامه درمان با مشارکت مراجع تنظیم می‌شود و در بازه‌های منظم بازنگری می‌گردد.",
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "۱. مشخصات",
                        fields = listOf(
                            DocumentFormPdf.Field("مراجع", name),
                            DocumentFormPdf.Field("تاریخ شروع برنامه", date),
                            DocumentFormPdf.Field("رویکرد غالب (CBT، ACT، …)", lines = 1)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "۲. مسائل هدف",
                        fields = listOf(
                            DocumentFormPdf.Field("مسئله اصلی ۱", lines = 2),
                            DocumentFormPdf.Field("مسئله اصلی ۲", lines = 2),
                            DocumentFormPdf.Field("نقاط قوت و منابع مراجع", lines = 2)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "۳. اهداف و شاخص‌ها",
                        fields = listOf(
                            DocumentFormPdf.Field("هدف کوتاه‌مدت + شاخص قابل اندازه‌گیری", lines = 3),
                            DocumentFormPdf.Field("هدف میان‌مدت + شاخص", lines = 3),
                            DocumentFormPdf.Field("هدف بلندمدت + شاخص", lines = 2)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "۴. مداخلات و زمان‌بندی",
                        fields = listOf(
                            DocumentFormPdf.Field("مداخلات پیشنهادی", lines = 3),
                            DocumentFormPdf.Field("توالی جلسات / مدت تقریبی دوره", lines = 2),
                            DocumentFormPdf.Field("معیار بازنگری برنامه", lines = 2)
                        )
                    )
                ),
                signatureLabels = listOf("امضای مراجع (موافقت)", "امضای درمانگر")
            )

            "progress" -> DocumentFormPdf.FormSpec(
                docTitle = "گزارش پیشرفت دوره‌ای درمان",
                docCode = "DOC-PRG-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "خلاصه پرونده",
                        fields = listOf(
                            DocumentFormPdf.Field("مراجع", name),
                            DocumentFormPdf.Field("تعداد جلسات تا این تاریخ", sessionCount.toString()),
                            DocumentFormPdf.Field("بازه گزارش", lines = 1)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "پیشرفت نسبت به اهداف",
                        fields = listOf(
                            DocumentFormPdf.Field("تغییرات مثبت مشاهده‌شده", lines = 3),
                            DocumentFormPdf.Field("موانع باقی‌مانده", lines = 2),
                            DocumentFormPdf.Field("نتایج آزمون‌ها / مقیاس‌ها (در صورت وجود)", lines = 2)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "برنامه ادامه",
                        fields = listOf(
                            DocumentFormPdf.Field("اولویت‌های جلسات آینده", lines = 3),
                            DocumentFormPdf.Field("نیاز به ارجاع یا همکاری بین‌تخصصی", lines = 2)
                        )
                    )
                ),
                signatureLabels = listOf("امضای درمانگر")
            )

            "discharge" -> DocumentFormPdf.FormSpec(
                docTitle = "خلاصه پایان دوره درمان / ترخیص",
                docCode = "DOC-DC-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "۱. مشخصات دوره",
                        fields = listOf(
                            DocumentFormPdf.Field("مراجع", name),
                            DocumentFormPdf.Field("تاریخ پایان / ترخیص", date),
                            DocumentFormPdf.Field("تعداد جلسات برگزارشده", sessionCount.toString()),
                            DocumentFormPdf.Field("دلیل پایان (تکمیل اهداف / انصراف / ارجاع)", lines = 2)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "۲. خلاصه بالینی",
                        fields = listOf(
                            DocumentFormPdf.Field("مسئله اولیه و مفهوم‌پردازی مختصر", lines = 3),
                            DocumentFormPdf.Field("مداخلات اصلی انجام‌شده", lines = 3),
                            DocumentFormPdf.Field("وضعیت علائم و کارکرد در پایان", lines = 3)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "۳. توصیه‌های پیگیری",
                        fields = listOf(
                            DocumentFormPdf.Field("توصیه‌ها به مراجع", lines = 3),
                            DocumentFormPdf.Field("زمان پیشنهادشده برای پیگیری", lines = 1),
                            DocumentFormPdf.Field("ارجاع احتمالی", lines = 2)
                        )
                    )
                ),
                signatureLabels = listOf("امضای مراجع", "امضای درمانگر / مسئول مرکز")
            )

            "referral" -> DocumentFormPdf.FormSpec(
                docTitle = "نامه ارجاع تخصصی",
                docCode = "DOC-REF-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                intro = "به استحضار همکار محترم می‌رساند؛ با توجه به ارزیابی انجام‌شده، ارجاع زیر پیشنهاد می‌گردد.",
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "گیرنده ارجاع",
                        fields = listOf(
                            DocumentFormPdf.Field("نام مرکز / متخصص گیرنده", lines = 1),
                            DocumentFormPdf.Field("تخصص مورد نظر", lines = 1)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "مشخصات مراجع",
                        fields = listOf(
                            DocumentFormPdf.Field("نام", name),
                            DocumentFormPdf.Field("کد ملی", nationalId),
                            DocumentFormPdf.Field("تلفن", phone)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "دلیل و یافته‌های مرتبط",
                        fields = listOf(
                            DocumentFormPdf.Field("علت ارجاع", lines = 3),
                            DocumentFormPdf.Field("مداخلات انجام‌شده تاکنون", lines = 2),
                            DocumentFormPdf.Field("داروها / ملاحظات ایمنی", lines = 2),
                            DocumentFormPdf.Field("درخواست مشخص از همکار", lines = 2)
                        )
                    )
                ),
                closing = listOf("با سپاس از همکاری — $cn — $date"),
                signatureLabels = listOf("امضا و مهر ارجاع‌دهنده")
            )

            "crisis" -> DocumentFormPdf.FormSpec(
                docTitle = "فرم ارزیابی بحران و برنامه ایمنی",
                docCode = "DOC-CRI-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                intro = "استفاده بالینی برای مستندسازی ارزیابی خطر و اقدامات ایمنی. در صورت خطر فوری، پروتکل اورژانس محلی اجرا شود.",
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "۱. مشخصات",
                        fields = listOf(
                            DocumentFormPdf.Field("مراجع", name),
                            DocumentFormPdf.Field("تلفن", phone),
                            DocumentFormPdf.Field("فرد تماس اضطراری", lines = 1)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "۲. ارزیابی خطر",
                        fields = listOf(
                            DocumentFormPdf.Field("افکار آسیب به خود (شدت، برنامه، وسیله)", lines = 3),
                            DocumentFormPdf.Field("افکار آسیب به دیگران", lines = 2),
                            DocumentFormPdf.Field("عوامل محافظ و حمایت موجود", lines = 2)
                        ),
                        checklist = listOf(
                            "خطر فوری — اقدام اورژانسی",
                            "خطر متوسط — پایش نزدیک + برنامه ایمنی",
                            "خطر پایین — پیگیری معمول"
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "۳. برنامه ایمنی توافق‌شده",
                        fields = listOf(
                            DocumentFormPdf.Field("علائم هشدار زودهنگام", lines = 2),
                            DocumentFormPdf.Field("راهبردهای مقابله‌ای توافق‌شده", lines = 2),
                            DocumentFormPdf.Field("افراد و شماره‌های قابل تماس", lines = 2),
                            DocumentFormPdf.Field("اقدام در صورت تشدید (اورژانس / بیمارستان)", lines = 2)
                        )
                    )
                ),
                signatureLabels = listOf("امضای مراجع", "امضای درمانگر")
            )

            "confidentiality" -> DocumentFormPdf.FormSpec(
                docTitle = "تعهدنامه محرمانگی اطلاعات پرونده",
                docCode = "DOC-CONF-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "تعهد مرکز / درمانگر",
                        paragraphs = listOf(
                            "این مرکز متعهد است اطلاعات هویتی و بالینی مراجع را مطابق اصول اخلاق حرفه‌ای و مقررات مربوط، محرمانه نگه دارد.",
                            "دسترسی به پرونده صرفاً برای افراد مجاز درمانی و در حد ضرورت حرفه‌ای خواهد بود.",
                            "افشای اطلاعات تنها در چارچوب موارد مصرح قانونی یا با رضایت کتبی مراجع انجام می‌شود."
                        ),
                        fields = listOf(
                            DocumentFormPdf.Field("نام مراجع مشمول این تعهد", name),
                            DocumentFormPdf.Field("مسئول پرونده", lines = 1)
                        )
                    )
                ),
                signatureLabels = listOf("امضا و مهر مرکز / درمانگر")
            )

            "telehealth" -> DocumentFormPdf.FormSpec(
                docTitle = "رضایت‌نامه خدمات روان‌شناختی از راه دور",
                docCode = "DOC-TEL-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "مشخصات",
                        fields = listOf(
                            DocumentFormPdf.Field("مراجع", name),
                            DocumentFormPdf.Field("تلفن / بستر ارتباط", phone),
                            DocumentFormPdf.Field("سکوی جلسه (مثلاً تماس تصویری امن)", lines = 1)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "شرایط و محدودیت‌ها",
                        paragraphs = listOf(
                            "جلسات آنلاین مکمل یا جایگزین جلسات حضوری است و در شرایط فنی یا بالینی ممکن است به حضوری تغییر یابد.",
                            "محرمانگی در بستر دیجیتال نسبی است؛ مراجع متعهد می‌شود در فضای خصوصی و بدون حضور افراد غیرمجاز در جلسه حاضر شود.",
                            "در بحران حاد، خدمات از راه دور جایگزین اورژانس حضوری نیست و باید با مراکز فوریت محلی تماس گرفته شود."
                        ),
                        checklist = listOf(
                            "از محدودیت‌های فنی و محرمانگی نسبی آگاه شدم.",
                            "آدرس تقریبی محل حضور در زمان جلسه را در اختیار درمانگر قرار می‌دهم (برای ایمنی).",
                            "رضایت به برگزاری جلسات از راه دور دارم."
                        )
                    )
                ),
                signatureLabels = listOf("امضای مراجع", "امضای درمانگر")
            )

            "feedback" -> DocumentFormPdf.FormSpec(
                docTitle = "فرم بازخورد و رضایت از خدمات",
                docCode = "DOC-FB-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "مراجع",
                        fields = listOf(
                            DocumentFormPdf.Field("نام (اختیاری در صورت ناشناس)", name),
                            DocumentFormPdf.Field("تعداد تقریبی جلسات", sessionCount.toString())
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "ارزیابی (از ۱ تا ۵)",
                        fields = listOf(
                            DocumentFormPdf.Field("احترام و رابطه درمانی", lines = 1),
                            DocumentFormPdf.Field("وضوح توضیح و همکاری در برنامه", lines = 1),
                            DocumentFormPdf.Field("مفید بودن جلسات", lines = 1),
                            DocumentFormPdf.Field("نظم نوبت‌دهی و هماهنگی", lines = 1)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "نظرات آزاد",
                        fields = listOf(
                            DocumentFormPdf.Field("نقاط قوت خدمت", lines = 2),
                            DocumentFormPdf.Field("پیشنهاد بهبود", lines = 2)
                        )
                    )
                ),
                signatureLabels = listOf("امضای مراجع (اختیاری)")
            )

            "attendance" -> DocumentFormPdf.FormSpec(
                docTitle = "گواهی حضور در جلسات مشاوره / روان‌درمانی",
                docCode = "DOC-ATT-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                intro = "بدین‌وسیله گواهی می‌شود نامبرده زیر در جلسات این مرکز حضور داشته است. جزئیات بالینی به دلیل محرمانگی ذکر نمی‌شود.",
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "مشخصات",
                        fields = listOf(
                            DocumentFormPdf.Field("نام و نام خانوادگی", name),
                            DocumentFormPdf.Field("کد ملی", nationalId),
                            DocumentFormPdf.Field("تعداد جلسات تأییدشده", sessionCount.toString()),
                            DocumentFormPdf.Field("بازه زمانی حضور", lines = 1),
                            DocumentFormPdf.Field("مرجع درخواست‌کننده گواهی", lines = 1)
                        )
                    )
                ),
                closing = listOf("این گواهی صرفاً برای ارائه به مرجع درخواست‌کننده صادر شده است. — $cn — $date"),
                signatureLabels = listOf("امضا و مهر مرکز")
            )

            "fee_agreement" -> DocumentFormPdf.FormSpec(
                docTitle = "توافق‌نامه مالی و نوبت‌دهی",
                docCode = "DOC-FEE-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "طرفین",
                        fields = listOf(
                            DocumentFormPdf.Field("مراجع", name),
                            DocumentFormPdf.Field("تلفن", phone),
                            DocumentFormPdf.Field("مرکز", cn)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "شرایط مالی",
                        fields = listOf(
                            DocumentFormPdf.Field("تعرفه هر جلسه (تومان)", lines = 1),
                            DocumentFormPdf.Field("نحوه پرداخت", lines = 1),
                            DocumentFormPdf.Field("سیاست لغو / جابجایی نوبت", lines = 3),
                            DocumentFormPdf.Field("سایر توافقات", lines = 2)
                        ),
                        checklist = listOf(
                            "از تعرفه و شرایط لغو نوبت آگاه شدم.",
                            "متعهد به رعایت زمان‌بندی اعلام‌شده هستم."
                        )
                    )
                ),
                signatureLabels = listOf("امضای مراجع", "امضای مسئول مرکز")
            )

            "homework" -> DocumentFormPdf.FormSpec(
                docTitle = "برگه تکلیف و تمرین بین‌جلسه‌ای",
                docCode = "DOC-HW-01",
                clinicName = cn,
                clinicPhone = clinicPhone,
                clinicAddress = clinicAddress,
                dateText = date,
                sections = listOf(
                    DocumentFormPdf.Section(
                        title = "جلسه",
                        fields = listOf(
                            DocumentFormPdf.Field("مراجع", name),
                            DocumentFormPdf.Field("تاریخ جلسه", date),
                            DocumentFormPdf.Field("موضوع کانونی جلسه", lines = 1)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "تکلیف توافق‌شده",
                        fields = listOf(
                            DocumentFormPdf.Field("شرح دقیق تمرین", lines = 4),
                            DocumentFormPdf.Field("زمان / تعداد تکرار پیشنهادی", lines = 1),
                            DocumentFormPdf.Field("موانع پیش‌بینی‌شده و راه‌حل", lines = 2)
                        )
                    ),
                    DocumentFormPdf.Section(
                        title = "ثبت انجام (توسط مراجع)",
                        fields = listOf(
                            DocumentFormPdf.Field("چه انجام شد؟", lines = 3),
                            DocumentFormPdf.Field("یادداشت احساس / نتیجه", lines = 3)
                        )
                    )
                ),
                signatureLabels = listOf("یادداشت درمانگر در جلسه بعد")
            )

            else -> buildForm("intake", client, clinicName, clinicPhone, clinicAddress, sessionCount, nowMillis)
        }
    }

    /** متن ساده برای اشتراک بدون PDF (پشتیبان) */
    fun plainTextSummary(spec: DocumentFormPdf.FormSpec): String = buildString {
        appendLine(spec.docTitle)
        appendLine("مرکز: ${spec.clinicName}")
        appendLine("تاریخ: ${spec.dateText}")
        appendLine()
        if (spec.intro.isNotBlank()) {
            appendLine(spec.intro)
            appendLine()
        }
        spec.sections.forEach { sec ->
            appendLine("■ ${sec.title}")
            sec.paragraphs.forEach { appendLine(it) }
            sec.fields.forEach { f ->
                appendLine("${f.label}: ${f.value.ifBlank { "………………" }}")
            }
            sec.checklist.forEach { appendLine("☐ $it") }
            appendLine()
        }
        spec.closing.forEach { appendLine(it) }
    }
}
