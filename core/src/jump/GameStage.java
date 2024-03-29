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
import jump.data.DatabaseConnector;
import jump.evolutionaryAlgorithm.EvolutionaryAlgorithm;
import jump.evolutionaryAlgorithm.Genotype;
import jump.userdata.HeroUserData;
import jump.userdata.PlatformUserData;

import java.util.ArrayList;
import java.util.List;

public class GameStage extends Stage implements ContactListener {

    private float accumulator, TIME_STEP;
    private Box2DDebugRenderer renderer;
    //private B2dModel b2dModel;
    EvolutionaryAlgorithm evolutionaryAlgorithm;
    private GoalActor goal;
    private List<PlatformActor> platforms;
    /*private LeftWallActor leftWall;
    private RightWallActor rightWall;*/
    private HeroActor player;
    private List<BotActor> bots;
    public Boolean right = false, left = false, jump = false;
    private int playerJumpTimer;
    private Camera camera;
    public Vector3 posCameraDesired;

    SpriteBatch batch = new SpriteBatch();
    BitmapFont font = new BitmapFont();
    private static float physicsSpeedup = ConfigManager.getInstance().getPhysicsSpeedup(); //TODO move to config
    private static int levelTimer;
    private static int resetTimer;
    private static int maxResetTimer;

    public GameStage(){
        super(new ExtendViewport(WorldMisc.minWorldWidth, WorldMisc.minWorldHeight, new OrthographicCamera(16f, 9f)));
        accumulator = 0.0F;
        TIME_STEP = 1/300F; // recommended by libgdx

        playerJumpTimer = 0;

        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear); //blurry text, instead of weird pixels
        font.getData().setScale(0.5f);

        setupWorld();

        DatabaseConnector.init();
        setupEA(true);

