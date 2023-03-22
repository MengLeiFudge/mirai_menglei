package mirai.utils;

import net.mamoe.mirai.utils.MiraiLogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class LogUtils {
    private LogUtils() {
    }

    public static MiraiLogger logger;

    enum LogType {
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    /**
     * 暂存要发给作者的 error 信息，使用 set 以去掉重复的 error.
     */
    private static final Set<String> TEMP_ERROR_SEND_TO_AUTHOR = new HashSet<>();

    static void sendAllErrorToAuthor() {
        //todo:补全
//        if (!TEMP_ERROR_SEND_TO_AUTHOR.isEmpty()) {
//            Api.changeAllSourceToPrivate(AUTHOR_QQ);
//            // 最多发送两条，防止发太多导致 bot 冻结
//            for (int i = 0; i < TEMP_ERROR_SEND_TO_AUTHOR.size(); i++) {
//                if (i < 3) {
//                    for (String s : TEMP_ERROR_SEND_TO_AUTHOR) {
//                        Api.send(Thread.currentThread().getId() + "\n" + s);
//                        sleep(500);
//                    }
//                } else {
//                    Api.send(Thread.currentThread().getId() + "\n出现多个错误，请查看日志！");
//                    break;
//                }
//            }
//        }
    }

    /**
     * 将 extraInfo 与 e 所含的信息拼接.
     * <p>
     * 如果 extraInfo 为 {@code null}，或进行 {@link String#trim()} 处理后为空字符串 ""，
     * extraInfo 将被定义为空；
     * <p>
     * 如果 e 为 {@code null}，或将其转化为具有异常类型、异常描述信息、方法调用情况的
     * 字符串并进行 {@link String#trim()} 处理后为空字符串 ""，
     * exceptionInfo 将被定义为空；
     * <p>
     * 根据 extraInfo 与 exceptionInfo 是否为空，有以下三种情况：
     * <ul>
     * <li>都没有实际意义，返回空字符串 ""
     * <li>仅一个有实际意义，返回二者直接拼接的结果
     * <li>都有实际意义，返回二者用 {@code \n} 拼接的结果
     * </ul>
     *
     * @param extraInfo 额外信息
     * @param e         异常
     * @return 拼接结果
     */
    static String spliceExtraInfoAndException(String extraInfo, Exception e) {
        if (extraInfo == null) {
            extraInfo = "";
        } else {
            extraInfo = extraInfo.trim();
        }
        boolean emptyExtraInfo = "".equals(extraInfo);
        String exceptionInfo;
        if (e == null) {
            exceptionInfo = "";
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            exceptionInfo = baos.toString().trim();
        }
        boolean emptyExceptionInfo = "".equals(exceptionInfo);
        if (emptyExtraInfo && emptyExceptionInfo) {
            return "";
        } else if (!emptyExtraInfo && !emptyExceptionInfo) {
            return extraInfo + "\n" + exceptionInfo;
        } else {
            return extraInfo + exceptionInfo;
        }
    }

    /**
     * show、write、send Log.
     * <ul>
     * <li>show：在 medic 日志界面显示日志
     * <li>write：根据设置决定是否保存日志到文件
     * <li>send：type 为 {@code LogType.ERROR} 时，将部分日志信息作为消息发出
     * </ul>
     * {@code extraInfo} 和 {@code e} 二者共同决定最终的日志信息，
     * 具体拼接规则见 {@link #spliceExtraInfoAndException(String, Exception)}.
     *
     * @param type    log类型
     * @param message 额外信息
     * @param e       异常
     */
    static void swsLog(LogType type, String message, Exception e) {
        switch (type) {
            case DEBUG:
                logger.debug(message, e);
                break;
            case INFO:
                logger.info(message, e);
                break;
            case WARNING:
                logger.warning(message, e);
                break;
            case ERROR:
                logger.error(message, e);
                break;
        }
        String fullInfo = spliceExtraInfoAndException(message, e);
        if (type == LogType.ERROR) {
            // 向消息源提示
            // todo:补全
//            send("似乎出现了一些错误捏orz\n" +
//                    "bug已反馈，请耐心等候修复（咕咕咕）");
            // 向作者发送消息，先将其暂存，因为群消息切到私聊就不能切回群
            TEMP_ERROR_SEND_TO_AUTHOR.add(fullInfo);
        }
    }

    /**
     * 记录并发送一个 Error.
     *
     * @param e 要记录的异常
     */
    public static void logError(Exception e) {
        swsLog(LogType.ERROR, null, e);
    }

    /**
     * 记录并发送一个 Error，可以附加额外信息.
     *
     * @param extraInfo 补充信息，用于定位问题，如方法的参数值等
     * @param e         异常
     */
    public static void logError(String extraInfo, Exception e) {
        swsLog(LogType.ERROR, extraInfo, e);
    }

    /**
     * 记录一个 Warn.
     *
     * @param e 要记录的异常
     */
    public static void logWarn(Exception e) {
        swsLog(LogType.WARNING, null, e);
    }

    /**
     * 记录一个 Warn，可以附加额外信息.
     *
     * @param extraInfo 补充信息，用于定位问题，如方法的参数值等
     * @param e         要记录的异常
     */
    public static void logWarn(String extraInfo, Exception e) {
        swsLog(LogType.WARNING, extraInfo, e);
    }

    /**
     * 记录一个 Info.
     *
     * @param info 要记录的信息
     */
    public static void logInfo(String info) {
        swsLog(LogType.INFO, info, null);
    }

}
