package com.codemir.multiplayer.entity.lifeless;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.codemir.multiplayer.scenes.Hud;
import com.codemir.multiplayer.entity.InteractiveTileObject;

public class BuyPoint extends InteractiveTileObject {
    private String shopType;

    public BuyPoint(World world, TiledMap map, Rectangle bounds, String shopType) {
        super(world, map, bounds);
        this.shopType = shopType;

        fixture.setUserData(this);
    }

    @Override
    public void onHeadHit() {
    }

    @Override
    public void noHeadHit() {
        Hud.weaponShopTable.setVisible(false);
    }

    @Override
    public void handleInput() {
        if (!Hud.weaponShopTable.isVisible() && Gdx.input.isKeyJustPressed(Input.Keys.E) && shopType.equals("WeaponShop")) {
            Hud.weaponShopTable.setVisible(true);
            Hud.inventoryTable.setVisible(false);
            Hud.myBodyTable.setVisible(false);
        } else if(Hud.weaponShopTable.isVisible() && Gdx.input.isKeyJustPressed(Input.Keys.E) && shopType.equals("WeaponShop")) {
            Hud.weaponShopTable.setVisible(false);
            Hud.inventoryTable.setVisible(false);
            Hud.myBodyTable.setVisible(false);
        }
    }
}