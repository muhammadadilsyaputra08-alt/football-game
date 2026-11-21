-- formations.lua
-- Posisi relatif pemain (normalized 0-1, pitch center = 0.5,0.5)
-- x: 0 = kiri gawang sendiri, 1 = gawang lawan
-- y: 0 = bawah, 1 = atas

local Formations = {}

Formations.list = { "4-3-3", "4-4-2", "4-2-3-1", "3-5-2" }

-- Format: { pos = "GK", x = 0.08, y = 0.50 }
Formations["4-3-3"] = {
  { pos = "GK",  x = 0.08, y = 0.50 },
  { pos = "RB",  x = 0.22, y = 0.18 },
  { pos = "CB",  x = 0.18, y = 0.38 },
  { pos = "CB",  x = 0.18, y = 0.62 },
  { pos = "LB",  x = 0.22, y = 0.82 },
  { pos = "CDM", x = 0.35, y = 0.50 },
  { pos = "CM",  x = 0.48, y = 0.35 },
  { pos = "CAM", x = 0.58, y = 0.50 },
  { pos = "RW",  x = 0.72, y = 0.20 },
  { pos = "ST",  x = 0.82, y = 0.50 },
  { pos = "LW",  x = 0.72, y = 0.80 }
}

Formations["4-4-2"] = {
  { pos = "GK",  x = 0.08, y = 0.50 },
  { pos = "RB",  x = 0.22, y = 0.18 },
  { pos = "CB",  x = 0.18, y = 0.38 },
  { pos = "CB",  x = 0.18, y = 0.62 },
  { pos = "LB",  x = 0.22, y = 0.82 },
  { pos = "CM",  x = 0.40, y = 0.28 },
  { pos = "CM",  x = 0.40, y = 0.72 },
  { pos = "CM",  x = 0.52, y = 0.40 },
  { pos = "CM",  x = 0.52, y = 0.60 },
  { pos = "ST",  x = 0.78, y = 0.38 },
  { pos = "ST",  x = 0.78, y = 0.62 }
}

Formations["4-2-3-1"] = {
  { pos = "GK",  x = 0.08, y = 0.50 },
  { pos = "RB",  x = 0.22, y = 0.18 },
  { pos = "CB",  x = 0.18, y = 0.38 },
  { pos = "CB",  x = 0.18, y = 0.62 },
  { pos = "LB",  x = 0.22, y = 0.82 },
  { pos = "CDM", x = 0.35, y = 0.38 },
  { pos = "CDM", x = 0.35, y = 0.62 },
  { pos = "CAM", x = 0.58, y = 0.50 },
  { pos = "RW",  x = 0.65, y = 0.22 },
  { pos = "ST",  x = 0.82, y = 0.50 },
  { pos = "LW",  x = 0.65, y = 0.78 }
}

Formations["3-5-2"] = {
  { pos = "GK",  x = 0.08, y = 0.50 },
  { pos = "CB",  x = 0.18, y = 0.30 },
  { pos = "CB",  x = 0.16, y = 0.50 },
  { pos = "CB",  x = 0.18, y = 0.70 },
  { pos = "CM",  x = 0.38, y = 0.22 },
  { pos = "CDM", x = 0.35, y = 0.50 },
  { pos = "CM",  x = 0.38, y = 0.78 },
  { pos = "CAM", x = 0.55, y = 0.40 },
  { pos = "CAM", x = 0.55, y = 0.60 },
  { pos = "ST",  x = 0.80, y = 0.38 },
  { pos = "ST",  x = 0.80, y = 0.62 }
}

-- Penalty jika pemain di luar job utama (multiplier performa)
Formations.position_penalty = {
  exact   = 1.00,
  similar = 0.90,  -- misal CB di RB
  related = 0.80,  -- misal CM di CAM
  wrong   = 0.65
}

return Formations
