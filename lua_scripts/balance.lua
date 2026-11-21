-- balance.lua
-- Nilai ekonomi, XP, reward, biaya upgrade (alpha balancing)

local Balance = {}

-- Biaya upgrade per level range (per 1 poin atribut)
Balance.upgrade_cost = {
  { min = 1,  max = 5,  cost = 50 },
  { min = 6,  max = 10, cost = 100 },
  { min = 11, max = 15, cost = 175 },
  { min = 16, max = 20, cost = 250 }
}

function Balance.get_upgrade_cost(level)
  for _, tier in ipairs(Balance.upgrade_cost) do
    if level >= tier.min and level <= tier.max then
      return tier.cost
    end
  end
  return 300
end

-- Reward pertandingan
Balance.match_reward = {
  win   = 100,
  lose  = 25,
  draw  = 40
}

-- Bonus turnamen
Balance.tournament_bonus = {
  ["Beginner Cup"] = {
    champion = 500, runner_up = 200, semi = 100
  },
  ["Cozy League Cup"] = {
    champion = 1200, runner_up = 500, semi = 250
  },
  ["Grand Pixel Cup"] = {
    champion = 2500, runner_up = 1000, semi = 400
  }
}

-- Achievement bonus
Balance.achievements = {
  first_win       = 150,
  clean_sheet     = 80,
  hat_trick       = 200,
  comeback        = 120,
  perfect_tourney = 400
}

-- XP dari training (alpha: kecil agar turnamen tetap utama)
Balance.training_xp = {
  S = 12,
  A = 8,
  B = 5,
  C = 2
}

-- Stamina
Balance.stamina = {
  max = 100,
  sprint_drain = 0.35,   -- per detik
  run_drain    = 0.08,
  recover_rate = 0.15,   -- saat idle / jalan pelan
  low_threshold = 25     -- di bawah ini speed & response turun
}

-- Durasi match (detik real time)
Balance.match_duration = {
  half = 180,            -- 3 menit per babak
  extra = 60,            -- extra time singkat
  penalty_kicks = 5
}

-- Pitch size (pixel, sesuai asset)
Balance.pitch = {
  width  = 640,
  height = 384,
  goal_width = 64,
  goal_depth = 16
}

return Balance
