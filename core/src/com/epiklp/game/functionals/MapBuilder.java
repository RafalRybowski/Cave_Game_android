package com.epiklp.game.functionals;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.epiklp.game.Cave;
import com.epiklp.game.actors.characters.Enemy;
import com.epiklp.game.actors.characters.Hero;
import com.epiklp.game.actors.characters.Rat;
import com.epiklp.game.actors.characters.Spider;
import com.epiklp.game.functionals.b2d.BodyCreator;


public class MapBuilder {
    public static Hero parseHeroFromObjectLayer(MapObjects objects){
        MapObject hero = objects.get("hero");
        if (hero instanceof RectangleMapObject) {
            RectangleMapObject rect = ((RectangleMapObject) hero);
            return new Hero(rect.getRectangle().x, rect.getRectangle().y);
        }
        return new Hero(0, 0);
    }

    public static Array<Enemy> parseEnemiesFromObjectLayer(MapObjects objects){
        Array<Enemy> enemies = new Array<Enemy>();
        for(MapObject object : objects){
            if(object instanceof RectangleMapObject){
                RectangleMapObject en = ((RectangleMapObject) object);
                if(Utils.equalsWithNulls(object.getName(), Rat.class.getSimpleName())){
                    enemies.add(new Rat(en.getRectangle().x, en.getRectangle().y));
                }else if(Utils.equalsWithNulls(object.getName(), Spider.class.getSimpleName())){
                    enemies.add(new Spider(en.getRectangle().x , en.getRectangle().y));
                }
            }

        }
        return enemies;
    }
    public static Array<Body> parseTiledObjectLayer(World world, MapObjects objects) {
        Array<Body> bodies = new Array<Body>();
        for (MapObject object : objects) {
            Shape shape;
            if (object instanceof PolylineMapObject) {
                shape = createPolyLine((PolylineMapObject) object);
            } else if (object instanceof RectangleMapObject) {
                shape = getRectangle((RectangleMapObject) object);
                if(Utils.equalsWithNulls(object.getName(), "CLIMBING_WALL")){
                    bodies.add(BodyCreator.createStaticBodyForMapBuild(shape, "CLIMBING_WALL"));
                    shape.dispose();
                    continue;
                }
            } else {
                continue;
            }
            bodies.add(BodyCreator.createStaticBodyForMapBuild(shape, MapBuilder.class.getSimpleName()));
            shape.dispose();
        }
        return bodies;
    }


    private static ChainShape createPolyLine(PolylineMapObject polyline) {
        float[] verticles = polyline.getPolyline().getTransformedVertices();
        Vector2[] worldVerticles = new Vector2[verticles.length / 2];
        for (int i = 0; i < verticles.length / 2; ++i) {
            worldVerticles[i] = new Vector2();
            worldVerticles[i].x = verticles[i * 2] / Cave.PPM * Cave.SCALE;
            worldVerticles[i].y = verticles[i * 2 + 1] / Cave.PPM * Cave.SCALE;
        }

        ChainShape cs = new ChainShape();
        cs.createChain(worldVerticles);
        return cs;
    }

    private static PolygonShape getRectangle(RectangleMapObject rectangleObject) {
        Rectangle rectangle = rectangleObject.getRectangle();
        PolygonShape polygon = new PolygonShape();
        Vector2 size = new Vector2((rectangle.x + rectangle.width * 0.5f) / Cave.PPM * Cave.SCALE,
                (rectangle.y + rectangle.height * 0.5f) / Cave.PPM * Cave.SCALE);
        polygon.setAsBox(rectangle.width * 0.5f / Cave.PPM * Cave.SCALE,
                rectangle.height * 0.5f / Cave.PPM * Cave.SCALE,
                size,
                0.0f);
        return polygon;
    }
}