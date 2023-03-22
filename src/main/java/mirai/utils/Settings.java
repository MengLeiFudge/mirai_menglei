package mirai.utils;

public class Settings {
    private Settings() {
    }

    /**
     * 作者昵称.
     * <p>
     * 作者账户是唯一的，具有最高权限，可执行增删管理员、开关 Log 等操作.
     */
    public static final String AUTHOR_NAME = "萌泪";
    /**
     * 作者 QQ.
     * <p>
     * 作者账户是唯一的，具有最高权限，可执行增删管理员、开关 Log 等操作.
     */
    public static final long AUTHOR_QQ = 605738729L;
    /**
     * Bot 测试群.
     * <p>
     * 有些特殊功能仅在该群生效，如创建初始化文件等.
     */
    public static final long TEST_GROUP = 516286670L;

    /**
     * 是否显示调试信息.
     */
    static final boolean DEBUG_MODE = false;

    /**
     * JVM 可用的最大 CPU 数量.
     */
    public static final int THREAD_NUM = Runtime.getRuntime().availableProcessors();
}
