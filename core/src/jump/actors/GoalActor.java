package jump.actors;

import com.badlogic.gdx.physics.box2d.Body;
import jump.userdata.GoalUserData;

public class GoalActor extends GameActors {

    public GoalActor(Body body) {
        super(body);
    }

    public GoalUserData getUserData() {
        return (GoalUserData) userData;
    }

}
