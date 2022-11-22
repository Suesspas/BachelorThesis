package jump;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import jump.geneticAlgorithm.Genotype;
import jump.userdata.BotUserData;
import jump.userdata.GoalUserData;
import jump.userdata.HeroUserData;
import jump.userdata.PlatformUserData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GameStage extends Stage implements ContactListener {

    private float accumulator, TIME_STEP;
    private Box2DDebugRenderer renderer;
    //private B2dModel b2dModel;
    GeneticAlgorithm geneticAlgorithm;
    private static World world;
    private WorldMisc wrl;
    private GoalActor goal;
    private PlatformActor[] platforms;
    /*private LeftWallActor leftWall;
    private RightWallActor rightWall;*/
    private HeroActor player;
    private List<BotActor> bots;
    public Boolean right = false, left = false, jump = false;
    int playerJumpTimer;

    private Camera camera;
    public Vector3 posCameraDesired;

    SpriteBatch batch = new SpriteBatch();
    BitmapFont font = new BitmapFont();

    public static final float minWorldWidth = 320f; //def 80f
    public static final float minWorldHeight = 180f; //def 45f

    public GameStage(){
        super(new ExtendViewport(minWorldWidth, minWorldHeight, new OrthographicCamera(16f, 9f)));
        accumulator = 0.0F;
        TIME_STEP = 1/300F; // recommended by libgdx

        playerJumpTimer = 0;

        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear); //blurry text, instead of weird pixels
        font.getData().setScale(0.5f);

        setupWorld();

        geneticAlgorithm = new GeneticAlgorithm();

        bots = new ArrayList<>();
        for (Genotype genome: geneticAlgorithm.population.genomes) {
            bots.add(genome.getBot());
        }

        camera = getViewport().getCamera();
    }

    private void setupWorld() {

        wrl = new WorldMisc();
        world = wrl.createWorld();
        renderer = new Box2DDebugRenderer(true,true,true,true,true,true);
        world.setContactListener(this);
        setupGoal();
        setupPlatforms();
        setupHero();
        //setupBots();
        /*setupLeftWall();
        setupRightWall();*/

    }

    private void setupGoal(){
        goal = new GoalActor(WorldMisc.createGoal());
        addActor(goal);
    }

    private void setupPlatforms(){
        platforms = new PlatformActor[4];
        float x = 0;
        float y = 0;
        for (int i = 0; i < platforms.length; i++){
            x = 8.0F + 15*i;
            y = 0.0F + 7*i;
            platforms[i] = new PlatformActor(WorldMisc.createPlatform(new Vector2(x, y), 5.0F, 1.0F)); //new Vector2(0.0F, 0.0F, 16.01F, 2.0F) DEFAULT
            platforms[i].setPosition(x, y);
            //System.out.println("Platform " + i + " x " + platforms[i].getX() + ", y " + platforms[i].getY() + " EXPECTED x " + x + ", y " + y);
        }
    }

    private void setupHero() {
        player = new HeroActor(WorldMisc.createHero(new Vector2(8F, 5F), -1));
        addActor(player);
    }

