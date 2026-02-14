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
git clone [https://github.com/mgorv/GermanWordQuest.git](https://github.com/mgorv/GermanWordQuest.git)

2. Open in Android Studio
Open Android Studio and select Open an Existing Project, then navigate to the cloned folder.

3. Firebase Setup (Crucial)
This project uses Firebase for data. To make it work:

Go to the Firebase Console.

Create a new project.

Add an Android App using the package name: com.example.germanwordquest.

Download the google-services.json file.

Place the file in the app/ directory of the project.

Enable Firestore Database in the Firebase Console.

4. Build and Run
Sync Gradle files and run the app on an emulator or physical device.

📂 Project Structure
GameScreen.kt: The main entry point containing the UI logic and navigation.

PuzzleLogic.kt: Contains the algorithms for generating the word search grid and placing words randomly.

GameData.kt: Data models representing the words and categories.

ui/theme: Custom pastel color palettes and typography.

🤝 Contributing
Contributions are welcome! If you have ideas for new features or want to add more words to the database logic:

Fork the repository.

Create a new branch (git checkout -b feature/AmazingFeature).

Commit your changes (git commit -m 'Add some AmazingFeature').

Push to the branch (git push origin feature/AmazingFeature).

Open a Pull Request.
