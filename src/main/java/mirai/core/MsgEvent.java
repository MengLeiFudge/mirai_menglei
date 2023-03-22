package mirai.core;

import lombok.Data;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;
import net.mamoe.mirai.event.events.StrangerMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static jdk.internal.net.http.common.Log.logError;
import static mirai.utils.LogUtils.logInfo;
import static mirai.utils.WebUtils.download;

/**
 * 自定义消息事件类，所有消息事件均需要从此处重新包装.
 *
 * @author MengLeiFudge
 */
@Data
public class MsgEvent {
    private Bot bot;
    private MessageChain msg;
    private Group group = null;
    private Member member = null;
    private Friend friend = null;
    private Stranger stranger = null;
    private long senderId;
    private String senderNick;
    private int time;
    private MsgType msgType;

    public enum MsgType {
        GROUP,
        GROUP_TEMP,
        FRIEND,
        STRANGER,
    }

    public MsgEvent(GroupMessageEvent event) {
        this.msgType = MsgType.GROUP;
        this.bot = event.getBot();
        this.msg = event.getMessage();
        this.group = event.getGroup();
        this.member = event.getSender();
        this.senderId = event.getSender().getId();
        this.senderNick = event.getSenderName();
        this.time = event.getTime();
    }

    public MsgEvent(GroupTempMessageEvent event) {
        this.msgType = MsgType.GROUP_TEMP;
        this.bot = event.getBot();
        this.msg = event.getMessage();
        this.group = event.getGroup();
        this.member = event.getSender();
        this.senderId = event.getSender().getId();
        this.senderNick = event.getSenderName();
        this.time = event.getTime();
    }

    public MsgEvent(FriendMessageEvent event) {
        this.msgType = MsgType.FRIEND;
        this.bot = event.getBot();
        this.msg = event.getMessage();
        this.friend = event.getSender();
        this.senderId = event.getSender().getId();
        this.senderNick = event.getSenderName();
        this.time = event.getTime();
    }

    public MsgEvent(StrangerMessageEvent event) {
        this.msgType = MsgType.STRANGER;
        this.bot = event.getBot();
        this.msg = event.getMessage();
        this.stranger = event.getSender();
        this.senderId = event.getSender().getId();
        this.senderNick = event.getSenderName();
        this.time = event.getTime();
    }

    /**
     * 只显示消息中的纯文本内容.
     * <p>
     * 其余内容会用纯文本代替，如图片会显示为“[图片]”
     *
     * @return 消息中的纯文本内容
     */
    public String getPlainMsg() {
        return msg.contentToString();
    }

    /**
     * 经过mirai处理后的消息内容.
     *
     * @return mirai处理后的消息内容
     */
    public String getMiraiMsg() {
        return msg.serializeToMiraiCode();
    }

    public long getGroupId() {
        return group == null ? 0L : group.getId();
    }

    /**
     * 向消息来源发送一个复杂消息.
     *
     * @param chain 要发送的消息
     */
    public void send(MessageChain chain) {
        switch (msgType) {
            case GROUP:
                if (changeToPrivate) {
                    member.sendMessage(chain);
                } else {
                    group.sendMessage(chain);
                }
                break;
            case GROUP_TEMP:
                member.sendMessage(chain);
                break;
            case FRIEND:
                friend.sendMessage(chain);
                break;
            case STRANGER:
                stranger.sendMessage(chain);
                break;
        }
    }

    /**
     * 向消息来源发送一个简单消息.
     *
     * @param s 要发送的消息
     */
    public void send(String s) {
        MessageChain chain = MessageUtils.newChain(new PlainText(s));
        send(chain);
    }

    /**
     * 向消息来源发送一个带有艾特的简单消息.
     *
     * @param atId 被艾特的对象
     * @param s    要发送的消息
     */
    public void send(long atId, String s) {
        if (msgType == MsgType.GROUP) {
            MessageChain chain = MessageUtils.newChain(new At(atId), new PlainText("\n" + s));
            send(chain);
        } else {
            send(s);
        }
    }

