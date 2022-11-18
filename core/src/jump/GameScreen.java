package jump;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Array;

public class GameScreen implements Screen {

    final Jump game;

    Texture dropImage;
    Texture bucketImage;
    Sound dropSound;
    Music rainMusic;
    OrthographicCamera camera;
    Rectangle bucket;
    Array<Rectangle> raindrops;
    long lastDropTime;
    int dropsGathered;
    Box2DDebugRenderer debugRenderer;
    //B2dModel model;
    private GameStage gameStage;


    public GameScreen(final Jump gam) {
        this.game = gam;

        //model = new B2dModel();
        gameStage = new GameStage();
        //Gdx.input.setInputProcessor(gameStage);


        /*// load the images for the droplet and the bucket, 64x64 pixels each
        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        // load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);*/

        // create the camera and the SpriteBatch
        //camera = new OrthographicCamera(32,24);
        //camera.setToOrtho(false, 32, 24);
        //debugRenderer = new Box2DDebugRenderer(true,true,true,true,true,true);

       /* // create a Rectangle to logically represent the bucket
        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2; // center the bucket horizontally
        bucket.y = 20; // bottom left corner of the bucket is 20 pixels above
        // the bottom screen edge
        bucket.width = 64;
        bucket.height = 64;*/

        // create the raindrops array and spawn the first raindrop
        /*raindrops = new Array<Rectangle>();
        spawnRaindrop();*/

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

        // camera.update();

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