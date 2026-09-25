package com.example.engine

import com.example.model.Player
import com.example.model.ValidationStatus
import java.text.Normalizer

data class ValidationResult(
    val status: ValidationStatus,
    val reason: String? = null
)

class AnswerValidator {

    /**
     * Validates the opponent's answer (OUI / NON) against the secret player's verified attributes.
     * Follows the 3 strict cases from the specification:
     * - CAS 1: Question understood + answer likely wrong -> LIKELY_INCORRECT
     * - CAS 2: Question understood + answer consistent -> CORRECT
     * - CAS 3: Question ambiguous, not understood, or unverified -> UNKNOWN
     */
    fun validateAnswer(secretPlayer: Player, question: String, answer: String): ValidationResult {
        val normQ = normalize(question)
        val isYes = answer.trim().equals("OUI", ignoreCase = true)
        val isNo = answer.trim().equals("NON", ignoreCase = true)

        if (!isYes && !isNo) {
            return ValidationResult(ValidationStatus.UNKNOWN, "Réponse non standard.")
        }

        // 1. Position checks
        if (normQ.contains("attaquant") || normQ.contains("buteur") || normQ.contains("avant centre")) {
            val isStriker = secretPlayer.position.equals("Attaquant", ignoreCase = true)
            return checkFact(isStriker, isYes, "Poste (Attaquant)")
        }
        if (normQ.contains("gardien") || normQ.contains("portier")) {
            val isKeeper = secretPlayer.position.equals("Gardien", ignoreCase = true)
            return checkFact(isKeeper, isYes, "Poste (Gardien)")
        }
        if (normQ.contains("defenseur") || normQ.contains("arriere") || normQ.contains("lateral")) {
            val isDefender = secretPlayer.position.equals("Défenseur", ignoreCase = true)
            return checkFact(isDefender, isYes, "Poste (Défenseur)")
        }
        if (normQ.contains("milieu") || normQ.contains("meneur")) {
            val isMidfielder = secretPlayer.position.equals("Milieu", ignoreCase = true)
            return checkFact(isMidfielder, isYes, "Poste (Milieu)")
        }

        // 2. Status checks
        if (normQ.contains("retraite") || normQ.contains("raccroche") || normQ.contains("fini sa carriere")) {
            val isRetired = secretPlayer.isRetired
            return checkFact(isRetired, isYes, "Statut (Retraité)")
        }
        if (normQ.contains("actif") || normQ.contains("activite") || normQ.contains("joue encore") || normQ.contains("actuel")) {
            val isActive = secretPlayer.isActive
            return checkFact(isActive, isYes, "Statut (En activité)")
        }

        // 3. Trophies & Awards (only when verified in data)
        if (normQ.contains("coupe du monde") || normQ.contains("champion du monde") || normQ.contains("world cup")) {
            if (secretPlayer.worldCup != null) {
                val hasWonWorldCup = secretPlayer.worldCup > 0
                return checkFact(hasWonWorldCup, isYes, "Coupe du Monde")
            }
        }
        if (normQ.contains("ballon d or") || normQ.contains("ballon dor") || normQ.contains("ballon d'or")) {
            if (secretPlayer.ballonDor != null) {
                val hasBallonDor = secretPlayer.ballonDor > 0
                return checkFact(hasBallonDor, isYes, "Ballon d'Or")
            }
        }
        if (normQ.contains("champions league") || normQ.contains("ligue des champions") || normQ.contains("ldc")) {
            if (secretPlayer.championsLeague != null) {
                val hasWonUcl = secretPlayer.championsLeague > 0
                return checkFact(hasWonUcl, isYes, "Ligue des Champions")
            }
        }

        // 4. Continents & Regions
        if (normQ.contains("europeen") || normQ.contains("europe")) {
            val isEuropean = isEuropeanNationality(secretPlayer.nationality)
            return checkFact(isEuropean, isYes, "Continent (Europe)")
        }
        if (normQ.contains("sud americain") || normQ.contains("amerique du sud")) {
            val isSouthAmerican = isSouthAmericanNationality(secretPlayer.nationality)
            return checkFact(isSouthAmerican, isYes, "Continent (Amérique du Sud)")
        }
        if (normQ.contains("africain") || normQ.contains("afrique")) {
            val isAfrican = isAfricanNationality(secretPlayer.nationality)
            return checkFact(isAfrican, isYes, "Continent (Afrique)")
        }

        // 5. Specific Nationalities
        val nat = secretPlayer.nationality?.lowercase()?.trim() ?: ""
        val nationalityMap = mapOf(
            "bresil" to listOf("bresilien", "bresil", "brazil", "brazilian"),
            "argentine" to listOf("argentin", "argentine", "argentina"),
            "france" to listOf("francais", "francaise", "france", "french"),
            "espagne" to listOf("espagnol", "espagnole", "espagne", "spanish", "spain"),
            "italie" to listOf("italien", "italienne", "italie", "italian", "italy"),
            "allemagne" to listOf("allemand", "allemande", "allemagne", "german", "germany"),
            "angleterre" to listOf("anglais", "anglaise", "angleterre", "english", "england"),
            "portugal" to listOf("portugais", "portugaise", "portugal", "portuguese"),
            "pays bas" to listOf("neerlandais", "hollandais", "pays bas", "dutch", "netherlands"),
            "belgique" to listOf("belge", "belgique", "belgian", "belgium"),
            "croatie" to listOf("croate", "croatie", "croatian", "croatia"),
            "algerie" to listOf("algerien", "algerienne", "algerie", "algerian", "algeria"),
            "maroc" to listOf("marocain", "marocaine", "maroc", "moroccan", "morocco"),
            "senegal" to listOf("senegalais", "senegal", "senegalese"),
            "nigeria" to listOf("nigerian", "nigeriane", "nigeria"),
            "cameroun" to listOf("camerounais", "cameroun", "cameroon")
        )

        for ((countryKey, aliases) in nationalityMap) {
            val asksAboutCountry = aliases.any { alias -> normQ.contains(alias) }
            if (asksAboutCountry) {
                val isPlayerOfCountry = aliases.any { alias -> nat.contains(alias) || normalize(nat) == countryKey }
                return checkFact(isPlayerOfCountry, isYes, "Nationalité ($countryKey)")
            }
        }

        // 6. Strong Foot (if recorded)
        if (normQ.contains("gaucher") || normQ.contains("pied gauche")) {
            if (secretPlayer.strongFoot != null) {
                val isLeftFooted = secretPlayer.strongFoot.equals("Gauche", ignoreCase = true) ||
                        secretPlayer.strongFoot.equals("Deux pieds", ignoreCase = true)
                return checkFact(isLeftFooted, isYes, "Pied gauche")
            }
        }
        if (normQ.contains("droitier") || normQ.contains("pied droit")) {
            if (secretPlayer.strongFoot != null) {
                val isRightFooted = secretPlayer.strongFoot.equals("Droit", ignoreCase = true) ||
                        secretPlayer.strongFoot.equals("Deux pieds", ignoreCase = true)
                return checkFact(isRightFooted, isYes, "Pied droit")
            }
        }

        // 7. Clubs check (only when player has known club list)
        val clubQueries = mapOf(
            "real madrid" to listOf("real madrid", "real"),
            "barcelona" to listOf("barcelone", "barcelona", "barca"),
            "bayern" to listOf("bayern", "bayern munich"),
            "manchester united" to listOf("manchester united", "man united", "man utd"),
            "manchester city" to listOf("manchester city", "man city"),
            "paris saint germain" to listOf("psg", "paris saint germain", "paris sg"),
            "juventus" to listOf("juventus", "juve"),
            "milan" to listOf("ac milan", "milan"),
            "inter" to listOf("inter milan", "inter"),
            "chelsea" to listOf("chelsea"),
            "arsenal" to listOf("arsenal"),
            "liverpool" to listOf("liverpool"),
            "dortmund" to listOf("dortmund", "borussia dortmund"),
            "atletico" to listOf("atletico madrid", "atletico")
        )

        for ((_, aliases) in clubQueries) {
            if (aliases.any { alias -> normQ.contains(alias) }) {
                if (secretPlayer.clubs.isNotEmpty()) {
                    val playedThere = secretPlayer.clubs.any { club ->
                        val normClub = normalize(club)
                        aliases.any { alias -> normClub.contains(alias) }
                    }
                    return checkFact(playedThere, isYes, "Club (${aliases[0]})")
                }
            }
        }

        // CAS 3: Custom or ambiguous question that cannot be confirmed safely
        return ValidationResult(
            status = ValidationStatus.UNKNOWN,
            reason = "Question libre ou données non certifiées pour cette question."
        )
    }

