package com.codemir.multiplayer.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.codemir.multiplayer.entity.living.FriendlyPlayer;

import static com.codemir.multiplayer.Multiplayer.*;

public class FPWeapon extends Sprite {
    public enum State { NOATTACK, ATTACKINGRIGHT, ATTACKINGLEFT, ATTACKINGUP, ATTACKINGDOWN };
    public State currentState;
    public State previousState;
    private TextureRegion test;
    private Animation<TextureRegion> swordAttackRight;
    private Animation<TextureRegion> swordAttackLeft;
    private Animation<TextureRegion> swordAttackUp;
    private Animation<TextureRegion> swordAttackDown;
    private float stateTimer;
    private float x;
    private float y;
    public World world;
    public Body body;
    public static int damage;

    public FPWeapon(World world, float x, float y) {
        this.world = world;
        this.x = x;
        this.y = y;
        currentState = State.NOATTACK;
        previousState = State.NOATTACK;
        stateTimer = 0;

        test = new TextureRegion(ironSword, 0, 0, 33, 32);

        setBounds(x + 12 / PPM, y + 48 / PPM, 33 / PPM, 32 / PPM);
        weaponPhysics();
    }

    public void update(float dt, float x, float y) {
        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
        changeTextures();
        setRegion(getFrame(dt, x, y));
    }

    public TextureRegion getFrame(float dt, float x, float y) {
        TextureRegion region;

        switch (currentState) {
            case ATTACKINGRIGHT:
                setAlpha(1);
                //setPosition(x + 24 / PPM, y);
                //body.setTransform(x + 40 / PPM, y + 16 / PPM, 0);
                Gdx.app.postRunnable(new Runnable() {

                    @Override
                    public void run () {
                        body.setActive(true);
                        body.setTransform(x + 40 / PPM, y + 16 / PPM, 0);
                    }
                });
                region = swordAttackRight.getKeyFrame(stateTimer, true);
                break;
            case ATTACKINGLEFT:
                setAlpha(1);
                //setPosition(x - 33 / PPM, y);
                //body.setTransform(x - 16 / PPM, y + 16 / PPM, 0);
                Gdx.app.postRunnable(new Runnable() {

                    @Override
                    public void run () {
                        body.setActive(true);
                        body.setTransform(x - 16 / PPM, y + 16 / PPM, 0);
                    }
                });
                region = swordAttackLeft.getKeyFrame(stateTimer, true);
                break;
            case ATTACKINGUP:
                setAlpha(1);
                //setPosition(x, y + 33 / PPM);
                //body.setTransform(x + 12 / PPM, y + 48 / PPM, 0);
                Gdx.app.postRunnable(new Runnable() {

                    @Override
                    public void run () {
                        body.setActive(true);
                        body.setTransform(x + 12 / PPM, y + 48 / PPM, 0);
                    }
                });
                region = swordAttackUp.getKeyFrame(stateTimer, true);
                break;
            case ATTACKINGDOWN:
                setAlpha(1);
                //setPosition(x, y - 33 / PPM);
                //body.setTransform(x + 12 / PPM, y - 16 / PPM, 0);
                Gdx.app.postRunnable(new Runnable() {

                    @Override
                    public void run () {
                        body.setActive(true);
                        body.setTransform(x + 12 / PPM, y - 16 / PPM, 0);
                    }
                });
                region = swordAttackDown.getKeyFrame(stateTimer, true);
                break;
            case NOATTACK:
                setAlpha(0);
                //setPosition(x, y);
                //body.setTransform(x + 16 / PPM, y + 16 / PPM, 0);
                Gdx.app.postRunnable(new Runnable() {

                    @Override
                    public void run () {
                        body.setActive(false);
                        body.setTransform(x + 16 / PPM, y + 16 / PPM, 0);
                    }
                });
                region = test;
                break;
            default:
                setAlpha(0);
                //setPosition(x, y);
                //body.setTransform(x + 16 / PPM, y + 16 / PPM, 0);
                Gdx.app.postRunnable(new Runnable() {

                    @Override
                    public void run () {
                        body.setActive(false);
                        body.setTransform(x + 16 / PPM, y + 16 / PPM, 0);
                    }
                });
                region = test;
                break;
        }

        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    public void changeTextures() {
        if (FriendlyPlayer.currentWeapon.equals("Iron Sword")) {
            Array<TextureRegion> frames = new Array<TextureRegion>();

            for(int i = 0; i < 3; i++)
                frames.add(new TextureRegion(ironSword, i * 33, 64, 33, 32));
            swordAttackRight = new Animation(0.1f, frames);
            frames.clear();

            for(int i = 0; i < 3; i++)
                frames.add(new TextureRegion(ironSword, i * 33, 32, 33, 32));
            swordAttackLeft = new Animation(0.1f, frames);
            frames.clear();

            for(int i = 0; i < 3; i++)
                frames.add(new TextureRegion(ironSword, i * 33, 96, 33, 32));
            swordAttackUp = new Animation(0.1f, frames);
            frames.clear();

            for(int i = 0; i < 3; i++)
                frames.add(new TextureRegion(ironSword, i * 33, 0, 33, 32));
            swordAttackDown = new Animation(0.1f, frames);
        }
        if (FriendlyPlayer.currentWeapon.equals("testwep")) {
            Array<TextureRegion> frames = new Array<TextureRegion>();

            for(int i = 0; i < 3; i++)
                frames.add(new TextureRegion(weaponMerchantTex, i * 33, 64, 33, 32));
            swordAttackRight = new Animation(0.1f, frames);
            frames.clear();

            for(int i = 0; i < 3; i++)
                frames.add(new TextureRegion(weaponMerchantTex, i * 33, 32, 33, 32));
            swordAttackLeft = new Animation(0.1f, frames);
            frames.clear();

            for(int i = 0; i < 3; i++)
                frames.add(new TextureRegion(weaponMerchantTex, i * 33, 96, 33, 32));
            swordAttackUp = new Animation(0.1f, frames);
            frames.clear();

            for(int i = 0; i < 3; i++)
                frames.add(new TextureRegion(weaponMerchantTex, i * 33, 0, 33, 32));
            swordAttackDown = new Animation(0.1f, frames);
        }
    }

    public void weaponPhysics() {
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();

        Rectangle rect = getBoundingRectangle();

        bdef.position.set(x + 12 / PPM, y + 48 / PPM);
        body = world.createBody(bdef);

        shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);

        fdef.shape = shape;
        fdef.isSensor = true;

        body.createFixture(fdef);
        //body.createFixture(fdef).setUserData("fpweapon");
    }
}
