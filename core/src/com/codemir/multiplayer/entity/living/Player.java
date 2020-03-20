package com.codemir.multiplayer.entity.living;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.codemir.multiplayer.items.Weapon;
import com.codemir.multiplayer.scenes.Hud;

import org.json.JSONException;
import org.json.JSONObject;

import static com.codemir.multiplayer.Multiplayer.*;

public class Player extends Sprite implements Disposable {
    public enum State { STANDINGDOWN, WALKINGRIGHT, WALKINGLEFT, WALKINGUP, WALKINGDOWN;};
    public static State currentState;
    public State previousState;
    public static String username;
    public static String currentWeapon;
    public static int currentWeaponDamage;
    public static int health;
    private static int previousHealth;
    public static int money;
    public static int level;
    private static int previousLevel;
    public static int exp;
    private static int previousExp;
    public static int nextLevelExp;
    private static int previousNextLevelExp;
    Vector2 previousPosition;
    public World world;
    public Body body;
    private TextureRegion playerStandRight;
    private TextureRegion playerStandLeft;
    private TextureRegion playerStandUp;
    private TextureRegion playerStandDown;
    private Animation<TextureRegion> playerWalkRight;
    private Animation<TextureRegion> playerWalkLeft;
    private Animation<TextureRegion> playerWalkUp;
    private Animation<TextureRegion> playerWalkDown;
    private float stateTimer;
    private OrthographicCamera camera;
    public float x;
    public float y;
    public boolean follow = true;
    public Weapon weapon;

    public Player(Texture texture, World world, OrthographicCamera camera, float x, float y, int w, int h){
        super(texture);
        this.world = world;
        this.camera = camera;
        this.x = x;
        this.y = y;
        currentState = State.STANDINGDOWN;
        previousState = State.STANDINGDOWN;
        stateTimer = 0;
        previousHealth = health;
        previousLevel = level;
        previousExp = exp;
        previousNextLevelExp = nextLevelExp;

        Array<TextureRegion> frames = new Array<TextureRegion>();
        for(int i = 1; i < 3; i++)
            frames.add(new TextureRegion(texture, i * 24, 66, w, h));
        playerWalkRight = new Animation(0.4f, frames);
        frames.clear();

        for(int i = 1; i < 3; i++)
            frames.add(new TextureRegion(texture, i * 24, 33, w, h));
        playerWalkLeft = new Animation(0.4f, frames);
        frames.clear();

        for(int i = 1; i < 3; i++)
            frames.add(new TextureRegion(texture, i * 24, 99, w, h));
        playerWalkUp = new Animation(0.4f, frames);
        frames.clear();

        for(int i = 1; i < 3; i++)
            frames.add(new TextureRegion(texture, i * 24, 0, w, h));
        playerWalkDown = new Animation(0.4f, frames);

        playerStandRight = new TextureRegion(texture, 0, 66, w, h);
        playerStandLeft = new TextureRegion(texture, 0, 33, w, h);
        playerStandUp = new TextureRegion(texture, 0, 99, w, h);
        playerStandDown = new TextureRegion(texture, 0, 0, w, h);

        weapon = new Weapon(username, world, getX(), getY());

        setBounds(x / PPM, y / PPM, w / PPM, h / PPM);
        playerPhysics();
        setRegion(playerStandDown);
        previousPosition = new Vector2(getX(), getY());
    }

