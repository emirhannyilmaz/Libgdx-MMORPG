package com.codemir.multiplayer.entity.living;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.codemir.multiplayer.entity.Monster;
import com.codemir.multiplayer.screens.GameScreen;
import com.codemir.multiplayer.tools.Funcs;

import org.json.JSONException;
import org.json.JSONObject;

import static com.codemir.multiplayer.Multiplayer.*;

public class PumpkinMonster extends Monster {
    public enum State { STANDING, WALKINGRL, WALKINGUP, WALKINGDOWN };
    public State currentState;
    public State previousState;
    public float x;
    public float y;
    public float vx;
    public float vy;
    private float speedX = 0;
    private float speedY = 0;
    public float impulseX = 0f;
    public float impulseY = 0f;
    private int health;
    private int previousHealth;
    private World world;
    private Vector2 previousPosition;
    public Body body;
    private TextureRegion monsterStand;
    private Animation<TextureRegion> monsterWalkRL;
    private Animation<TextureRegion> monsterWalkUp;
    private Animation<TextureRegion> monsterWalkDown;
    private float stateTimer;
    private boolean walkingRight;
    private boolean setToDestroy;
    private boolean destroyed;
    private Array<Fixture> fixtures;
    private int friendlyPlayersCount = 0;
    public int id;

    public PumpkinMonster(int id, float x, float y, int w, int h, int health, Texture texture, World world) {
        super(texture, w, h, world);
        this.id = id;
        this.x = x;
        this.y = y;
        this.health = health;
        this.world = world;
        this.body = super.body;
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        walkingRight = false;
        setToDestroy = false;
        destroyed = false;
        previousHealth = this.health;

        Array<TextureRegion> frames = new Array<TextureRegion>();
        for(int i = 0; i < 3; i++)
            frames.add(new TextureRegion(texture, i * 28, 49, w, h));
        monsterWalkRL = new Animation(0.2f, frames);
        frames.clear();

        for(int i = 0; i < 3; i++)
            frames.add(new TextureRegion(texture, i * 28, 147, w, h));
        monsterWalkUp = new Animation(0.2f, frames);
        frames.clear();

        for(int i = 0; i < 3; i++)
            frames.add(new TextureRegion(texture, i * 28, 0, w, h));
        monsterWalkDown = new Animation(0.2f, frames);

        monsterStand = new TextureRegion(texture, 28, 0, w, h);

        setBounds(x, y, w / PPM, h / PPM);
        setRegion(monsterStand);
        previousPosition = new Vector2(getX(), getY());

        body.setTransform(x + 14 / PPM, y + 24.5f / PPM, 0);

        fixture.setUserData(this);

        fixtures = new Array<Fixture>();
        world.getFixtures(fixtures);
        friendlyPlayersCount = GameScreen.friendlyPlayers.size();
    }

    @Override
    public void update(float dt) {
        if (health != previousHealth) {
            previousHealth = health;

            JSONObject data = new JSONObject();
            try {
                data.put("id", id);
                data.put("health", health);
                socket.emit("monsterDamaged", data);
            } catch (JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending update data!");
            }
        }

        if(health <= 0) {
            setToDestroy = true;
        }

        if (setToDestroy && !destroyed) {
            destroy();
        } else if(!destroyed) {
            setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
            setRegion(getFrame(dt));

            body.applyLinearImpulse(
                    (0 - body.getLinearVelocity().x) * body.getMass(),
                    (0 - body.getLinearVelocity().y) * body.getMass(),
                    body.getWorldCenter().x, body.getWorldCenter().y, true
            );
        }

        if (GameScreen.friendlyPlayers.size() != friendlyPlayersCount) {
            friendlyPlayersCount = GameScreen.friendlyPlayers.size();
            world.getFixtures(fixtures);
        }
    }

