package com.cozypixel.soccer.engine;

import com.badlogic.gdx.math.Vector2;

/** Input pemain manusia (diisi MatchScreen, dibaca MatchEngine). Flag *Pressed dikonsumsi engine. */
public final class HumanInput {
    public final Vector2 move = new Vector2();
    public boolean sprint;
    public boolean shootHeld;
    public boolean shootPressed, passPressed, throughPressed, tacklePressed, switchPressed;

    public void clearEdges() {
        shootPressed = passPressed = throughPressed = tacklePressed = switchPressed = false;
    }
}
