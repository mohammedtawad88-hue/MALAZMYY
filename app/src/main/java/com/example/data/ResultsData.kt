package com.example.data

data class City(
    val id: String,
    val name: String,
    val nameEn: String,
    val emoji: String
)

data class ResultsAlert(
    val text: String = "",
    val isActive: Boolean = false,
    val date: String = ""
)

object IraqiCitiesData {
    val cities = listOf(
        City("result_baghdad", "بغداد", "Baghdad", "🏰"),
        City("result_nineveh", "نينوى", "Nineveh", "🌉"),
        City("result_basra", "البصرة", "Basra", "🌴"),
        City("result_dhi_qar", "ذي قار", "Dhi Qar", "⛵"),
        City("result_babil", "بابل", "Babylon", "🦁"),
        City("result_anbar", "الأنبار", "Anbar", "🏜️"),
        City("result_erbil", "أربيل", "Erbil", "⛰️"),
        City("result_najaf", "النجف", "Najaf", "🕌"),
        City("result_karbala", "كربلاء", "Karbala", "✨"),
        City("result_qadisiyah", "القادسية", "Qadisiyah", "🌾"),
        City("result_wasit", "واسط", "Wasit", "🌱"),
        City("result_salah_al_din", "صلاح الدين", "Salah Al-Din", "🕌"),
        City("result_duhok", "دهوك", "Duhok", "⛰️"),
        City("result_sulaymaniyah", "السليمانية", "Sulaymaniyah", "🏔️"),
        City("result_kirkuk", "كركوك", "Kirkuk", "🔥"),
        City("result_maysan", "ميسان", "Maysan", "🌾"),
        City("result_muthanna", "المثنى", "Muthanna", "🏺"),
        City("result_diyala", "ديالى", "Diyala", "🍊")
    )
}
