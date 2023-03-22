package mirai.utils;

import com.alibaba.fastjson2.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static mirai.utils.MyFileUtils.DEF_STRING;
import static mirai.utils.MyFileUtils.ERR_STRING;
import static mirai.utils.MyFileUtils.getFile;
import static mirai.utils.MyFileUtils.getString;
import static mirai.utils.WebUtils.getInfoFromUrl;

/**
 * @author MengLeiFudge
 */
public class NickUtils {
    private NickUtils() {
    }

    private static final File NICK_DIR = getFile(MyFileUtils.Dir.SETTINGS, "groupNick");


    /**
     * 保存当前消息中所有群昵称.
     * <p>
     * 由于私聊消息群昵称为""且一定不含AT，故本方法仅在当前消息为群消息时有实际意义。
     */
    public static void saveNick() {
        //todo: 补全
//        File nickFile = getFile(NICK_DIR, group + ".txt");
//        lock(nickFile);
//        try {
//            setKeyValue(nickFile, qq, groupNick);
//            for (int i = 0; i < msgEvent.getAtNum(); i++) {
//                long atq = getAtQQ(i);
//                setKeyValue(nickFile, atq, getAtNick(i));
//            }
//        } finally {
//            unlock(nickFile);
//        }
    }

    /**
     * 返回本地数据库/API中某个 QQ 对应的昵称.
     *
     * @param userId 要获取昵称的 QQ
     * @return 对应的昵称，本地数据库或api都没有时返回未知昵称
     */
    public static String getNick(long groupId, long userId) {
        // 从本地数据库的本群获取昵称
        String nick = getString(getFile(NICK_DIR, groupId + ".txt"), userId);
        if (!DEF_STRING.equals(nick)) {
            return nick;
        }
        // 从本地数据库的其他群获取昵称
        File[] files = getFile(NICK_DIR).listFiles();
        if (files != null) {
            for (File f : files) {
                nick = getString(f, userId);
                if (!DEF_STRING.equals(nick)) {
                    return nick;
                }
            }
        }
        // 从api获取昵称
        nick = getNickFromVvhan(userId);
        if (!ERR_STRING.equals(nick)) {
            return nick;
        }
        return "未知昵称";
    }

    private static String getNickFromVvhan(long id) {
        // {"success":true,"imgurl":"https://q2.qlogo.cn/headimg_dl?dst_uin=605738729&spec=640",
        // "name":"萌泪酱最可爱啦","qemail":"605738729@qq.com",
        // "qzone":"https://user.qzone.qq.com/605738729"}
        Map<String, String> urlParams = new HashMap<>(16);
        urlParams.put("qq", id + "");
        String s = getInfoFromUrl("https://api.vvhan.com/api/qq", urlParams, null);
        if (ERR_STRING.equals(s)) {
            return ERR_STRING;
        }
        JSONObject obj = JSONObject.parse(s);
        if (obj != null
                && obj.containsKey("success")
                && obj.getBooleanValue("success")
                && obj.containsKey("name")) {
            return obj.getString("name");
        } else {
            return ERR_STRING;
        }
    }
}
