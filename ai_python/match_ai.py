#!/usr/bin/env python3
"""
Pixel Soccer Cozy — Match AI (Python)
Rule-based decision making untuk AI lawan & rekan satu tim.
Dapat diekspor ke Java atau dijalankan via process/socket untuk prototyping.

Usage:
  python match_ai.py --demo
  python match_ai.py --state state.json   # baca state match, output action
"""

from __future__ import annotations
import json
import math
import random
import argparse
from dataclasses import dataclass, field, asdict
from typing import List, Dict, Optional, Tuple
from enum import Enum


class ActionType(str, Enum):
    HOLD = "hold"
    MOVE = "move"
    PASS = "pass"
    SHOOT = "shoot"
    TACKLE = "tackle"
    SPRINT = "sprint"
    SWITCH = "switch"
    THROUGH = "through"


@dataclass
class Vec2:
    x: float
    y: float

    def dist(self, other: "Vec2") -> float:
        return math.hypot(self.x - other.x, self.y - other.y)

    def sub(self, other: "Vec2") -> "Vec2":
        return Vec2(self.x - other.x, self.y - other.y)

    def add(self, other: "Vec2") -> "Vec2":
        return Vec2(self.x + other.x, self.y + other.y)

    def scale(self, s: float) -> "Vec2":
        return Vec2(self.x * s, self.y * s)

    def normalize(self) -> "Vec2":
        d = math.hypot(self.x, self.y)
        if d < 1e-6:
            return Vec2(0, 0)
        return Vec2(self.x / d, self.y / d)


@dataclass
class PlayerState:
    id: str
    team: int                 # 0 = home, 1 = away
    pos: Vec2
    vel: Vec2 = field(default_factory=lambda: Vec2(0, 0))
    has_ball: bool = False
    stamina: float = 100.0
    role: str = "CM"          # GK, CB, RB, LB, CDM, CM, CAM, RW, LW, ST
    stats: Dict[str, float] = field(default_factory=dict)
    is_controlled: bool = False


@dataclass
class BallState:
    pos: Vec2
    vel: Vec2
    owner_id: Optional[str] = None


@dataclass
class MatchState:
    players: List[PlayerState]
    ball: BallState
    score_home: int = 0
    score_away: int = 0
    time_left: float = 180.0
    half: int = 1
    pitch_w: float = 640.0
    pitch_h: float = 384.0


@dataclass
class AIAction:
    player_id: str
    action: ActionType
    target: Optional[Vec2] = None
    target_player_id: Optional[str] = None
    power: float = 0.7          # 0-1 untuk shoot/pass
    confidence: float = 0.5


