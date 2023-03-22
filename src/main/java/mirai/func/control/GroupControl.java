package mirai.func.control;

import mirai.core.FuncProcess;
import mirai.core.MsgEvent;
import mirai.core.ProcessMsgEvent;

import static mirai.utils.BotAdminUtils.isBotAdmin;
import static mirai.utils.TalkUtils.allowGroupTalking;
import static mirai.utils.TalkUtils.allowTalking;
import static mirai.utils.TalkUtils.notAllowGroupTalking;
import static mirai.utils.TalkUtils.notAllowTalking;
import static mirai.utils.TalkUtils.removeMember;

/**
 * @author MengLeiFudge
 */
public class GroupControl extends FuncProcess {
    public GroupControl(MsgEvent msgEvent, ProcessMsgEvent.MyFunction myFunction) {
        super(msgEvent, myFunction);
    }

    @Override
    public void menu() {
    }

    @Override
    public boolean process() {
        if (!isBotAdmin(msgEvent.getSenderId()) || msgEvent.getMsgType() != MsgEvent.MsgType.GROUP) {
            return false;
        }
        if (msgEvent.getPlainMsg().matches("(?i)((禁|禁言)?[1-9][0-9]*[smh]?@.*)")) {
            int time = Integer.parseInt(msgEvent.getPlainMsg().split("\\D+")[1]);
            if (msgEvent.getPlainMsg().contains("m@")) {
                notAllowTalking(msgEvent.getGroupId(), msgEvent.getAtId(), time * 60);
            } else if (msgEvent.getPlainMsg().contains("h@")) {
                notAllowTalking(msgEvent.getGroupId(), msgEvent.getAtId(), time * 3600);
            } else {
                notAllowTalking(msgEvent.getGroupId(), msgEvent.getAtId(), time);
            }
            return true;
        } else if (msgEvent.getPlainMsg().matches("(解|解禁)@.*")) {
            allowTalking(msgEvent.getGroupId(), msgEvent.getAtId());
            return true;
        } else if (msgEvent.getPlainMsg().matches("群禁|群禁言")) {
            notAllowGroupTalking(msgEvent.getGroupId());
            return true;
        } else if (msgEvent.getPlainMsg().matches("解禁|群解禁")) {
            allowGroupTalking(msgEvent.getGroupId());
            return true;
        } else if (msgEvent.getPlainMsg().matches("(踢|踢出)@+")) {
            removeMember(msgEvent.getGroupId(), msgEvent.getAtId());
        }
        return false;
    }
}
