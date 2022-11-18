package jump.actors;

import com.badlogic.gdx.physics.box2d.Body;
import jump.userdata.PlatformUserData;

public class PlatformActor extends GameActors {

    public PlatformActor(Body body) {
            super(body);
        }
        public PlatformUserData getUserData() {
            return (PlatformUserData) userData;
        }

}


