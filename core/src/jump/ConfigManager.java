package jump;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static ConfigManager instance = null;
    private Properties properties;
    private int[] EAconfs;
    private int[] NNconfs;
    private int[] levels;
    private float physicsSpeedup;
    private int counter;
    private int currentEAconf;
    private int currentNNconf;
    private int currentLevel;
    private int maxGen;

    private ConfigManager() {
        try {
            FileInputStream input = new FileInputStream("core/src/config.properties");
            properties = new Properties();
            properties.load(input);
            EAconfs = parseIntegerArray(properties.getProperty("ea.config"));
            NNconfs = parseIntegerArray(properties.getProperty("nn.config"));
            levels = parseIntegerArray(properties.getProperty("level"));
            physicsSpeedup = Float.parseFloat(properties.getProperty("physics.speedup"));
            maxGen = Integer.parseInt(properties.getProperty("maxGen"));
            counter = 0;
            currentEAconf = EAconfs[counter];
            currentNNconf = NNconfs[counter];
            currentLevel = levels[counter];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateConfigProps() {
        if (counter >= EAconfs.length * NNconfs.length * levels.length) {
            counter = 0;
        } else {
            counter++;
        }

        // Debugging information
        System.out.println("Counter: " + counter);

        int eaIndex = counter % EAconfs.length;
        int nnIndex = (counter / EAconfs.length) % NNconfs.length;
        int levelIndex = (counter / (EAconfs.length * NNconfs.length)) % levels.length;

        // Debugging information
        System.out.println("EA Index: " + eaIndex);
        System.out.println("NN Index: " + nnIndex);
        System.out.println("Level Index: " + levelIndex);

        currentEAconf = EAconfs[eaIndex];
        currentNNconf = NNconfs[nnIndex];
        currentLevel = levels[levelIndex];

        // Debugging information
        System.out.println("Current EAconf: " + currentEAconf);
        System.out.println("Current NNconf: " + currentNNconf);
        System.out.println("Current Level: " + currentLevel);
    }

    private int[] parseIntegerArray(String str) {
        String[] parts = str.split(",");
        int[] array = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            array[i] = Integer.parseInt(parts[i]);
        }
        return array;
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

    public int getCurrentEAconf(){
        return currentEAconf;
    }

    public int getCurrentNNconf(){
        return currentNNconf;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public float getPhysicsSpeedup() {
        return physicsSpeedup;
    }

    public int getMaxGen() {
        return maxGen;
    }

    public void saveProperties() throws IOException {
        FileOutputStream output = new FileOutputStream("config.properties");
        properties.store(output, "Modified properties");
        output.close();
    }
}

