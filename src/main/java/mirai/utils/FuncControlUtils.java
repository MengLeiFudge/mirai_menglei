package mirai.utils;

import mirai.core.ProcessMsgEvent;

import static mirai.utils.MyFileUtils.getBoolean;
import static mirai.utils.MyFileUtils.getFile;
import static mirai.utils.MyFileUtils.setKeyValue;

/**
 * @author MengLeiFudge
 */
public class FuncControlUtils {
    private FuncControlUtils() {
    }

    /**
     * 获取功能在群中的启用状态，默认关闭.
     *
     * @param group      群号
     * @param myFunction 某个功能
     * @return 功能在群中的启用状态
     */
    public static boolean getFuncState(long group, ProcessMsgEvent.MyFunction myFunction) {
        if (myFunction.getIndex() <= 0) {
            return true;
        }
        return getBoolean(getFile(MyFileUtils.Dir.SETTINGS, "funcState", group + ".txt"), myFunction.getName());
    }

    /**
     * 开启/关闭指定功能.
     *
     * @param group      群号
     * @param myFunction 功能
     * @param open       开启还是关闭功能
     */
    public static void setFuncState(long group, ProcessMsgEvent.MyFunction myFunction, boolean open) {
        setKeyValue(getFile(MyFileUtils.Dir.SETTINGS, "funcState", group + ".txt"), myFunction.getName(), open);
    }


    /**
     * 获取功能启用状态字符串.
     *
     * @param group      群号
     * @param myFunction 某个功能
     * @return 功能在群中的启用状态
     */
    public static String getFuncStateStr(long group, ProcessMsgEvent.MyFunction myFunction) {
        return getFuncState(group, myFunction) ? "开启" : "关闭";
    }

}
