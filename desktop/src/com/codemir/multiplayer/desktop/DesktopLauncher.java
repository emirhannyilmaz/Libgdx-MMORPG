package com.codemir.multiplayer.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.codemir.multiplayer.Multiplayer;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.title = "Multiplayer";
		config.width = 1280;
		config.height = 720;
		config.fullscreen = false;

		new LwjglApplication(new Multiplayer(), config);
	}
}
