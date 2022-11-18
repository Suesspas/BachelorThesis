package jump;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import jump.actors.BotActor;
import jump.actors.GoalActor;
import jump.actors.HeroActor;
import jump.actors.PlatformActor;
import jump.geneticAlgorithm.GeneticAlgorithm;
import jump.userdata.HeroUserData;

public class GameStage extends Stage implements ContactListener {

    private float accumulator, TIME_STEP;
    private Box2DDebugRenderer renderer;
    //private B2dModel b2dModel;
    GeneticAlgorithm geneticAlgorithm;
    private World world;
    private WorldMisc wrl;
    private GoalActor Goal;
    private PlatformActor[] platforms;
    /*private LeftWallActor leftWall;
    private RightWallActor rightWall;*/
    private HeroActor player;
    private BotActor[] bots;
    public Boolean right = false, left = false, jump = false;
    int playerJumpTimer;

    private OrthographicCamera camera;
    public Vector3 posCameraDesired;

    public GameStage(){
        super(new ExtendViewport(80f, 45f, new OrthographicCamera(16f, 9f)));
        accumulator = 0.0F;
        TIME_STEP = 1/300F; // recommended by libgdx

        playerJumpTimer = 0;

        //camera = new OrthographicCamera(16f, 9f);

        setupWorld();

        geneticAlgorithm = new GeneticAlgorithm(bots);
    }

    private void setupWorld() {

        wrl = new WorldMisc();
        world = wrl.createWorld();
        renderer = new Box2DDebugRenderer(true,true,true,true,true,true);
        world.setContactListener(this);
        setupGoal();
        setupPlatforms();
        setupHero();
        setupBots();
        /*setupLeftWall();
        setupRightWall();*/

    }

    private void setupGoal(){
        Goal = new GoalActor(WorldMisc.createGoal(world));
        addActor(Goal);
    }

    private void setupPlatforms(){
        platforms = new PlatformActor[4];
        float x = 0;
        float y = 0;
        for (int i = 0; i < platforms.length; i++){
            x = 8.0F + 15*i;
            y = 0.0F + 7*i;
            platforms[i] = new PlatformActor(WorldMisc.createPlatform(world, new Vector2(x, y), 5.0F, 1.0F)); //new Vector2(0.0F, 0.0F, 16.01F, 2.0F) DEFAULT
            platforms[i].setPosition(x, y);
            System.out.println("Platform " + i + " x " + platforms[i].getX() + ", y " + platforms[i].getY() + " EXPECTED x " + x + ", y " + y);
        }
    }

    private void setupHero() {
        player = new HeroActor(WorldMisc.createHero(world, new Vector2(8F, 5F), -1));
        addActor(player);
    }

    private void setupBots() {
        bots = new BotActor[100]; //mehr als 100 bots ruckelt bzw crashed
        for (int i = 0; i < bots.length; i++){
            bots[i] = new BotActor(WorldMisc.createHero(world, new Vector2(8f, 5f), i));

            addActor(bots[i]);
        }
    }

    /*private void setupLeftWall(){
        leftWall = wrl.createLeftWall(world);
    }

    private void setupRightWall(){
        rightWall = wrl.createRightWall(world);
    }*/


    int count = 60; //test
    @Override
    public void act(float delta) {

        super.act(delta);
        accumulator += delta;

        while (accumulator >= delta) {
            world.step(TIME_STEP, 8, 4);
            accumulator -= TIME_STEP;
        }

        //Player Movement
        if (left) {
            player.moveLeft();
//            bots[1].moveLeft();
        } else if (right) {
            player.moveRight();
//            bots[1].moveRight();
        } else {
            player.moveStop();
//            bots[1].moveStop();
        }
        if (jump){
            playerJump();
//            bots[1].jump();
        }
        playerJumpTimer--;

        botMovement();

        for (BotActor b: bots) {
            b.decrementJumpTimer();
        }
    }

    private void botMovement() {
        for (BotActor b: bots) {
            if (b.getUserData().getBotNumber() == 1) {
                //b.moveLeft();
            } else {
                b.moveRight();
            }
            count++;
            if (count > 70){
                if (Math.random() > 0.5){
                    b.jump();
                    count = 0;
                }
            }
        }
    }

    @Override
    public void draw() {

        super.draw();
        renderer.render(world, getViewport().getCamera().combined);
        //processCameraMovement();
        //camera.position.lerp(posCameraDesired,0.1f);//vector of the camera desired position and smoothness of the movement


    }

    public void playerJump() {
        if (playerJumpTimer > 0) {
            return;
        }
        player.jump();
        playerJumpTimer = 15;
    }

    /*private void processCameraMovement(){
        /// make some camera movement
        posCameraDesired.x+=100.0f * Gdx.graphics.getDeltaTime();
        posCameraDesired.y+=100.0f * Gdx.graphics.getDeltaTime();
    }*/

    @Override
    public void beginContact(Contact contact) { //TODO bot contact + no collision between heroes
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        Body bodyA = a.getBody();
        Body bodyB = b.getBody();

        //TODO identifiziere welcher bot kollidiert und führe landed() für denjenigen aus
        if (a.isSensor() && !BodyMisc.bodyIsHero(bodyB)){
            landed(bodyA);
        } else if (b.isSensor() && !BodyMisc.bodyIsHero(bodyA)) {
            landed(bodyB);
        }

        if ((a.isSensor() && BodyMisc.bodyIsGoal(b.getBody())) || (b.isSensor() && BodyMisc.bodyIsGoal(a.getBody()))){
            //level clear
            System.out.println("Level clear");
        }
    }

    private void landed(Body bodyA) {
        HeroUserData userDataA = (HeroUserData) bodyA.getUserData();
        int botNumber = userDataA.getBotNumber();
        if (botNumber < 0){
            player.landed();
            playerJumpTimer = 2;
        } else {
            bots[botNumber].landed();
            //System.out.println("bot " + botNumber + " landed");
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        Body bodyA = a.getBody();
        Body bodyB = b.getBody();

        if (a.isSensor() && !BodyMisc.bodyIsHero(bodyB)){
            setAirBorne(bodyA);
        } else if (b.isSensor() && !BodyMisc.bodyIsHero(bodyA)) {
            setAirBorne(bodyB);
        }
    }

    private void setAirBorne(Body bodyA) {
        HeroUserData userDataA = (HeroUserData) bodyA.getUserData();
        int botNumber = userDataA.getBotNumber();
        if (botNumber < 0){
            player.setAirBorne();
        } else {
            bots[botNumber].setAirBorne();
            //System.out.println("bot " + botNumber + " airborne");
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}