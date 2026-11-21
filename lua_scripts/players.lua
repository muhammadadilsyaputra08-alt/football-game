-- players.lua
-- Data pemain default + recruitable + passive skill definitions
-- Digunakan oleh LibGDX via LuaJ atau di-load sebagai JSON setara

local Players = {}

-- ============================================================
-- DEFAULT 11 PLAYERS (Cozy FC)
-- ============================================================
Players.default = {
  {
    id = "GK", name = "Bima", pos = "GK", job = "Goalkeeper", num = 1,
    hair = "black", style = "short", skin = "s3",
    level = 1, maxLevel = 18,
    stats = { Reflex = 72, Handling = 68, Positioning = 70, Diving = 65, Distribution = 60, Physical = 68 },
    passive = nil, rarity = "Common", price = 0
  },
  {
    id = "RB", name = "Raka", pos = "RB", job = "Right Back", num = 2,
    hair = "brown", style = "short", skin = "s2",
    level = 1, maxLevel = 16,
    stats = { Pace = 70, Shooting = 35, Passing = 62, Dribbling = 55, Defense = 72, Physical = 68 },
    passive = nil, rarity = "Common", price = 0
  },
  {
    id = "CB1", name = "Dimas", pos = "CB", job = "Center Back", num = 3,
    hair = "black", style = "bob", skin = "s3",
    level = 1, maxLevel = 17,
    stats = { Pace = 58, Shooting = 30, Passing = 55, Dribbling = 48, Defense = 78, Physical = 80 },
    passive = nil, rarity = "Common", price = 0
  },
  {
    id = "CB2", name = "Arga", pos = "CB", job = "Center Back", num = 4,
    hair = "brown", style = "spiky", skin = "s4",
    level = 1, maxLevel = 17,
    stats = { Pace = 60, Shooting = 32, Passing = 58, Dribbling = 50, Defense = 76, Physical = 78 },
    passive = nil, rarity = "Common", price = 0
  },
  {
    id = "LB", name = "Fajar", pos = "LB", job = "Left Back", num = 5,
    hair = "olive", style = "short", skin = "s1",
    level = 1, maxLevel = 16,
    stats = { Pace = 72, Shooting = 38, Passing = 65, Dribbling = 58, Defense = 70, Physical = 65 },
    passive = nil, rarity = "Common", price = 0
  },
  {
    id = "CDM", name = "Galih", pos = "CDM", job = "Defensive Midfielder", num = 6,
    hair = "brown", style = "round", skin = "s2",
    level = 1, maxLevel = 18,
    stats = { Pace = 65, Shooting = 48, Passing = 70, Dribbling = 62, Defense = 75, Physical = 72 },
    passive = nil, rarity = "Common", price = 0
  },
  {
    id = "CM", name = "Nando", pos = "CM", job = "Central Midfielder", num = 8,
    hair = "ginger", style = "fringe", skin = "s1",
    level = 1, maxLevel = 18,
    stats = { Pace = 68, Shooting = 55, Passing = 75, Dribbling = 70, Defense = 60, Physical = 65 },
    passive = nil, rarity = "Common", price = 0
  },
  {
    id = "CAM", name = "Yoga", pos = "CAM", job = "Attacking Midfielder", num = 10,
    hair = "blonde", style = "fringe", skin = "s2",
    level = 1, maxLevel = 19,
    stats = { Pace = 72, Shooting = 68, Passing = 78, Dribbling = 80, Defense = 42, Physical = 58 },
    passive = nil, rarity = "Common", price = 0
  },
  {
    id = "RW", name = "Satria", pos = "RW", job = "Right Winger", num = 7,
    hair = "sage", style = "spiky", skin = "s3",
    level = 1, maxLevel = 17,
    stats = { Pace = 82, Shooting = 65, Passing = 68, Dribbling = 78, Defense = 35, Physical = 55 },
    passive = nil, rarity = "Common", price = 0
  },
  {
    id = "ST", name = "Bagas", pos = "ST", job = "Striker", num = 9,
    hair = "ginger", style = "spiky", skin = "s2",
    level = 1, maxLevel = 18,
    stats = { Pace = 75, Shooting = 78, Passing = 55, Dribbling = 70, Defense = 30, Physical = 72 },
    passive = nil, rarity = "Common", price = 0
  },
  {
    id = "LW", name = "Tio", pos = "LW", job = "Left Winger", num = 11,
    hair = "black", style = "short", skin = "s3",
    level = 1, maxLevel = 17,
    stats = { Pace = 80, Shooting = 62, Passing = 70, Dribbling = 76, Defense = 32, Physical = 52 },
    passive = nil, rarity = "Common", price = 0
  }
}

-- ============================================================
-- RECRUITABLE PLAYERS
-- ============================================================
Players.recruits = {
  {
    id = "Leon", name = "Leon", title = "The Guardian", rarity = "Rare",
    pos = "GK", job = "Goalkeeper", num = 1,
    level = 1, maxLevel = 18,
    stats = { Reflex = 86, Handling = 79, Positioning = 78, Diving = 82, Distribution = 70, Physical = 75 },
    passive = "Last Hope", price = 1200
  },
  {
    id = "Rafael", name = "Rafael", title = "The Sniper", rarity = "Epic",
    pos = "ST", job = "Striker", num = 9,
    level = 1, maxLevel = 20,
    stats = { Pace = 82, Shooting = 91, Passing = 58, Dribbling = 75, Defense = 28, Physical = 70 },
    passive = "Sniper", price = 2500
  },
  {
    id = "Kai", name = "Kai", title = "The Maestro", rarity = "Legendary",
    pos = "CAM", job = "Attacking Midfielder", num = 10,
    level = 1, maxLevel = 20,
    stats = { Pace = 78, Shooting = 72, Passing = 94, Dribbling = 90, Defense = 45, Physical = 62 },
    passive = "Playmaker", price = 4000
  }
}

-- ============================================================
-- PASSIVE SKILLS
-- ============================================================
Players.passives = {
  ["Quick Feet"]   = { desc = "Akselerasi meningkat saat membawa bola", effect = { accel_mult = 1.12 } },
  ["Sniper"]       = { desc = "Akurasi tembakan dari luar kotak penalti +", effect = { outside_box_acc = 1.25 } },
  ["Iron Wall"]    = { desc = "Kemampuan menghadang lawan meningkat", effect = { tackle_success = 1.18 } },
  ["Playmaker"]    = { desc = "Umpan ke rekan lebih akurat", effect = { pass_acc = 1.15 } },
  ["Eagle Eye"]    = { desc = "Akurasi umpan jauh meningkat", effect = { long_pass_acc = 1.22 } },
  ["Last Hope"]    = { desc = "Refleks kiper naik saat peluang berbahaya", effect = { clutch_reflex = 1.30 } },
  ["Speed Demon"]  = { desc = "Bonus kecepatan saat sprint", effect = { sprint_mult = 1.15 } },
  ["Team Spirit"]  = { desc = "Bonus kecil ke rekan sekitar", effect = { aura_radius = 80, aura_bonus = 0.05 } },
  ["Predator"]     = { desc = "Penyelesaian di dalam kotak penalti +", effect = { inside_box_finish = 1.28 } },
  ["Stamina King"] = { desc = "Penurunan stamina lebih lambat", effect = { stamina_drain = 0.75 } }
}

return Players
