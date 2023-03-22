package mirai.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static mirai.utils.LogUtils.logWarn;

public class DateUtils {
    private DateUtils() {
    }


    /**
     * 时间戳格式化为日期字符串.
     *
     * @param timestamp 通常是System.currentTimeMillis()
     * @param format    格式标准
     * @return 指定格式的字符串
     */
    public static String timestampToStr(long timestamp, String format) {
        return new SimpleDateFormat(format, Locale.CHINA).format(new Date(timestamp));
    }

    /**
     * 日期实例格式化为日期字符串.
     *
     * @param date   日期实例
     * @param format 格式标准
     * @return 指定格式的字符串
     */
    public static String timestampToStr(Date date, String format) {
        return new SimpleDateFormat(format, Locale.CHINA).format(date);
    }

    /**
     * 时间戳格式化为完整时间字符串.
     *
     * @param timestamp 通常是System.currentTimeMillis()
     * @return 格式为"2020-01-01 00:00:00"的完整时间字符串
     */
    public static String getFullTimeStr(long timestamp) {
        return timestampToStr(timestamp, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 时间戳格式化为年月日字符串.
     *
     * @param timestamp 通常是System.currentTimeMillis()
     * @return 格式为"2020-01-01"的年月日字符串
     */
    public static String getDateStr(long timestamp) {
        return timestampToStr(timestamp, "yyyy-MM-dd");
    }

    /**
     * 时间戳格式化为时分秒字符串.
     *
     * @param timestamp 通常是System.currentTimeMillis()
     * @return 格式为"00:00:00"的时分秒字符串
     */
    public static String getTimeStr(long timestamp) {
        return timestampToStr(timestamp, "HH:mm:ss");
    }

    /**
     * 返回当天零点时间戳.
     *
     * @param timestamp 任意时间戳
     * @return 当天零点时间戳
     */
    public static long getZeroHourTimestamp(long timestamp) {
        return timestamp - (timestamp + TimeZone.getDefault().getRawOffset()) % (24 * 60 * 60 * 1000);
    }

    /**
     * 返回当前小时对应整点的时间戳.
     *
     * @param timestamp 任意时间戳
     * @return 当前小时对应整点的时间戳.
     */
    public static long getThisHourTimestamp(long timestamp) {
        return timestamp - timestamp % (60 * 60 * 1000);
    }

    /**
     * 返回下一小时对应整点的时间戳.
     *
     * @param timestamp 任意时间戳
     * @return 下一小时对应整点的时间戳.
     */
    public static long getNextHourTimestamp(long timestamp) {
        return getThisHourTimestamp(timestamp) + 60 * 60 * 1000;
    }


    /**
     * 返回本周一日期.
     *
     * @param timestamp 通常是System.currentTimeMillis()
     * @return 格式为"2020-01-01"的本周一日期.
     */
    public static String getThisMonday(long timestamp) {
        Calendar calendar = new GregorianCalendar(Locale.CHINA);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(new Date(timestamp));
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                .format(calendar.getTime());
    }

    /**
     * 返回下周一日期.
     *
     * @param timestamp 通常是System.currentTimeMillis()
     * @return 格式为"2020-01-01"的下周一日期.
     */
    public static String getNextMonday(long timestamp) {
        return getThisMonday(timestamp + 7 * 24 * 60 * 60 * 1000);
    }

    public static final String LESS_THAN_ONE_MINUTE = "小于1分钟";

    /**
     * 将以秒 s 为单位的时间差改成字符串，可选是否展示秒.
     * 如果某部分数字为0，则不会显示该部分。比如传入60，输出为"1分"。
     *
     * @param second     要转换的时间长度，单位s
     * @param showSecond 是否展示秒
     * @return 对应的格式化时间字符串
     */
    public static String secondToStr(int second, boolean showSecond) {
        if (second < 0) {
            logWarn(new Exception("时间差" + second + "应该为正"));
            second = -second;
        }
        if (second == 0) {
            return "0秒";
        }
        if (!showSecond && second < 60) {
            return LESS_THAN_ONE_MINUTE;
        }
        String str = "";
        if (second >= 86400) {
            str = str + (second / 86400) + "天";
            second %= 86400;
        }
        if (second >= 3600) {
            str = str + (second / 3600) + "时";
            second %= 3600;
        }
        if (second >= 60) {
            str = str + (second / 60) + "分";
            second %= 60;
        }
        if (showSecond && second != 0) {
            str = str + second + "秒";
        }
        return str;
    }

    /**
     * 将以毫秒 ms 为单位的时间差改成字符串，可选是否展示秒.
     *
     * @param beginTime  时间开始，单位ms
     * @param endTime    时间截止，单位ms
     * @param showSecond 是否展示秒
     * @return 对应的格式化时间字符串
     */
    public static String milliSecondToStr(long beginTime, long endTime, boolean showSecond) {
        return secondToStr((int) (Math.abs(endTime - beginTime) / 1000), showSecond);
    }

    /**
     * 将以纳秒 ns 为单位的时间差改成字符串，可选是否展示秒.
     *
     * @param beginTime  时间开始，单位ns
     * @param endTime    时间截止，单位ns
     * @param showSecond 是否展示秒
     * @return 对应的格式化时间字符串
     */
    public static String nanoSecondToStr(long beginTime, long endTime, boolean showSecond) {
        return secondToStr((int) (Math.abs(endTime - beginTime) / 1e9), showSecond);
    }
}
