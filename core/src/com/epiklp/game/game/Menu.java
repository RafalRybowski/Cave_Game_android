package com.epiklp.game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.epiklp.game.Cave;
import com.epiklp.game.functionals.Assets;
import com.epiklp.game.functionals.Controller;
import com.epiklp.game.functionals.b2d.BodyCreator;
import com.epiklp.game.functionals.b2d.TheBox;
import com.epiklp.game.actors.characters.Hero;


/**
 * Created by epiklp on 23.12.17.
 */

public class Menu implements Screen {



    final Cave cave;
    private Camera camera;
    private Viewport viewport;
    private Stage stage;
    private Controller controller;
    private Hero hero;
    private float horizontalForce = 0;
    private Box2DDebugRenderer b2dr;
    private Image image;
    private Image shop;
    private Image credit;
    private boolean enterShop, enterCredit, enterCave;
    private Body creditBody, shopBody, caveBody;
    private CreditsScreen creditsScreen;


    //private PauseMenu MenuPause;

    public Menu(final Cave cave) {

        this.cave = cave;

        creditsScreen = new CreditsScreen();

        enterCredit = false;
        enterShop = false;

        //Download Assets
        image = new Image(Assets.manager.get(Assets.menuLayer));
        image.setSize(Cave.WIDTH, Cave.HEIGHT);
        shop = new Image(Assets.manager.get(Assets.shopLayer));
        shop.setSize(200, 200);
        shop.setPosition(500, 55);
        credit = new Image(Assets.manager.get(Assets.creditLayer));
        credit.setSize(150, 150);
        credit.setPosition(180, 55);

        //Create stage
        camera = new OrthographicCamera(Cave.WIDTH, Cave.HEIGHT);
        viewport = new ExtendViewport(Cave.WIDTH / 1.5f, Cave.HEIGHT / 1.2f, camera);
        stage = new Stage(viewport);
        stage.addActor(image);
        stage.addActor(shop);
        stage.addActor(credit);;
        hero = new Hero(10, 100);  //Hero, przykładowa pozycja
        stage.addActor(hero);


        b2dr = new Box2DDebugRenderer();
        Body floor = BodyCreator.createBody(0, 25, true);
        floor.setUserData("floor");
        BodyCreator.createBoxShape(floor, Cave.WIDTH, 2, 0, 0);
        BodyCreator.createBoxShape(floor, 2, Cave.HEIGHT, 0, 0);

        creditBody = BodyCreator.createBody(130, 75, true);
        BodyCreator.createBoxSensor(creditBody, 60, 60);
        creditBody.setUserData("credit");

        shopBody = BodyCreator.createBody(300, 75, true);
        BodyCreator.createBoxSensor(shopBody, 80, 50);
        shopBody.setUserData("shop");

        caveBody = BodyCreator.createBody(520, 75, true);
        BodyCreator.createBoxSensor(caveBody, 40, 50);
        caveBody.setUserData("cave");

        //Multi Events

        controller = new Controller(true);
        Gdx.input.setInputProcessor(new InputMultiplexer());
        InputMultiplexer inputMultiplexer = (InputMultiplexer) Gdx.input.getInputProcessor();
        inputMultiplexer.addProcessor(Cave.MenuPause);
        inputMultiplexer.addProcessor(Cave.ui);
        inputMultiplexer.addProcessor(controller);
        inputMultiplexer.addProcessor(creditsScreen);


        TheBox.world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if (a.getUserData() instanceof Hero && b.getUserData().equals("credit")) {
                    controller.enterOn();
                    enterCredit = true;
                    return;
                }

                if (a.getUserData() instanceof Hero && b.getUserData().equals("shop")) {
                    controller.enterOn();
                    enterShop = true;
                    return;
                }

                if (a.getUserData() instanceof Hero && b.getUserData().equals("cave")) {
                    controller.enterOn();
                    enterCave = true;
                    TheBox.cleanWorld();
                    cave.setScreen(new GameLevel(cave));
                }
            }

            @Override
            public void endContact(Contact contact) {
                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();
                if (a.getUserData() instanceof Hero && b.getUserData() == "credit") {
                    controller.enterOff();
                    enterCredit = false;
                    return;
                }

                if (a.getUserData() instanceof Hero && b.getUserData() == "shop") {
                    controller.enterOff();
                    enterShop = false;
                    return;
                }

                if (a.getUserData() instanceof Hero && b.getUserData() == "cave") {
                    controller.enterOff();
                    enterCave = false;

                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }


    public void initzializeWorld()
    {

    }

    @Override
    public void show() {

    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (Cave.state.equals(Cave.STATE.OPTION)) {
            stage.draw();
            Cave.MenuPause.draw();
        //    updateMenu(Gdx.graphics.getDeltaTime());
        } else if (Cave.state.equals(Cave.STATE.GAME)) {
            stage.act();
            stage.draw();
            controller.draw();
            Cave.ui.update(hero.maxLife, hero.actLife, hero.maxMana, hero.actMana, hero.maxAura, hero.actAura);
            cave.ui.draw();
            update(Gdx.graphics.getDeltaTime());
            //TheBox.world.setContactListener(myContactListner);
        } else if (Cave.state.equals(Cave.STATE.CREDIT)) {
            creditsScreen.draw();
            creditsScreenUpdate();
        }
        if(Cave.renderBox2D)
            b2dr.render(TheBox.world, camera.combined.scl(Cave.PPM));

    }

    private void creditsScreenUpdate() {
        if (creditsScreen.isBackPress())
            Cave.state = Cave.STATE.GAME;
    }

    private void update(float delta) {
        TheBox.world.step(1 / 60f, 6, 2);
        inputUpdate();  //Controler
    }
    // po tym, jak wyrzuciłem z GameLevel te obliczenia prędkości z metody sterowania do hero, tutaj też musiałem Ci zmienić, żeby się nie sypało

    private void inputUpdate() {
        if (Gdx.input.isTouched()) {
            if (controller.isLeftPressed())
                hero.wantToMoveLeft();
            else if (controller.isRightPressed())
                hero.wantToMoveRight();
        } else {
            hero.wantToIdle();
        }

        if (controller.isSellectPresed()) {
            if (enterShop) System.out.println("weeee shoping time!!!!!!");
            else if (enterCredit) Cave.state = Cave.STATE.CREDIT;
            else if (enterCave) System.out.println("map");
        }

        if (controller.isHomePresed()) {
            Cave.state = Cave.STATE.OPTION;
        }
    }

    /*public void updateMenu(float delta) {
        if (MenuPause.presssResume) {
            Cave.state = Cave.STATE.GAME;
        }
        if (MenuPause.pressRestart) {
            //dispose();
            //cave.setScreen(new Menu(cave));
        }
        if (MenuPause.pressExit) {
            dispose();
            Gdx.app.exit();
        }
    }*/

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
        stage.dispose();
        controller.dispose();
        hero.destroy();
    }
}
