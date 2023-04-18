package jump;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Array;

public class GameScreen implements Screen {

    final Jump game;
    OrthographicCamera camera;
    Box2DDebugRenderer debugRenderer;
    //B2dModel model;
    private GameStage gameStage;
    BitmapFont font = new BitmapFont();
    SpriteBatch batch = new SpriteBatch();


    public GameScreen(final Jump gam) {
        this.game = gam;

        //model = new B2dModel();
        gameStage = new GameStage();
        //Gdx.input.setInputProcessor(gameStage);

        // create the camera and the SpriteBatch
        camera = new OrthographicCamera(gameStage.getWidth(), gameStage.getHeight());
        camera.setToOrtho(false, gameStage.getWidth(), gameStage.getHeight());
        //font.getData().scale(0.01f);
        //debugRenderer = new Box2DDebugRenderer(true,true,true,true,true,true);

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        gameStage.draw();
        gameStage.act(delta);

        gameStage.left = Gdx.input.isKeyPressed(Keys.LEFT);
        gameStage.right = Gdx.input.isKeyPressed(Keys.RIGHT);
        gameStage.jump = Gdx.input.isKeyPressed(Keys.UP);

//        batch.setProjectionMatrix(camera.combined); //or your matrix to draw GAME WORLD, not UI
//
//        batch.begin();
//
//        font.draw(batch, "1", 1, gameStage.getHeight()-3);
//
//
//        batch.end();
//        camera.update();

        /*model.logicStep(delta);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        debugRenderer.render(model.world, camera.combined);*/

    }

    @Override
    public void resize(int width, int height) {
        gameStage.getViewport().update(width,height,true);
    }

    @Override
    public void show() {
        //gameStage = new GameStage();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        gameStage.dispose();
    }

}