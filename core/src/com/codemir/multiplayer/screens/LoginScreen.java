package com.codemir.multiplayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.codemir.multiplayer.Multiplayer;
import com.codemir.multiplayer.entity.living.Player;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginScreen implements Screen {

    private Multiplayer game;
    //private Player player;
    private static Stage stage;
    private TextButton btnLogin;
    private TextButton btnRegister;
    private TextField txfUsername;
    private TextField txfPassword;
    public static Label lblRegisterStatus;
    private Label lblLoginError;
    private boolean isLoggedIn = false;
    private String loginControlResponse;

    public LoginScreen(Multiplayer game) {
        this.game = game;

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("pixthulhu/skin/pixthulhu-ui.json"));

        lblLoginError = new Label("Username or password incorrect!", skin);
        lblLoginError.setPosition(Gdx.graphics.getWidth() / 2 - 205, Gdx.graphics.getHeight() / 2 + 60);
        lblLoginError.setSize(300, 60);
        lblLoginError.setColor(Color.RED);
        lblLoginError.setVisible(false);

        lblRegisterStatus = new Label("You have registered!", skin);
        lblRegisterStatus.setPosition(Gdx.graphics.getWidth() / 2 - 135, Gdx.graphics.getHeight() / 2 + 60);
        lblRegisterStatus.setSize(300, 60);
        lblRegisterStatus.setColor(Color.GREEN);
        lblRegisterStatus.setVisible(false);

        txfUsername = new TextField("", skin);
        txfUsername.setPosition(Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 10);
        txfUsername.setSize(300, 60);
        txfUsername.setMessageText("Username");

        txfPassword = new TextField("", skin);
        txfPassword.setPosition(Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 80);
        txfPassword.setSize(300, 60);
        txfPassword.setMessageText("Password");

        btnLogin = new TextButton("Login", skin);
        btnLogin.setPosition(Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 150);
        btnLogin.setSize(300, 60);
        btnLogin.addListener(new ClickListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                JSONObject data = new JSONObject();
                try {
                    data.put("username", txfUsername.getText());
                    data.put("password", txfPassword.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                login(data, "POST");

                while(loginControlResponse == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(!isLoggedIn) {
                    game.setScreen(new LoginScreen(game));
                }

                if(isLoggedIn) {
                    Player.username = txfUsername.getText();
                    JSONObject data2 = new JSONObject();
                    try {
                        data2.put("username", txfUsername.getText());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getPlayerData(data2, "POST");

                    game.setScreen(new GameScreen(game));
                }
            }
        });

        btnRegister = new TextButton("Register", skin);
        btnRegister.setPosition(Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 220);
        btnRegister.setSize(300, 60);
        btnRegister.addListener(new ClickListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    game.setScreen(new RegisterScreen(game));
            }
        });

        stage.addActor(txfUsername);
        stage.addActor(txfPassword);
        stage.addActor(btnLogin);
        stage.addActor(btnRegister);
        stage.addActor(lblLoginError);
        stage.addActor(lblRegisterStatus);
    }

    public void login(JSONObject requestObject, String method) {

        final Net.HttpRequest request = new Net.HttpRequest(method);
        final String url = "http://localhost:8081/login";
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

                String responseJson = httpResponse.getResultAsString();
                loginControlResponse = responseJson;

                if(responseJson.contains("true")) {
                    System.out.println("Login Successful!");
                    isLoggedIn = true;
                }

                if(responseJson.contains("false")) {
                    lblRegisterStatus.setVisible(false);
                    System.out.println("Login Failed!");
                    isLoggedIn = false;
                }
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely");
            }

            @Override
            public void cancelled() {
                System.out.println("Request Cancelled");
            }

        });

    }

    public void getPlayerData(JSONObject requestObject, String method) {

        final Net.HttpRequest request = new Net.HttpRequest(method);
        final String url = "http://localhost:8081/getplayerdata";
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

                String responseJson = httpResponse.getResultAsString();

                JsonReader json = new JsonReader();
                JsonValue base = json.parse(responseJson);

                Player.health = base.getInt("health");
                Player.money = base.getInt("money");
                Player.level = base.getInt("level");
                Player.exp = base.getInt("exp");
                Player.nextLevelExp = base.getInt("next_level_exp");
                Player.currentWeapon = base.getString("current_weapon");
                Player.currentWeaponDamage = base.getInt("current_weapon_damage");
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely");
            }

            @Override
            public void cancelled() {
                System.out.println("Request Cancelled");
            }

        });
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
