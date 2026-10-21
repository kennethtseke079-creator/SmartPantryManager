# Smart Pantry Manager

Android app (Java, SQLite) for Mobile App Development 700, Practical Assignment 1.

Name: Kenneth Tseke
Student number: 402311145

## What it does
You add the ingredients you have at home. The app only suggests recipes you can
cook with what you have: every ingredient must be in your pantry in the amount
the recipe needs.

## Screens
1. Pantry list - shows your ingredients
2. Add / edit ingredient
3. Suggested recipes
4. Recipe detail
5. Settings

## Features
- Add, edit, delete and view ingredients (saved in SQLite)
- 19 recipes loaded the first time the app runs
- Strict recipe matching (all ingredients and amounts must be met)
- Works with plural names (tomato / tomatoes) and units (kg / g, tbsp / ml)
- Message shown when no recipe matches
- Settings saved with SharedPreferences

## Database
SQLite, because the data is only used on the phone and it is built into Android.

Tables: `ingredients`, `recipes`, `recipe_ingredients`

## How to run
1. Clone: `git clone https://github.com/kennethtseke079-creator/SmartPantryManager.git`
2. Open in Android Studio and let Gradle sync
3. Run the `app` configuration on a phone or emulator (Android 8.1 or newer)

Unit tests: `./gradlew test`
