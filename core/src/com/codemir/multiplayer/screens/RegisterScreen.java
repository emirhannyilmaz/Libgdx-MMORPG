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
import com.codemir.multiplayer.Multiplayer;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterScreen implements Screen {

    private Multiplayer game;
    private Stage stage;
    private TextField txfUsername;
    private TextField txfEmail;
    private TextField txfPassword;
    private TextField txfRePassword;
    private TextButton btnRegister;
    private TextButton btnBackToLogin;
    private Label lblError;
    private boolean emailAvailable = false;
    private boolean usernameAvailable = false;
    private String emailControlResponse;
    private String usernameControlResponse;

    public RegisterScreen(Multiplayer game) {
        this.game = game;

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("pixthulhu/skin/pixthulhu-ui.json"));

        lblError = new Label("Error!", skin);
        lblError.setPosition(Gdx.graphics.getWidth() / 2 - 35, Gdx.graphics.getHeight() / 2 + 130);
        lblError.setSize(300, 60);
        lblError.setColor(Color.RED);
        lblError.setVisible(Multiplayer.error);

        txfEmail = new TextField("", skin);
        txfEmail.setPosition(Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 + 60);
        txfEmail.setSize(300, 60);
        txfEmail.setMessageText("Email");

        txfUsername = new TextField("", skin);
        txfUsername.setPosition(Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 10);
        txfUsername.setSize(300, 60);
        txfUsername.setMessageText("Username");

        txfPassword = new TextField("", skin);
        txfPassword.setPosition(Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 80);
        txfPassword.setSize(300, 60);
        txfPassword.setMessageText("Password");

        txfRePassword = new TextField("", skin);
        txfRePassword.setPosition(Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 150);
        txfRePassword.setSize(300, 60);
        txfRePassword.setMessageText("Re Password");

        btnRegister = new TextButton("Register", skin);
        btnRegister.setPosition(Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 220);
        btnRegister.setSize(300, 60);
        btnRegister.addListener(new ClickListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    JSONObject data2 = new JSONObject();
                    try {
                        data2.put("email", txfEmail.getText());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    sendEmailControlRequest(data2, "POST");

                    JSONObject data3 = new JSONObject();
                    try {
                        data3.put("username", txfUsername.getText());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    sendUsernameControlRequest(data3, "POST");

                    while(emailControlResponse == null || usernameControlResponse == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if(!txfEmail.getText().isEmpty() && !txfUsername.getText().isEmpty() && !txfPassword.getText().isEmpty() && !txfRePassword.getText().isEmpty() &&
                        txfEmail.getText().contains("@") && txfEmail.getText().contains(".com") && txfPassword.getText().equals(txfRePassword.getText()) && emailAvailable && usernameAvailable) {

                    JSONObject data = new JSONObject();
                    try {
                        data.put("email", txfEmail.getText());
                        data.put("username", txfUsername.getText());
                        data.put("password", txfPassword.getText());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    sendRegisterRequest(data, "POST");
                    lblError.setVisible(false);
                    game.setScreen(new LoginScreen(game));
                    LoginScreen.lblRegisterStatus.setVisible(true);
                } else {
                    Multiplayer.error = true;
                    game.setScreen(new RegisterScreen(game));
                }
            }
        });

        btnBackToLogin = new TextButton("Back to Login", skin);
        btnBackToLogin.setPosition(Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 290);
        btnBackToLogin.setSize(300, 60);
        btnBackToLogin.addListener(new ClickListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new LoginScreen(game));
            }
        });

        stage.addActor(txfEmail);
        stage.addActor(txfUsername);
        stage.addActor(txfPassword);
        stage.addActor(txfRePassword);
        stage.addActor(btnRegister);
        stage.addActor(btnBackToLogin);
        stage.addActor(lblError);
    }

    public void sendRegisterRequest(JSONObject requestObject, String method) {
        Net.HttpRequest request = new Net.HttpRequest(method);
        String url = "http://localhost:8081/register";
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

    public void sendEmailControlRequest(JSONObject requestObject, String method) {
        Net.HttpRequest request = new Net.HttpRequest(method);
        String url = "http://localhost:8081/email";
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
                emailControlResponse = responseJson;

                if(responseJson.contains("true")) {
                    emailAvailable = true;
                }

                if(responseJson.contains("false")) {
                    emailAvailable = false;
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

    public void sendUsernameControlRequest(JSONObject requestObject, String method) {
        Net.HttpRequest request = new Net.HttpRequest(method);
        String url = "http://localhost:8081/username";
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
                usernameControlResponse = responseJson;

                if(responseJson.contains("true")) {
                    usernameAvailable = true;
                }

                if(responseJson.contains("false")) {
                    usernameAvailable = false;
                }
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely");
            }

            @Override
            public void cancelled() {
                System.out.println("request cancelled");
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
