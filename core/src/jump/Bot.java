package jump;

public class Bot {
    private boolean left;
    private boolean right;
    private boolean jump;

    public Bot() {
        this.left = false;
        this.right = true;
        this.jump = true;
    }

    public Bot(boolean left, boolean right, boolean jump) {
        this.left = left;
        this.right = right;
        this.jump = jump;
    }



    private void predict(){ //TODO predict stuff
        //TODO starten mit rechts halten und zu richtigen Zeitpunkten springen, dann erst Neuroevolution
        left = true;
        right = true;
        jump = true;
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return right;
    }

    public boolean isJump() {
        return jump;
    }
}
