package mirai.func.sakura;

import mirai.core.MsgEvent;
import mirai.func.sakura.character.player.Player;
import mirai.utils.MyFileUtils;

import java.io.File;

import static mirai.utils.MyFileUtils.getFile;
import static mirai.utils.MyFileUtils.serialize;

/**
 * @author MengLeiFudge
 */
public class SakuraUtils {
    private SakuraUtils() {
    }

    static File getRootDir() {
        return getFile(MyFileUtils.Dir.DATA, "sakuraCity");
    }

    private static File getPlayerDir() {
        return getFile(getRootDir(), "player");
    }

    private static File getPlayerFile(long qq) {
        return getFile(getPlayerDir(), qq + ".json");
    }

    static Player getPlayer(long qq, MsgEvent msgEvent) {
        return MyFileUtils.deserializeThenNotUnlock(getPlayerFile(qq), Player.class, msgEvent);
    }

    static void save(Player player) {
        serialize(player, getPlayerFile(player.getQq()));
    }

}
