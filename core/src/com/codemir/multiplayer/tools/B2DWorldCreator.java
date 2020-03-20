package com.codemir.multiplayer.tools;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.codemir.multiplayer.Multiplayer;
import com.codemir.multiplayer.entity.lifeless.BuyPoint;
import com.codemir.multiplayer.entity.lifeless.InDoor;
import com.codemir.multiplayer.entity.lifeless.OutDoor;

import java.util.ArrayList;
import java.util.List;

public class B2DWorldCreator {
    private Multiplayer game;
    private TiledMap map;
    private World world;

    public B2DWorldCreator(World world, TiledMap map, Multiplayer game, int collisionLayer) {
        this.game = game;
        this.map = map;
        this.world = world;

        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;

        for(MapObject object : map.getLayers().get(collisionLayer).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / 100, (rect.getY() + rect.getHeight() / 2) / 100);

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2 / 100, rect.getHeight() / 2 / 100);
            fdef.shape = shape;
            body.createFixture(fdef);
        }
    }

    public List<OutDoor> createOutDoors(int doorsLayer) {
        List<OutDoor> outDoors = new ArrayList<OutDoor>();

        for(MapObject object : map.getLayers().get(doorsLayer).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            OutDoor outDoor = new OutDoor(world, map, rect, game, object.getName());
            outDoors.add(outDoor);
        }

        return outDoors;
    }

    public List<InDoor> createInDoors(int doorsLayer) {
        List<InDoor> inDoors = new ArrayList<InDoor>();

        for(MapObject object : map.getLayers().get(doorsLayer).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            InDoor inDoor = new InDoor(world, map, rect, game, object.getName());
            inDoors.add(inDoor);
        }

        return inDoors;
    }

    public List<BuyPoint> createBuyPoints(int buyPointsLayer) {
        List<BuyPoint> buyPoints = new ArrayList<BuyPoint>();

        for(MapObject object : map.getLayers().get(buyPointsLayer).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            BuyPoint buyPoint = new BuyPoint(world, map, rect, object.getName());
            buyPoints.add(buyPoint);
        }

        return buyPoints;
    }

    /*public B2DWorldCreator(World world, TiledMap map, Multiplayer game, int collisionLayer, int doorLayer, boolean createDoors, String room) {
        this.game = game;

        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;

        for(MapObject object : map.getLayers().get(collisionLayer).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / 100, (rect.getY() + rect.getHeight() / 2) / 100);

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2 / 100, rect.getHeight() / 2 / 100);
            fdef.shape = shape;
            body.createFixture(fdef);
        }

        //Town
        if(createDoors && room.equals("town")) {
            for(MapObject object : map.getLayers().get(doorLayer).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                new GunShopDoorOut(world, map, rect, game);
            }
        }

        //Gun Shop
        if(createDoors && room.equals("gunshop")) {
            for(MapObject object : map.getLayers().get(doorLayer).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                new GunShopDoorIn(world, map, rect, game);
            }
        }

        if (room.equals("gunshop")) {
            for(MapObject object : map.getLayers().get(doorLayer).getObjects().getByType(RectangleMapObject.class)) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                new WeaponBuyPoint(world, map, rect);
            }
        }
    }*/
}
