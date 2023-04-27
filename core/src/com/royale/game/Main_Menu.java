package com.royale.game;
import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
//./gradlew html:superDev
public class Main_Menu implements Screen
{
    Sound background_music;
    BitmapFont font;
    OrthographicCamera camera;
    final royale game;

    public Main_Menu(final royale input)
    {
        this.game = input;
        camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
    }

    @Override
    public void render(float delta)
    {
        ScreenUtils.clear(0,0,0, 0);
        camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		
		game.batch.begin();
        //Add title/intro card - change later (set better assets)
		game.font.draw(game.batch, "Welcome to Tycoon Royale:", 300, 240);
		game.font.draw(game.batch, "Tap anywhere to begin.", 300, 290);
		game.batch.end();

		if (Gdx.input.isTouched()) 
        {
			game.setScreen(new Game_Display(game));
			dispose();
		}
    }
    @Override
	public void dispose()
	{}
    //unused (for the most part) - but needed to implement Screen
    //===============================================================================
    @Override
	public void resize(int width, int height){}
	@Override
	public void show(){}
	@Override
	public void hide(){}
	@Override
	public void pause(){}
	@Override
	public void resume(){}
	
}