        camera = getViewport().getCamera();
    }

    private void setupEA(boolean firstSetup) {
        evolutionaryAlgorithm = new EvolutionaryAlgorithm(firstSetup);

        bots = new ArrayList<>();
        for (Genotype genome: evolutionaryAlgorithm.population.genomes) {
            genome.assignBodyNumber(bots.size());
            bots.add(genome.getBot());
            bots.forEach(BotActor::setAirBorne);
        }
        System.out.println("bots size: " + bots.size());
    }

    private void setupWorld() {
        WorldMisc.createWorld();
        renderer = new Box2DDebugRenderer(true,true,true,true,true,true);
        WorldMisc.world.setContactListener(this);
        setupLevel(WorldMisc.level);
        setupHero();
    }

    private void setupLevel(int levelNumber){//TODO set up levels 4+
        List<Vector2> platformCoords = new ArrayList<>();
        WorldMisc.updateSpawnCoords(levelNumber);
        int platformCount;
        switch (levelNumber){
            case 1:
                clearLevel();
                for (int i = 0; i < 4; i++){
                    platformCoords.add(new Vector2(8.0F + 15*i, 0.0F + 7*i));
                }
                setupPlatforms(platformCoords);
                setupGoal(70F, 30F, 8F, 2F);
                levelTimer = 0;
                resetTimer = 0;
                maxResetTimer = (int) (700 / physicsSpeedup);
                break;
            case 2:
                clearLevel();
                for (int i = 0; i < 8; i++){
                    platformCoords.add(new Vector2(8.0F + 15*i, 0.0F + 7*i));
                }
                setupPlatforms(platformCoords);
                setupGoal(130F, 55F, 8F, 2F);
                levelTimer = 0;
                resetTimer = 0;
                maxResetTimer = (int) (1200 / physicsSpeedup);
                break;
            case 3:
                clearLevel();
                for (int i = 0; i < 8; i++){
                    platformCoords.add(new Vector2(i > 4? 68.0F - 15*(i-4) : 8.0F + 15*i, 0.0F + 7*i));
                }
                setupPlatforms(platformCoords);
                setupGoal(10F, 55F, 8F, 2F);
                levelTimer = 0;
                resetTimer = 0;
                maxResetTimer = (int) (2000 / physicsSpeedup);
                break;
            case 4:
                clearLevel();
                platformCount = 8;
                float[][] offset = {{0f,5f,10f,15f,20f,30f,40f,50f},
                        {0f,0f,0f,0f,0f,0f,0f,0f}};
                for (int i = 0; i < platformCount; i++){
                    platformCoords.add(new Vector2((8.0F * (i + 1)) + offset[0][i], 0.0F));
                }

                setupPlatforms(platformCoords);
                setupGoal(135F, 0F, 8F, 2F);
                levelTimer = 0;
                resetTimer = 0;
                maxResetTimer = (int) (1800 / physicsSpeedup);
                break;
            case 5:
                clearLevel();
                platformCount = 12;
                for (int i = 0; i < platformCount; i++){
                    platformCoords.add(new Vector2(WorldMisc.spawnCoords.x - 10 * i,
                            WorldMisc.spawnCoords.y - 20));
                }

                for (int i = 0; i < 4; i++){
                    platformCoords.add(new Vector2(8.0F + 15*i, 0.0F + 7*i));
                }

                setupPlatforms(platformCoords);
                setupGoal(70F, 30F, 8F, 2F);
                levelTimer = 0;
                resetTimer = 0;
                maxResetTimer = (int) (3000 / physicsSpeedup);
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
        Body goalHitbox = WorldMisc.createGoal(new Vector2(x, y), hx, hy);
        goal = new GoalActor(goalHitbox);
        PlatformActor goalActor = new PlatformActor(goalHitbox);
        platforms.add(goalActor);
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
    boolean resetDone = false;

    @Override
    public void act(float delta) {

        super.act(delta * physicsSpeedup); //* physicsSpeedup ?
        _doPhysicsStep(delta * physicsSpeedup);

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

        evolutionaryAlgorithm.updatePopulation(platforms, goal, resetTimer);

        List<BotActor> deadBots = new ArrayList<>();

        //jumptimer and get dead bots
        for (BotActor b: bots) {
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
                //WorldMisc.world.destroyBody(b.getBody()); //TODO reuse bodies
                //b.getBody().setActive(false);
                Body botbody = b.getBody();
                HeroUserData userData = (HeroUserData) botbody.getUserData();
                userData.setBotNumber(-1);
                botbody.setAwake(false);
                b.setAirBorne();
            }
            //update bot numbers
            for (BotActor b : bots) {
                HeroUserData userData = (HeroUserData) b.getBody().getUserData();
                userData.setBotNumber(bots.indexOf(b));
            }
        }

        if (resetDone) {
            System.out.println(levelTimer + " delta: " + delta);
            resetDone = false;
        }

        if (evolutionaryAlgorithm.populationDead() || resetTimer > maxResetTimer) {//TODO change countdown timer for different levels
            reset();
            resetDone = true;
            for (BotActor b: bots) {
                //WorldMisc.world.destroyBody(b.getBody());
                //b.getBody().setActive(false);
                b.setAirBorne();
            }
        }
        levelTimer++;
        resetTimer++;
    }

    private void doPhysicsStep(float delta) {
        float frameTime = Math.min(delta, 0.05f); //before 0.25f
        accumulator += frameTime;
//        WorldMisc.world.step(frameTime, 6, 2);
        while (accumulator >= frameTime) {
            WorldMisc.world.step(TIME_STEP, 6, 2); //TODO iterations were 8/4, but less may yield better perf
            accumulator -= TIME_STEP;
//            evolutionaryAlgorithm.updatePopulation(platforms, goal, resetTimer);
        }
    }

    private void _doPhysicsStep(float delta) {
        float frameTime = Math.min(delta, 0.05f); //before 0.25f
        accumulator += delta;
        WorldMisc.world.step(TIME_STEP * physicsSpeedup * 5, 6, 2);
//        while (accumulator >= TIME_STEP) {
//            WorldMisc.world.step(TIME_STEP, 6, 2);
//            accumulator -= TIME_STEP;
//            evolutionaryAlgorithm.updatePopulation(platforms, goal, resetTimer);
//        }
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
        generationStr = "Generation " + evolutionaryAlgorithm.generation;
        topScoreStr = "Top score: " + evolutionaryAlgorithm.getBestScore();
        // topScoreStr = "Top score: " + String.format("%.3f", evolutionaryAlgorithm.getBestScore());
        botsAliveStr = "Bots alive: "+ bots.size();
        bestBotScore = Math.max(evolutionaryAlgorithm.getBestScore(), bestBotScore) ;

        batch.begin();

        font.draw(batch, String.valueOf(levelTimer), 1, getHeight()-3);
        font.draw(batch, generationStr, getWidth()/3, getHeight()-3);
        font.draw(batch, botsAliveStr, 2*getWidth()/3, getHeight()-3);
        font.draw(batch, topScoreStr, 1, getHeight()-15);
        font.draw(batch, "Best score so far: " + bestBotScore, 1, getHeight()-23);
        //font.draw(batch, "Best score so far: " + String.format("%.3f", bestBotScore), 1, getHeight()-23);
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
        WorldMisc.resetBotBodies();
        if (evolutionaryAlgorithm.generation >= ConfigManager.getInstance().getMaxGen()){
            ConfigManager.getInstance().updateConfigProps();
            setupEA(false);
        } else {
            evolutionaryAlgorithm.evolvePopulation();
        }
        List<Genotype> genomes = evolutionaryAlgorithm.population.genomes;
        for (int i = 0; i < genomes.size(); i++) {
            BotActor bot = evolutionaryAlgorithm.population.genomes.get(i).getBot();
            if (i < bots.size()) {
                bots.set(i, bot);
                //bot.getUserData().setBotNumber(i);
                WorldMisc.setBotUserData(bot.getBody(), i);
                addActor(bot);
            } else {
                bots.add(bot);
                WorldMisc.setBotUserData(bot.getBody(), bots.size()-1);
                addActor(bot);
            }
            bot.setAirBorne();
            bot.setAlive();
        }
        resetTimer = 0;
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
            bot.update(goal, resetTimer);
        } else if (b.isSensor() && !BodyMisc.bodyIsCharacter(bodyA)) {
            landed(bodyB);
            BotActor bot = bots.get(getBotNumber(bodyB));
            PlatformUserData platformUserData = (PlatformUserData) bodyA.getUserData();
            bot.updateHighestPlatform(platformUserData.getPlatformNumber());
            bot.update(goal, resetTimer);
        }
        //Bot reaches goal
        if ((a.isSensor() && BodyMisc.bodyIsGoal(b.getBody()))){
            BotActor bot = bots.get(getBotNumber(bodyA));
            bot.reachedGoal();
            bot.update(goal, resetTimer);
        } else if (b.isSensor() && BodyMisc.bodyIsGoal(a.getBody())){
            BotActor bot = bots.get(getBotNumber(bodyB));
            bot.reachedGoal();
            bot.update(goal, resetTimer);
        }
    }

    private void landed(Body body) {
        int botNumber = getBotNumber(body);
        if (botNumber < 0){
            player.landed();
            playerJumpTimer = 2;
        } else {
            bots.get(botNumber).landed();
            //if (botNumber < 10) System.out.println("bot " + botNumber + " landed");
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