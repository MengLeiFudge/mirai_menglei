package mirai.func.qa;

import mirai.core.MsgEvent;
import mirai.utils.MyFileUtils;

import java.io.File;

import static mirai.utils.MyFileUtils.getFile;
import static mirai.utils.MyFileUtils.serialize;

/**
 * @author MengLeiFudge
 */
public class QaUtils {
    private QaUtils() {
    }

    private static File getQaFile() {
        return getFile(MyFileUtils.Dir.DATA, "QA", "qaList.json");
    }

    static QaList getQaList(MsgEvent msgEvent) {
        QaList list = MyFileUtils.deserializeThenNotUnlock(getQaFile(), QaList.class, msgEvent);
        if (list == null) {
            list = new QaList();
        }
        return list;
    }

    static void save(QaList list) {
        serialize(list, getQaFile());
    }
}
