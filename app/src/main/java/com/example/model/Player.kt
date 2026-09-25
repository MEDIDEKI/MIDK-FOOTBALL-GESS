package com.example.model

data class Player(
    val id: String,
    val name: String,
    val nationality: String?,
    val position: String,
    val status: String,
    val difficulty: String,
    val difficultyScore: Int,
    val nationalTeam: String? = null,
    val birthDate: String? = null,
    val strongFoot: String? = null,
    val ballonDor: Int? = null,
    val worldCup: Int? = null,
    val championsLeague: Int? = null,
    val clubs: List<String> = emptyList(),
    val photoUrl: String? = null
) {
    val isRetired: Boolean
        get() = status.equals("Retraité", ignoreCase = true) || status.contains("Retraité", ignoreCase = true)

    val isActive: Boolean
        get() = status.equals("Actif", ignoreCase = true) || status.contains("Actif", ignoreCase = true)

    val countryFlag: String
        get() = when (nationality?.lowercase()?.trim()) {
            "argentina", "argentine" -> "🇦🇷"
            "portugal" -> "🇵🇹"
            "brazil", "brésil" -> "🇧🇷"
            "france" -> "🇫🇷"
            "germany", "allemagne" -> "🇩🇪"
            "netherlands", "pays-bas" -> "🇳🇱"
            "spain", "espagne" -> "🇪🇸"
            "italy", "italie" -> "🇮🇹"
            "england", "angleterre" -> "🏴󠁧󠁢󠁥󠁮󠁧󠁿"
            "belgium", "belgique" -> "🇧🇪"
            "croatia", "croatie" -> "🇭🇷"
            "uruguay" -> "🇺🇾"
            "poland", "pologne" -> "🇵🇱"
            "sweden", "suède" -> "🇸🇪"
            "egypt", "égypte" -> "🇪🇬"
            "algeria", "algérie" -> "🇩🇿"
            "morocco", "maroc" -> "🇲🇦"
            "senegal", "sénégal" -> "🇸🇳"
            "nigeria" -> "🇳🇬"
            "cameroon", "cameroun" -> "🇨🇲"
            "ivory coast", "côte d'ivoire" -> "🇨🇮"
            "colombia", "colombie" -> "🇨🇴"
            "chile", "chili" -> "🇨🇱"
            "mexico", "mexique" -> "🇲🇽"
            "denmark", "danemark" -> "🇩🇰"
            "norway", "norvège" -> "🇳🇴"
            "south korea", "corée du sud" -> "🇰🇷"
            "japan", "japon" -> "🇯🇵"
            "ghana" -> "🇬🇭"
            "czech republic", "république tchèque" -> "🇨🇿"
            "hungary", "hongrie" -> "🇭🇺"
            "bulgaria", "bulgarie" -> "🇧🇬"
            "romania", "roumanie" -> "🇷🇴"
            "ukraine" -> "🇺🇦"
            "serbia", "serbie" -> "🇷🇸"
            "paraguay" -> "🇵🇾"
            "costa rica" -> "🇨🇷"
            "slovenia", "slovénie" -> "🇸🇮"
            "georgia", "géorgie" -> "🇬🇪"
            "wales", "pays de galles" -> "🏴󠁧󠁢󠁷󠁬󠁳󠁿"
            "northern ireland", "irlande du nord" -> "🇬🇧"
            "ireland", "irlande" -> "🇮🇪"
            "australia", "australie" -> "🇦🇺"
            "iran" -> "🇮🇷"
            "togo" -> "🇹🇬"
            "liberia", "libéria" -> "🇱🇷"
            "zambia", "zambie" -> "🇿🇲"
            "montenegro", "monténégro" -> "🇲🇪"
            "soviet union", "urss" -> "🚩"
            else -> "⚽"
        }

    val positionEmoji: String
        get() = when (position.lowercase()) {
            "attaquant" -> "⚡ Attaquant"
            "milieu" -> "🎯 Milieu"
            "défenseur" -> "🛡️ Défenseur"
            "gardien" -> "🧤 Gardien"
            else -> "⚽ $position"
        }
}
