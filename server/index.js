var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var players = [];
var weaponshopPlayers = [];
var potionshopPlayers = [];
var monsters = [];

server.listen(8080, function() {
    console.log("Server is now running...");
    monsters.push(new monster(1, 600 / 100, 1000 / 100, 0, 0, 100));
    monsters.push(new monster(2, 700 / 100, 1200 / 100, 0, 0, 100));
    monsters.push(new monster(3, 1400 / 100, 800 / 100, 0, 0, 100));
    monsters.push(new monster(4, 1300 / 100, 650 / 100, 0, 0, 100));
    monsters.push(new monster(5, 1600 / 100, 1500 / 100, 0, 0, 100));
    monsters.push(new monster(6, 900 / 100, 600 / 100, 0, 0, 100));
    monsters.push(new monster(7, 1400 / 100, 1400 / 100, 0, 0, 100));
    monsters.push(new monster(8, 1780 / 100, 1750 / 100, 0, 0, 100));
    monsters.push(new monster(9, 1300 / 100, 1700 / 100, 0, 0, 100));
    monsters.push(new monster(10, 1400 / 100, 1550 / 100, 0, 0, 100));
    monsters.push(new monster(11, 1000 / 100, 1450 / 100, 0, 0, 100));
    monsters.push(new monster(12, 800 / 100, 1530 / 100, 0, 0, 100));
});

io.on('connection', function(socket) {
    socket.emit('socketID', { id: socket.id });

    socket.on('joinRoom', function(data) {
        oldRoom = socket.room;
        console.log("switchRoom before: " + socket.room);
        socket.room = data.newroom;
        socket.leave(oldRoom);
        socket.join(socket.room);
        socket.emit('createPlayer');
        socket.to(oldRoom).emit('playerDisconnected', { id: socket.id });//socket.broadcast.to(oldRoom).emit('playerDisconnected', { id: socket.id });
        socket.to(socket.room).emit('newPlayer', { id: socket.id });//socket.broadcast.to(socket.room).emit('newPlayer', { id: socket.id });
        
        if(oldRoom == 'town') {
            for(var i = 0; i < players.length; i++) {
                if(players[i].id == socket.id) {
                    players.splice(i, 1);
                    console.log("town sil");
                }
            }
        }
        if(oldRoom == 'weaponshop') {
            for(var i = 0; i < weaponshopPlayers.length; i++) {
                if(weaponshopPlayers[i].id == socket.id) {
                    weaponshopPlayers.splice(i, 1);
                    console.log("weaponshop sil");
                }
            }
        }
        if(oldRoom == 'potionshop') {
            for(var i = 0; i < potionshopPlayers.length; i++) {
                if(potionshopPlayers[i].id == socket.id) {
                    potionshopPlayers.splice(i, 1);
                    console.log("potionshop sil");
                }
            }
        }

        console.log("switchRoom after: " + socket.room);

        if(socket.room == 'town') {
            socket.emit('getPlayers', players);
            socket.emit('getMonsters', monsters);
            players.push(new player(socket.id, "", 0, "town", 1220 / 100, 1100 / 100, 0, 0, 0, 0));
            console.log("town ekle");
        }
        if(socket.room == 'weaponshop') {
            socket.emit('getPlayers', weaponshopPlayers);
            weaponshopPlayers.push(new player(socket.id, "", 0, "weaponshop", 56 / 100, 195 / 100, 0, 0, 0, 0));
            console.log("weaponshop ekle");
        }
        if(socket.room == 'potionshop') {
            socket.emit('getPlayers', potionshopPlayers);
            potionshopPlayers.push(new player(socket.id, "", 0, "potionshop", 56 / 100, 195 / 100, 0, 0, 0, 0));
            console.log("potionshop ekle");
        }

        console.log("Player (" + socket.id + ") switched room");
    });

    socket.on('playerMoved', function(data) {
        data.id = socket.id;
        socket.to(socket.room).emit('playerMoved', data);//socket.broadcast.to(socket.room).emit('playerMoved', data);

        for(var i = 0; i < players.length; i++) {
            if(players[i].id == data.id) {
                players[i].username = data.username;
                players[i].level = data.level;
                players[i].x = data.x;
                players[i].y = data.y;
                players[i].vx = data.vx;
                players[i].vy = data.vy;
                players[i].bx = data.bx;
                players[i].by = data.by;
            }
        }
    });

    socket.on('monsterMoved', function(data) {
        for(var i = 0; i < monsters.length; i++) {
            if(monsters[i].id == data.id) {
                monsters[i].x = data.x;
                monsters[i].y = data.y;
            }
        }
    });
    socket.on('monsterDamaged', function(data) {
        for(var i = 0; i < monsters.length; i++) {
            if(monsters[i].id == data.id) {
                monsters[i].health = data.health;
                socket.to(socket.room).emit('monsterDamaged', data);//socket.broadcast.to(socket.room).emit('monsterDamaged', data);
                console.log("Pumpkin monster damaged! ID: " + monsters[i].id + " Health: " + monsters[i].health);
            }
        }
    });
    socket.on('monsterKilled', function(data) {
        //socket.emit("monsterKilled", data);
        for(var i = 0; i < monsters.length; i++) {
            if(monsters[i].id == data.id) {
                monsters.splice(i, 1);
                setTimeout(function() {
                    monsters.push(new monster(data.id, data.x, data.y, 0, 0, 100));
                    socket.emit('getMonsters', monsters);
                    socket.to(socket.room).emit('getMonsters', monsters)//socket.broadcast.to(socket.room).emit('getMonsters', monsters)
                    console.log("monsterKilled: " + socket.room);
                }, 5000)
            }
        }
    });
    socket.on('playerAttacking', function(data) {
        data.id = socket.id;
        socket.to(socket.room).emit('playerAttacking', data);//socket.broadcast.to(socket.room).emit('playerAttacking', data);
    });

    socket.on('test', function(data) {
        socket.emit('getPlayers', weaponshopPlayers);
    });

    socket.on('disconnect', function() {
        console.log("Player disconnected! (" + socket.id + ")");
        socket.to(socket.room).emit('playerDisconnected', { id: socket.id});
        if(socket.room == 'town') {
            for(var i = 0; i < players.length; i++) {
                if(players[i].id == socket.id) {
                    players.splice(i, 1);
                    console.log("town disc");
                }
            }
        }
        if(socket.room == 'weaponshop') {
            for(var i = 0; i < weaponshopPlayers.length; i++) {
                if(weaponshopPlayers[i].id == socket.id) {
                    weaponshopPlayers.splice(i, 1);
                    console.log("weaponshop disc");
                }
            }
        }
        if(socket.room == 'potionshop') {
            for(var i = 0; i < potionshopPlayers.length; i++) {
                if(potionshopPlayers[i].id == socket.id) {
                    potionshopPlayers.splice(i, 1);
                    console.log("potionshop disc");
                }
            }
        }
    });
});
















