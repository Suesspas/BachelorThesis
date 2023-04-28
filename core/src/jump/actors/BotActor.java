package jump.actors;

import com.badlogic.gdx.math.Vector2;
import jump.WorldMisc;
import jump.neuralNetwork.NeuralNetwork;

import java.util.LinkedList;
import java.util.List;

public class BotActor extends HeroActor{
    private NeuralNetwork neuralNetwork;
    private int jumpTimer;

    private boolean isAlive;
    private float score;

    private boolean reachedGoal;
    private static final Vector2 spawn = new Vector2(8f,5f);

    private static final int numberOfSeenPlatforms = 3;

    private int highestPlatformReached; //TODO a priori platformnummer? oder einfach inkrement bei neuer platform?




    public BotActor(int botNumber, int[] nnTopology) {
        super(WorldMisc.createHero(spawn, botNumber));
        neuralNetwork = new NeuralNetwork(nnTopology); //TODO test different topologies, was (numberOfSeenPlatforms*2)+1,5,3
        jumpTimer = 0;
        isAlive = true;
        reachedGoal = false;
        score = 0;
        highestPlatformReached = 0;
    }

    public BotActor(NeuralNetwork.FlattenNetwork net, int botNumber) {
        super(WorldMisc.createHero(spawn, botNumber));
        neuralNetwork = NeuralNetwork.expand(net);
        jumpTimer = 0;
        isAlive = true;
        reachedGoal = false;
        score = 0;
        highestPlatformReached = 0;
    }

    public void updateHighestPlatform(int platformNumber){
        this.highestPlatformReached = Math.max(this.highestPlatformReached, platformNumber);
    }

    public void update(GoalActor goal, int aliveTime) {
        this.score = Math.max(this.score, scoreCalc(goal, aliveTime));
    }

    private float scoreCalc(GoalActor goal, int aliveTime) { //TODO alivetime sinvoll? + fitness value tuning
        float goalDistScore = (reachedGoal ? 1000 : 100) / (1 + distanceTo(goal.body.getPosition()));
        return goalDistScore + Math.min(highestPlatformReached, 10); //encourage reaching new platforms to a certain degree (only first x platforms reached)
    }

    public float distanceTo(Vector2 target){
        return target.dst(this.body.getPosition());
    }

    public float angleTo(Vector2 target){ //normalized angle between [-1,1]
        double atan = Math.atan2(this.body.getPosition().x - target.x, this.body.getPosition().y - target.y);
        return (float) (atan / Math.PI);
    }

    public float getScore() {
        return score;
    }

    public NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    public void feed(PlatformActor closestPlatform, float distanceToGoal) { //TODO inputs in [-1,1]
        if (closestPlatform != null && isAlive) {

            float[] inputs = {
                    //1f, //bias, is that needed? or should you add this to NN?
                    distanceToGoal / WorldMisc.MAXDIST , // alternative distance / GameStage.minWorldWidth
                    this.body.getPosition().x / WorldMisc.minWorldWidth,
                    this.body.getPosition().y / WorldMisc.minWorldHeight,
                    closestPlatform.getX() / WorldMisc.minWorldWidth,
                    closestPlatform.getY() / WorldMisc.minWorldHeight,
            };

            botActions(inputs);

        }
    }

    float[] test;
    public void feed2(List<PlatformActor> platformsByDistance, float distanceToGoal) {
        if (platformsByDistance != null && isAlive) {

            test = new float[]{
                    //1f, //bias, is that needed? or should you add this to NN?
                    distanceToGoal / WorldMisc.MAXDIST , // alternative distance / GameStage.minWorldWidth
                    this.distanceTo(platformsByDistance.get(0).getPosition()) / WorldMisc.MAXDIST, //TODO numberofseenplatforms + sind angles sinnvoll?
                    this.angleTo(platformsByDistance.get(0).getPosition()),
                    this.distanceTo(platformsByDistance.get(1).getPosition()) / WorldMisc.MAXDIST,
                    this.angleTo(platformsByDistance.get(1).getPosition()),
                    this.distanceTo(platformsByDistance.get(2).getPosition()) / WorldMisc.MAXDIST,
                    this.angleTo(platformsByDistance.get(2).getPosition()),
            };

            botActions(test);
        }
    }

    private void botActions(float... inputs) {
        float[] output = this.getNeuralNetwork().eval(inputs); //TODO outputs in what range?
//        if (this.getUserData().getBotNumber() == 1){
//            System.out.println("Bot number " + this.getUserData().getBotNumber() + Arrays.toString(inputs));
//            System.out.println("outputs: " + Arrays.toString(output));
//            System.out.println("Bot 1 highest platform reached: " + highestPlatformReached);
//        }
        if (output[0] > 0.5 && output[0] > output[1]){
            this.moveRight();
        } else if (output[1] > 0.5 && output[1] > output[0]){
            this.moveLeft();
        } else {
            this.moveStop();
        }
        if (output[2] > 0.5)
            this.jump();
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

    public void setNumber(int botNumber) { //TODO whack
        body = WorldMisc.createHero(spawn, botNumber);
    }

    public boolean isOutOfBounds(float x, float y){
        return (this.body.getPosition().x > x) || (this.body.getPosition().x < 0)
                || (this.body.getPosition().y > y) || (this.body.getPosition().y < 0);
    }

    public void reachedGoal() {
        reachedGoal = true;
    }


}
