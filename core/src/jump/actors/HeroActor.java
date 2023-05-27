package jump.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Timer;
import jump.userdata.HeroUserData;

public class HeroActor extends GameActors {

    private Vector2 velocityEachTime;
    private Vector2 movingLinearImpulse;
    private float velocityX;
    private float finalVel;

    private final float maxSpeed = 10;
    private float  heroPositionY, heroPositionX;
    private boolean jumping = true;
    private boolean dodging = false;
    private boolean land = false;
    private Vector2 heroPosition;
    private Timer timer1 = new Timer();

    private boolean spawned;


    public HeroActor(Body body) {
        super(body);
        movingLinearImpulse = new Vector2();
        spawned = true;
    }

    public HeroUserData getUserData() {
        return (HeroUserData) userData;
    }

    public void moveRight() {

        velocityEachTime = body.getLinearVelocity();
        velocityX = velocityEachTime.x;
        if (velocityX > 0){
            finalVel = getUserData().getSpeedImpulse() - Math.min((velocityX),maxSpeed);
        } else {
            finalVel = getUserData().getSpeedImpulse() - Math.max((velocityX), -maxSpeed);
        }
        //finalVel = getUserData().getSpeedImpulse() - velocityX;
        movingLinearImpulse.set(finalVel, 0.0F);
        body.applyLinearImpulse(movingLinearImpulse, body.getWorldCenter(), true);

    }

    public void moveLeft() {

        velocityEachTime = body.getLinearVelocity();
        velocityX = velocityEachTime.x;
        if (velocityX > 0){
            finalVel = -getUserData().getSpeedImpulse() - Math.min((velocityX),maxSpeed);
        } else {
            finalVel = -getUserData().getSpeedImpulse() - Math.max((velocityX), -maxSpeed);
        }
        //finalVel = -getUserData().getSpeedImpulse() - velocityX;
        movingLinearImpulse.set(finalVel, 0.0F);
        body.applyLinearImpulse(movingLinearImpulse, body.getWorldCenter(), true);

    }

    public void moveStop() {

        velocityEachTime = body.getLinearVelocity();
        velocityX = velocityEachTime.x;
        if (velocityX > 0){
            finalVel = 0.0f - Math.min((velocityX),maxSpeed);
        } else {
            finalVel = 0.0f - Math.max((velocityX), -maxSpeed);
        }
        movingLinearImpulse.set(finalVel/2, 0.0F);
        body.applyLinearImpulse(movingLinearImpulse, body.getWorldCenter(), true);

    }

    public void jump() {
        if(!jumping && !dodging && land && !spawned) {
            body.applyLinearImpulse(getUserData().getJumpingImpulse(), body.getWorldCenter(), true);
            //setAirBorne(); //nicht mehr nötig weil check auf endcontact
        }
    }

    public void setAirBorne() {
        jumping = true;
        land = false;
    }

    public void landed() {
        jumping = false;
        land = true;
        spawned = false;
    }

    public void dodge() {
        if(land && !dodging) {
            heroPosition = body.getPosition();
            heroPositionY = heroPosition.y - 0.4F;
            heroPositionX = heroPosition.x;
            heroPosition = new Vector2(heroPositionX, heroPositionY);
            body.setTransform(heroPosition, -1.5707964F); // -90 degrees in radians
            dodging = true;
            Timer.Task task = new Timer.Task() {
                public void run() {
                    heroPosition = body.getPosition();
                    heroPositionY = heroPosition.y + 0.4F;
                    heroPositionX = heroPosition.x;
                    heroPosition = new Vector2(heroPositionX, heroPositionY);
                    body.setTransform(heroPosition, 0.0F);
                    dodging = false;
                }
            };
            timer1.scheduleTask(task, getUserData().getSlideDuration());
        }
    }
}



