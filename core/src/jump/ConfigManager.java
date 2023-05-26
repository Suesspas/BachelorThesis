package jump;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static ConfigManager instance = null;
    private Properties properties;
    private int EAconf;
    private int NNconf;
    private int level;
    private float physicsSpeedup;

    private ConfigManager() {
        try {
            FileInputStream input = new FileInputStream("core/src/config.properties");
            properties = new Properties();
            properties.load(input);
            EAconf = Integer.parseInt(properties.getProperty("ea.config"));
            NNconf = Integer.parseInt(properties.getProperty("nn.config"));
            level = Integer.parseInt(properties.getProperty("level"));
            physicsSpeedup = Float.parseFloat(properties.getProperty("physics.speedup"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public Properties getProperties() {
        return properties;
    }

    //Can add updateConfig() Method to change values at runtime
    //and propagate the changes to the specific classes

    public int getEAconf(){
        return EAconf;
    }

    public int getNNconf(){
        return NNconf;
    }

    public int getLevel() {
        return level;
    }

    public float getPhysicsSpeedup() {
        return physicsSpeedup;
    }

    public void setEAconf(int EAconf) {
        this.EAconf = EAconf;
    }

    public void setNNconf(int NNconf) {
        this.NNconf = NNconf;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void saveProperties() throws IOException {
        FileOutputStream output = new FileOutputStream("config.properties");
        properties.store(output, "Modified properties");
        output.close();
    }
}

