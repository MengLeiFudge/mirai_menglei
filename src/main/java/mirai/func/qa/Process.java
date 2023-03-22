package mirai.func.qa;

import mirai.core.FuncProcess;
import mirai.core.MsgEvent;
import mirai.core.ProcessMsgEvent;

import static mirai.utils.BotAdminUtils.isBotAdmin;
import static mirai.utils.OtherUtils.strFormat;
import static mirai.utils.OtherUtils.strToRegex;

/**
 * @author MengLeiFudge
 */
public class Process extends FuncProcess {
    public Process(MsgEvent msgEvent, ProcessMsgEvent.MyFunction myFunction) {
        super(msgEvent, myFunction);
        menuList.add("问答");
        menuList.add("智能问答");
    }

    @Override
    public void menu() {
        msgEvent.send("标识 [g]群聊可用 [p]私聊可用\n" +
                "说明 [a]仅Bot管理员可用\n" +
                "[gp]问题列表：查看所有问题及对应序号\n" +
                "[gp]/问+问题+/答+回答：设置问题，栗子【/问你好/答你好啊】\n" +
                "[gp]查/查看+问题序号：查看该问题的所有回答及对应序号\n" +
                "[gp(a)]删+问题序号+回答序号：删除问题，只能删自己设置的；管理员可以删除任意问答\n" +
                "[a][gp]删+问题序号：删除问题以及该问题所有回答"
        );
    }

    @Override
    public boolean process() {
        if (true) {
            return false;
        }

        // 排除纯图片消息
        if (msgEvent.getPlainMsg().matches("问题列表")) {
            //getAllQ();
        } else if (msgEvent.getPlainMsg().matches("/问.+/答.+")) {
            int index = msgEvent.getPlainMsg().indexOf("/答");
            String q = strFormat(msgEvent.getPlainMsg().substring(2, index)).trim();
            String a = msgEvent.getPlainMsg().substring(index + 2).trim();
            //setQA(q, a);
        } else if (msgEvent.getPlainMsg().matches("(查|查看) *[1-9][0-9]*")) {
            int qNum = Integer.parseInt(msgEvent.getPlainMsg().split("\\D+")[1]);
            //getA(qNum);
        } else if (msgEvent.getPlainMsg().matches("(删|删除) *[1-9][0-9]* *[1-9][0-9]*")) {
            String[] data = msgEvent.getPlainMsg().split("\\D+");
            int qNum = Integer.parseInt(data[1]);
            int aNum = Integer.parseInt(data[2]);
            //delA(qNum, aNum);
        } else if (msgEvent.getPlainMsg().matches("(删|删除) *[1-9][0-9]*")) {
            if (!isBotAdmin(msgEvent.getSenderId())) {
                msgEvent.send(msgEvent.getSenderId(), "你不是我的管理员，没有权限删除整个问题哦！\n" +
                        "指令提示：【删+问题序号+回答序号】");
            }
            int qNum = Integer.parseInt(msgEvent.getPlainMsg().split("\\D+")[1]);
            //delQ(qNum);
        } else if (msgEvent.getMsgType() == MsgEvent.MsgType.GROUP) {
            if (!msgEvent.getPlainMsg().contains("@")) {
                // 没有艾特则只在本地问答查找
                //localQA(strFormat(textMsg));
            } else if (msgEvent.getAtId() == msgEvent.getBotId()) {
                // 艾特棉花糖，则去掉艾特后先本地问答查找，没有时使用api
                String regex = " *@" + strToRegex(msgEvent.getAtNick()) + " *";
                String q = strFormat(msgEvent.getPlainMsg().replaceAll(regex, ""));
                //if (!localQA(q)) {
                //smartQA(q);
                //}
            }
        } else {
            // 先寻找本地问答，没有再找网上问答
            //if ("".equals(textMsg) && !localQA(strFormat(textMsg))) {
            //smartQA(textMsg);
            //}
        }
        return false;
    }


