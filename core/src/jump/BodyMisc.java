package jump;

import com.badlogic.gdx.physics.box2d.Body;
import jump.userdata.UserData;
import jump.userdata.UserDataType;

public class BodyMisc {

    public BodyMisc(){

    }

    public static boolean bodyIsHero(Body body) {
        UserData userData = (UserData)body.getUserData();
        return userData != null && ((userData.getUserDataType() == UserDataType.HERO) || (userData.getUserDataType() == UserDataType.BOT));
    }

    public static boolean bodyIsGoal(Body body) {
        UserData userData = (UserData)body.getUserData();
        return userData != null && userData.getUserDataType() == UserDataType.GOAL;
    }

    //TODO also for y coordinates
    public static boolean bodyInBounds(Body body) {
        float worldWidth = 160F;
        UserData userData = (UserData)body.getUserData();
        return body.getPosition().x + userData.getWidth() / 2.0F > -4.0F && body.getPosition().x + userData.getWidth() / 2.0F < (worldWidth + 4);

    }

    public static boolean bodyIsPlatform(Body b) {
        UserData userData = (UserData) b.getUserData();
        return userData != null && userData.getUserDataType() == UserDataType.PLATFORM;
    }
}
