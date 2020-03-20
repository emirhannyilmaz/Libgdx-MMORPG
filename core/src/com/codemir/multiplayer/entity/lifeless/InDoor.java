package com.codemir.multiplayer.entity.lifeless;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.codemir.multiplayer.Multiplayer;
import com.codemir.multiplayer.screens.GameScreen;
import com.codemir.multiplayer.entity.InteractiveTileObject;

public class InDoor extends InteractiveTileObject {
    private Multiplayer game;
    private String targetRoom;

    public InDoor(World world, TiledMap map, Rectangle bounds, Multiplayer game, String targetRoom) {
        super(world, map, bounds);
        this.game = game;
        this.targetRoom = targetRoom;

        fixture.setUserData(this);
    }

    @Override
    public void onHeadHit() {
    }

    @Override
    public void noHeadHit() {
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && targetRoom.equals("Town"))
            game.setScreen(new GameScreen(game));
    }
}