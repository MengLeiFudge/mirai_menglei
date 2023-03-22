package mirai.func.thunder;

import mirai.core.FuncProcess;
import mirai.core.MsgEvent;
import mirai.core.ProcessMsgEvent;

import java.io.File;
import java.text.DecimalFormat;

import static mirai.utils.BotAdminUtils.isBotAdmin;
import static mirai.utils.MyFileUtils.DEF_STRING;
import static mirai.utils.MyFileUtils.Dir;
import static mirai.utils.MyFileUtils.getFile;
import static mirai.utils.MyFileUtils.getString;
import static mirai.utils.MyFileUtils.setKeyValue;
import static mirai.utils.RandomUtils.getRandomDouble;
import static mirai.utils.RandomUtils.getRandomInt;
import static mirai.utils.TalkUtils.notAllowTalking;

/**
 * @author MengLeiFudge
 */
public class Thunder extends FuncProcess {
    public Thunder(MsgEvent msgEvent, ProcessMsgEvent.MyFunction myFunction) {
        super(msgEvent, myFunction);
        String s = getString(THUNDER_FILE, msgEvent.getGroupId());
        if (DEF_STRING.equals(s)) {
            // 默认概率：5%，默认禁言时间5-20s
            chance = 0.05;
            minSecond = 5;
            maxSecond = 20;
            save();
        } else {
            String[] data = s.split("-");
            chance = Double.parseDouble(data[0]);
            minSecond = Integer.parseInt(data[1]);
            maxSecond = Integer.parseInt(data[2]);
        }
        menuList.add("随机禁言");
    }

    @Override
    public void menu() {
        msgEvent.send("[g]群聊 [p]私聊 [a]仅管理员\n" +
                "[a][pg]设置(随机)禁言概率+概率(+%)：设置随机禁言概率为指定值的一百倍\n" +
                "栗子：【设置禁言概率2.5】表示本群随机禁言概率将设为2.5%\n" +
                "注意，概率最低为0.01%，最高为50%\n" +
                "本群当前随机禁言概率：" + chance2Str(chance) + "\n" +
                "[a][pg]设置(随机)禁言时间+时间下限+时间上限：设置随机禁言时间为指定范围，单位为秒\n" +
                "栗子：【设置禁言时间5 20】表示本群随机禁言时间将设为5s-20s\n" +
                "注意，时间最低为1s，最高为30s\n" +
                "本群当前随机禁言时间：" + minSecond + "s - " + maxSecond + "s"
        );
    }

    private static final File THUNDER_FILE = getFile(Dir.SETTINGS, "thunder.txt");
    private static double chance;
    private static int minSecond;
    private static int maxSecond;

    @Override
    public boolean process() {
        if (msgEvent.getMsgType() != MsgEvent.MsgType.GROUP) {
            return false;
        }
        if (isBotAdmin(msgEvent.getSenderId())) {
            if (msgEvent.getPlainMsg().matches("设置(随机)?禁言概率 *[0-9]+(\\.[0-9]+)?%?")) {
                String[] data = msgEvent.getPlainMsg().split("[^0-9.]+");
                chance = Math.min(50, Math.max(Double.parseDouble(data[1]), 0.01)) / 100;
                save();
                msgEvent.send("已将本群随机禁言概率设为 " + chance2Str(chance) + "！");
                return true;
            } else if (msgEvent.getPlainMsg().matches("设置(随机)?禁言时间 *[0-9]+ +[0-9]+")) {
                String[] data = msgEvent.getPlainMsg().split("\\D+");
                minSecond = Math.min(30, Math.max(Integer.parseInt(data[1]), 1));
                maxSecond = Math.min(30, Math.max(Integer.parseInt(data[2]), 1));
                if (minSecond > maxSecond) {
                    minSecond ^= maxSecond;
                    maxSecond ^= minSecond;
                    minSecond ^= maxSecond;
                }
                save();
                msgEvent.send("已将本群随机禁言时间设为 " + minSecond + "s - " + maxSecond + "s！");
                return true;
            }
        }
        // 必须先判断指令是否为修改复读概率，再判断是否执行复读操作
        // 否则复读概率过高将导致修改指令无法执行
        if (getRandomDouble(0, 1) < chance) {
            int second = getRandomInt(minSecond, maxSecond);
            notAllowTalking(msgEvent.getGroupId(), msgEvent.getSenderId(), second);
            //msgEvent.changeAllSourceToPrivate(qq);
            msgEvent.send(msgEvent.getSenderId(), "你被棉花糖的闪电击中，禁言" + second + "s！");
            return true;
        }
        return false;
    }

    private String chance2Str(double chance) {
        return new DecimalFormat("#0.000%").format(chance);
    }

    private void save() {
        setKeyValue(THUNDER_FILE, msgEvent.getGroupId(), chance + "-" + minSecond + "-" + maxSecond);
    }
}