    private fun checkFact(factIsTrue: Boolean, answeredYes: Boolean, criteria: String): ValidationResult {
        return if (factIsTrue == answeredYes) {
            ValidationResult(ValidationStatus.CORRECT, "Réponse cohérente avec $criteria.")
        } else {
            ValidationResult(
                ValidationStatus.LIKELY_INCORRECT,
                "⚠️ Réponse probablement incorrecte d'après les données vérifiées pour : $criteria."
            )
        }
    }

    private fun isEuropeanNationality(nationality: String?): Boolean {
        if (nationality == null) return false
        val euro = setOf(
            "france", "germany", "allemagne", "spain", "espagne", "italy", "italie",
            "england", "angleterre", "portugal", "netherlands", "pays-bas", "belgium",
            "belgique", "croatia", "croatie", "poland", "pologne", "sweden", "suède",
            "denmark", "danemark", "norway", "norvège", "czech republic", "hungary",
            "bulgaria", "romania", "ukraine", "serbia", "slovenia", "georgia",
            "wales", "northern ireland", "ireland", "montenegro", "soviet union"
        )
        return euro.contains(nationality.lowercase().trim())
    }

    private fun isSouthAmericanNationality(nationality: String?): Boolean {
        if (nationality == null) return false
        val sa = setOf(
            "argentina", "argentine", "brazil", "brésil", "uruguay", "colombia",
            "colombie", "chile", "chili", "paraguay"
        )
        return sa.contains(nationality.lowercase().trim())
    }

    private fun isAfricanNationality(nationality: String?): Boolean {
        if (nationality == null) return false
        val af = setOf(
            "egypt", "égypte", "algeria", "algérie", "morocco", "maroc", "senegal",
            "sénégal", "nigeria", "cameroon", "cameroun", "ivory coast", "côte d'ivoire",
            "ghana", "togo", "liberia", "zambia"
        )
        return af.contains(nationality.lowercase().trim())
    }

    private fun normalize(text: String): String {
        val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
        return decomposed.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .replace("'", " ")
            .replace("’", " ")
            .replace("-", " ")
            .replace("?", "")
            .replace("!", "")
            .replace(".", "")
            .replace(",", "")
            .trim()
            .replace("\\s+".toRegex(), " ")
    }
}
