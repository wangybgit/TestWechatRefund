

import java.util.UUID;

/**
 * Created by jia on 2015/7/25.
 */
public class Ids {

    public static String uuidAsHex() {
        return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
    }
}
