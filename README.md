# GradeVITian - Android App

A Android application for VIT students to calculate GPA, CGPA, predict grades, and track attendance — built with **Kotlin**, **Jetpack Compose**, and **Firebase**.

> Inspired from [gradeVITian website](https://gradevitian.in)

---

## 📱 Features

| Feature | Description |
|---|---|
| **GPA Calculator** | Calculate semester GPA from course credits and grades (S/A/B/C/D/E/F/N).|
| **CGPA Calculator** | Semester-wise CGPA or instant CGPA from current stats. |
| **CGPA Estimator** | Find the minimum GPA needed next semester to achieve your target CGPA. |
| **Attendance Calculator** | Two modes: Simple (present/absent) and Detailed (total classes + present or absent). |
| **Grade Predictor** | Predict your course grade from CAT, DA, FAT, Lab, and J-Component marks (absolute grading). |
| **Weightage Converter** | Convert marks from one scale to another. |
| **Saved History** | Firebase-synced calculation history for GPA, CGPA, and attendance records. |
| **User Account** | Email/Password and Google Sign-In via Firebase Authentication. |
| **Settings** | Dark mode, dynamic colors (Material You on Android 12+). |

Feel Free to open issue if there is any inaccuracy in calculation.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────┐
│                Compose UI                   │
│      (Screens, Components, Navigation)      │
├─────────────────────────────────────────────┤
│              ViewModels                     │
│        (State management, UI logic)         │
├─────────────────────────────────────────────┤
│            Domain Layer                     │
│ Calculators │ Models │ Repository Interfaces│
├─────────────────────────────────────────────┤
│             Data Layer                      │
│  Firebase Auth │ Firestore │ DataStore      │
└─────────────────────────────────────────────┘
```

**Pattern**: MVVM + Clean Architecture + Repository Pattern  
**DI**: Hilt  
**Async**: Kotlin Coroutines + Flow

### Folder Structure

```
app/src/main/java/me/jitish/gradevitian/
├── GradeVitianApp.kt              # @HiltAndroidApp
├── MainActivity.kt                # @AndroidEntryPoint + Previews
├── data/
│   └── repository/
│       ├── AuthRepositoryImpl.kt       # Firebase Auth implementation
│       ├── RecordsRepositoryImpl.kt    # Firestore CRUD implementation
│       └── PreferencesRepositoryImpl.kt # DataStore preferences
├── inject/
│   ├── FirebaseModule.kt          # Firebase DI providers
│   └── RepositoryModule.kt        # Repository bindings
├── domain/
│   ├── calculator/
│   │   ├── GpaCalculator.kt       # GPA formula
│   │   ├── CgpaCalculator.kt      # CGPA + Instant CGPA
│   │   ├── CgpaEstimator.kt       # Required GPA estimator
│   │   ├── AttendanceCalculator.kt # Attendance % (2 formats)
│   │   └── GradePredictor.kt      # Grade prediction + Weightage converter
│   ├── model/                      # Data classes
│   ├── repository/                 # Repository interfaces
│   └── util/
│       └── Resource.kt             # Success/Error/Loading wrapper
└── ui/
    ├── components/
    │   └── CommonComponents.kt     # Reusable composables
    ├── navigation/
    │   ├── Screen.kt               # Type-safe routes
    │   └── AppNavHost.kt           # Navigation graph
    ├── screens/
    │   ├── auth/                   # Sign In / Sign Up
    │   ├── home/                   # Dashboard grid
    │   ├── gpa/                    # GPA Calculator
    │   ├── cgpa/                   # CGPA Calculator (2 tabs)
    │   ├── estimator/              # CGPA Estimator
    │   ├── attendance/             # Attendance Calculator (2 tabs)
    │   ├── gradepredictor/         # Grade Predictor + Weightage Converter
    │   ├── history/                # Saved records (3 tabs)
    │   ├── profile/                # User profile
    │   ├── settings/               # App settings
    │   └── Previews.kt            # Composable previews
    └── theme/
        ├── Color.kt                # GradeVITian brand palette
        ├── Theme.kt                # M3 Light + Dark + Dynamic
        └── Type.kt                 # Typography
```

---

## 🔥 Firebase Schema

```
Firestore Database:
└── users/
    └── {userId}/
        ├── profile (document fields):
        │   ├── uid: String
        │   ├── displayName: String
        │   ├── email: String
        │   ├── photoUrl: String
        │   ├── university: String ("VIT")
        │   ├── registrationNumber: String
        │   └── createdAt: Long (timestamp)
        │
        ├── gpa_records/ (subcollection)
        │   └── {recordId}/
        │       ├── courses: Array<{credits, grade, ...}>
        │       ├── gpa: Double
        │       ├── totalCredits: Int
        │       └── timestamp: Long
        │
        ├── cgpa_records/ (subcollection)
        │   └── {recordId}/
        │       ├── semesters: Array<{credits, gpa, ...}>
        │       ├── cgpa: Double
        │       ├── totalCredits: Int
        │       └── timestamp: Long
        │
        └── attendance_records/ (subcollection)
            └── {recordId}/
                ├── attendancePercentage: Double
                ├── classesPresent: Int
                ├── classesAbsent: Int
                ├── totalClasses: Int
                └── timestamp: Long
```

**Security Rules** — see `firestore.rules` in project root.

---

## 🚀 Setup Instructions

### Prerequisites
- Android Studio
- JDK 17
- A Firebase project

### Step 1: Clone & Open
```bash
git clone <repo-url>
# Open in Android Studio
```

### Step 2: Configure Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use an existing one)
3. **Add an Android app** with package name: `com.example.appname`
4. Download `google-services.json` and place it in `app/` (replacing the placeholder)
5. Enable these Firebase services:

#### Authentication
- Go to **Authentication → Sign-in method**
- Enable **Email/Password**
- Enable **Google** (set a support email)

#### Cloud Firestore
- Go to **Firestore Database → Create database**
- Start in **production mode**
- Choose your nearest region
- Go to **Rules** tab and paste the contents of `firestore.rules`

#### Analytics & Crashlytics
- These are auto-enabled when you add the app to Firebase

### Step 3: Build & Run
```bash
Run using android studio...
```

---

## 🧮 Calculation Formulas


| Calculator | Formula |
|---|---|
| **GPA** | `Σ(gradePoint × credits) / Σ(credits)` — grades: S=10, A=9, B=8, C=7, D=6, E=5, F=0, N=0 |
| **CGPA** | `Σ(semCredits × semGPA) / Σ(semCredits)` |
| **Instant CGPA** | `(semGPA × semCredits + currentCGPA × completedCredits) / (semCredits + completedCredits)` |
| **CGPA Estimator** | `requiredGPA = (desiredCGPA × (completed + new) - currentCGPA × completed) / new` |
| **Attendance (Simple)** | `present / (present + absent) × 100` |
| **Attendance (Detailed)** | `present / total × 100` or `(total - absent) / total × 100` |
| **Grade Predictor** | Theory: `(CAT1/50×15 + CAT2/50×15 + DA1 + DA2 + DA3 + FAT×40/100)` weighted by credits. Lab: `(internal + FAT×40/50)`. J-comp: `(R1+R2+R3)`. Grade: ≥90=S, ≥80=A, ≥70=B, ≥60=C, ≥55=D, ≥50=E, <50=F |

---

## 🧩 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Async | Coroutines + Flow |
| Auth | Firebase Authentication |
| Database | Cloud Firestore (offline persistence) |
| Preferences | DataStore |
| Navigation | Navigation Compose (type-safe) |
| Analytics | Firebase Analytics |
| Crash Reporting | Firebase Crashlytics |
| Build | Gradle 8.9 + AGP 8.7.3 |

---

## 📦 Extension Guide

### Adding a new calculator
1. Create calculator class in `domain/calculator/`
2. Create model in `domain/model/`
3. Add Firestore collection handling in `RecordsRepository`
4. Create ViewModel + Screen in `ui/screens/`
5. Add route in `Screen.kt` and `AppNavHost.kt`
6. Add card in `HomeScreen.kt`

### Adding a new Firebase collection
1. Add model data class
2. Add methods to `RecordsRepository` interface
3. Implement in `RecordsRepositoryImpl`
4. Update `firestore.rules`
5. Create UI to consume

---

## 📄 License

MIT License. Built with ❤️ for VITians.

