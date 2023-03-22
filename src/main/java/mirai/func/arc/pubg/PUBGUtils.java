package mirai.func.arc.pubg;

import mirai.utils.MyFileUtils;

import java.io.File;

import static mirai.utils.MyFileUtils.getFile;

/**
 * @author MengLeiFudge
 */
public class PUBGUtils {
    private PUBGUtils() {
    }

    static File getRootDir() {
        return getFile(MyFileUtils.Dir.DATA, "pubg");
    }

    private static File getPUBGFile(long groupId) {
        return getFile(getRootDir(), groupId + "", "pubg.json");
    }
}
