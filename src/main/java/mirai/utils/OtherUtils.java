package mirai.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static jdk.internal.net.http.common.Log.logError;

/**
 * 工具类，用于读取文件、取随机数等操作.
 */
public final class OtherUtils {
    private OtherUtils() {
    }

    /**
     * 使调用该方法的线程休眠一段时间.
     *
     * @param milliTime 要休眠的时间，单位 ms
     */
    public static void sleep(long milliTime) {
        try {
            Thread.sleep(milliTime);
        } catch (InterruptedException e) {
            logError(e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 返回斐波那契数列的第 index 项的值.
     *
     * @param index 下标，从1开始
     * @return 斐波那契数列对应下标的值
     */
    public static int getFibonacci(int index) {
        if (index <= 0) {
            logError(new Exception("错误的项数：" + index));
            return -1;
        }
        if (index == 1 || index == 2) {
            return 1;
        } else {
            return getFibonacci(index - 1) + getFibonacci(index - 2);
        }
    }

    /**
     * 计算一个字符串的 MD5.
     *
     * @param s 要计算 MD5 的字符串
     * @return 字符串对应的 MD5
     */
    public static String getMd5(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(s.getBytes());
            byte[] byteArr = md5.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : byteArr) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logError(e);
        }
        return null;
    }

    /**
     * 转义正则特殊字符 "$()*+.[]?\^{},|".
     *
     * @param s 要转义的字符串
     * @return 正则表达式
     */
    public static String strToRegex(String s) {
        if (s != null && !s.equals("")) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (s.contains(key)) {
                    s = s.replace(key, "\\" + key);
                }
            }
        }
        return s;
    }

    /**
     * 标准化字符串.
     *
     * @param s 要标准化的字符串
     * @return 标准化后的字符串
     */
    @Deprecated
    public static String strFormat(String s) {
        if (s != null && !"".equals(s)) {
            s = s.replace("！", "!")
                    .replace("【", "[")
                    .replace("】", "]")
                    .replace("，", ",")
                    .replace("。", ".")
                    .replace("？", "?")
                    .replace(" ", "")
                    .replace("\\n", "")
                    .replace("\\\\", "")
                    .replace("/", "")
                    .replace(":", "")
                    .replace("\\*", "")
                    .replace("\\?", "")
                    .replace("\"", "")
                    .replace("<", "")
                    .replace(">", "")
                    .replace("\\|", "");
        }
        return s;
    }


}
