package jump.userdata;

import com.badlogic.gdx.math.Vector2;

public class HeroUserData extends UserData {

    private Vector2 jumpingImpulse;
    private float slideDuration;
    private float speedImpulse;

    protected int botNumber; //-1 if player, 0 - n-1 for n bots

    public HeroUserData(float width, float height) {

        super(width, height);
        jumpingImpulse = new Vector2(0,25F); //11 default
        slideDuration = 1.5F;
        speedImpulse = 8.0F; //5 default
        userDataType = UserDataType.HERO;
        botNumber = -1;
    }

    public void setJumpingImpulse(Vector2 jumpingImpulse) {
        this.jumpingImpulse = jumpingImpulse;
    }

    public void setSlideDuration(float slideDuration) {
        this.slideDuration = slideDuration;
    }

    public void setSpeedImpulse(float SpeedImpulse) {
        this.speedImpulse = speedImpulse;
    }

    public Vector2 getJumpingImpulse() {
        return jumpingImpulse;
    }

    public float getSlideDuration() {
        return slideDuration;
    }

    public float getSpeedImpulse() {
        return speedImpulse;
    }

    public int getBotNumber() {
        return botNumber;
    }
}