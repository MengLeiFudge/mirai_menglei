package mirai.core;

/**
 * 被抛出以指示方法运行到了意料之外的步骤，此时应调整代码.
 *
 * @author MengLeiFudge
 */
public class UnexpectedStateException extends RuntimeException {
    /**
     * 用指定的详细信息构建一个 <code>UnexpectedStateException</code>.
     *
     * @param s 详细信息
     */
    public UnexpectedStateException(String s) {
        super(s);
    }
}
