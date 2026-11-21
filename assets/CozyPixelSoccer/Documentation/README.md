# Cozy Pixel Soccer — Asset Pack v1 (alpha)

Gaya: Style DNA dari `top-down-pack-2.png` (outline maroon #390c14, chibi kepala besar, palet hangat-autumn). Lihat `STYLE_DNA.json`.

## Konvensi
- Sprite pemain: cell 32x32, pivot (16,28). Sheet 4 kolom; baris didefinisikan di `*.json` (nama, jumlah frame, fps, loop).
- Arah: down, up, left, right (left = flipX right, sudah dirender).
- Orientasi lapangan: horizontal (gawang kiri/kanan), 640x384 per stadion, 1 tile = 16 px.
- Semua PNG RGBA, filter Nearest, skala integer. UI `_9slice`: border 6 px (panel/tooltip) / 5 px (button).
- Audio: WAV 22.05 kHz + OGG. Loop: lihat `Audio/audio_meta.json`.

## Catatan jujur
- Seluruh aset dibuat prosedural (kode); kualitas = fondasi konsisten, belum seperti hasil tangan artis. Master template karakter ada di `Player_ST`.
- BGM/SFX sintetis chiptune = placeholder.
- Stadium Sunset/Night = color-grade dari Day (bukan palet terkunci); QA palet ada di `QA_REPORT.json`.
- Dive kiper (Dive_Left/Right) hadap depan & tubuh horizontal; putar 90° bila pitch vertikal.
- Papan iklan memakai merek fiktif generik; logo studio = placeholder.
- Ikon per-passive (10 skill) belum dibuat; ikon Passive/Skill/Special generik tersedia.
