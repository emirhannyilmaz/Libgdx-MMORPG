package com.codemir.multiplayer.net;


import com.badlogic.gdx.Gdx;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class Connection {

    Socket socket;

    public Connection(int port, String address) {
        try {
            socket = IO.socket("http://" + address + ":" + port);
            socket.connect();
        } catch (Exception e) {
            Gdx.app.log("Socket.IO", "Error connecting to the server!");
        }
    }
}
