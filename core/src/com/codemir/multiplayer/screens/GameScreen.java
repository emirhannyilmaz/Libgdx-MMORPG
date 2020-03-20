package com.codemir.multiplayer.screens;

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
import com.codemir.multiplayer.maps.PotionShop;
import com.codemir.multiplayer.maps.WeaponShop;
import com.codemir.multiplayer.scenes.Hud;
import com.codemir.multiplayer.entity.living.FriendlyPlayer;
import com.codemir.multiplayer.items.FPWeapon;
import com.codemir.multiplayer.entity.living.PumpkinMonster;
import com.codemir.multiplayer.entity.lifeless.OutDoor;
import com.codemir.multiplayer.entity.living.Player;
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

import static com.codemir.multiplayer.Multiplayer.*;

public class GameScreen implements Screen {
    private final float UPDATE_TIME = 1/60f;
    float timer;
    String id;
    public static Player player;
    public static HashMap<String, FriendlyPlayer> friendlyPlayers;
    public static HashMap<Integer, PumpkinMonster> monsters;
    private Multiplayer game;
    TiledMap tiledMap;
    OrthographicCamera camera;
    private Viewport gamePort;
    TiledMapRenderer tiledMapRenderer;
    private Hud hud;
    public static World world;
    private Box2DDebugRenderer box2DDebugRenderer;
    private BitmapFont usernameFont;
    String playerID;
    B2DWorldCreator b2DWorldCreator;
    private List<OutDoor> outDoors;

    public GameScreen(Multiplayer game) {
        this.game = game;

        Gdx.input.setInputProcessor(null);

        friendlyPlayers = new HashMap<String, FriendlyPlayer>();
        monsters = new HashMap<Integer, PumpkinMonster>();

        float w = Gdx.graphics.getWidth() / PPM;
        float h = Gdx.graphics.getHeight() / PPM;

        camera = new OrthographicCamera();
        camera.setToOrtho(false,w,h);
        camera.update();

        tiledMap = new TmxMapLoader().load("maps/town.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1 / PPM);

        world = new World(new Vector2(0, 0), false);

        box2DDebugRenderer = new Box2DDebugRenderer();

        b2DWorldCreator = new B2DWorldCreator(world, tiledMap, game, 2);
        outDoors = b2DWorldCreator.createOutDoors(3);

        gamePort = new FitViewport(V_WIDTH / PPM, V_HEIGHT / PPM, camera);

        hud = new Hud(game.batch);

        usernameFont = new BitmapFont(Gdx.files.internal("pixthulhu/raw/font-export.fnt"));
        usernameFont.setUseIntegerPositions(false);
        usernameFont.getData().setScale(1 / 140f);
        usernameFont.setColor(Color.WHITE);

        world.setContactListener(new WorldContactListener());

        connectToRoom();
        configSocketEvents();
    }

    private void updateServer(float dt) {
        timer += dt;

        if(timer >= UPDATE_TIME && player != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("x", player.getX());
                data.put("y", player.getY());
                data.put("vx", player.body.getLinearVelocity().x);
                data.put("vy", player.body.getLinearVelocity().y);
                data.put("bx", player.body.getPosition().x);
                data.put("by", player.body.getPosition().y);
                data.put("username", player.username);
                data.put("level", player.level);
                socket.emit("playerMoved", data);
            } catch (JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending update data!");
            }
        }

        if(timer >= UPDATE_TIME && player != null && !player.currentWeapon.equals("")) {
            JSONObject data = new JSONObject();
            try {
                data.put("currentState", player.weapon.currentState);
                data.put("currentWeapon", player.currentWeapon);
                socket.emit("playerAttacking", data);
            } catch (JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending update data!");
            }
        }
    }

    public void handleInput() {
        hud.handleInput();

        for (int i = 0; i < outDoors.size(); i++) {
            if (outDoors.get(i).isHeadHit) {
                outDoors.get(i).handleInput();
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.U)) {
            camera.zoom += 0.1f;
        } else if(Gdx.input.isKeyPressed(Input.Keys.Y)) {
            camera.zoom -= 0.1f;
        }
    }

