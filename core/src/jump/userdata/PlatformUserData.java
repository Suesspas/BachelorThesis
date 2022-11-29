package jump.userdata;

public class PlatformUserData extends UserData {

    int platformNumber;
    public PlatformUserData(float width, float height, int platformNumber) {
        super(width, height);
        userDataType = UserDataType.PLATFORM;
        this.platformNumber = platformNumber;
    }

    public int getPlatformNumber() {
        return platformNumber;
    }
}
