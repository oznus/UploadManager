package nusem.oz.uploadmanager.Utils;

import java.util.Random;

/**
 * Created by oz.nusem on 1/25/16.
 */
public class GeneralUtils {

    public static int generateUID() {
        Random random = new Random();
        return random.nextInt(10000);
    }

}
