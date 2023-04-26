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
import java.util.List;

public class GameStage extends Stage implements ContactListener {

    private float accumulator, TIME_STEP;
    private Box2DDebugRenderer renderer;
    //private B2dModel b2dModel;
    GeneticAlgorithm geneticAlgorithm;
    private GoalActor goal;
    private List<PlatformActor> platforms;
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
    private static float physicsSpeedup = 1f;
    private static int levelTimer;
    private static int maxLevelTimer;

    public GameStage(){
        super(new ExtendViewport(WorldMisc.minWorldWidth, WorldMisc.minWorldHeight, new OrthographicCamera(16f, 9f)));
        accumulator = 0.0F;
        TIME_STEP = 1/300F; // recommended by libgdx

        playerJumpTimer = 0;

        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear); //blurry text, instead of weird pixels
        font.getData().setScale(0.5f);

        setupWorld();

        geneticAlgorithm = new GeneticAlgorithm();
        DBConTest.init();

        bots = new ArrayList<>();
        for (Genotype genome: geneticAlgorithm.population.genomes) {
            bots.add(genome.getBot());
        }

        camera = getViewport().getCamera();
    }

    private void setupWorld() {
        WorldMisc.createWorld();
        renderer = new Box2DDebugRenderer(true,true,true,true,true,true);
        WorldMisc.world.setContactListener(this);
        setupLevel(2);
        setupHero();
    }

    private void setupLevel(int levelNumber){//TODO set up levels 4+
        List<Vector2> platformCoords = new ArrayList<>();
        switch (levelNumber){
            case 1:
                clearLevel();
                setupGoal(70F, 30F, 8F, 2F);
                for (int i = 0; i < 4; i++){
                    platformCoords.add(new Vector2(8.0F + 15*i, 0.0F + 7*i));
                }
                setupPlatforms(platformCoords);
                levelTimer = 0;
                maxLevelTimer = (int) (500 / physicsSpeedup);
                break;
            case 2:
                clearLevel();
                setupGoal(130F, 55F, 8F, 2F);
                for (int i = 0; i < 8; i++){
                    platformCoords.add(new Vector2(8.0F + 15*i, 0.0F + 7*i));
                }
                setupPlatforms(platformCoords);
                levelTimer = 0;
                maxLevelTimer = (int) (900 / physicsSpeedup);
                break;
            case 3:
                clearLevel();
                setupGoal(10F, 55F, 8F, 2F);
                for (int i = 0; i < 8; i++){
                    platformCoords.add(new Vector2(i > 4? 68.0F - 15*(i-4) : 8.0F + 15*i, 0.0F + 7*i));
                }
                setupPlatforms(platformCoords);
                levelTimer = 0;
                maxLevelTimer = (int) (900 / physicsSpeedup);
                break;
            case 4:
                break;
            case 5:
                break;
            default: return;
        }
    }

    private void clearLevel(){
        if (platforms == null) return;
        for (PlatformActor p : platforms) {
            WorldMisc.world.destroyBody(p.getBody());
        }
        WorldMisc.world.destroyBody(goal.getBody());
    }

    private void setupGoal(float x, float y, float hx, float hy){
        goal = new GoalActor(WorldMisc.createGoal(new Vector2(x, y), hx, hy));
        addActor(goal);
    }

    private void setupPlatforms(List<Vector2> platformCoords){//TODO make hx and hy variable
        platforms = new ArrayList<>();
        for (int i = 0; i < platformCoords.size(); i++){
            PlatformActor platformActor = new PlatformActor(WorldMisc.createPlatform(platformCoords.get(i), 5.0F, 1.0F, i));
            platforms.add(platformActor);
            platforms.get(i).setPosition(platformCoords.get(i).x, platformCoords.get(i).y);
            addActor(platformActor);
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

    @Override
    public void act(float delta) {

        super.act(delta); //* physicsSpeedup ?
        doPhysicsStep(delta * physicsSpeedup);

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

        geneticAlgorithm.updatePopulation(platforms, goal, levelTimer);

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
                WorldMisc.world.destroyBody(b.getBody());
            }
            //update bot numbers
            for (BotActor b : bots) {
                HeroUserData userData = (HeroUserData) b.getBody().getUserData();
                userData.setBotNumber(bots.indexOf(b));
            }
        }


        if (geneticAlgorithm.populationDead() || levelTimer % maxLevelTimer == maxLevelTimer-1) {//TODO change countdown timer for different levels
            for (BotActor b: bots) {
                WorldMisc.world.destroyBody(b.getBody());
            }
            reset();
        }
        levelTimer++;
    }

    private void doPhysicsStep(float delta) {
        float frameTime = Math.min(delta, 0.25f);
        accumulator += frameTime;
        while (accumulator >= frameTime) {
            WorldMisc.world.step(TIME_STEP, 6, 2); //TODO iterations were 8/4, but less may yield better perf
            accumulator -= TIME_STEP;
        }
    }

    String generationStr; //TODO
    String topScoreStr;
    String botsAliveStr;
    float bestBotScore = 0f;
    @Override
    public void draw() {

        super.draw();
        renderer.render(WorldMisc.world, camera.combined);

        batch.setProjectionMatrix(camera.combined);
        generationStr = "Generation " + geneticAlgorithm.generation;
        topScoreStr = "Top score: " + geneticAlgorithm.getBestScore();
        botsAliveStr = "Bots alive: "+ bots.size();
        bestBotScore = Math.max(geneticAlgorithm.getBestScore(), bestBotScore) ;

        batch.begin();

        font.draw(batch, String.valueOf(levelTimer), 1, getHeight()-3);
        font.draw(batch, generationStr, getWidth()/3, getHeight()-3);
        font.draw(batch, botsAliveStr, 2*getWidth()/3, getHeight()-3);
        font.draw(batch, topScoreStr, 1, getHeight()-15);
        font.draw(batch, "Best Fitness so far:" + bestBotScore, 1, getHeight()-30);

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
    public void beginContact(Contact contact) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        Body bodyA = a.getBody();
        Body bodyB = b.getBody();

        if (BodyMisc.bodyIsHero(bodyA) || BodyMisc.bodyIsHero(bodyB)) return; //TODO zu testzwecken hero noch in code, entfernen oder code unten anpassen

        //identifiziere welcher bot kollidiert und führe landed() für denjenigen aus
        if (a.isSensor() && !BodyMisc.bodyIsCharacter(bodyB)){
            landed(bodyA);
            BotActor bot = bots.get(getBotNumber(bodyA));
            PlatformUserData platformUserData = (PlatformUserData) bodyB.getUserData(); //TODO schauen ob conversion bei goalUserData funktioniert bei contact
            bot.updateHighestPlatform(platformUserData.getPlatformNumber());
            bot.update(goal, levelTimer);
        } else if (b.isSensor() && !BodyMisc.bodyIsCharacter(bodyA)) {
            landed(bodyB);
            BotActor bot = bots.get(getBotNumber(bodyB));
            PlatformUserData platformUserData = (PlatformUserData) bodyA.getUserData();
            bot.updateHighestPlatform(platformUserData.getPlatformNumber());
            bot.update(goal, levelTimer);
        }
        //Bot reaches goal
        if ((a.isSensor() && BodyMisc.bodyIsGoal(b.getBody()))){
            BotActor bot = bots.get(getBotNumber(bodyA));
            bot.reachedGoal();
            bot.update(goal, levelTimer);
        } else if (b.isSensor() && BodyMisc.bodyIsGoal(a.getBody())){
            BotActor bot = bots.get(getBotNumber(bodyB));
            bot.reachedGoal();
            bot.update(goal, levelTimer);
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

        if (a.isSensor() && !BodyMisc.bodyIsCharacter(bodyB)){
            setAirBorne(bodyA);
        } else if (b.isSensor() && !BodyMisc.bodyIsCharacter(bodyA)) {
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


}