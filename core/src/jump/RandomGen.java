package jump;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;

import java.util.Date;

public abstract class RandomGen {
    private static MersenneTwister twister = new MersenneTwister(new Date());
    private static MersenneTwister64 twister64 = new MersenneTwister64(new Date());

    public static int nextInt(){
        return twister.nextInt();
    }

    public static float nextFloat(){
        return twister.nextFloat();
    }

    public static double nextDouble(){
        return twister64.nextDouble();
    }

    /**
     * From java.util.Random nextGaussian() but with MersenneTwister.nextDouble()
     */
    private static double nextNextGaussian;
    private static boolean haveNextNextGaussian = false;
    public static synchronized double nextGaussian() {
        // See Knuth, TAOCP, Vol. 2, 3rd edition, Section 3.4.1 Algorithm C.
        if (haveNextNextGaussian) {
            haveNextNextGaussian = false;
            return nextNextGaussian;
        } else {
            double v1, v2, s;
            do {
                v1 = 2 * twister64.nextDouble() - 1; // between -1 and 1
                v2 = 2 * twister64.nextDouble() - 1; // between -1 and 1
                s = v1 * v1 + v2 * v2;
            } while (s >= 1 || s == 0);
            double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s)/s);
            nextNextGaussian = v2 * multiplier;
            haveNextNextGaussian = true;
            return v1 * multiplier;
        }
    }
}
