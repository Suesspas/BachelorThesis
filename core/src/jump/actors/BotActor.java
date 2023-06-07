package jump.actors;

import com.badlogic.gdx.math.Vector2;
import jump.WorldMisc;
import jump.neuralNetwork.NeuralNetwork;

import java.util.List;

public class BotActor extends HeroActor{
    private NeuralNetwork neuralNetwork;
    private int jumpTimer;

    private boolean isAlive;
    private float score;

    private boolean reachedGoal;

    private static final int numberOfSeenPlatforms = 3;
    private int highestPlatformReached; //TODO a priori platformnummer? oder einfach inkrement bei neuer platform?
    String scoreEvaluation;



    public BotActor(int botNumber, int[] nnTopology, String scoreEvaluation) {
        super(WorldMisc.createHero(WorldMisc.spawnCoords, botNumber));
        neuralNetwork = new NeuralNetwork(nnTopology); //TODO test different topologies, was (numberOfSeenPlatforms*2)+1,5,3
        jumpTimer = 15;
        isAlive = true;
        reachedGoal = false;
        score = 0;
        highestPlatformReached = 0;
        this.scoreEvaluation = scoreEvaluation;
        setAirBorne();
    }

    public BotActor(NeuralNetwork.FlattenNetwork net, int botNumber, String scoreEvaluation) {
        //super(WorldMisc.createHero(WorldMisc.spawnCoords, botNumber)); //TODO reuse bodies also anstatt createhero
        super(WorldMisc.botBodies.get(botNumber));
        neuralNetwork = NeuralNetwork.expand(net);
        jumpTimer = 0;
        isAlive = true;
        reachedGoal = false;
        score = 0;
        highestPlatformReached = 0;
        this.scoreEvaluation = scoreEvaluation;
        setAirBorne();
    }

    public void updateHighestPlatform(int platformNumber){
        this.highestPlatformReached = Math.max(this.highestPlatformReached, platformNumber);
    }

    public void update(GoalActor goal, int aliveTime) {
        this.score = Math.max(this.score, scoreCalc(goal, aliveTime));
    }

    private float scoreCalc(GoalActor goal, int aliveTime) { //TODO alivetime sinvoll? + fitness value tuning
        if (this.scoreEvaluation.equals("std")){
            float goalDistScore = (reachedGoal ? 1000 : 100) / (1 + distanceTo(goal.body.getPosition()));
            return goalDistScore + Math.min(highestPlatformReached, 10); //encourage reaching new platforms to a certain degree (only first x platforms reached)

        } else if (this.scoreEvaluation.equals("nogoal")){
            float goalDistScore = (1 + distanceTo(goal.body.getPosition()));
            return goalDistScore + Math.min(highestPlatformReached, 10);
        } else {
            throw new RuntimeException("no valid parameter for score calculation");
        }
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

    private float[] inputVector = new float[7]; // TODO make variable through NN input conf

    public void feed2(List<PlatformActor> platformsByDistance, float distanceToGoal) {
        if (platformsByDistance != null && isAlive) {

            // set values in inputVector array
            inputVector[0] = distanceToGoal / WorldMisc.MAXDIST;
            inputVector[1] = this.distanceTo(platformsByDistance.get(0).getPosition()) / WorldMisc.MAXDIST;
            inputVector[2] = this.angleTo(platformsByDistance.get(0).getPosition());
            inputVector[3] = this.distanceTo(platformsByDistance.get(1).getPosition()) / WorldMisc.MAXDIST;
            inputVector[4] = this.angleTo(platformsByDistance.get(1).getPosition());
            inputVector[5] = this.distanceTo(platformsByDistance.get(2).getPosition()) / WorldMisc.MAXDIST;
            inputVector[6] = this.angleTo(platformsByDistance.get(2).getPosition());
            //inputVector[7] = 1; //bias

            botActions(inputVector); // pass inputVector array to botActions method
        }
    }

    private float[] output = new float[3]; //TODO make outputs variable trough NN conf
    private void botActions(float... inputs) {
        output = this.getNeuralNetwork().eval(inputs); //TODO outputs in what range?
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
        setAirBorne();
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
        if (jumpTimer > 100){
            return;
        }
        super.landed();
        jumpTimer = 2;
    }

    public void assignBodyNumber(int botNumber) { //TODO verify bot bodies are assigned correctly
        body = WorldMisc.botBodies.get(botNumber);
    }

    public boolean isOutOfBounds(float x, float y){
        float p = 10f; //padding so body completely OOB
        return (this.body.getPosition().x > x + p) || (this.body.getPosition().x < -p)
                || (this.body.getPosition().y > y + p) || (this.body.getPosition().y < -p);
    }

    public void reachedGoal() {
        reachedGoal = true;
    }


}
