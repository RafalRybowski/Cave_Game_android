package com.epiklp.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;

import java.awt.Font;

/**
 * Created by epiklp on 29.11.17.
 */

public class Assets {

    public static final AssetManager manager = new AssetManager();

    public static final AssetDescriptor<Texture> player = new AssetDescriptor<Texture>("character/1.png", Texture.class);
    public static final AssetDescriptor<Texture>[] layer = new AssetDescriptor[9];
    public static BitmapFont character;

    public static void load()
    {
        manager.load(player);
        String tmp="";
        for(int i =0; i<9; i++)
        {
            tmp = "layer/" + (i+1)+".png";
            layer[i] = new AssetDescriptor<Texture>(tmp, Texture.class);
            manager.load(layer[i]);
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/font.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 80;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
        BitmapFont ala = generator.generateFont(parameter);
        character = ala;

    }

    public static void dispose()
    {
        manager.dispose();
    }
}