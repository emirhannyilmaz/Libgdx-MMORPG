package com.codemir.multiplayer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.codemir.multiplayer.screens.LoginScreen;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class Multiplayer extends Game {
	public static final int V_WIDTH = 800;//Divided by 1.6
	public static final int V_HEIGHT = 450;//Divided by 1.6
	public SpriteBatch batch;
	public static final float PPM = 100;
	public static boolean error = false;
	private Game game;
	public static Texture heartTex;
	public static Texture moneyTex;
	public static Texture healthbarBackground;
	public static Texture healthbarForeground;
	public static NinePatch healthbar;
	public static Texture expbarBackground;
	public static Texture expbarForeground;
	public static NinePatch expbar;
	public static Texture playerTex;
	public static Texture monsterTex;
	public static Texture weaponMerchantTex;
	public static Texture ironSword;
	public static Socket socket;

	@Override
	public void create () {
		game = this;
		batch = new SpriteBatch();
		heartTex = new Texture("hud/heart.png");
		moneyTex = new Texture("hud/money.png");
		healthbarBackground = new Texture("hud/progress-bar-health.png");
		healthbarForeground = new Texture("hud/progress-bar-health-knob.png");
		healthbar = new NinePatch(healthbarForeground, 0, 0, 0, 0);
		expbarBackground = new Texture("hud/progress-bar-exp.png");
		expbarForeground = new Texture("hud/progress-bar-exp-knob.png");
		expbar = new NinePatch(expbarForeground, 0, 0, 0, 0);
		playerTex = new Texture("characters/character.png");
		monsterTex = new Texture("npc/monster.png");
		weaponMerchantTex = new Texture("npc/weapon_merchant.png");
		ironSword = new Texture("items/iron_sword.png");

		try {
			socket = IO.socket("http://localhost:8080");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		setScreen(new LoginScreen(this));

	}

	@Override
	public void render () {
		super.render();
	}
}