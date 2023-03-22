package mirai.func.menu;

import mirai.core.FuncProcess;
import mirai.core.MsgEvent;
import mirai.core.ProcessMsgEvent;

import java.util.ArrayList;
import java.util.Comparator;

import static mirai.utils.FuncControlUtils.getFuncStateStr;

/**
 * @author MengLeiFudge
 */
public class Menu extends FuncProcess {
    public Menu(MsgEvent msgEvent, ProcessMsgEvent.MyFunction myFunction) {
        super(msgEvent, myFunction);
    }

    @Override
    public void menu() {
    }

    @Override
    public boolean process() {
        if (msgEvent.getPlainMsg().matches("菜单")) {
            showMenu();
            return true;
        }
        return false;
    }

    private void showMenu() {
        if (msgEvent.getMsgType() == MsgEvent.MsgType.GROUP) {
            sortFuncThenSend();
        } else {
            msgEvent.send("请在群中发送【菜单】以了解Bot各功能开启情况！");
        }
    }

    private void sortFuncThenSend() {
        ArrayList<ProcessMsgEvent.MyFunction> list = new ArrayList<>();
        for (ProcessMsgEvent.MyFunction myFunction : ProcessMsgEvent.MyFunction.values()) {
            if (myFunction.getIndex() > 0) {
                list.add(myFunction);
            }
        }
        list.sort(Comparator.comparingInt(ProcessMsgEvent.MyFunction::getIndex));

        StringBuilder sb = new StringBuilder("本群功能开启情况如下：");
        for (ProcessMsgEvent.MyFunction myFunction : list) {
            sb.append("\n").append(myFunction.getIndex()).append(".").append(myFunction.getName())
                    .append("：").append(getFuncStateStr(msgEvent.getGroupId(), myFunction));
        }
        sb.append("\ntips：【菜单+功能序号】获得对应功能菜单，如【菜单21】");
        msgEvent.send(sb.toString());
    }
}
