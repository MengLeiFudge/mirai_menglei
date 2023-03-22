package mirai.utils;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import static mirai.utils.LogUtils.logError;
import static mirai.utils.LogUtils.logWarn;

public class RandomUtils {
    private RandomUtils() {
    }

    private static final Random RANDOM = new Random();

    /**
     * 返回一个均匀分布的随机整数（包括上下限）.
     *
     * @param min 下限
     * @param max 上限
     * @return 随机整数
     */
    public static int getRandomInt(int min, int max) {
        if (min > max) {
            logWarn(new Exception("随机数上下限颠倒：min " + min + ", max " + max));
            min ^= max;
            max ^= min;
            min ^= max;
        }
        // random.nextInt(a)随机生成[0,a)的随机数
        return RANDOM.nextInt(max - min + 1) + min;
    }

    /**
     * 返回一个均匀分布的随机小数（包括上限，不包括下限）.
     *
     * @param min 下限
     * @param max 上限
     * @return 随机小数
     */
    public static double getRandomDouble(double min, double max) {
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        // random.nextDouble()随机生成[0,1)的随机数
        return RANDOM.nextDouble() * (max - min) + min;
    }

    public static final double TWENTY_PERCENT = 0.84;
    public static final double TEN_PERCENT = 1.28;
    public static final double FIVE_PERCENT = 1.64;
    public static final double THREE_PERCENT = 1.88;
    public static final double TWO_PERCENT = 2.05;
    public static final double ONE_PERCENT = 2.32;
    public static final double FIVE_PER_THOUSAND = 2.57;
    public static final double THREE_PER_THOUSAND = 2.75;
    public static final double TWO_PER_THOUSAND = 2.88;
    public static final double ONE_PER_THOUSAND = 3.08;

    public static double getDistributionDouble(double x, double miu, double sigma) {
        return 1.0 / (Math.sqrt(2.0 * Math.PI) * sigma) *
                Math.pow(Math.E, -Math.pow(x - miu, 2.0) / (2.0 * Math.pow(sigma, 2.0)));
    }

    public static double getNormalDistributionDouble(double x) {
        return getDistributionDouble(x, 0, 1);
    }

    /**
     * 返回一个正态分布的随机小数（包括上下限）.
     *
     * @param min      下限
     * @param max      上限
     * @param coverage 以标准正态分布表为准，确定正态分布有效范围
     *                 传入Mx.ONE_PERCENT等数值，或者自定义数值（必须为正）
     *                 比如，查表得2.32为0.9898，2.33为0.9901，
     *                 则传入2.32代表取得上限概率约为1%，取得下限概率约为1%
     * @return 随机小数
     */
    public static double getRandomDistributionDouble(double min, double max, double coverage) {
        if (coverage <= 0) {
            return (max + min) / 2;
        }
        double a = RANDOM.nextGaussian();
        if (a > coverage) {
            a = coverage;
        } else if (a < -coverage) {
            a = -coverage;
        }
        a = a / (coverage * 2) + 0.5;// [0,1]
        return (max - min) * a + min;
    }

    public static double getRandomDistributionDouble(double min, double max) {
        return getRandomDistributionDouble(min, max, ONE_PERCENT);
    }

    /**
     * 返回一个正态分布的随机整数（包括上下限）.
     * 利用正态分布小数，扩充范围至[min,max+1)
     *
     * @param min      下限
     * @param max      上限
     * @param coverage 以标准正态分布表为准，确定正态分布有效范围
     *                 传入Mx.ONE_PERCENT等数值，或者自定义数值（必须为正）
     *                 比如，查表得2.32为0.9898，2.33为0.9901，
     *                 则传入2.32代表取得上限概率约为1%，取得下限概率约为1%
     * @return 随机小数
     */
    public static int getRandomDistributionInt(int min, int max, double coverage) {
        double num = getRandomDistributionDouble(min, max + 1.0, coverage);
        return num >= max + 1.0 ? max : (int) num;
    }

    public static int getRandomDistributionInt(int min, int max) {
        return getRandomDistributionInt(min, max, ONE_PERCENT);
    }

    /**
     * 返回一个随机汉字.
     *
     * @return 随机中文字符
     */
    public static char getRandomChineseChar() {
        String str = "";
        byte[] b = new byte[2];
        b[0] = (byte) (176 + Math.abs(RANDOM.nextInt(39)));// 高位
        b[1] = (byte) (161 + Math.abs(RANDOM.nextInt(93)));// 低位
        try {
            str = new String(b, "GBK");
        } catch (UnsupportedEncodingException e) {
            logError(e);
        }
        return str.charAt(0);
    }

    /**
     * 返回指定长度随机中文字符串.
     *
     * @return 指定长度随机中文字符串
     */
    public static String getRandomChineseStr(int len) {
        if (len <= 0) {
            logError(new Exception("错误的长度：" + len));
            return "";
        }
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < len; i++) {
            name.append(getRandomChineseChar());
        }
        return name.toString();
    }

    /**
     * 返回随机长度随机中文字符串.
     *
     * @return 指定长度随机中文字符串
     */
    public static String getRandomChineseStr(int lenMin, int lenMax) {
        if (lenMin <= 0) {
            logError(new Exception("错误的长度：" + lenMin));
            return "";
        }
        if (lenMax <= 0) {
            logError(new Exception("错误的长度：" + lenMax));
            return "";
        }
        return getRandomChineseStr(getRandomInt(lenMin, lenMax));
    }

}
