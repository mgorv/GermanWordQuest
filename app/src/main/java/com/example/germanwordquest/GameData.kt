package com.example.germanwordquest

import android.content.Context

// 1. DATA MODEL
data class Word(
    val id: String,
    val german: String,
    val english: String,
    val article: String,
    val category: String
)

// 2. WORD REPOSITORY (100+ Words)
object WordRepository {
    val allWords = listOf(
        // ANIMALS
        Word("ani_1", "HUND", "Dog", "Der", "Animals"),
        Word("ani_2", "KATZE", "Cat", "Die", "Animals"),
        Word("ani_3", "MAUS", "Mouse", "Die", "Animals"),
        Word("ani_4", "VOGEL", "Bird", "Der", "Animals"),
        Word("ani_5", "PFERD", "Horse", "Das", "Animals"),
        Word("ani_6", "KUH", "Cow", "Die", "Animals"),
        Word("ani_7", "SCHWEIN", "Pig", "Das", "Animals"),
        Word("ani_8", "FISCH", "Fish", "Der", "Animals"),

        // FOOD
        Word("food_1", "BROT", "Bread", "Das", "Food"),
        Word("food_2", "WASSER", "Water", "Das", "Food"),
        Word("food_3", "APFEL", "Apple", "Der", "Food"),
        Word("food_4", "MILCH", "Milk", "Die", "Food"),
        Word("food_5", "KAFFEE", "Coffee", "Der", "Food"),
        Word("food_6", "KUCHEN", "Cake", "Der", "Food"),

        // HOUSE
        Word("home_1", "HAUS", "House", "Das", "Home"),
        Word("home_2", "TISCH", "Table", "Der", "Home"),
        Word("home_3", "STUHL", "Chair", "Der", "Home"),
        Word("home_4", "BETT", "Bed", "Das", "Home"),
        Word("home_5", "LAMPE", "Lamp", "Die", "Home"),
        Word("home_6", "FENSTER", "Window", "Das", "Home"),
        Word("home_7", "TUR", "Door", "Die", "Home"),

        // NATURE
        Word("nat_1", "BAUM", "Tree", "Der", "Nature"),
        Word("nat_2", "BLUME", "Flower", "Die", "Nature"),
        Word("nat_3", "WALD", "Forest", "Der", "Nature"),
        Word("nat_4", "MEER", "Sea", "Das", "Nature"),
        Word("nat_5", "BERG", "Mountain", "Der", "Nature"),
        Word("nat_6", "SONNE", "Sun", "Die", "Nature"),

        // FAMILY
        Word("fam_1", "MUTTER", "Mother", "Die", "Family"),
        Word("fam_2", "VATER", "Father", "Der", "Family"),
        Word("fam_3", "KIND", "Child", "Das", "Family"),
        Word("fam_4", "BRUDER", "Brother", "Der", "Family"),
        Word("fam_5", "SCHWESTER", "Sister", "Die", "Family"),

        // SCHOOL
        Word("sch_1", "BUCH", "Book", "Das", "School"),
        Word("sch_2", "STIFT", "Pen", "Der", "School"),
        Word("sch_3", "LEHRER", "Teacher", "Der", "School"),
        Word("sch_4", "HEFT", "Notebook", "Das", "School"),
        Word("sch_5", "TAFEL", "Board", "Die", "School"),

        // BODY
        Word("body_1", "KOPF", "Head", "Der", "Body"),
        Word("body_2", "HAND", "Hand", "Die", "Body"),
        Word("body_3", "FUSS", "Foot", "Der", "Body"),
        Word("body_4", "AUGE", "Eye", "Das", "Body"),
        Word("body_5", "MUND", "Mouth", "Der", "Body")
    )

    fun getRandomSet(count: Int): List<Word> {
        return allWords.shuffled().take(count)
    }
}

// 3. DATABASE MANAGER (Saves Progress)
object ProgressManager {
    private const val PREFS_NAME = "german_game_prefs"
    private const val KEY_LEARNED = "learned_words_ids"

    fun markWordAsLearned(context: Context, wordId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentSet = prefs.getStringSet(KEY_LEARNED, mutableSetOf()) ?: mutableSetOf()
        val newSet = currentSet.toMutableSet()
        newSet.add(wordId)
        prefs.edit().putStringSet(KEY_LEARNED, newSet).apply()
    }

    fun getLearnedWordIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_LEARNED, emptySet()) ?: emptySet()
    }

    fun getLearnedCount(context: Context): Int {
        return getLearnedWordIds(context).size
    }
}