    public TextureRegion getFrame(float dt) {
        currentState = getState();

        TextureRegion region;

        switch (currentState) {
            case WALKINGUP:
                region = monsterWalkUp.getKeyFrame(stateTimer, true);
                break;
            case WALKINGDOWN:
                region = monsterWalkDown.getKeyFrame(stateTimer, true);
                break;
            case WALKINGRL:
                region = monsterWalkRL.getKeyFrame(stateTimer, true);
                break;
            case STANDING:
            default:
                region = monsterStand;
                break;
        }

        if((body.getLinearVelocity().x < 0 || !walkingRight) && !region.isFlipX()) {
            region.flip(true, false);
            walkingRight = false;
        } else if((body.getLinearVelocity().x > 0 || walkingRight) && region.isFlipX()) {
            region.flip(true, false);
            walkingRight = true;
        }

        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    public State getState() {
        if(body.getLinearVelocity().y > 0)
            return State.WALKINGUP;
        else if(body.getLinearVelocity().y < 0)
            return State.WALKINGDOWN;
        else if(body.getLinearVelocity().x != 0)
            return State.WALKINGRL;
        else
            return State.STANDING;
    }

    @Override
    public void chase() {
        for (int i = 0; i < fixtures.size; i++) {
            if (fixtures.get(i).getUserData() != null) {
                if (fixtures.get(i).getUserData().equals("player") || fixtures.get(i).getUserData().equals("friendlyplayer")) {
                    if(Funcs.distance(body.getPosition(), fixtures.get(i).getBody().getPosition()) <= 1.3f) {
                        if(fixtures.get(i).getBody().getPosition().x > body.getPosition().x) {
                            speedX = 0.5f;
                        } else if(fixtures.get(i).getBody().getPosition().x < body.getPosition().x) {
                            speedX = -0.5f;
                        } else {
                            speedX = 0f;
                        }

                        if(fixtures.get(i).getBody().getPosition().y > body.getPosition().y) {
                            speedY = 0.5f;
                        } else if(fixtures.get(i).getBody().getPosition().y < body.getPosition().y) {
                            speedY = -0.5f;
                        } else {
                            speedY = 0f;
                        }

                        body.applyLinearImpulse(
                                (speedX - body.getLinearVelocity().x) * body.getMass(),
                                (speedY - body.getLinearVelocity().y) * body.getMass(),
                                body.getWorldCenter().x, body.getWorldCenter().y, true
                        );

                        JSONObject data = new JSONObject();
                        try {
                            data.put("id", id);
                            data.put("x", getX());
                            data.put("y", getY());
                            socket.emit("monsterMoved", data);
                        } catch (JSONException e) {
                            Gdx.app.log("SOCKET.IO", "Error sending update data!");
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean hasMoved() {
        if(previousPosition.x != getX() || previousPosition.y != getY()){
            previousPosition.x = getX();
            previousPosition.y = getY();
            return true;
        }
        return false;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public void onHit(String userData, float wx, float wy) {
        Player.exp += 50;

        health -= Player.currentWeaponDamage;

        if (wx > body.getPosition().x) {
            impulseX = -10f;
        } else if (wx < body.getPosition().x) {
            impulseX = 10f;
        } else {
            impulseX = 0f;
        }

        if (wy > body.getPosition().y) {
            impulseY = -10f;
        } else if (wy < body.getPosition().y) {
            impulseY = 10f;
        } else {
            impulseY = 0f;
        }

        body.applyLinearImpulse(
                (impulseX) * body.getMass(),
                (impulseY) * body.getMass(),
                body.getWorldCenter().x, body.getWorldCenter().y, true
        );
    }

    @Override
    public void noHit() {
    }

    @Override
    public void draw(Batch batch) {
        if (!destroyed)
            super.draw(batch);
    }

    @Override
    public void destroy() {
        world.destroyBody(body);
        destroyed = true;
        JSONObject data = new JSONObject();
        try {
            data.put("id", id);
            data.put("x", getX());
            data.put("y", getY());
            socket.emit("monsterKilled", data);
        } catch (JSONException e) {
            Gdx.app.log("SOCKET.IO", "Error sending update data!");
        }

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run () {
                if (GameScreen.monsters.get(id) != null)
                    GameScreen.monsters.remove(id);
            }
        });
    }
}