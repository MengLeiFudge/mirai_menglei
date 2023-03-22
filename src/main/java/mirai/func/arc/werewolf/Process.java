package mirai.func.arc.werewolf;

import mirai.core.FuncProcess;
import mirai.core.MsgEvent;
import mirai.core.ProcessMsgEvent;

/**
 * @author MengLeiFudge
 */
public class Process extends FuncProcess {
    public Process(MsgEvent msgEvent, ProcessMsgEvent.MyFunction myFunction) {
        super(msgEvent, myFunction);
        menuList.add("狼人杀");
        menuList.add("arc狼人杀");
        menuList.add("arcaea狼人杀");
    }

    @Override
    public void menu() {
        msgEvent.send("标识 [g]群聊可用 [p]私聊可用\n" +
                "说明 [a]仅Bot管理员可用\n" +
                "[g]加入狼人杀：加入arc狼人杀对局\n" +
                "[g]其他指令待定。"
        );
    }

    @Override
    public boolean process() {
        if (msgEvent.getPlainMsg().matches("加入狼人杀")) {
            msgEvent.send(msgEvent.getSenderId(), "加入成功");
            return true;
        } else if (msgEvent.getPlainMsg().matches("test")) {
            Player player = WerewolfUtils.getUser(msgEvent.getSenderId(), msgEvent);
            if (player == null) {
                player = new Player(msgEvent.getSenderId());
                WerewolfUtils.save(player);
            }
            msgEvent.send(msgEvent.getSenderId(), "get player ok, begin sleep");
            sleep(3000);
            WerewolfUtils.save(player);
            msgEvent.send(msgEvent.getSenderId(), "save player ok, end sleep");
            return true;
        }
        return false;
    }
}
