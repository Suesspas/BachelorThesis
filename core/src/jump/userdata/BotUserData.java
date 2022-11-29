package jump.userdata;

public class BotUserData extends HeroUserData{

    /*public BotUserData(float width, float height) {
        super(width, height);
        userDataType = UserDataType.BOT;
    }*/

    public BotUserData(float width, float height, int botNumber) {
        super(width, height);
        this.botNumber = botNumber;
        userDataType = UserDataType.BOT;
    }
}