    /**
     * 显示当前所有问题及其序号.
     *//*
    public void getAllQ() {
        QaList dataBase = getQaList();
        List<Process> list = dataBase.getSortedList();
        if (list.isEmpty()) {
            msgEvent.send("当前本地问答数据库为空！");
            return;
        }
        StringBuilder sb = new StringBuilder("问题列表如下：\n");
        for (int i = 0; i < list.size(); i++) {
            Process base = list.get(i);
            if (i % 10 != 0) {
                sb.append("\n");
            }
            sb.append(i + 1).append(".").append(base.getQuestion());
            if (i % 10 == 9) {
                msgEvent.send(sb.toString());
                sb = new StringBuilder();
            }
        }
        String s = sb.toString();
        if (!s.equals("")) {
            msgEvent.send(s);
        }
    }*/

    /**
     * 设置一个问答.
     *
     * @param question
     * @param answer
     *//*
    public void setQA(String question, String answer) {
        QaList dataBase = getQaList();
        dataBase.add(question, answer, qq);
        save(dataBase);
        msgEvent.send(msgEvent.getSenderId(), "已添加该问答！");
    }

    public void getA(int qNum) {
        qNum--;
        QaList dataBase = getQaList();
        List<Process> list = dataBase.getSortedList();
        if (list.isEmpty()) {
            msgEvent.send("当前本地问答数据库为空！");
            return;
        }
        if (qNum >= list.size()) {
            msgEvent.send(msgEvent.getSenderId(), "没有找到该问题呢QAQ");
            return;
        }
        Process base = list.get(qNum);
        StringBuilder sb = new StringBuilder("问题：" + base.getQuestion() + "\n回答：\n");
        List<String> answers = base.getAnswerList();
        for (int i = 0; i < answers.size(); i++) {
            if (i % 10 != 0) {
                sb.append("\n");
            }
            sb.append(i + 1).append(".").append(answers.get(i));
            if (i % 10 == 9) {
                msgEvent.send(sb.toString());
                sb = new StringBuilder();
            }
        }
        String s = sb.toString();
        if (!s.equals("")) {
            msgEvent.send(s);
        }
    }

    public void delA(int qNum, int aNum) {
        qNum--;
        aNum--;
        QaList dataBase = getQaList();
        List<Process> list = dataBase.getSortedList();
        if (list.isEmpty()) {
            msgEvent.send("当前本地问答数据库为空！");
            return;
        }
        if (qNum >= list.size()) {
            msgEvent.send(msgEvent.getSenderId(), "没有找到该问题呢QAQ");
            return;
        }
        Process base = list.get(qNum);
        if (aNum == 1 && base.getAnswerList().size() == 1) {
            dataBase.remove(qNum);
            return;
        }
        if (base.remove(aNum, qq)) {
            dataBase.set(qNum, base);
            msgEvent.send(msgEvent.getSenderId(), "已删除该问题的对应回答！");
            save(dataBase);
        } else {
            msgEvent.send(msgEvent.getSenderId(), "没有找到该问题的对应答复呢QAQ");
        }
    }

    public void delQ(int qNum) {
        qNum--;
        QaList dataBase = getQaList();
        List<Process> list = dataBase.getSortedList();
        if (list.isEmpty()) {
            msgEvent.send("当前本地问答数据库为空！");
            return;
        }
        if (qNum >= list.size()) {
            msgEvent.send(msgEvent.getSenderId(), "没有找到该问题呢QAQ");
            return;
        }
        dataBase.remove(qNum);
        msgEvent.send(msgEvent.getSenderId(), "已删除该问题及其所有回答！");
    }*/


    /**
     * 本地读取问答并回复.
     * 如果有该问答，随机输出一个回答，返回 true;
     * 否则返回 false.
     *
     * @param question
     * @return
     *//*
    public boolean localQA(String question) {
        QaList dataBase = getQaList();
        List<Process> list = dataBase.getSortedList();
        for (Process base : list) {
            if (base.getQuestion().matches(question)) {
                msgEvent.send(base.getRandomAnswer());
                return true;
            }
        }
        return false;
    }

    public void smartQA(String question) {
        if (question.equals("")) {
            msgEvent.send(msgEvent.getSenderId(), "找我有什么事吗？QwQ");
            return;
        }
    }*/
}
