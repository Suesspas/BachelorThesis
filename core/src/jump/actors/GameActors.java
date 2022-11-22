package jump.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import jump.userdata.UserData;

public abstract class GameActors extends Actor {

    protected Body body;
    protected UserData userData;

    public GameActors(Body body) {
        this.body = body;
        this.userData = (UserData)body.getUserData();
    }

    public Vector2 getPosition(){
        return body.getPosition();
    }

    public abstract UserData getUserData();
}