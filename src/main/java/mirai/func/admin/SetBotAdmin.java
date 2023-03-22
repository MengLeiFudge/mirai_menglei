package mirai.func.admin;

import mirai.core.FuncProcess;
import mirai.core.MsgEvent;
import mirai.core.ProcessMsgEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.List;

import static mirai.utils.BotAdminUtils.setAdmin;
import static mirai.utils.Settings.AUTHOR_QQ;

/**
 * @author MengLeiFudge
 */
public class SetBotAdmin extends FuncProcess {
    public SetBotAdmin(MsgEvent msgEvent, ProcessMsgEvent.MyFunction myFunction) {
        super(msgEvent, myFunction);
    }

    @Override
    public void menu() {
    }

    @Override
    public boolean process() {
        if (msgEvent.getSenderId() == AUTHOR_QQ && msgEvent.getAtNum() != 0) {
            if (msgEvent.getPlainMsg().matches("(增加|设置|设|加)(管理|管理员)@.+")) {
                addBotAdmin();
                return true;
            } else if (msgEvent.getPlainMsg().matches("(删除|取消|删)(管理|管理员)@.+")) {
                removeBotAdmin();
                return true;
            }
        }
        return false;
    }

    private void addBotAdmin() {
        List<Long> atIds = msgEvent.getAtIds();
        MessageChainBuilder builder = new MessageChainBuilder();
        for (int i = 0; i < msgEvent.getAtNum(); i++) {
            setAdmin(atIds.get(i), true);
            builder.append(i == 0 ? "已将 " : "、").append(new At(atIds.get(i)));
        }
        builder.append(" 设为Bot管理！");
        msgEvent.send(builder.build());
    }

    private void removeBotAdmin() {
        List<Long> atIds = msgEvent.getAtIds();
        MessageChainBuilder builder = new MessageChainBuilder();
        for (int i = 0; i < msgEvent.getAtNum(); i++) {
            setAdmin(atIds.get(i), false);
            builder.append(i == 0 ? "已取消 " : "、").append(new At(atIds.get(i)));
        }
        builder.append(" 的Bot管理权限！");
        msgEvent.send(builder.build());
    }
}
