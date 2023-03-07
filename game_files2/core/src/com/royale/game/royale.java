package com.royale.game;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class royale extends Game
{
	SpriteBatch batch;
	ShapeRenderer shape;
	Texture img;
	BitmapFont font;

	@Override
	public void create() {
		batch = new SpriteBatch();
		shape = new ShapeRenderer();
		font = new BitmapFont();
		//img = new Texture("test_asset.jpg");
		this.setScreen(new Main_Menu(this));
	}

	@Override
	public void render()
	{
		ScreenUtils.clear(0,0,0,0);
		super.render();
	}

	@Override
	public void dispose()
	{
		batch.dispose();
		font.dispose();
		//img.dispose();
	}
}
