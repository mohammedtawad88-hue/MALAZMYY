package com.example.data

data class Subject(
    val id: String,
    val name: String,
    val icon: String,
    val type: String // "book" or "notes"
)

data class StudyClass(
    val id: String,
    val name: String,
    val emoji: String,
    val subjects: List<Subject>
)

data class Stage(
    val id: String,
    val name: String,
    val emoji: String,
    val classes: List<StudyClass> = emptyList(),
    val branches: Map<String, List<StudyClass>> = emptyMap()
)

object IraqiEducationData {
    val stages = listOf(
        Stage(
            id = "ibtidaee",
            name = "الابتدائية",
            emoji = "🌱",
            classes = listOf(
                StudyClass("ib1", "الأول الابتدائي", "🌟", subjectsIb(1)),
                StudyClass("ib2", "الثاني الابتدائي", "⭐", subjectsIb(2)),
                StudyClass("ib3", "الثالث الابتدائي", "🌈", subjectsIb(3)),
                StudyClass("ib4", "الرابع الابتدائي", "🎯", subjectsIb(4)),
                StudyClass("ib5", "الخامس الابتدائي", "🔥", subjectsIb(5)),
                StudyClass("ib6", "السادس الابتدائي", "🏆", subjectsIb(6))
            )
        ),
        Stage(
            id = "mutawasit",
            name = "المتوسطة",
            emoji = "📘",
            classes = listOf(
                StudyClass("mt1", "الأول المتوسط", "📘", subjectsMt(1)),
                StudyClass("mt2", "الثاني المتوسط", "📗", subjectsMt(2)),
                StudyClass("mt3", "الثالث المتوسط", "📙", subjectsMt(3))
            )
        ),
        Stage(
            id = "i3dadee",
            name = "الإعدادية",
            emoji = "🎓",
            branches = mapOf(
                "ilmi" to listOf(
                    StudyClass("id4s", "الرابع العلمي", "🔬", subjectsIlmi(4)),
                    StudyClass("id5s", "الخامس العلمي", "🧪", subjectsIlmi(5)),
                    StudyClass("id6s", "السادس العلمي", "🎓", subjectsIlmi(6))
                ),
                "adabi" to listOf(
                    StudyClass("id4a", "الرابع الأدبي", "📖", subjectsAdabi(4)),
                    StudyClass("id5a", "الخامس الأدبي", "✒️", subjectsAdabi(5)),
                    StudyClass("id6a", "السادس الأدبي", "🏅", subjectsAdabi(6))
                )
            )
        )
    )

    private fun subjectsIb(grade: Int): List<Subject> {
        if (grade == 1) {
            return listOf(
                Subject("ib1_isl", "اسلامية", "🕌", "book"),
                Subject("ib1_math", "رياضيات", "🔢", "book"),
                Subject("ib1_read", "قراءة", "📖", "book"),
                Subject("ib1_eng", "انكليزي", "🇬🇧", "book"),
                Subject("ib1_sci", "العلوم", "🔭", "book"),
                Subject("ib1_ethics", "التربية الأخلاقية", "🌸", "book")
            )
        }
        val subjects = mutableListOf(
            Subject("ib${grade}_ar", "اللغة العربية", "📝", "book"),
            Subject("ib${grade}_math", "الرياضيات", "🔢", "book"),
            Subject("ib${grade}_isl", "التربية الإسلامية", "🕌", "book"),
            Subject("ib${grade}_sci", "العلوم", "🔭", "book")
        )
        if (grade >= 2) {
            subjects.add(Subject("ib${grade}_eng", "اللغة الإنجليزية", "🇬🇧", "book"))
        }
        if (grade == 2) {
            subjects.add(Subject("ib2_ethics", "التربية الأخلاقية", "🌸", "book"))
        }
        if (grade >= 3) {
            subjects.add(Subject("ib${grade}_soc", "الاجتماعيات", "🗺️", "notes"))
        }
        if (grade >= 5) {
            subjects.add(Subject("ib${grade}_comp", "الحاسوب", "💻", "notes"))
        }
        return subjects
    }

    private fun subjectsMt(grade: Int): List<Subject> {
        return listOf(
            Subject("mt${grade}_ar", "اللغة العربية", "📝", "book"),
            Subject("mt${grade}_eng", "اللغة الإنجليزية", "🇬🇧", "book"),
            Subject("mt${grade}_math", "الرياضيات", "🔢", "book"),
            Subject("mt${grade}_sci", "العلوم", "🔬", "book"),
            Subject("mt${grade}_hist", "التاريخ", "📜", "notes"),
            Subject("mt${grade}_geo", "الجغرافية", "🌍", "notes"),
            Subject("mt${grade}_isl", "التربية الإسلامية", "🕌", "book"),
            Subject("mt${grade}_comp", "الحاسوب", "💻", "notes")
        )
    }

