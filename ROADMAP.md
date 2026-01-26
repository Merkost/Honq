# Honq - Development Roadmap

## Immediate Priorities

### 1. Results Screen
After mock test completion, show:
- Score breakdown (passed/failed)
- Time taken
- Questions review with correct/incorrect indicators
- Option to retry or go home

### 2. Review Incorrect Answers
Let users revisit questions they got wrong

### 3. Categories/Topics Filter
Practice by category:
- Road Rules
- Road Signs
- Safety
- Alcohol & Drugs

---

## Data & Backend

### 4. Supabase Integration
Switch from FakeQuestionRepository to real backend

### 5. Offline-First
Proper Room caching so app works offline

### 6. Question Sync
Background sync with conflict resolution

---

## User Experience

### 7. Onboarding Flow
Welcome screens explaining the app

### 8. Empty States
Better UI when no progress or questions

### 9. Haptic Feedback
Vibration on correct/incorrect answers

### 10. Bookmarks
Save difficult questions for later review

---

## Polish

### 11. Splash Screen
Branded app launch experience

### 12. Settings Screen
- Preferences
- Reset progress
- About section

### 13. iOS Testing
Verify everything works on iOS

### 14. Error States
Better error handling and retry UI

---

## Future Features

### 15. Statistics Dashboard
Progress charts over time

### 16. Spaced Repetition
Smart question scheduling based on performance

### 17. Light Theme
Theme toggle option

---

## Tech Stack
- Kotlin Multiplatform (Android + iOS)
- Compose Multiplatform
- FlowMVI for state management
- Room KMP for local database
- Koin for dependency injection
- Supabase for backend
