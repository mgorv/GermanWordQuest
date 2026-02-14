# GermanWordQuest 🇩🇪

**GermanWordQuest** is a modern, interactive native Android word puzzle game designed to make learning German vocabulary fun and effective. Built entirely with **Kotlin** and **Jetpack Compose**, it features a clean, elegant UI and real-time cloud data integration.

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9.0-purple?style=for-the-badge&logo=kotlin" />
  <img src="https://img.shields.io/badge/Android-Jetpack_Compose-green?style=for-the-badge&logo=android" />
  <img src="https://img.shields.io/badge/Firebase-Firestore-orange?style=for-the-badge&logo=firebase" />
</p>

## ✨ Features

-   **Interactive Word Search Grid:** A custom-built algorithm generates dynamic 10x10 grids based on the current level's vocabulary.
-   **Smart German Logic:** Automatically handles German special characters in the grid (e.g., `ß` → `SS`, `Ä` → `A`) while preserving correct spelling in the learning list.
-   **Cloud-Based Vocabulary:** Fetches 140+ words from **Firebase Firestore**, organized into categories (Food, Travel, Nature, Basics, etc.).
-   **Progress Tracking:** Uses local storage (`SharedPreferences`) to remember learned words and unlock new levels.
-   **Text-to-Speech (TTS):** Tap any word to hear its native German pronunciation.
-   **Elegant UI:** Features a soft, pastel color palette that changes dynamically or can be customized in Settings.
-   **Full-Screen Dictionary:** A dedicated hub to review all learned words, categorized for easy study.

## 🛠 Tech Stack

-   **Language:** Kotlin
-   **UI Framework:** Jetpack Compose (Material3)
-   **Backend/Database:** Firebase Firestore
-   **Architecture:** MVVM (Model-View-ViewModel) pattern
-   **Audio:** Android TextToSpeech API
-   **Navigation:** Compose Navigation

## 🚀 Getting Started

To run this project locally, follow these steps:

### 1. Clone the Repository
```bash
git clone https://github.com/mgorv/GermanWordQuest.git


