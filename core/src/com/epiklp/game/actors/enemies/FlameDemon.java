package com.epiklp.game.actors.enemies;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.epiklp.game.Assets;
import com.epiklp.game.Cave;
import com.epiklp.game.TheBox;
import com.epiklp.game.actors.enemies.Enemy;

/**
 * Created by Asmei on 2017-11-29.
 */

public class FlameDemon extends Enemy {
    public FlameDemon() {
        super(new Sprite(Assets.manager.get(Assets.flameDemon)));
        sprite.setSize(1.4f * Cave.PPM * Cave.SCALE, 1.8f * Cave.PPM * Cave.SCALE);
        sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2 + 1.f);
        body = TheBox.createBox(1000, 100, 30f, 50f, false);
        TheBox.createBoxSensor(body, 200f, 90f, new Vector2(0,45f));

        body.setUserData(this);

        initStats();

    }

    @Override
    public void initStats() {
        this.life        = 50;
        this.attackSpeed = 2;
        this.speedWalk   = 4;
        this.strengh     = 10;
        this.attackRange = 5f;
        this.watchRange  = 3f;

    }
}
