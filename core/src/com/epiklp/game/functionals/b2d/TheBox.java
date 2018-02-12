package com.epiklp.game.functionals.b2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.epiklp.game.Cave;
import com.epiklp.game.actors.GameObject;

import java.util.Iterator;

import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;

/**
 * Created by Asmei on 2017-11-28.
 */

public class TheBox {


    public static final short CATEGORY_PLAYER = 0x0001;
    public static final short CATEGORY_ENEMY = 0x0002;
    public static final short CATEGORY_MAP = 0x0004;
    public static final short CATEGORY_ITEM = 0x0008;
    public static final short CATEGORY_SENSOR = 0x0016;
    public static final short CATEGORY_BULLET = 0x0032;
    public static final short CATEGORY_LIGHT = 0x0064;
    public static final short CATEGORY_WALL = 0x0128;

    public static final short MASK_PLAYER = 1;
    public static final short MASK_WALL = 1;
    public static final short MASK_ENEMY = CATEGORY_MAP | CATEGORY_SENSOR | CATEGORY_PLAYER | CATEGORY_BULLET;
    public static final short MASK_LIGHT = CATEGORY_MAP | CATEGORY_BULLET | CATEGORY_PLAYER | CATEGORY_ENEMY | CATEGORY_LIGHT | CATEGORY_BULLET;
    public static final short MASK_SENSOR = CATEGORY_PLAYER;
    public static final short MASK_BULLET = CATEGORY_MAP | CATEGORY_BULLET | CATEGORY_PLAYER | CATEGORY_ENEMY;


    public static World world;
    public static RayHandler rayHandler;


    private static Array<GameObject> deleteArrayGameObjects = new Array<GameObject>();
    private static Array<Joint> deleteArrayJoints = new Array<Joint>();

    public static void initWorld() {
        world = new World(new Vector2(0, -30f), true);
        initRayHandler();
    }

    //it need a position in PIXEL not in meter from box2d!
    //so if you want to create a Body with position from body.getPosition, you must multiply it by (* Cave.PPM / Cave.SCALE)
    public static Body createBody(float x, float y, boolean isStatic) {
        Body pBody;
        BodyDef def = new BodyDef();
        if (isStatic) def.type = BodyDef.BodyType.StaticBody;
        else def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x / Cave.PPM * Cave.SCALE, y / Cave.PPM * Cave.SCALE);
        def.fixedRotation = true;
        pBody = world.createBody(def);
        return pBody;
    }

    public static Body createStaticBodyForMapBuild(Shape shape, Object userData) {
        Body pBody;
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.fixedRotation = true;
        pBody = world.createBody(def);
        pBody.createFixture(shape, 1);
        pBody.setUserData(userData);
        return pBody;
    }

    public static void createBoxShape(Body body, float width, float height, float density, float friction) {
        PolygonShape shape = new PolygonShape();
        FixtureDef fixDef = new FixtureDef();
        shape.setAsBox(width / Cave.PPM, height / Cave.PPM);
        fixDef.shape = shape;
        fixDef.density = density;
        fixDef.friction = friction;
        fixDef.isSensor = false;

        body.createFixture(fixDef);
        shape.dispose();
    }

    public static void createBoxSensor(Body body, float width, float height, Vector2 shiftFromCenter) {
        PolygonShape shape = new PolygonShape();
        FixtureDef fixDef = new FixtureDef();
        shape.setAsBox(width / Cave.PPM, height / Cave.PPM, new Vector2(shiftFromCenter.x / Cave.PPM, shiftFromCenter.y / Cave.PPM), 0);
        fixDef.shape = shape;
        fixDef.density = 0;
        fixDef.friction = 0;
        fixDef.isSensor = true;
        body.createFixture(fixDef);
        shape.dispose();
    }

    public static void createBoxSensor(Body body, float width, float height) {
        PolygonShape shape = new PolygonShape();
        FixtureDef fixDef = new FixtureDef();
        shape.setAsBox(width / Cave.PPM, height / Cave.PPM);
        fixDef.shape = shape;
        fixDef.density = 0;
        fixDef.friction = 0;
        fixDef.isSensor = true;
        body.createFixture(fixDef);
        shape.dispose();
    }

    public static void createBoxSensor(Body body, float width, float height, Vector2 shiftFromCenter, Object userData) {
        PolygonShape shape = new PolygonShape();
        FixtureDef fixDef = new FixtureDef();
        shape.setAsBox(width / Cave.PPM, height / Cave.PPM, new Vector2(shiftFromCenter.x / Cave.PPM, shiftFromCenter.y / Cave.PPM), 0);
        fixDef.shape = shape;
        fixDef.density = 0;
        fixDef.friction = 0;
        fixDef.isSensor = true;
        Fixture fix = body.createFixture(fixDef);
        fix.setUserData(userData);
        shape.dispose();
    }

    public static void destroyWorld() {
        if (rayHandler != null) {
            rayHandler.dispose();
        }
        if (world != null) {
            world.dispose();
        }

    }

    public static void initRayHandler() {
        RayHandler.setGammaCorrection(false);
        RayHandler.useDiffuseLight(false);
        rayHandler = new RayHandler(BodyCreator.TheBox.world);
        rayHandler.setAmbientLight(1); //0.2f
        rayHandler.setCulling(true);
        rayHandler.setShadows(true);
        Light.setGlobalContactFilter(BodyCreator.TheBox.CATEGORY_LIGHT, (short) 0, BodyCreator.TheBox.MASK_LIGHT);
    }

    public static PointLight createPointLight(Body body, int rays, Color color, int distance, int x, int y) {
        PointLight pointLight = new PointLight(rayHandler, rays, color, 10, -2, -2);
        pointLight.attachToBody(body);
        pointLight.setXray(true);
        pointLight.setIgnoreAttachedBody(true);
        return pointLight;

    }

    //chamska metoda, do zmiany, jak wymyślę lepszą....
    //Jednak sama deklaracja raczej zostanie
    public static void cleanWorld() {
        world = new World(new Vector2(0, -25f), true);
        initRayHandler();
    }

    //It should be check world.isLocket before you use it
    public static void destroyBody(Body body) {
        if (!world.isLocked())
            world.destroyBody(body);
    }

    public static void addToDeleteArray(GameObject gameObject) {
        deleteArrayGameObjects.add(gameObject);
    }

    public static Iterator<GameObject> getDeleteArrayIter() {
        return deleteArrayGameObjects.iterator();
    }

    public static void addToDeleteArrayJoints(Joint joint) {
        deleteArrayJoints.add(joint);
    }

    public static Iterator<Joint> getDeleteArrayIterJoins() {
        return deleteArrayJoints.iterator();
    }


    public static void sweepDeadBodies() {
        if (!world.isLocked()) {
            Iterator<Joint> j = BodyCreator.TheBox.getDeleteArrayIterJoins();
            while (j.hasNext()) {
                world.destroyJoint(j.next());
                j.remove();
            }
            Iterator<GameObject> i = BodyCreator.TheBox.getDeleteArrayIter();
            while (i.hasNext()) {
                i.next().destroy();
                i.remove();
            }
        }
    }
}