class MatchAI:
    """Rule-based AI untuk satu tim."""

    def __init__(self, team_id: int = 1, difficulty: float = 0.6):
        self.team_id = team_id
        self.difficulty = max(0.2, min(1.0, difficulty))
        self.aggression = 0.5 + difficulty * 0.4
        self.pass_bias = 0.55
        self.shoot_threshold = 0.65 - difficulty * 0.15

    def decide(self, state: MatchState) -> List[AIAction]:
        actions = []
        my_players = [p for p in state.players if p.team == self.team_id]
        opponents = [p for p in state.players if p.team != self.team_id]

        ball_owner = None
        for p in state.players:
            if p.has_ball or (state.ball.owner_id and p.id == state.ball.owner_id):
                ball_owner = p
                break

        for p in my_players:
            if p.role == "GK":
                actions.append(self._gk_decide(p, state, ball_owner))
            elif ball_owner and ball_owner.id == p.id:
                actions.append(self._ball_carrier_decide(p, state, my_players, opponents))
            else:
                actions.append(self._support_decide(p, state, ball_owner, my_players, opponents))
        return actions

    def _gk_decide(self, gk: PlayerState, state: MatchState, ball_owner: Optional[PlayerState]) -> AIAction:
        goal_x = 0.08 * state.pitch_w if self.team_id == 0 else 0.92 * state.pitch_w
        goal_y = 0.5 * state.pitch_h
        ball = state.ball.pos

        # Jika bola dekat dan datang ke gawang → dive / hold
        dist_ball = gk.pos.dist(ball)
        if dist_ball < 40 and ball_owner is None:
            return AIAction(gk.id, ActionType.MOVE, target=ball, confidence=0.9)
        if dist_ball < 55 and ball_owner and ball_owner.team != self.team_id:
            return AIAction(gk.id, ActionType.TACKLE, target=ball, confidence=0.8)

        # Positioning: ikuti bola di sumbu Y, tetap dekat gawang
        target_y = max(0.25 * state.pitch_h, min(0.75 * state.pitch_h, ball.y))
        target = Vec2(goal_x + (8 if self.team_id == 0 else -8), target_y)
        return AIAction(gk.id, ActionType.MOVE, target=target, confidence=0.7)

    def _ball_carrier_decide(self, p: PlayerState, state: MatchState,
                             teammates: List[PlayerState],
                             opponents: List[PlayerState]) -> AIAction:
        goal_x = 0.92 * state.pitch_w if self.team_id == 0 else 0.08 * state.pitch_w
        goal_y = 0.5 * state.pitch_h
        goal = Vec2(goal_x, goal_y)
        dist_goal = p.pos.dist(goal)

        # Hitung pressure
        pressure = sum(1 for o in opponents if o.pos.dist(p.pos) < 45)
        pressure += sum(0.5 for o in opponents if o.pos.dist(p.pos) < 70)

        # Shooting window
        angle_ok = abs(p.pos.y - goal_y) < 90
        in_box = dist_goal < 120
        outside_ok = dist_goal < 180 and p.stats.get("Shooting", 60) > 70

        shoot_score = 0.0
        if in_box and angle_ok:
            shoot_score = 0.85 + random.uniform(-0.1, 0.1)
        elif outside_ok and angle_ok:
            shoot_score = 0.55 + (p.stats.get("Shooting", 60) / 200)

        if shoot_score > self.shoot_threshold and pressure < 2.5:
            power = 0.75 if in_box else 0.9
            return AIAction(p.id, ActionType.SHOOT, target=goal, power=power, confidence=shoot_score)

        # Pass ke rekan yang lebih baik
        best_pass = None
        best_score = -1.0
        for t in teammates:
            if t.id == p.id or t.role == "GK":
                continue
            d = p.pos.dist(t.pos)
            if d < 25 or d > 220:
                continue
            # ruang ke depan
            forward_bonus = 0.0
            if self.team_id == 0:
                forward_bonus = (t.pos.x - p.pos.x) / state.pitch_w
            else:
                forward_bonus = (p.pos.x - t.pos.x) / state.pitch_w
            # open space
            open_score = 1.0
            for o in opponents:
                if o.pos.dist(t.pos) < 35:
                    open_score -= 0.4
            score = forward_bonus * 1.5 + open_score + (0.2 if t.role in ("ST", "CAM", "RW", "LW") else 0)
            if score > best_score:
                best_score = score
                best_pass = t

        if best_pass and (pressure > 1.5 or random.random() < self.pass_bias):
            return AIAction(p.id, ActionType.PASS, target_player_id=best_pass.id,
                            target=best_pass.pos, power=0.65, confidence=0.75)

        # Dribble / maju
        dir_to_goal = goal.sub(p.pos).normalize()
        # Hindari lawan terdekat
        nearest = min(opponents, key=lambda o: o.pos.dist(p.pos), default=None)
        if nearest and nearest.pos.dist(p.pos) < 40:
            avoid = p.pos.sub(nearest.pos).normalize()
            dir_to_goal = dir_to_goal.add(avoid.scale(0.7)).normalize()

        sprint = p.stamina > 35 and dist_goal > 80
        action = ActionType.SPRINT if sprint else ActionType.MOVE
        target = p.pos.add(dir_to_goal.scale(40))
        return AIAction(p.id, action, target=target, confidence=0.6)

    def _support_decide(self, p: PlayerState, state: MatchState,
                        ball_owner: Optional[PlayerState],
                        teammates: List[PlayerState],
                        opponents: List[PlayerState]) -> AIAction:
        # Positioning berdasarkan role + bola
        ball = state.ball.pos
        home_goal_x = 0.08 * state.pitch_w if self.team_id == 0 else 0.92 * state.pitch_w
        away_goal_x = 0.92 * state.pitch_w if self.team_id == 0 else 0.08 * state.pitch_w

        # Ideal relative position (sangat sederhana)
        role_offsets = {
            "CB":  (0.18, 0.0),
            "RB":  (0.22, -0.28),
            "LB":  (0.22,  0.28),
            "CDM": (0.35, 0.0),
            "CM":  (0.48, 0.0),
            "CAM": (0.58, 0.0),
            "RW":  (0.70, -0.30),
            "LW":  (0.70,  0.30),
            "ST":  (0.78, 0.0),
        }
        ox, oy = role_offsets.get(p.role, (0.45, 0.0))
        if self.team_id == 1:
            ox = 1.0 - ox

        # Blend dengan posisi bola
        target_x = ox * state.pitch_w * 0.6 + ball.x * 0.4
        target_y = 0.5 * state.pitch_h + oy * state.pitch_h * 0.5
        # Clamp
        target_x = max(20, min(state.pitch_w - 20, target_x))
        target_y = max(30, min(state.pitch_h - 30, target_y))

        # Jika lawan pegang bola dekat → press
        if ball_owner and ball_owner.team != self.team_id:
            if p.pos.dist(ball) < 90 and p.role in ("CDM", "CM", "CB", "RB", "LB"):
                return AIAction(p.id, ActionType.MOVE, target=ball, confidence=0.7)
            if p.pos.dist(ball) < 35:
                return AIAction(p.id, ActionType.TACKLE, target=ball, confidence=0.75)

        return AIAction(p.id, ActionType.MOVE, target=Vec2(target_x, target_y), confidence=0.55)


