package com.codemir.multiplayer.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.codemir.multiplayer.Multiplayer.*;

public abstract class Monster extends Sprite {
    protected World world;
    protected Body body;
    protected Fixture fixture;

    public Monster(Texture texture, int width, int height, World world) {
        super(texture);
        this.world = world;

        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();

        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(getX(), getY());

        body = world.createBody(bdef);

        shape.setAsBox(width / 2 / PPM, height / 2 / PPM);

        fdef.shape = shape;
        fdef.isSensor = true;

        fixture = body.createFixture(fdef);
    }

    public abstract void update(float dt);
    public abstract void chase();
    public abstract boolean hasMoved();
    public abstract void setHealth(int health);
    public abstract void onHit(String userData, float wx, float wy);
    public abstract void noHit();
    public abstract void destroy();
}