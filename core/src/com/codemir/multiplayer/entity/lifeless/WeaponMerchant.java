package com.codemir.multiplayer.entity.lifeless;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.codemir.multiplayer.Multiplayer;

public class WeaponMerchant extends Sprite implements Disposable {
    private float x;
    private float y;

    public WeaponMerchant(int x, int y, int w, int h, Texture texture) {
        super(texture);
        this.x = x;
        this.y = y;

        setBounds(x / Multiplayer.PPM, y / Multiplayer.PPM, w / Multiplayer.PPM, h / Multiplayer.PPM);
    }

    @Override
    public void dispose() {

    }
}
