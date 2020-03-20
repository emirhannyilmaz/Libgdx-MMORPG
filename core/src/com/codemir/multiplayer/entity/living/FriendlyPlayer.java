package com.codemir.multiplayer.entity.living;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.codemir.multiplayer.Multiplayer;
import com.codemir.multiplayer.items.FPWeapon;

import static com.codemir.multiplayer.Multiplayer.*;

public class FriendlyPlayer extends Sprite implements Disposable {
    public enum PlayerState { STANDINGDOWN, WALKINGRIGHT, WALKINGLEFT, WALKINGUP, WALKINGDOWN };
    public PlayerState currentState;
    public PlayerState previousState;
    private float x;
    private float y;
    public float vx;
    public float vy;
    public float bx;
    public float by;
    public String username = "";
    public int level = 0;
    public static String currentWeapon = "";
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
    private Multiplayer game;
    public FPWeapon weapon;

    public FriendlyPlayer(Texture texture, World world, Multiplayer game, float x, float y, int w, int h){
        super(texture);
        this.world = world;
        this.game = game;
        this.x = x;
        this.y = y;
        currentState = PlayerState.STANDINGDOWN;
        previousState = PlayerState.STANDINGDOWN;
        stateTimer = 0;

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

        weapon = new FPWeapon(world, getX(), getY());

        setBounds(x / PPM, y / PPM, 24 / PPM, 32 / PPM);
        playerPhysics();
        setRegion(playerStandDown);
        previousPosition = new Vector2(getX(), getY());
    }
    
    public void update(float dt) {
        setRegion(getFrame(dt));

        if (currentWeapon != null && !currentWeapon.equals(""))
            weapon.update(dt, getX(), getY());
    }

    public void render(Batch batch, float dt) {
        draw(batch);
        if (weapon != null && currentWeapon != null && !currentWeapon.equals("") && weapon.currentState != FPWeapon.State.NOATTACK)
            weapon.draw(batch);
    }

    public void setPosition() {
        body.setTransform(bx, by, 0);
    }

    public TextureRegion getFrame(float dt) {
        currentState = getState();

        TextureRegion region;

        switch (currentState) {
            case WALKINGUP:
                region = playerWalkUp.getKeyFrame(stateTimer, true);
                if (vy == 0)
                    region = playerStandUp;
                break;
            case WALKINGDOWN:
                region = playerWalkDown.getKeyFrame(stateTimer, true);
                if (vy == 0)
                    region = playerStandDown;
                break;
            case WALKINGRIGHT:
                region = playerWalkRight.getKeyFrame(stateTimer, true);
                if (vx == 0)
                    region = playerStandRight;
                break;
            case WALKINGLEFT:
                region = playerWalkLeft.getKeyFrame(stateTimer, true);
                if (vx == 0)
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

    public PlayerState getState() {
        PlayerState ps = previousState;

        if(vy > 0)
            return PlayerState.WALKINGUP;
        else if(vy < 0)
            return PlayerState.WALKINGDOWN;
        else if(vx > 0)
            return PlayerState.WALKINGRIGHT;
        else if(vx < 0)
            return PlayerState.WALKINGLEFT;
        else
            return ps;
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
        fdef.isSensor = true;

        body.createFixture(fdef).setUserData("friendlyplayer");
    }

    public boolean hasMoved(){
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
    }
}