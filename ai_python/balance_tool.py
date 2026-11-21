#!/usr/bin/env python3
"""
Tool sederhana untuk balancing ekonomi & strength rating.
Jalankan: python balance_tool.py
"""

def simulate_economy(matches=20, win_rate=0.55, recruit_count=1):
    coins = 500
    for m in range(matches):
        if m / matches < win_rate:
            coins += 100
        else:
            coins += 25
        # tournament bonus every 4 matches
        if (m + 1) % 4 == 0:
            coins += 500
    # recruit cost
    coins -= 1200 * recruit_count
    # upgrade cost estimate (10 upgrades)
    coins -= 50 * 5 + 100 * 5
    return coins

def strength_rating(stats: dict) -> float:
    keys = ["Pace", "Shooting", "Passing", "Dribbling", "Defense", "Physical"]
    vals = [stats.get(k, 50) for k in keys]
    return sum(vals) / len(vals)

if __name__ == "__main__":
    print("=== Economy simulation after 20 matches ===")
    print(f"Final coins (approx): {simulate_economy()}")
    print()
    print("=== Sample OVR ===")
    print("Default ST:", strength_rating({"Pace":75,"Shooting":78,"Passing":55,"Dribbling":70,"Defense":30,"Physical":72}))
    print("Rafael:    ", strength_rating({"Pace":82,"Shooting":91,"Passing":58,"Dribbling":75,"Defense":28,"Physical":70}))
    print("Kai:       ", strength_rating({"Pace":78,"Shooting":72,"Passing":94,"Dribbling":90,"Defense":45,"Physical":62}))
