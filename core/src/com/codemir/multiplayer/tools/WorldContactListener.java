package com.codemir.multiplayer.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.codemir.multiplayer.Multiplayer;
import com.codemir.multiplayer.entity.InteractiveTileObject;
import com.codemir.multiplayer.entity.Monster;
import com.codemir.multiplayer.entity.living.Player;
import com.codemir.multiplayer.screens.GameScreen;

import java.util.Objects;

public class WorldContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        if(fixA.getUserData() == "head" || fixB.getUserData() == "head") {
            Fixture head = fixA.getUserData() == "head" ? fixA : fixB;
            Fixture object = head == fixA ? fixB : fixA;

            if (object.getUserData() != null && InteractiveTileObject.class.isAssignableFrom(object.getUserData().getClass())) {
                ((InteractiveTileObject) object.getUserData()).isHeadHit = true;
                ((InteractiveTileObject) object.getUserData()).onHeadHit();
            }
        }

        if(fixA.getUserData() == "weapon" || fixB.getUserData() == "weapon") {
            Fixture weapon = fixA.getUserData() == "weapon" ? fixA : fixB;
            Fixture monster = weapon == fixA ? fixB : fixA;

            if (monster.getUserData() != null && Monster.class.isAssignableFrom(monster.getUserData().getClass())) {
                ((Monster) monster.getUserData()).onHit(weapon.getUserData().toString(), weapon.getBody().getPosition().x, weapon.getBody().getPosition().y);
            }
        }

        if(fixA.getUserData() != null && fixB.getUserData() != null) {
            if (Monster.class.isAssignableFrom(fixA.getUserData().getClass()) || Monster.class.isAssignableFrom(fixB.getUserData().getClass())) {
                Fixture monster = Monster.class.isAssignableFrom(fixA.getUserData().getClass()) ? fixA : fixB;
                Fixture player = monster == fixA ? fixB : fixA;

                if (player.getUserData() != null && monster.getUserData() != null && player.getUserData() == "player") {
                    Gdx.app.log("Player", "Damaged!");
                    Player.health -= 5;
                }
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        if(fixA.getUserData() == "head" || fixB.getUserData() == "head") {
            Fixture head = fixA.getUserData() == "head" ? fixA : fixB;
            Fixture object = head == fixA ? fixB : fixA;

            if (object.getUserData() != null && InteractiveTileObject.class.isAssignableFrom(object.getUserData().getClass())) {
                ((InteractiveTileObject) object.getUserData()).isHeadHit = false;
                ((InteractiveTileObject) object.getUserData()).noHeadHit();
            }
        }

        if(fixA.getUserData() == "weapon" || fixB.getUserData() == "weapon") {
            Fixture weapon = fixA.getUserData() == "weapon" ? fixA : fixB;
            Fixture monster = weapon == fixA ? fixB : fixA;

            if (monster.getUserData() != null && Monster.class.isAssignableFrom(monster.getUserData().getClass())) {
                ((Monster) monster.getUserData()).noHit();
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
