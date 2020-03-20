package com.codemir.multiplayer.maps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.codemir.multiplayer.Multiplayer;
import com.codemir.multiplayer.entity.lifeless.BuyPoint;
import com.codemir.multiplayer.entity.lifeless.InDoor;
import com.codemir.multiplayer.entity.lifeless.WeaponMerchant;
import com.codemir.multiplayer.entity.living.FriendlyPlayer;
import com.codemir.multiplayer.entity.living.Player;
import com.codemir.multiplayer.scenes.Hud;
import com.codemir.multiplayer.tools.B2DWorldCreator;
import com.codemir.multiplayer.tools.WorldContactListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import static com.codemir.multiplayer.Multiplayer.PPM;
import static com.codemir.multiplayer.Multiplayer.V_HEIGHT;
import static com.codemir.multiplayer.Multiplayer.V_WIDTH;
import static com.codemir.multiplayer.Multiplayer.playerTex;
import static com.codemir.multiplayer.Multiplayer.socket;
import static com.codemir.multiplayer.Multiplayer.weaponMerchantTex;

public class PotionShop implements Screen {
    private Multiplayer game;
    TiledMap tiledMap;
    OrthographicCamera camera;
    private Viewport gamePort;
    TiledMapRenderer tiledMapRenderer;
    private Hud hud;
    private World world;
    private Box2DDebugRenderer box2DDebugRenderer;
    private final float UPDATE_TIME = 1/60f;
    float timer;
    String id;
    HashMap<String, FriendlyPlayer> friendlyPlayers;
    public static Player player;
    private BitmapFont usernameFont;
    private WeaponMerchant weaponMerchant;
    private B2DWorldCreator b2DWorldCreator;
    private List<InDoor> inDoors;
    private List<BuyPoint> buyPoints;