    /**
     * 上传一张本地图片以供后续发送.
     *
     * @param imgFile
     * @return
     */
    public Image uploadImage(File imgFile) {
        logInfo("开始上传图片: " + imgFile);
        Image image = null;
        try (ExternalResource resource = ExternalResource.create(imgFile).toAutoCloseable()) {
            switch (msgType) {
                case GROUP:
                    if (changeToPrivate) {
                        //.files.uploadNewFile("/foo/test.txt", resource); // 或者用来上传文件
                        image = member.uploadImage(resource);
                    } else {
                        image = group.uploadImage(resource);
                    }
                    break;
                case GROUP_TEMP:
                    image = member.uploadImage(resource);
                    break;
                case FRIEND:
                    image = friend.uploadImage(resource);
                    break;
                case STRANGER:
                    image = stranger.uploadImage(resource);
                    break;
            }
            logInfo("上传成功: " + imgFile);
        } catch (IOException e) {
            logError("上传失败: " + imgFile, e);
        }
        return image;
    }

    /**
     * 缓存一张图片，并上传以供后续发送.
     *
     * @param url
     * @param imgFile
     * @return 如果上传成功，返回图片实例对象，否则返回null
     */
    public Image uploadImage(String url, File imgFile) {
        logInfo("开始下载图片: " + url);
        if (!download(url, imgFile)) {
            return null;
        }
        return uploadImage(imgFile);
    }

    @Deprecated
    public Image uploadFile(File file) {
        Image image = null;
        try (ExternalResource resource = ExternalResource.create(file).toAutoCloseable()) {
            switch (msgType) {
                case GROUP:
                    if (changeToPrivate) {
                        //// 或者用来上传文件
                        //   image = member.files.uploadNewFile("/foo/test.txt", resource);
                    } else {
                        image = group.uploadImage(resource);
                    }
                    break;
                case GROUP_TEMP:
                    image = member.uploadImage(resource);
                    break;
                case FRIEND:
                    image = friend.uploadImage(resource);
                    break;
                case STRANGER:
                    image = stranger.uploadImage(resource);
                    break;
            }
        } catch (IOException e) {
            logError(e);
        }
        return image;
    }


    public long getMilliTime() {
        return time * 1000L;
    }

    private List<Long> atIds = null;

    public List<Long> getAtIds() {
        if (atIds == null) {
            atIds = new ArrayList<>();
            for (SingleMessage message : getMsg()) {
                if (message instanceof At) {
                    atIds.add(((At) message).getTarget());
                }
            }
        }
        return atIds;
    }

    public String[] getAtNicks() {
        //todo:
        return new String[]{senderNick};
    }

    public int getAtNum() {
        return getAtIds().size();
    }

    public long getAtId() {
        return getAtIds().size() > 0 ? getAtIds().get(0) : 0L;
    }

    public String getAtNick() {
        return getAtNicks().length > 0 ? getAtNicks()[0] : "不应出现的未知昵称";
    }

    boolean changeToPrivate = false;

    public void changeGroupToPrivate() {
        changeToPrivate = true;
    }

    public long getBotId() {
        return bot.getId();
    }


    /**
     * #使用戳一戳
     * 戳一戳的被动接收与消息的接收相同，也是以事件的形式（NudgeEvent）。
     * 要发起戳一戳，使用 Contact.nudge() 创建一个戳一戳动作（Nudge）然后将其发送到目标用户或群（Nudge.sendTo）。
     *
     * #操作群成员禁言和移除
     * 使用 Member.mute, Member.unmute, Member.kick。
     */

    /**
     * MessageChain chain = new MessageChainBuilder()
     *     .append(new PlainText("string"))
     *     .append("string") // 会被构造成 PlainText 再添加, 相当于上一行
     *     .append(AtAll.INSTANCE)
     *     .append(Image.fromId("{f8f1ab55-bf8e-4236-b55e-955848d7069f}.png"))
     *     .build();
     */
}
