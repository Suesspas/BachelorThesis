package jump.userdata;

public class PlatformUserData extends UserData {
    public PlatformUserData(float width, float height) {
        super(width, height);
        userDataType = UserDataType.PLATFORM;
    }
}
