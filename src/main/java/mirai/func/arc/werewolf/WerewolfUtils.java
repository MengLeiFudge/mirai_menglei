package mirai.func.arc.werewolf;

import mirai.core.MsgEvent;
import mirai.utils.MyFileUtils;

import java.io.File;

import static mirai.utils.MyFileUtils.getFile;
import static mirai.utils.MyFileUtils.serialize;

/**
 * @author MengLeiFudge
 */
public class WerewolfUtils {
    private WerewolfUtils() {
    }

    public enum Role {
        // 狼人
        WEREWOLVES,
        // 狼人
        WOLF,
        WOLF1,
        NVWU,
        LIEREN,
        YUYANJIA,
        YUZHE,

    }


    static File getRootDir() {
        return getFile(MyFileUtils.Dir.DATA, "arc_werewolf");
    }

    private static File getUserDir() {
        return getFile(getRootDir(), "user");
    }

    private static File getUserFile(long qq) {
        return getFile(getUserDir(), qq + ".json");
    }

    static Player getUser(long qq, MsgEvent msgEvent) {
        return MyFileUtils.deserializeThenNotUnlock(getUserFile(qq), Player.class, msgEvent);
    }

    static void save(Player player) {
        serialize(player, getUserFile(player.getQq()));
    }

}
