package mirai.func.control;

import mirai.core.FuncProcess;
import mirai.core.MsgEvent;
import mirai.core.ProcessMsgEvent;

import static mirai.utils.BotAdminUtils.isBotAdmin;
import static mirai.utils.FuncControlUtils.setFuncState;

/**
 * @author MengLeiFudge
 */
public class FuncControl extends FuncProcess {
    public FuncControl(MsgEvent msgEvent, ProcessMsgEvent.MyFunction myFunction) {
        super(msgEvent, myFunction);
    }

    @Override
    public void menu() {
    }

    @Override
    public boolean process() {
        if (msgEvent.getPlainMsg().matches("(开启|关闭)(功能)?[0-9]+")) {
            int funcIndex = Integer.parseInt(msgEvent.getPlainMsg().split("\\D+")[1]);
            changeFuncState(funcIndex, msgEvent.getPlainMsg().startsWith("开"));
            return true;
        }
        return false;
    }

    private void changeFuncState(int funcIndex, boolean open) {
        if (msgEvent.getMsgType() == MsgEvent.MsgType.GROUP) {
            if (!isBotAdmin(msgEvent.getSenderId())) {
                msgEvent.send("只有Bot管理员才能开关功能哦！");
                return;
            }
            for (ProcessMsgEvent.MyFunction myFunction : ProcessMsgEvent.MyFunction.values()) {
                if (myFunction.getIndex() == funcIndex) {
                    setFuncState(msgEvent.getGroupId(), myFunction, open);
                    msgEvent.send((open ? "已开启" : "已关闭") + myFunction.getName() + "！");
                    return;
                }
            }
        } else {
            msgEvent.send("只能在群内开关功能哦！");
        }
    }
}
