# Pixel Soccer: Cozy Football (Alpha)

Game sepak bola 2D top-down pixel art cozy untuk Android, dibangun dengan **LibGDX + Lua + Python AI**.

## Konsep (sesuai GDD)

- **Arcade Football** + Team Management + Player Collection + Tournament
- Offline, single player, landscape, virtual joystick
- Bahasa Indonesia
- Local save
- 11 pemain default + recruitment (Leon, Rafael, Kai)
- Training mini-games + 3 turnamen knockout
- Passive skill unik, level 1–20, upgrade statistik dengan koin

## Struktur Proyek

```
PixelSoccerCozy/
├── core/                  # Kode bersama (LibGDX)
│   └── src/com/cozypixel/soccer/
│       ├── model/         # Player, Team, Match, SaveData
│       ├── entity/        # Ball, PlayerEntity, Goal
│       ├── system/        # Physics, AI, Stamina, Formation
│       ├── screen/        # MainMenu, Match, Team, Shop, Training, Tournament
│       ├── ai/            # Interface ke Python AI / rule-based
│       ├── ui/            # HUD, Joystick, Panels
│       └── util/          # AssetLoader, SaveManager
├── desktop/               # Launcher desktop (testing cepat)
├── android/               # Modul Android
├── lua_scripts/           # Konfigurasi balance & data
├── ai_python/             # AI lawan + balancing tools
├── assets/                # (symlink / copy dari CozyPixelSoccer_AssetPack)
└── docs/
```

## Asset Pack

Gunakan folder `CozyPixelSoccer/` dari asset pack yang dilampirkan.

Konvensi penting (dari README asset):
- Sprite pemain: 32×32, pivot (16,28)
- Arah: down / up / left / right
- Lapangan: 640×384 (1 tile = 16 px)
- Filter: Nearest, scale integer
- UI 9-slice: border 5–6 px

## Cara Setup

### 1. Prerequisites
- JDK 17+ (direkomendasikan 21)
- Android Studio / IntelliJ
- LibGDX Project Generator (atau copy struktur ini)
- (Opsional) LuaJ untuk scripting runtime

### 2. Import Asset
```bash
# Dari root proyek
cp -r /path/to/CozyPixelSoccer assets/
# atau di Android Studio: letakkan di android/assets/
```

### 3. Build Desktop (testing)
```bash
./gradlew desktop:run
```

### 4. Build Android APK
```bash
./gradlew android:assembleDebug
```

## Fitur Alpha yang Diimplementasikan

| Fitur                    | Status |
|--------------------------|--------|
| Core match (top-down)    | ✅     |
| Joystick + action buttons| ✅     |
| 11 default players       | ✅     |
| Formasi 4-3-3, 4-4-2...  | ✅     |
| Training mini-games      | ✅     |
| Tournament (3 cups)      | ✅     |
| Shop + Recruitment       | ✅     |
| Level & Stat Upgrade     | ✅     |
| Passive Skills           | ✅     |
| Local Save (JSON)        | ✅     |
| Stamina system           | ✅     |
| AI rule-based + Python   | ✅     |
| Multiplayer / Online     | ❌     |

## Python AI

Folder `ai_python/` berisi:
- `match_ai.py` — decision tree untuk AI lawan (pass, shoot, tackle, positioning)
- `balance_tool.py` — simulasi ekonomi & strength rating
- `export_to_java.py` — generate table untuk dibaca Java

Jalankan contoh:
```bash
cd ai_python
python match_ai.py --demo
```

## Lua Scripts

- `players.lua` — data default + recruits + passive
- `formations.lua` — posisi relative per formasi
- `balance.lua` — biaya upgrade, reward turnamen, XP training
- `passives.lua` — efek passive skill

## Target Alpha

Pemain bisa:
1. Mainkan pertandingan arcade
2. Latih pemain (Shooting / Passing / Dribbling / GK)
3. Ikuti turnamen → dapat koin
4. Rekrut pemain baru (Leon / Rafael / Kai)
5. Upgrade statistik
6. Simpan progres offline

---

Dibuat sesuai Game Design Document — Pixel Soccer: Cozy Football.
Engine: LibGDX + Lua (config) + Python (AI).

## GitHub Actions CI

Workflow tersedia di `.github/workflows/build.yml`.

Jobs:
1. **build-desktop** — compile core + desktop, package JAR
2. **test-python-ai** — jalankan `match_ai.py --demo` + balance tool
3. **build-android** — assembleDebug APK (butuh Android SDK di runner)

Trigger: push ke `main` / `master` / `develop` / `alpha`, pull request, atau manual (`workflow_dispatch`).

Artifact yang di-upload:
- `desktop-jar` → JAR runnable
- `android-apk` → APK debug

### Cara setup repo

```bash
# 1. Buat repo di GitHub
# 2. Clone / push proyek ini
git init
git add .
git commit -m "Initial alpha: Pixel Soccer Cozy"
git branch -M main
git remote add origin https://github.com/USERNAME/PixelSoccerCozy.git
git push -u origin main

# 3. GitHub Actions akan otomatis jalan
```

### Build lokal

```bash
# Install Gradle 8.5+ atau gunakan wrapper
gradle :core:compileJava
gradle :desktop:compileJava
gradle :desktop:dist          # → desktop/build/libs/*.jar
gradle :desktop:run           # jalankan game

# Android (butuh SDK + local.properties)
gradle :android:assembleDebug
```

### Catatan asset

Letakkan isi asset pack `CozyPixelSoccer/` ke folder `assets/CozyPixelSoccer/`  
(atau symlink). Folder Audio/BGM berukuran besar; boleh di-exclude dari git.

