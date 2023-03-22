package mirai.func.reread;

import mirai.core.FuncProcess;
import mirai.core.MsgEvent;
import mirai.core.ProcessMsgEvent;
import net.mamoe.mirai.message.data.FileMessage;
import net.mamoe.mirai.message.data.SingleMessage;

import java.io.File;
import java.text.DecimalFormat;

import static mirai.utils.BotAdminUtils.isBotAdmin;
import static mirai.utils.MyFileUtils.DEF_DOUBLE;
import static mirai.utils.MyFileUtils.Dir;
import static mirai.utils.MyFileUtils.getDouble;
import static mirai.utils.MyFileUtils.getFile;
import static mirai.utils.MyFileUtils.setKeyValue;
import static mirai.utils.RandomUtils.getRandomDouble;

/**
 * @author MengLeiFudge
 */
public class Reread extends FuncProcess {
    public Reread(MsgEvent msgEvent, ProcessMsgEvent.MyFunction myFunction) {
        super(msgEvent, myFunction);
        double d = getDouble(REREAD_FILE, msgEvent.getGroupId());
        if (d == DEF_DOUBLE) {
            save();
        } else {
            chance = d;
        }
        menuList.add("复读");
    }

    @Override
    public void menu() {
        msgEvent.send("[g]群聊 [p]私聊 [a]仅管理员\n" +
                "[a][pg]设置复读(概率)+概率(+%)：设置复读概率为指定值的一百倍\n" +
                "栗子：【设置复读2.5】表示本群复读概率将设为2.5%\n" +
                "注意，概率最低为0.01%，最高为50%\n" +
                "复读时，有一半概率倒序复读\n" +
                "本群当前复读概率：" + chance2Str(chance)
        );
    }

    private static final File REREAD_FILE = getFile(Dir.SETTINGS, "reread.txt");

    /**
     * 复读概率，默认值 5%.
     */
    private double chance = 0.05;

    @Override
    public boolean process() {
        // 必须先判断指令是否为修改复读概率，再判断是否执行复读操作
        // 否则复读概率过高将导致修改指令无法执行
        if (isBotAdmin(msgEvent.getSenderId())) {
            if (msgEvent.getPlainMsg().matches("设置复读(概率)? *[0-9]+(\\.[0-9]+)?%?")) {
                String[] data = msgEvent.getPlainMsg().split("[^0-9.]+");
                chance = Math.min(50, Math.max(Double.parseDouble(data[1]), 0.01)) / 100;
                save();
                msgEvent.send("已将本群复读概率设为 " + chance2Str(chance) + "！");
                return true;
            }
        }
        // 超出指定复读概率，则不复读
        if (getRandomDouble(0, 1) > chance) {
            return false;
        }
        // 只有纯文本消息才会复读
        boolean isPlainMsg = true;
        for (SingleMessage msg : msgEvent.getMsg()) {
            String s = msg.contentToString();
            isPlainMsg &= !"".equals(s) && !s.startsWith("[") && !s.endsWith("]");
            // 不复读文件消息
            if (msg instanceof FileMessage) {
                return false;
            }
        }
        if (isPlainMsg) {
            if (getRandomDouble(0, 1) < 0.5) {
                // 文字正序输出
                msgEvent.send(msgEvent.getMsg());
            } else {
                // 文字倒序输出
                char[] strChar = msgEvent.getPlainMsg().toCharArray();
                for (int i = 0; i < strChar.length / 2; i++) {
                    char c = strChar[i];
                    strChar[i] = strChar[strChar.length - 1 - i];
                    strChar[strChar.length - 1 - i] = c;
                }
                msgEvent.send(new String(strChar));
            }
        } else {
            msgEvent.send(msgEvent.getMsg());
        }
        return true;
    }

    private String chance2Str(double chance) {
        return new DecimalFormat("0.000%").format(chance);
    }

    private void save() {
        setKeyValue(REREAD_FILE, msgEvent.getGroupId(), chance);
    }
}
