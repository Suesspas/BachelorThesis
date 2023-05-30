package jump;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import jump.userdata.BotUserData;
import jump.userdata.GoalUserData;
import jump.userdata.HeroUserData;
import jump.userdata.PlatformUserData;

import java.util.ArrayList;
import java.util.List;

public abstract class WorldMisc {
    static final float heroHeight = 0.8f;
    static final float heroWidth = 0.4f;
    public static final float minWorldWidth = 160f; //def 80f
    public static final float minWorldHeight = 90f; //def 45f
    public static World world;
    public static final float MAXDIST = new Vector2(0,0).dst(minWorldWidth, minWorldHeight);

    public static int level = ConfigManager.getInstance().getLevel();
    public static List<Body> botBodies = new ArrayList<Body>();
    public static final Vector2 spawnCoords = new Vector2(8f,5f);

    public static void createWorld() {

//            System.out.println(MAXDIST);
        world = new World(new Vector2(0, -10), true);
    }

    public static Body createGoal(Vector2 pos, float hx, float hy) {

        BodyDef goal = new BodyDef();
        goal.type = BodyDef.BodyType.StaticBody;
        goal.position.set(pos);
        Body body = world.createBody(goal);
        PolygonShape goalShape = new PolygonShape();
        goalShape.setAsBox(hx, hy); //16.01F, 2.0F default

           /* //give the body gravity, also needs dynamicBody
            FixtureDef groundFixture = new FixtureDef();
            groundFixture.density = 0.5F;
            groundFixture.shape = goalShape;
            groundFixture.friction = 0.0F;
            body.createFixture(groundFixture);
            body.setGravityScale(0.01F);
            body.resetMassData();*/

        body.createFixture(goalShape, 0.5F);
        body.setUserData(new GoalUserData(2*(hx), 2*hy));
        goalShape.dispose();
        return body;
    }

    public static Body createPlatform(Vector2 pos, float hx, float hy, int platformNumber) {

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
        body.setUserData(new PlatformUserData(2*hx, 2*hy, platformNumber));
        platformShape.dispose();
        return body;

    }

    public static Body createHero(Vector2 pos, int botNumber) {
        //TODO instead of createHero every new gen, save popnumber bot bodys and reuse them
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
            setBotUserData(body, botNumber);
            if (botBodies.size() <= botNumber){
                botBodies.add(body);
            } else {
                botBodies.set(botNumber, body);
            }
        }
        heroShape.dispose();
        return body;
    }

    public static void setBotUserData(Body body, int botNumber){
        body.setUserData(new BotUserData(2*heroWidth, 2*heroHeight, botNumber));
    }

    public static void resetBotBodies() {
        for (Body b:
             botBodies) {
            b.setTransform(spawnCoords, b.getAngle());
            b.setAwake(true);
            //b.setActive(true);
        }
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