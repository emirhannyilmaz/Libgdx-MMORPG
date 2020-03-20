package com.codemir.multiplayer.tools;

import com.badlogic.gdx.math.Vector2;

public class Funcs {

    public static float distance(Vector2 object1, Vector2 object2) {
        return (float) Math.sqrt(Math.pow((object2.x - object1.x), 2) + Math.pow((object2.y - object1.y), 2));
    }
}