    public void update(float dt) {
        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
        setRegion(getFrame(dt));
        if(follow) {
            camera.position.x = body.getPosition().x;
            camera.position.y = body.getPosition().y;
        }

        weapon.update(dt, getX(), getY());

        if (exp >= nextLevelExp) {
            level++;
            nextLevelExp += 150;
            exp = 0;
        }

        if (health != previousHealth) {
            JSONObject data = new JSONObject();
            try {
                data.put("process", "health");
                data.put("username", username);
                data.put("health", health);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setPlayerData(data, "POST", "health");

            previousHealth = health;
        }

        if (level != previousLevel) {
            JSONObject data = new JSONObject();
            try {
                data.put("process", "level");
                data.put("username", username);
                data.put("level", level);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setPlayerData(data, "POST", "level");

            Hud.updateLevel();

            previousLevel = level;
        }

        if (exp != previousExp) {
            JSONObject data = new JSONObject();
            try {
                data.put("process", "exp");
                data.put("username", username);
                data.put("exp", exp);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setPlayerData(data, "POST", "exp");

            previousExp = exp;
        }

        if (nextLevelExp != previousNextLevelExp) {
            JSONObject data = new JSONObject();
            try {
                data.put("process", "next_level_exp");
                data.put("username", username);
                data.put("next_level_exp", nextLevelExp);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setPlayerData(data, "POST", "next_level_exp");

            previousNextLevelExp = nextLevelExp;
        }

        if (health <= 0) {
            body.setTransform(1220 / PPM, 1100 / PPM, 0);

            health = 100;
            previousHealth = health;

            JSONObject data = new JSONObject();
            try {
                data.put("process", "health");
                data.put("username", username);
                data.put("health", health);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setPlayerData(data, "POST", "health");
        }
    }

    public void render(Batch batch, float dt) {
        draw(batch);
        if (!currentWeapon.equals(""))
            weapon.draw(batch);
    }

    public TextureRegion getFrame(float dt) {
        currentState = getState();

        TextureRegion region;

        switch (currentState) {
            case WALKINGUP:
                region = playerWalkUp.getKeyFrame(stateTimer, true);
                if (body.getLinearVelocity().y == 0)
                    region = playerStandUp;
                break;
            case WALKINGDOWN:
                region = playerWalkDown.getKeyFrame(stateTimer, true);
                if (body.getLinearVelocity().y == 0)
                    region = playerStandDown;
                break;
            case WALKINGRIGHT:
                region = playerWalkRight.getKeyFrame(stateTimer, true);
                if (body.getLinearVelocity().x == 0)
                    region = playerStandRight;
                break;
            case WALKINGLEFT:
                region = playerWalkLeft.getKeyFrame(stateTimer, true);
                if (body.getLinearVelocity().x == 0)
                    region = playerStandLeft;
                break;
            case STANDINGDOWN:
            default:
                region = playerStandDown;
                break;
        }

        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    public State getState() {
        State ps = previousState;

        if(body.getLinearVelocity().y > 0)
            return State.WALKINGUP;
        else if(body.getLinearVelocity().y < 0)
            return State.WALKINGDOWN;
        else if(body.getLinearVelocity().x > 0)
            return State.WALKINGRIGHT;
        else if(body.getLinearVelocity().x < 0)
            return State.WALKINGLEFT;
        else
            return ps;
    }

    public void handleInput() {
            final float speedX;
            final float speedY;

            if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                speedX = -1;
            } else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                speedX = 1;
            } else {
                speedX = 0;
            }

            if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
                speedY = 1;
            } else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                speedY = -1;
            } else {
                speedY = 0;
            }

            body.applyLinearImpulse(
                    (speedX - body.getLinearVelocity().x) * body.getMass(),
                    (speedY - body.getLinearVelocity().y) * body.getMass(),
                    body.getWorldCenter().x, body.getWorldCenter().y, true
            );
    }

    private void playerPhysics() {
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();

        Rectangle rect = getBoundingRectangle();

        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x / PPM, y / PPM);

        body = world.createBody(bdef);

        shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);

        fdef.shape = shape;

        body.createFixture(fdef).setUserData("player");

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-body.getPosition().x / 2 / PPM, 25 / PPM), new Vector2(body.getPosition().x / 2 / PPM, 25 / PPM));

        fdef.shape = head;
        fdef.isSensor = true;

        body.createFixture(fdef).setUserData("head");
    }

    public boolean hasMoved() {
        if(previousPosition.x != getX() || previousPosition.y != getY()){
            previousPosition.x = getX();
            previousPosition.y = getY();
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        body.getWorld().destroyBody(body);
        weapon.dispose();
    }

    public void setPlayerData(JSONObject requestObject, String method, String process) {
        final Net.HttpRequest request = new Net.HttpRequest(method);
        final String url = "http://localhost:8081/setplayerdata";
        request.setUrl(url);

        request.setContent(requestObject.toString());

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {

            public void handleHttpResponse(Net.HttpResponse httpResponse) {

                int statusCode = httpResponse.getStatus().getStatusCode();
                if(statusCode != HttpStatus.SC_OK) {
                    System.out.println("Request Failed");
                    return;
                }

                if (process.equals("money")) {
                    String responseJson = httpResponse.getResultAsString();

                    JsonReader json = new JsonReader();
                    JsonValue base = json.parse(responseJson);

                    Player.money = base.getInt("money");
                }
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely reduce money");
            }

            @Override
            public void cancelled() {
                System.out.println("Request Cancelled");
            }
        });
    }
}