    public PotionShop(Multiplayer game) {
        this.game = game;

        Gdx.input.setInputProcessor(null);

        friendlyPlayers = new HashMap<String, FriendlyPlayer>();

        float w = Gdx.graphics.getWidth() / PPM;
        float h = Gdx.graphics.getHeight() / PPM;

        camera = new OrthographicCamera();
        camera.setToOrtho(false,w,h);
        camera.update();

        tiledMap = new TmxMapLoader().load("maps/potionshop.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1 / PPM);

        world = new World(new Vector2(0, 0), true);
        box2DDebugRenderer = new Box2DDebugRenderer();

        b2DWorldCreator = new B2DWorldCreator(world, tiledMap, game, 6);
        inDoors = b2DWorldCreator.createInDoors(8);
        buyPoints = b2DWorldCreator.createBuyPoints(7);

        gamePort = new FitViewport(V_WIDTH / PPM, V_HEIGHT / PPM, camera);

        hud = new Hud(game.batch);

        weaponMerchant = new WeaponMerchant(128, 192, 16, 28, weaponMerchantTex);

        usernameFont = new BitmapFont(Gdx.files.internal("pixthulhu/raw/font-export.fnt"));
        usernameFont.setUseIntegerPositions(false);
        usernameFont.getData().setScale(1 / 140f);
        usernameFont.setColor(Color.WHITE);

        world.setContactListener(new WorldContactListener());

        connectToRoom();
        configSocketEvents();
    }

    public void updateServer(float dt) {
        timer += dt;
        if(timer >= UPDATE_TIME && player != null && player.hasMoved()) {
            JSONObject data = new JSONObject();
            try {
                data.put("x", player.getX());
                data.put("y", player.getY());
                data.put("vx", player.body.getLinearVelocity().x);
                data.put("vy", player.body.getLinearVelocity().y);
                data.put("bx", player.body.getPosition().x);
                data.put("by", player.body.getPosition().y);
                data.put("username", player.username);
                socket.emit("playerMoved", data);
            } catch (JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending update data!");
            }
        }
    }

    private void handleInput(float dt) {
        hud.handleInput();

        for (int i = 0; i < inDoors.size(); i++) {
            if (inDoors.get(i).isHeadHit)
                inDoors.get(i).handleInput();
        }

        for (int i = 0; i < buyPoints.size(); i++) {
            if (buyPoints.get(i).isHeadHit)
                buyPoints.get(i).handleInput();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W))
            camera.translate(0, 0.1f);
        if (Gdx.input.isKeyPressed(Input.Keys.A))
            camera.translate(-0.1f, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.S))
            camera.translate(0, -0.1f);
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            camera.translate(0.1f, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.Y))
            camera.zoom += 0.01f;
        if (Gdx.input.isKeyPressed(Input.Keys.H))
            camera.zoom -= 0.01f;
    }

    @Override
    public void show() {
        camera.position.set(1.2000002f, 1.1000001f, 0);
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        updateServer(Gdx.graphics.getDeltaTime());

        world.step(1/60f, 6, 2);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        box2DDebugRenderer.render(world, camera.combined);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        if(player != null) {
            player.follow = false;
            player.update(delta);
            player.draw(game.batch);
            player.handleInput();
        }

        if (weaponMerchant != null) {
            weaponMerchant.draw(game.batch);
        }

        camera.update();

        for(HashMap.Entry<String, FriendlyPlayer> entry : friendlyPlayers.entrySet()){
            entry.getValue().draw(game.batch);
            entry.getValue().setPosition();
            usernameFont.draw(game.batch, entry.getValue().username, entry.getValue().bx - 15 / PPM, entry.getValue().by + 38 / PPM);
        }

        game.batch.end();

        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
        hud.draw(delta);
    }

    public void connectToRoom() {
        try {
            JSONObject data = new JSONObject();
            try {
                data.put("newroom", "potionshop");
                socket.emit("joinRoom", data);
            } catch (JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending update data!");
            }
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void configSocketEvents() {
        socket.on("createPlayer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                player = new Player(playerTex, world, camera, 56, 195, 24, 32);
            }
        }).on("newPlayer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String playerId = data.getString("id");
                    Gdx.app.log("SocketIO", "New player connected to potionshop: " + playerId);
                    friendlyPlayers.put(playerId, new FriendlyPlayer(playerTex, world, game, 56, 195, 24, 32));
                } catch (JSONException e) {
                }
            }
        }).on("playerMoved", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String playerId = data.getString("id");
                    Double x = data.getDouble("x");
                    Double y = data.getDouble("y");
                    Double vx = data.getDouble("vx");
                    Double vy = data.getDouble("vy");
                    Double bx = data.getDouble("bx");
                    Double by = data.getDouble("by");
                    String username = data.getString("username");
                    if (friendlyPlayers.get(playerId) != null) {
                        friendlyPlayers.get(playerId).setPosition(x.floatValue(), y.floatValue());
                        friendlyPlayers.get(playerId).vx = vx.floatValue();
                        friendlyPlayers.get(playerId).vy = vy.floatValue();
                        friendlyPlayers.get(playerId).bx = bx.floatValue();
                        friendlyPlayers.get(playerId).by = by.floatValue();
                        friendlyPlayers.get(playerId).username = username;
                        friendlyPlayers.get(playerId).update(Gdx.graphics.getDeltaTime());
                    }
                } catch (JSONException e) {
                }
            }
        }).on("getPlayers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = (JSONArray) args[0];
                try {
                    for (int i = 0; i < objects.length(); i++) {
                        FriendlyPlayer coopPlayer = new FriendlyPlayer(playerTex, world, game, 56, 195, 24, 32);
                        Vector2 position = new Vector2();
                        Float vx, vy;
                        Float bx, by;
                        String username;
                        position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
                        position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
                        vx = ((Double) objects.getJSONObject(i).getDouble("vx")).floatValue();
                        vy = ((Double) objects.getJSONObject(i).getDouble("vy")).floatValue();
                        bx = ((Double) objects.getJSONObject(i).getDouble("bx")).floatValue();
                        by = ((Double) objects.getJSONObject(i).getDouble("by")).floatValue();
                        username = objects.getJSONObject(i).getString("username");
                        coopPlayer.setPosition(position.x, position.y);
                        coopPlayer.vx = vx;
                        coopPlayer.vy = vy;
                        coopPlayer.bx = bx;
                        coopPlayer.by = by;
                        coopPlayer.username = username;
                        friendlyPlayers.put(objects.getJSONObject(i).getString("id"), coopPlayer);
                        coopPlayer.update(Gdx.graphics.getDeltaTime());
                    }
                } catch (JSONException e) {
                }
            }
        }).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    id = data.getString("id");
                    friendlyPlayers.get(id).dispose();
                    friendlyPlayers.remove(id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting disconnected PlayerID");
                }
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        if (player != null)
            player.dispose();
    }

    @Override
    public void dispose() {

    }
}