package jump.userdata;

public class GoalUserData extends PlatformUserData {
    public GoalUserData(float width, float height) {
        super(width, height, Integer.MAX_VALUE); //TODO ist int max value sinnvoll?
        userDataType = UserDataType.GOAL;
    }
}
