package com.epiklp.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.epiklp.game.actors.Enemy;
import com.epiklp.game.actors.FireBall;
import com.epiklp.game.actors.FlameDemon;
import com.epiklp.game.actors.GameActor;
import com.epiklp.game.actors.Hero;

import java.util.Iterator;

/**
 * Created by epiklp on 27.11.17.
 * <p>
 * Stage dla controllera
 */

class GameScreen implements Screen {
    final Cave cave;

    private Stage stage;

    private OrthographicCamera camera;
    private Controller controller;
//	private TextureGame textureGame;

    //physic world 2D
    private Box2DDebugRenderer b2dr;

    private float horizontalForce = 0;

    //texture && sprite && font && map
    private UI ui;
    private OrthogonalTiledMapRenderer tmr;
    private TiledMap map;
    private Viewport viewport;
    //hero
    private Hero hero;
    private GameActor enemy;
    private Array<Enemy> enemies;
    private Array<Body> bodies;
    private Array<Enemy> deadBodies;

    private Array<FireBall> activeFireBalls;
    private float nextAtack =0;


//    private RayHandler rayHandler;

    public GameScreen(Cave cave) {
        this.cave = cave;
        camera = new OrthographicCamera(Cave.WIDTH, Cave.HEIGHT);
        viewport = new ExtendViewport(Cave.WIDTH / Cave.SCALE, Cave.HEIGHT / Cave.SCALE, camera);
        stage = new Stage(viewport);
        TheBox.initWorld();
        Gdx.input.setInputProcessor(stage);

        controller = new Controller();
        ui = new UI();

        b2dr = new Box2DDebugRenderer();
        //rayHandler = new RayHandler(world);

        deadBodies = new Array<Enemy>();

        enemy = new FlameDemon();
        stage.addActor(enemy);
        hero = new Hero();
        stage.addActor(hero);
        activeFireBalls = new Array<FireBall>();
        map = new TmxMapLoader().load("Map/map.tmx");
        tmr = new OrthogonalTiledMapRenderer(map, 2f);

        bodies = TiledObject.parseTiledObjectLayer(TheBox.world, map.getLayers().get("collision").getObjects());

        //rayHandler = new RayHandler(world);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        update(Gdx.graphics.getDeltaTime());
        TheBox.world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                boolean aIsSensor = contact.getFixtureA().isSensor();
                boolean bIsSensor = contact.getFixtureB().isSensor();
                if (a.getUserData() instanceof Enemy && b.getUserData() instanceof Hero && !aIsSensor) {
                    Hero hero = (Hero) b.getUserData();
                    Enemy enemy = (Enemy) a.getUserData();
                    hero.setLife(-enemy.getStrengh());
                    enemy.setLife(-hero.getStrengh());
                    if (enemy.isDead()) deadBodies.add(enemy);

                }
                if (b.getUserData() instanceof Enemy && a.getUserData() instanceof Hero && !bIsSensor) {
                    Hero hero = (Hero) a.getUserData();
                    Enemy enemy = (Enemy) b.getUserData();
                    hero.setLife(-enemy.getStrengh());
                    enemy.setLife(-hero.getStrengh());
                    if (enemy.isDead()) deadBodies.add(enemy);
                }
                //sensor
                if (a.getUserData() instanceof Enemy && b.getUserData() instanceof Hero && aIsSensor) {
                    Hero hero = (Hero) b.getUserData();
                    Enemy enemy = (Enemy) a.getUserData();
                    enemy.followHero(hero.getBody().getPosition());
                }
                if (b.getUserData() instanceof Enemy && a.getUserData() instanceof Hero && bIsSensor) {
                    Hero hero = (Hero) a.getUserData();
                    Enemy enemy = (Enemy) b.getUserData();
                    enemy.followHero(hero.getBody().getPosition());
                }

                if(b.getUserData() instanceof FireBall && a.getUserData() instanceof Enemy && !aIsSensor)
                {
                    Enemy enemy = (Enemy) a.getUserData();
                    enemy.setLife(-10);
                    FireBall fireBall = (FireBall) b.getUserData();
                    activeFireBalls.removeValue(fireBall, false);
                    if (enemy.isDead()) deadBodies.add(enemy);
                    TheBox.world.destroyBody(fireBall.getBody());
                }
            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });

        checkEndGame();
        //textureGame.draw();
        tmr.render();
        b2dr.render(TheBox.world, camera.combined.scl(Cave.PPM));

        stage.act();
        stage.draw();

        controller.draw();
        ui.draw(hero.getLife(), hero.getMagic(), hero.getBody().getPosition().x, hero.getBody().getPosition().y );

    }

    private void checkEndGame() {
        if (hero.isDead()) {
            cave.setScreen(new EndScreen(cave));
        }
    }

    public void update(float delta) {
        nextAtack += delta;
        sweepDeadBodies();
        TheBox.world.step(1 / 60f, 6, 2);
        inputUpdate();
        cameraUpdate();
        FireBallUpdate(delta);
        tmr.setView(camera);
        stage.getViewport().setCamera(camera);
    }

    private void FireBallUpdate(float delta) {
        for(FireBall active: activeFireBalls)
        {
            if(active.getalive() == false)
            {
                TheBox.world.destroyBody(active.getBody());
                activeFireBalls.removeValue(active, false);
            }
            active.update(delta);
        }
    }

    private void cameraUpdate() {
        Vector3 position = camera.position;
        position.x = hero.getBody().getPosition().x * Cave.PPM;
        position.y = hero.getBody().getPosition().y * Cave.PPM;//HEIGHT/PPM/4;
        camera.position.set(position);
        camera.update();
    }


    private void inputUpdate() {

        if (Gdx.input.isTouched()) {
            if (controller.isLeftPressed()) {
                hero.getSprite().setFlip(true, false);
                if (horizontalForce > -(hero.getSpeedWalk()))
                    horizontalForce -= 0.4f;
            } else if (controller.isRightPressed()) {
                hero.getSprite().setFlip(false, false);
                if (horizontalForce < (hero.getSpeedWalk()))
                    horizontalForce += 0.4f;
            }
        } else {
            if (horizontalForce > 0.1) {
                horizontalForce -= 0.2f;
            } else if (horizontalForce < -0.1) {
                horizontalForce += 0.2f;
            } else {
                horizontalForce = 0;
            }
        }
        hero.setSpeedX(horizontalForce);
        if (controller.isUpPressed() && hero.getBody().getLinearVelocity().y == 0) {
            hero.setSpeedY(7f);
        }

        if(controller.isatackPressed() && nextAtack > 1f && hero.getMagic() >=10)
        {
            FireBall tmp = new FireBall(hero.getBody().getPosition().x, hero.getBody().getPosition().y);
            activeFireBalls.add(tmp);
            nextAtack = 0;
            hero.setMagic(-10);
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);

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
//        rayHandler.dispose();
        TheBox.destroyWorld();
        b2dr.dispose();
        ui.dispose();
        controller.dispose();
        tmr.dispose();
        map.dispose();
    }

    public void sweepDeadBodies() {
        if (!TheBox.world.isLocked()) {
            Iterator<Enemy> i = deadBodies.iterator();
            while (i.hasNext()) {
                i.next().destroy();
                i.remove();
            }
        }
    }
}