/*
io.of("/town").on("connection", (socket) => {
    console.log("Player connected to town! (" + socket.id + ")");
    socket.emit('socketID', { id: socket.id });
    socket.emit('getPlayers', players);
    socket.emit('getMonsters', monsters);
    socket.broadcast.emit('newPlayer', { id: socket.id, room: "town" });
    socket.on('playerMoved', function(data) {
        data.id = socket.id;
        socket.broadcast.emit('playerMoved', data);

        for(var i = 0; i < players.length; i++) {
            if(players[i].id == data.id) {
                players[i].username = data.username;
                players[i].level = data.level;
                players[i].x = data.x;
                players[i].y = data.y;
                players[i].vx = data.vx;
                players[i].vy = data.vy;
                players[i].bx = data.bx;
                players[i].by = data.by;
            }
        }
    });
    socket.on('monsterMoved', function(data) {
        for(var i = 0; i < monsters.length; i++) {
            if(monsters[i].id == data.id) {
                monsters[i].x = data.x;
                monsters[i].y = data.y;
            }
        }
    });
    socket.on('monsterDamaged', function(data) {
        for(var i = 0; i < monsters.length; i++) {
            if(monsters[i].id == data.id) {
                monsters[i].health = data.health;
                socket.broadcast.emit('monsterDamaged', data);
                console.log("Pumpkin monster damaged! ID: " + monsters[i].id + " Health: " + monsters[i].health);
            }
        }
    });
    socket.on('monsterKilled', function(data) {
        //socket.emit("monsterKilled", data);
        for(var i = 0; i < monsters.length; i++){
            if(monsters[i].id == data.id){
                monsters.splice(i, 1);
                console.log(monsters.length);
                setTimeout(function() {
                    monsters.push(new monster(data.id, data.x, data.y, 0, 0, 100));
                    socket.emit('getMonsters', monsters);
                    socket.broadcast.emit('getMonsters', monsters)
                    console.log(monsters.length);
                }, 5000)
            }
        }
    });
    socket.on('playerAttacking', function(data) {
        data.id = socket.id;
        socket.broadcast.emit('playerAttacking', data);
    });
    socket.on('disconnect', function() {
        console.log("Player disconnected from town! (" + socket.id + ")");
        console.log(players);
        console.log("socketid: " + socket.id);
        socket.broadcast.emit('playerDisconnected', { id: socket.id });
        for(var i = 0; i < players.length; i++) {
            if(players[i].id == socket.id) {
                players.splice(i, 1);
            }
        }
    });

    players.push(new player(socket.id, "", 0, "town", 1220 / 100, 1100 / 100, 0, 0, 0, 0));
});

io.of("/weaponshop").on("connection", (socket) => {
    console.log("Player connected to weaponshop! (" + socket.id + ")");
    socket.emit('getPlayers', weaponshopPlayers);
    socket.broadcast.emit('newPlayer', { id: socket.id, room: "weaponshop" });
    socket.on('playerMoved', function(data) {
        data.id = socket.id;
        socket.broadcast.emit('playerMoved', data);

        for(var i = 0; i < weaponshopPlayers.length; i++) {
            if(weaponshopPlayers[i].id == data.id) {
                weaponshopPlayers[i].username = data.username;
                weaponshopPlayers[i].level = data.level;
                weaponshopPlayers[i].x = data.x;
                weaponshopPlayers[i].y = data.y;
                weaponshopPlayers[i].vx = data.vx;
                weaponshopPlayers[i].vy = data.vy;
                weaponshopPlayers[i].bx = data.bx;
                weaponshopPlayers[i].by = data.by;
            }
        }
    });
    socket.on('disconnect', function() {
        console.log("Player disconnected from weaponshop! (" + socket.id + ")");
        socket.broadcast.emit('playerDisconnected', { id: socket.id });
        for(var i = 0; i < weaponshopPlayers.length; i++){
            if(weaponshopPlayers[i].id == socket.id){
                weaponshopPlayers.splice(i, 1);
            }
        }
    });

    weaponshopPlayers.push(new player(socket.id, "", 0, "weaponshop", 56 / 100, 195 / 100, 0, 0, 0, 0));
});

io.of("/potionshop").on("connection", (socket) => {
    console.log("Player connected to potionshop! (" + socket.id + ")");
    socket.emit('getPlayers', potionshopPlayers);
    socket.broadcast.emit('newPlayer', { id: socket.id, room: "potionshop" });
    socket.on('playerMoved', function(data) {
        data.id = socket.id;
        socket.broadcast.emit('playerMoved', data);

        for(var i = 0; i < potionshopPlayers.length; i++) {
            if(potionshopPlayers[i].id == data.id) {
                potionshopPlayers[i].username = data.username;
                potionshopPlayers[i].level = data.level;
                potionshopPlayers[i].x = data.x;
                potionshopPlayers[i].y = data.y;
                potionshopPlayers[i].vx = data.vx;
                potionshopPlayers[i].vy = data.vy;
                potionshopPlayers[i].bx = data.bx;
                potionshopPlayers[i].by = data.by;
            }
        }
    });
    socket.on('disconnect', function() {
        console.log("Player disconnected from potionshop! (" + socket.id + ")");
        socket.broadcast.emit('playerDisconnected', { id: socket.id });
        for(var i = 0; i < potionshopPlayers.length; i++) {
            if(potionshopPlayers[i].id == socket.id) {
                potionshopPlayers.splice(i, 1);
            }
        }
    });

    potionshopPlayers.push(new player(socket.id, "", 0, "potionshop", 56 / 100, 195 / 100, 0, 0, 0, 0));
});
*/
function player(id, username, level, room, x, y, vx, vy, bx, by) {
    this.id = id;
    this.username = username;
    this.level = level;
    this.room = room;
    this.x = x;
    this.y = y;
    this.vx = vx;
    this.vy = vy;
    this.bx = bx;
    this.by = by;
}

function monster(id, x, y, vx, vy, health) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.vx = vx;
    this.vy = vy;
    this.health = health;
}