def demo():
    """Demo sederhana state + AI decision."""
    pitch_w, pitch_h = 640.0, 384.0
    players = []

    # Home team (team 0)
    home_roles = ["GK", "RB", "CB", "CB", "LB", "CDM", "CM", "CAM", "RW", "ST", "LW"]
    for i, role in enumerate(home_roles):
        x = 80 + (i % 4) * 40
        y = 60 + (i // 4) * 80
        players.append(PlayerState(
            id=f"H{i}", team=0, pos=Vec2(x, y), role=role,
            stats={"Shooting": 70, "Passing": 70, "Pace": 70}
        ))

    # Away team (team 1) — AI
    for i, role in enumerate(home_roles):
        x = 480 + (i % 4) * 30
        y = 50 + (i // 4) * 70
        players.append(PlayerState(
            id=f"A{i}", team=1, pos=Vec2(x, y), role=role,
            stats={"Shooting": 68, "Passing": 65, "Pace": 72},
            has_ball=(i == 9)  # ST punya bola
        ))

    ball = BallState(pos=Vec2(520, 190), vel=Vec2(0, 0), owner_id="A9")
    state = MatchState(players=players, ball=ball)

    ai = MatchAI(team_id=1, difficulty=0.7)
    actions = ai.decide(state)

    print("=== Pixel Soccer Cozy AI Demo ===")
    print(f"Ball at ({ball.pos.x:.0f},{ball.pos.y:.0f}) owner={ball.owner_id}")
    print("-" * 50)
    for a in actions:
        tgt = f" -> ({a.target.x:.0f},{a.target.y:.0f})" if a.target else ""
        pid = f" pass_to={a.target_player_id}" if a.target_player_id else ""
        print(f"{a.player_id:4s}  {a.action.value:8s}{tgt}{pid}  conf={a.confidence:.2f}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--demo", action="store_true")
    parser.add_argument("--state", type=str, help="JSON match state file")
    parser.add_argument("--difficulty", type=float, default=0.6)
    args = parser.parse_args()

    if args.demo or not args.state:
        demo()
    else:
        with open(args.state) as f:
            data = json.load(f)
        # Minimal loader (bisa diperluas)
        print(json.dumps({"error": "full state loader not implemented in alpha"}, indent=2))