    @Override
    public void render (float delta) {
        world.step(1/60f, 6, 2);

        handleInput();
        updateServer(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        box2DDebugRenderer.render(world, camera.combined);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        if(player != null) {
            player.update(delta);
            player.render(game.batch, delta);
            player.handleInput();
        }

        for(HashMap.Entry<String, FriendlyPlayer> entry : friendlyPlayers.entrySet()) {
            entry.getValue().render(game.batch, delta);
            entry.getValue().setPosition();
            usernameFont.draw(game.batch, entry.getValue().username + " Lv." + entry.getValue().level, entry.getValue().getX() - 40 / PPM, entry.getValue().by + 38 / PPM);
        }

        for(HashMap.Entry<Integer, PumpkinMonster> entry : monsters.entrySet()) {
            entry.getValue().draw(game.batch);
            entry.getValue().update(delta);
            entry.getValue().chase();
        }

        camera.update();

        game.batch.end();

        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.draw(delta);
    }

    private void connectToRoom() {
        try {
            if (!socket.connected())
                socket.connect();

            JSONObject data = new JSONObject();
            try {
                data.put("newroom", "town");
                socket.emit("joinRoom", data);
            } catch (JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending update data!");
            }
        } catch(Exception e) {
            System.out.println(e);
        }
    }
    private void configSocketEvents() {
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Gdx.app.log("SocketIO", "Connected");
                }
            }).on("createPlayer", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    player = new Player(playerTex, world, camera, 1220, 1100, 24, 32);
                }
            }).on("socketID", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        id = data.getString("id");
                        Gdx.app.log("SocketIO", "My ID: " + id);
                    } catch (JSONException e) {
                        Gdx.app.log("SocketIO", "Error getting ID");
                    }
                }
            }).on("newPlayer", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String playerId = data.getString("id");
                        Gdx.app.log("SocketIO", "New player connect: " + playerId);
                        friendlyPlayers.put(playerId, new FriendlyPlayer(playerTex, world, game, 1220, 1100, 24, 32));
                    } catch (JSONException e) {
                        Gdx.app.log("SocketIO", "Error getting New PlayerID");
                    }
                }
            }).on("playerDisconnected", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            id = data.getString("id");
                            playerID = null;
                            friendlyPlayers.get(id).dispose();
                            friendlyPlayers.remove(id);
                        } catch (JSONException e) {
                            Gdx.app.log("SocketIO", "Error getting disconnected PlayerID");
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
                        Integer level = data.getInt("level");
                        if (friendlyPlayers.get(playerId) != null) {
                            friendlyPlayers.get(playerId).setPosition(x.floatValue(), y.floatValue());
                            friendlyPlayers.get(playerId).vx = vx.floatValue();
                            friendlyPlayers.get(playerId).vy = vy.floatValue();
                            friendlyPlayers.get(playerId).bx = bx.floatValue();
                            friendlyPlayers.get(playerId).by = by.floatValue();
                            friendlyPlayers.get(playerId).username = username;
                            friendlyPlayers.get(playerId).level = level;
                            friendlyPlayers.get(playerId).update(Gdx.graphics.getDeltaTime());
                        }
                    } catch (JSONException e) {

                    }
                }
            }).on("playerAttacking", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String playerId = data.getString("id");
                        String cs = data.getString("currentState");
                        String cw = data.getString("currentWeapon");
                        friendlyPlayers.get(playerId).currentWeapon = cw;

                        if (cs.equals("ATTACKINGRIGHT"))
                            friendlyPlayers.get(playerId).weapon.currentState = FPWeapon.State.ATTACKINGRIGHT;
                        if (cs.equals("ATTACKINGLEFT"))
                            friendlyPlayers.get(playerId).weapon.currentState = FPWeapon.State.ATTACKINGLEFT;
                        if (cs.equals("ATTACKINGUP"))
                            friendlyPlayers.get(playerId).weapon.currentState = FPWeapon.State.ATTACKINGUP;
                        if (cs.equals("ATTACKINGDOWN"))
                            friendlyPlayers.get(playerId).weapon.currentState = FPWeapon.State.ATTACKINGDOWN;
                        if (cs.equals("NOATTACK"))
                            friendlyPlayers.get(playerId).weapon.currentState = FPWeapon.State.NOATTACK;
                    } catch (JSONException e) {

                    }
                }
            }).on("getPlayers", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONArray objects = (JSONArray) args[0];
                    try {
                        System.out.println("gs getPlayers");
                        for (int i = 0; i < objects.length(); i++) {
                            FriendlyPlayer coopPlayer = new FriendlyPlayer(playerTex, world, game, 1220, 1100, 24, 32);
                            Vector2 position = new Vector2();
                            Float vx, vy;
                            Float bx, by;
                            position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
                            position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
                            vx = ((Double) objects.getJSONObject(i).getDouble("vx")).floatValue();
                            vy = ((Double) objects.getJSONObject(i).getDouble("vy")).floatValue();
                            bx = ((Double) objects.getJSONObject(i).getDouble("bx")).floatValue();
                            by = ((Double) objects.getJSONObject(i).getDouble("by")).floatValue();
                            String username = objects.getJSONObject(i).getString("username");
                            Integer level = objects.getJSONObject(i).getInt("level");
                            coopPlayer.setPosition(position.x, position.y);
                            coopPlayer.vx = vx;
                            coopPlayer.vy = vy;
                            coopPlayer.bx = bx;
                            coopPlayer.by = by;
                            coopPlayer.username = username;
                            coopPlayer.level = level;
                            friendlyPlayers.put(objects.getJSONObject(i).getString("id"), coopPlayer);
                            coopPlayer.update(Gdx.graphics.getDeltaTime());
                        }
                    } catch (JSONException e) {
                    }
                }
            }).on("getMonsters", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONArray objects = (JSONArray) args[0];
                    try {
                        for (int i = 0; i < objects.length(); i++) {
                            int id = objects.getJSONObject(i).getInt("id");
                            if (monsters.get(id) == null) {
                                Vector2 position = new Vector2();
                                Float vx, vy;
                                position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
                                position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
                                vx = ((Double) objects.getJSONObject(i).getDouble("vx")).floatValue();
                                vy = ((Double) objects.getJSONObject(i).getDouble("vy")).floatValue();
                                int health = ((Double) objects.getJSONObject(i).getDouble("health")).intValue();
                                PumpkinMonster monster = new PumpkinMonster(id, position.x, position.y, 28, 49, health, monsterTex, world);
                                monster.setPosition(position.x, position.y);
                                monster.vx = vx;
                                monster.vy = vy;
                                monsters.put(id, monster);
                            }
                        }
                    } catch (JSONException e) {
                    }
                }
            }).on("monsterDamaged", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        int id = data.getInt("id");
                        int health = ((Double) data.getDouble("health")).intValue();

                        monsters.get(id).setHealth(health);
                    } catch (JSONException e) {
                    }
                }
            });
    }

    @Override
    public void show() {
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
        if (player != null)
            player.dispose();
    }
}