    private fun subjectsIlmi(grade: Int): List<Subject> {
        if (grade == 4) {
            return listOf(
                Subject("ids4_isl", "اسلامية", "🕌", "book"),
                Subject("ids4_ar", "اللغة العربية", "📝", "book"),
                Subject("ids4_eng", "اللغة الانكليزية", "🇬🇧", "book"),
                Subject("ids4_math", "الرياضيات", "🔢", "book"),
                Subject("ids4_chem", "الكيمياء", "🧪", "book"),
                Subject("ids4_bio", "الاحياء", "🧬", "book"),
                Subject("ids4_phy", "الفيزياء", "⚡", "book"),
                Subject("ids4_comp", "الحاسوب", "💻", "notes"),
                Subject("ids4_fr", "الفرنسي", "🇫🇷", "notes"),
                Subject("ids4_ku", "الكردي", "☀️", "notes"),
                Subject("ids4_baath", "كتائب جرائم حزب البعث", "📜", "notes")
            )
        }
        if (grade == 5) {
            return listOf(
                Subject("ids5_isl", "الاسلامية", "🕌", "book"),
                Subject("ids5_ar", "العربي", "📝", "book"),
                Subject("ids5_eng", "الانكليزي", "🇬🇧", "book"),
                Subject("ids5_math", "الرياضيات", "🔢", "book"),
                Subject("ids5_bio", "الاحياء", "🧬", "book"),
                Subject("ids5_chem", "الكيمياء", "🧪", "book"),
                Subject("ids5_phy", "الفيزياء", "⚡", "book"),
                Subject("ids5_earth", "علم الارض", "🌍", "book"),
                Subject("ids5_comp", "الحاسوب", "💻", "notes"),
                Subject("ids5_ku", "الكردي", "☀️", "notes"),
                Subject("ids5_fr", "الفرنسي", "🇫🇷", "notes")
            )
        }
        if (grade == 6) {
            return listOf(
                Subject("ids6_isl", "الاسلامية", "🕌", "book"),
                Subject("ids6_ar", "العربي", "📝", "book"),
                Subject("ids6_eng", "الانكليزي", "🇬🇧", "book"),
                Subject("ids6_math", "الرياضيات", "🔢", "book"),
                Subject("ids6_bio", "الاحياء", "🧬", "book"),
                Subject("ids6_chem", "الكيمياء", "🧪", "book"),
                Subject("ids6_phy", "الفيزياء", "⚡", "book"),
                Subject("ids6_fr", "الفرنسي", "🇫🇷", "notes")
            )
        }
        return listOf(
            Subject("ids${grade}_ar", "اللغة العربية", "📝", "book"),
            Subject("ids${grade}_eng", "اللغة الإنجليزية", "🇬🇧", "book"),
            Subject("ids${grade}_math", "الرياضيات", "🔢", "book"),
            Subject("ids${grade}_phy", "الفيزياء", "⚡", "book"),
            Subject("ids${grade}_chem", "الكيمياء", "🧪", "book"),
            Subject("ids${grade}_bio", "الأحياء", "🧬", "book"),
            Subject("ids${grade}_isl", "التربية الإسلامية", "🕌", "book"),
            Subject("ids${grade}_hist", "التاريخ", "📜", "notes")
        )
    }

    private fun subjectsAdabi(grade: Int): List<Subject> {
        if (grade == 6) {
            return listOf(
                Subject("ida6_isl", "الاسلامية", "🕌", "book"),
                Subject("ida6_ar", "العربي", "📝", "book"),
                Subject("ida6_eng", "الانكليزي", "🇬🇧", "book"),
                Subject("ida6_math", "الرياضيات", "🔢", "book"),
                Subject("ida6_hist", "التاريخ", "📜", "book"),
                Subject("ida6_geo", "الجغرافية", "🌍", "book"),
                Subject("ida6_econ", "الاقتصاد", "💹", "book")
            )
        }
        return listOf(
            Subject("ida${grade}_ar", "اللغة العربية", "📝", "book"),
            Subject("ida${grade}_eng", "اللغة الإنجليزية", "🇬🇧", "book"),
            Subject("ida${grade}_hist", "التاريخ", "📜", "book"),
            Subject("ida${grade}_geo", "الجغرافية", "🌍", "book"),
            Subject("ida${grade}_philo", "الفلسفة والمنطق", "🧠", "book"),
            Subject("ida${grade}_psych", "علم النفس والاجتماع", "🤝", "notes"),
            Subject("ida${grade}_isl", "التربية الإسلامية", "🕌", "book"),
            Subject("ida${grade}_econ", "الاقتصاد", "💹", "notes")
        )
    }
}