//    private void setupBots() {
//        bots = new ArrayList<BotActor>(); //mehr als 100 bots ruckelt bzw crashed
//        BotActor bot;
//        for (int i = 0; i < botnumber; i++){
//            bot = new BotActor(i);
//            bots.add(bot);
//            addActor(bot); //TODO add actor when new generation
//        }
//    }

    /*private void setupLeftWall(){
        leftWall = wrl.createLeftWall(world);
    }

    private void setupRightWall(){
        rightWall = wrl.createRightWall(world);
    }*/


    static int count = 60; //test
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

        //botMovement();
        geneticAlgorithm.updatePopulation(platforms, goal);

        List<BotActor> deadBots = new ArrayList<>();

        //jumptimer and get dead bots
        for (BotActor b: bots) { //TODO kill bots physics bodies
            b.decrementJumpTimer();
            if(!b.isAlive()){
                deadBots.add(b);
            }
        }
        //remove dead bots
        if (deadBots.size() > 0) {
            for (BotActor b : deadBots) {
                bots.remove(b);
//            HeroUserData userData = (HeroUserData) b.getBody().getUserData();
//            userData.setBotNumber(-1);
                world.destroyBody(b.getBody());
            }
            //update bot numbers
            for (BotActor b : bots) {
                HeroUserData userData = (HeroUserData) b.getBody().getUserData();
                userData.setBotNumber(bots.indexOf(b));
            }
        }


        if (geneticAlgorithm.populationDead() || count % 500 == 499) {
            for (BotActor b: bots) {
                    world.destroyBody(b.getBody());
            }
            reset();
        }
        count++;
    }

    private void botMovement() {
        for (BotActor b: bots) {
            b.update(goal);

            if (b.getUserData().getBotNumber() == 1) {
                //b.moveLeft();
            } else {
                b.moveRight();
            }
            count++;
            if (count > 70){
                if (Math.random() > 0.5){
                    b.jump();
//                    System.out.println(b.getBody().getPosition() + ", OOB? " + b.isOutOfBounds(minWorldWidth, minWorldHeight));
//                    System.out.println(b.getBody().getPosition() + ", score " + b.getScore());
//                    System.out.println("BEST score " + geneticAlgorithm.getBestScore());
                    count = 0;
                }
            }
        }

    }


    @Override
    public void draw() {

        super.draw();
        renderer.render(world, camera.combined);

        batch.setProjectionMatrix(camera.combined);
        String generationStr = "Generation " + geneticAlgorithm.generation;
        String topScoreStr = "Top score: " + geneticAlgorithm.getBestScore();
        String botsAliveStr = "Bots alive: "+ bots.size();

        batch.begin();

        font.draw(batch, String.valueOf(count), 1, getHeight()-3);
        font.draw(batch, generationStr, 100, getHeight()-3);
        font.draw(batch, botsAliveStr, 200, getHeight()-3);
        font.draw(batch, topScoreStr, 1, getHeight()-20);

        batch.end();

        //camera.update();
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

    private void reset() {
        geneticAlgorithm.evolvePopulation(); //TODO test
        List<Genotype> genomes = geneticAlgorithm.population.genomes;
        for (int i = 0; i < genomes.size(); i++) {
            BotActor bot = geneticAlgorithm.population.genomes.get(i).getBot();
            if (i < bots.size()) {
                bots.set(i, bot);
                addActor(bot);
            } else {
                bots.add(bot);
                addActor(bot);
            }
        }
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
        //Bot reaches goal
        if ((a.isSensor() && BodyMisc.bodyIsGoal(b.getBody()))){
            BotActor bot = bots.get(getBotNumber(bodyA));
            bot.reachedGoal();
            bot.update(goal);
        } else if (b.isSensor() && BodyMisc.bodyIsGoal(a.getBody())){
            BotActor bot = bots.get(getBotNumber(bodyB));
            bot.reachedGoal();
            bot.update(goal);
        }
    }

    private void landed(Body bodyA) {
        int botNumber = getBotNumber(bodyA);
        if (botNumber < 0){
            player.landed();
            playerJumpTimer = 2;
        } else {
            bots.get(botNumber).landed();
            //System.out.println("bot " + botNumber + " landed");
        }
    }

    private static int getBotNumber(Body bodyA) {
        HeroUserData userDataA = (HeroUserData) bodyA.getUserData();
        return userDataA.getBotNumber();
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
            bots.get(botNumber).setAirBorne();
            //System.out.println("bot " + botNumber + " airborne");
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public static class WorldMisc {

        static final float heroHeight = 0.8f;
        static final float heroWidth = 0.4f;

        public  World createWorld() {

            return new World(new Vector2(0, -10), true);
        }

        public static Body createGoal() {

            BodyDef goal = new BodyDef();
            goal.type = BodyDef.BodyType.StaticBody;
            goal.position.set(new Vector2(70F, 30F));
            Body body = world.createBody(goal);
            PolygonShape goalShape = new PolygonShape();
            goalShape.setAsBox(8F, 2.0F); //16.01F, 2.0F default

           /* //give the body gravity, also needs dynamicBody
            FixtureDef groundFixture = new FixtureDef();
            groundFixture.density = 0.5F;
            groundFixture.shape = goalShape;
            groundFixture.friction = 0.0F;
            body.createFixture(groundFixture);
            body.setGravityScale(0.01F);
            body.resetMassData();*/

            body.createFixture(goalShape, 0.5F);
            body.setUserData(new GoalUserData(2*(8F), 4.0F));
            goalShape.dispose();
            return body;

        }

        public static Body createPlatform(Vector2 pos, float hx, float hy) {

            BodyDef platform = new BodyDef();
            platform.type = BodyDef.BodyType.StaticBody;
            platform.position.set(pos);
            Body body = world.createBody(platform);
            PolygonShape platformShape = new PolygonShape();
            platformShape.setAsBox(hx, hy);

           /* //give the body gravity, also needs dynamicBody
            FixtureDef groundFixture = new FixtureDef();
            groundFixture.density = 0.5F;
            groundFixture.shape = groundShape;
            groundFixture.friction = 0.0F;
            body.createFixture(groundFixture);
            body.setGravityScale(0.01F);
            body.resetMassData();*/

            body.createFixture(platformShape, 0F);
            body.setUserData(new PlatformUserData(2*hx, 2*hy));
            platformShape.dispose();
            return body;

        }

        public static Body createHero(Vector2 pos, int botNumber) {

            BodyDef hero = new BodyDef();
            hero.type = BodyDef.BodyType.DynamicBody;
            hero.position.set(pos); // new Vector2(8F, 5F) default
            hero.fixedRotation = true;
            PolygonShape heroShape = new PolygonShape();
            heroShape.setAsBox(heroWidth, heroHeight);
            Body body = world.createBody(hero);
            FixtureDef heroFixture = new FixtureDef();
            heroFixture.density = 0.5F;
            heroFixture.shape = heroShape;
            heroFixture.friction = 0.0F;
            heroFixture.filter.groupIndex = -1; //keine kollision zwischen heroes
            body.createFixture(heroFixture);

            PolygonShape footSensor = new PolygonShape();
            footSensor.setAsBox(heroWidth - 0.1f, 0.1F, new Vector2(0,-heroHeight), 0);
            FixtureDef footSensorFixture = new FixtureDef();
            footSensorFixture.density = 0F;
            footSensorFixture.shape = footSensor;
            footSensorFixture.friction = 0.0F;
            footSensorFixture.isSensor = true;
            footSensorFixture.filter.groupIndex = -1;
            body.createFixture(footSensorFixture);

            body.setGravityScale(5F);
            body.resetMassData();
            if (botNumber < 0){
                body.setUserData(new HeroUserData(2*heroWidth, 2*heroHeight));
            } else {
                body.setUserData(new BotUserData(2*heroWidth, 2*heroHeight, botNumber));
            }
            heroShape.dispose();
            return body;
        }

        /*public Body createLeftWall(World world) {

            BodyDef leftWall = new BodyDef();
            leftWall.type = BodyDef.BodyType.StaticBody;
            leftWall.position.set(new Vector2(-1.0F, 12.0F));
            Body body = world.createBody(leftWall);
            PolygonShape leftWallShape = new PolygonShape();
            leftWallShape.setAsBox(1.0F, 11.0F);
            body.createFixture(leftWallShape, 0.5F);
            body.setUserData(new LeftWallUserData(2.0F, 22.0F));
            leftWallShape.dispose();
            return body;

        }

        public Body createRightWall(World world) {

            BodyDef rightWall = new BodyDef();
            rightWall.type = BodyDef.BodyType.StaticBody;
            rightWall.position.set(new Vector2(17F, 12.0F));
            Body body = world.createBody(rightWall);
            PolygonShape rightWallShape = new PolygonShape();
            rightWallShape.setAsBox(0.999F, 11.0F);
            body.createFixture(rightWallShape, 0.5F);
            body.setUserData(new RightWallUserData(1.998F, 22.0F));
            rightWallShape.dispose();
            return body;

        }*/
    }
}