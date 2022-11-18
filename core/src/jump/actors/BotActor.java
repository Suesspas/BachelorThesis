package jump.actors;

import com.badlogic.gdx.physics.box2d.Body;
import jump.Bot;
import jump.neuralNetwork.NeuralNetwork;

public class BotActor extends HeroActor{
    private Bot bot;
    private NeuralNetwork neuralNetwork;
    private int jumpTimer;

    private boolean isAlive;
    private int score;



    public BotActor(Body body) {
        super(body);
        jumpTimer = 0;
        bot = new Bot();
        isAlive = true;
        neuralNetwork = new NeuralNetwork(3,3,3); //TODO figure out topology
        score = 0;
    }

    public void update() {
        this.score++;
    }

    public int getScore() {
        return score;
    }

    public NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    public void feed(PlatformActor closestPlatform, float distance) { //TODO dist y values
//        if (closestPlatform != null && isAlive) {
//            float[] inputs = {
//                    distance / Screen.WIDTH,
//                    (this.y + this.radius - (Screen.HEIGHT - closestPlatform.height)) / Screen.HEIGHT,
//                    ((Screen.HEIGHT - closestPlatform.gap - closestPlatform.height) - this.y - this.radius) / Screen.HEIGHT,
//            };
//            float[] output = this.net.eval(inputs);
//            if (output[0] > output[1])
//                this.flap();
//        }
    }

    @Override
    public void jump() {
        if (jumpTimer > 0) {
            return;
        }
        super.jump();
        jumpTimer = 15;
    }

    public void decrementJumpTimer(){
        jumpTimer--;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void dead() {
        isAlive = false;
    }

    @Override
    public void landed() {
        super.landed();
        jumpTimer = 2;
    }
}
