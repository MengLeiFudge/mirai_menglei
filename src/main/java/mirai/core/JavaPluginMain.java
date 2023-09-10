package mirai.core;

import mirai.utils.RandomUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.event.events.StrangerMessageEvent;

import static mirai.utils.LogUtils.logger;
import static mirai.utils.OtherUtils.sleep;

/**
 * settings.gradle.kts  生成的插件.jar名称
 * build.gradle.kts     依赖库和插件版本
 * JavaPluginMain()     插件名称，id和版本
 * 使用 test.kotlin.RunMirai.kt 可以在ide里运行，不用复制到 mirai console loader 或其他启动器
 *
 * @author MengLeiFudge
 */
public final class JavaPluginMain extends JavaPlugin {
    public static final JavaPluginMain INSTANCE = new JavaPluginMain();

    private JavaPluginMain() {
        //FixProtocolVersion.fetch(BotConfiguration.MiraiProtocol.ANDROID_PAD, "latest");
        super(new JvmPluginDescriptionBuilder("org.menglei.mirai-mengleibot", "0.1.0")
                .info("EG")
                .build());
    }

    @Override
    public void onEnable() {
        System.setProperty("https.protocols", "TLSv1.2");
        logger = getLogger();
        //GlobalEventChannel 会监听到来自所有 Bot 的事件，如果只希望监听某一个 Bot 的事件，请使用 bot.eventChannel。
        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(this);
        //自动同意好友申请
        eventChannel.subscribeAlways(NewFriendRequestEvent.class,
                NewFriendRequestEvent::accept);
        //自动同意加群申请
        eventChannel.subscribeAlways(BotInvitedJoinGroupRequestEvent.class,
                BotInvitedJoinGroupRequestEvent::accept);
        //监听群消息
        eventChannel.subscribeAlways(GroupMessageEvent.class, event -> {
            ProcessMsgEvent.process(new MsgEvent(event));
        });
        //监听群临时会话消息
        eventChannel.subscribeAlways(GroupTempMessageEvent.class, event -> {
            ProcessMsgEvent.process(new MsgEvent(event));
        });
        //监听好友消息
        eventChannel.subscribeAlways(FriendMessageEvent.class, event -> {
            ProcessMsgEvent.process(new MsgEvent(event));
        });
        //监听陌生人消息
        eventChannel.subscribeAlways(StrangerMessageEvent.class, event -> {
            ProcessMsgEvent.process(new MsgEvent(event));
        });
        //监听戳一戳消息
        eventChannel.subscribeAlways(NudgeEvent.class, event -> {
            Bot bot = Bot.getInstances().get(0);
            if (event.getFrom().getId() == bot.getId()) {
                return;
            }
            int randInt = RandomUtils.getRandomInt(0, 2);
            if (event.getTarget().getId() == bot.getId()) {
                //有人戳了bot，戳回去
                if (event.getFrom().nudge().sendTo(event.getSubject())) {
                    event.getSubject().sendMessage("谁让你戳我的？我戳！");
                    if (randInt >= 1) {
                        sleep(1000);
                        event.getFrom().nudge().sendTo(event.getSubject());
                        event.getSubject().sendMessage("我再戳！");
                        if (randInt >= 2) {
                            sleep(1000);
                            event.getFrom().nudge().sendTo(event.getSubject());
                            event.getSubject().sendMessage("我还戳！");
                        }
                    }
                } else {
                    //对方禁用戳一戳，或当前bot戳一戳次数达到上限
                    String s;
                    switch (randInt) {
                        case 0:
                            s = "谁让你戳我的？我戳……戳不动惹";
                            break;
                        case 1:
                            s = "莫欺少年穷，等我日后把你戳爆！";
                            break;
                        default:
                            s = "痛痛痛……不要再戳我惹";
                    }
                    event.getSubject().sendMessage(s);
                }
            } else {
                //有人戳了别人，跟着戳一戳
                if (randInt == 0) {
                    event.getTarget().nudge().sendTo(event.getSubject());
                }
            }
        });
    }
}
