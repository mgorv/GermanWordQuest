package com.example.germanwordquest

import kotlin.random.Random

object PuzzleLogic {
    const val GRID_SIZE = 10

    // Generates a 10x10 grid with hidden words
    fun generateGrid(words: List<String>): List<Char> {
        val grid = arrayOfNulls<Char>(GRID_SIZE * GRID_SIZE)
        // Place longer words first to avoid collisions
        val sortedWords = words.sortedByDescending { it.length }

        for (word in sortedWords) {
            placeWordRecursively(grid, word.uppercase())
        }
        // Fill empty spots with random letters
        return grid.map { it ?: ('A'..'Z').random() }
    }

    private fun placeWordRecursively(grid: Array<Char?>, word: String, attempts: Int = 0) {
        if (attempts > 100) return // Give up if too hard to fit

        val isHorizontal = Random.nextBoolean()
        val startRow = Random.nextInt(GRID_SIZE)
        val startCol = Random.nextInt(GRID_SIZE)

        if (canPlace(grid, word, startRow, startCol, isHorizontal)) {
            for (i in word.indices) {
                val index = if (isHorizontal) {
                    (startRow * GRID_SIZE) + (startCol + i)
                } else {
                    ((startRow + i) * GRID_SIZE) + startCol
                }
                grid[index] = word[i]
            }
        } else {
            placeWordRecursively(grid, word, attempts + 1)
        }
    }

    private fun canPlace(grid: Array<Char?>, word: String, row: Int, col: Int, horizontal: Boolean): Boolean {
        if (horizontal) {
            if (col + word.length > GRID_SIZE) return false
            for (i in word.indices) {
                val charAtLoc = grid[(row * GRID_SIZE) + (col + i)]
                if (charAtLoc != null && charAtLoc != word[i]) return false
            }
        } else {
            if (row + word.length > GRID_SIZE) return false
            for (i in word.indices) {
                val charAtLoc = grid[((row + i) * GRID_SIZE) + col]
                if (charAtLoc != null && charAtLoc != word[i]) return false
            }
        }
        return true
    }
}