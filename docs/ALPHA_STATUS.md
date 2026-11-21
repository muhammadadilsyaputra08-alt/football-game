# Alpha Development Status — Pixel Soccer: Cozy Football

## Yang sudah dibuat

### Data & Config (Lua)
- `lua_scripts/players.lua` — 11 default + 3 recruits + 10 passive skills
- `lua_scripts/formations.lua` — 4 formasi + position penalty
- `lua_scripts/balance.lua` — biaya upgrade, reward, stamina, durasi match

### Python AI
- `ai_python/match_ai.py` — rule-based AI (GK positioning, ball carrier decisions, support runs, pressure, shoot/pass logic)
- `ai_python/balance_tool.py` — simulasi ekonomi & OVR

### Core Model (Java / LibGDX)
- `Player.java` — stats, level, passive, upgrade cost, overall
- `Team.java` — starting XI, bench, formation, recruit
- `SaveData.java` — struktur save offline sesuai GDD §15
- `Ball.java` — fisika bola sederhana
- `SaveManager.java` — JSON local save
- `AssetLoader.java` — queue texture dari asset pack

### Screens
- `MainMenuScreen` — header koin/pemain, navigasi
- `TrainingScreen` — 6 jenis latihan
- `TournamentScreen` — 3 cup, unlock progression, reward
- `ShopScreen` — Leon / Rafael / Kai recruitment
- `TeamScreen` — lineup + ganti formasi
- `PlayersScreen` — list + upgrade statistik

### Launcher
- `DesktopLauncher.java` — window 1280×768 (2× integer scale)

## Yang masih perlu dikerjakan (Phase berikutnya)

1. **MatchScreen penuh** — rendering lapangan 640×384, 22 player sprites, joystick virtual, passing/shooting physics, AI integration (panggil Python atau port rule ke Java).
2. **Sprite animation system** — parse JSON sheet (idle/walk/run/shoot/dive) 32×32.
3. **Skin UI pixel** — ganti uiskin default dengan 9-slice dari asset pack.
4. **Gradle build files** — `build.gradle`, `settings.gradle`, AndroidManifest, agar bisa `./gradlew desktop:run` dan `android:assembleDebug`.
5. **Sound** — load BGM/SFX dari asset pack (Audio/).
6. **Mini-game training** — implementasi aktual shooting/passing course.
7. **Bracket visual** — tournament bracket 4/8 tim.
8. **Chemistry / Captain / PoTM** — fitur rekomendasi GDD §18.

## Cara lanjut

1. Generate proyek LibGDX resmi (gdx-setup) dengan package `com.cozypixel.soccer`.
2. Copy seluruh folder `core/src`, `desktop/src`, `lua_scripts`, `ai_python` ke dalam proyek.
3. Letakkan asset pack di `android/assets/CozyPixelSoccer/`.
4. Tambah dependency LuaJ (opsional) dan pastikan `uiskin.json` ada atau buat programmatic skin.
5. Implementasikan `MatchScreen` sebagai prioritas utama Phase 1.

## Catatan desain penting (dari GDD)

- Setiap pemain punya identitas & potensi berbeda (max level, passive).
- Common yang di-upgrade tetap bisa berguna.
- Training tidak butuh energi.
- Shop rotasi setelah turnamen (alpha: stok tetap 3 pemain).
- Offline total, local save.
