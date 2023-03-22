package mirai.utils;

import static mirai.utils.MyFileUtils.Dir;
import static mirai.utils.MyFileUtils.getBoolean;
import static mirai.utils.MyFileUtils.getFile;
import static mirai.utils.MyFileUtils.setKeyValue;
import static mirai.utils.Settings.AUTHOR_QQ;

/**
 * @author MengLeiFudge
 */
public class BotAdminUtils {
    private BotAdminUtils() {
    }

    public static void setAdmin(long id, boolean isBotAdmin) {
        setKeyValue(getFile(Dir.SETTINGS, "botAdmin.txt"), id, isBotAdmin);
    }

    public static boolean isBotAdmin(long id) {
        return id == AUTHOR_QQ || getBoolean(getFile(Dir.SETTINGS, "botAdmin.txt"), id);
    }
}
