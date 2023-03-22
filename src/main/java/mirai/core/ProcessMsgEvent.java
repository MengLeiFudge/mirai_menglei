package mirai.core;

import mirai.func.admin.SetBotAdmin;
import mirai.func.control.FuncControl;
import mirai.func.control.GroupControl;
import mirai.func.donate.Donate;
import mirai.func.lolicon.LoliconImg;
import mirai.func.menu.Menu;
import mirai.func.reread.Reread;
import mirai.func.thunder.Thunder;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;

import static mirai.utils.LogUtils.logError;
import static mirai.utils.MyFileUtils.unlockFiles;
import static mirai.utils.NickUtils.saveNick;

/**
 * 处理一条消息.
 *
 * @author MengLeiFudge
 */
public class ProcessMsgEvent {
    ProcessMsgEvent() {
    }

    /**
     * 功能枚举（群之间功能状态不互通）.
     */
    public enum MyFunction {
        MENU(1, -1, "菜单", Menu.class),
        FUNC_CONTROL(1, -1, "功能开关", FuncControl.class),
        SET_BOT_ADMIN(1, -1, "设置Bot管理员", SetBotAdmin.class),
        GROUP_CONTROL(1, -1, "群管", GroupControl.class),
        DONATE(1, -1, "捐献", Donate.class),
        REREAD(3, 1, "随机复读", Reread.class),
        THUNDER(3, 2, "随机禁言", Thunder.class),
        H_PICTURE(5, 3, "Lolicon美图", LoliconImg.class),
        KEEP_KUN(5, 11, "养鲲", mirai.func.kun.Process.class),
        SAKURA_CITY(5, 12, "落樱之都", mirai.func.sakura.Process.class),
        ARC_QUERY(5, 13, "Arc查询", mirai.func.arc.query.Process.class),
        ARC_PUBG(5, 14, "Arc狼人杀", mirai.func.arc.werewolf.Process.class),
        ARC_WEREWOLF(5, 15, "Arc吃鸡", mirai.func.arc.pubg.Process.class),
        SHAPEZ(5, 16, "异形工厂", mirai.func.shapez.Process.class),
        QA(10, 4, "智能问答", mirai.func.qa.Process.class);

        private final int priority;
        private final int index;
        private final String name;
        private final Class<? extends FuncProcess> clazz;

        /**
         * 构造一个功能.
         *
         * @param priority 功能对应的优先级. 所有功能会按优先级从小到大的顺序依次执行，同优先级的功能会同时执行
         * @param index    唯一功能索引. 小于等于0时，该功能会强制开启，且不会在菜单中显示
         * @param name     功能名，定下后不可修改，否则可能导致功能开关状态错误
         * @param clazz    方法对应的主类，继承于 {@link FuncProcess}
         */
        MyFunction(int priority, int index, String name, Class<? extends FuncProcess> clazz) {
            this.priority = priority;
            this.index = index;
            this.name = name;
            this.clazz = clazz;
        }

        public int getPriority() {
            return priority;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public Class<? extends FuncProcess> getClazz() {
            return clazz;
        }

        public static MyFunction getFuncByIndex(int index) {
            for (MyFunction f : MyFunction.values()) {
                if (f.index == index) {
                    return f;
                }
            }
            return null;
        }
    }

    public static void process(MsgEvent msgEvent) {
        try {
            // 存储昵称，供排行榜等使用
            saveNick();
            MyFunction[] myFunctions = Arrays.stream(MyFunction.values())
                    .sorted(Comparator.comparingInt(o -> o.priority)).toArray(MyFunction[]::new);
            // 按优先级顺序依次执行，成功匹配词条则不执行后续优先级的功能
            for (MyFunction myFunction : myFunctions) {
                // 这里不判断功能是否启用，防止菜单xx失效
                FuncProcess process = myFunction.getClazz().
                        getConstructor(MsgEvent.class, MyFunction.class).newInstance(msgEvent, myFunction);
                if (process.matchesWords()) {
                    break;
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException |
                 InstantiationException | InvocationTargetException | RuntimeException e) {
            msgEvent.send("出错了捏，需要改代码");
            logError(e);
        } finally {
            unlockFiles(msgEvent);
        }
    }

}
