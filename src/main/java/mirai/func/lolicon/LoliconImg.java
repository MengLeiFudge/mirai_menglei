package mirai.func.lolicon;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import mirai.core.FuncProcess;
import mirai.core.MsgEvent;
import mirai.core.ProcessMsgEvent;
import mirai.utils.MyFileUtils;
import mirai.utils.MyThreadFactory;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static mirai.utils.BotAdminUtils.isBotAdmin;
import static mirai.utils.LogUtils.logError;
import static mirai.utils.MyFileUtils.DEF_INT;
import static mirai.utils.MyFileUtils.getFile;
import static mirai.utils.MyFileUtils.getInt;
import static mirai.utils.MyFileUtils.setKeyValue;
import static mirai.utils.Settings.THREAD_NUM;
import static mirai.utils.WebUtils.getInfoFromUrl;
import static mirai.utils.WebUtils.unicodeToUtf8;

/**
 * @author MengLeiFudge
 */
public class LoliconImg extends FuncProcess {
    public LoliconImg(MsgEvent msgEvent, ProcessMsgEvent.MyFunction myFunction) {
        super(msgEvent, myFunction);
        int i = getInt(SETU_FILE, msgEvent.getGroupId());
        if (i == DEF_INT) {
            groupFlag = false;
            showImageFlag = false;
            save();
        } else {
            groupFlag = (i & (1 << 0)) == 1 << 0;
            showImageFlag = (i & (1 << 1)) == 1 << 1;
        }
    }

    //api来源：https://api.lolicon.app/#/setu

    private static final File SETU_FILE = getFile(MyFileUtils.Dir.SETTINGS, "loliconImgConfig.txt");
    private static boolean groupFlag;
    private static boolean showImageFlag;

    private void save() {
        int value = 0;
        if (groupFlag) {
            value += 1;
        }
        if (showImageFlag) {
            value += 1 << 1;
        }
        setKeyValue(SETU_FILE, msgEvent.getGroupId(), value);
    }

    enum R18Type {
        // 非r18
        NON_R18,
        // r18
        R18,
        // 混合
        MIXED;

        @Override
        public String toString() {
            if (this == NON_R18) {
                return "美图";
            } else if (this == R18) {
                return "色图";
            } else {
                return "混合图";
            }
        }

        public int getTypeIndex() {
            if (this == NON_R18) {
                return 0;
            } else if (this == R18) {
                return 1;
            } else {
                return 2;
            }
        }

        public static R18Type getR18TypeByStr(String s) {
            if (s.matches("美图")) {
                return R18Type.NON_R18;
            } else if (s.matches("[色涩蛇]图")) {
                return R18Type.R18;
            } else {
                return R18Type.MIXED;
            }
        }
    }

    /**
     * v1版本需要，现已无用.
     */
    static final String API_KEY = "589871545edc96be118785";

    @Override
    public void menu() {
        msgEvent.send("[g]群聊 [p]私聊 [a]仅管理员\n" +
                "[gp](来点)色图/涩图/蛇图(+数目)：获取指定数目的色图，默认1张\n" +
                "[gp](来点)色图+tag(+数目)：获取指定数目的包含tag的色图（可能获取不到足够多的数量，默认5张）\n" +
                "[a]开/关群色图：控制能否在群内展示色图消息\n" +
                "[a]开/关图片显示：控制图片消息是否包含缩略图\n" +
                "色图(r18)可换为美图(无r18)或混合(r18和非r18都有)\n" +
                "以下是一些指令示例：\n" +
                "【色图20】表示20张色图\n" +
                "【美图凯露】表示5张凯露的美图\n" +
                "【美图凯露20】表示20张凯露的美图\n" +
                "【美图 凯露 可可萝 10】表示10张同时包含凯露和可可萝的美图\n" +
                "【美图 凯露|可可萝 10】表示10张包含凯露或可可萝的美图\n" +
                "【色图 萝莉|少女 白丝|黑丝】表示5张(萝莉或少女)的(白丝或黑丝)的色图\n" +
                "至多3个tag，每个tag至多20个选项\n" +
                "群色图状态：" + (groupFlag ? "开启" : "关闭") + "\n" +
                "群图片显示状态：" + (showImageFlag ? "开启" : "关闭")
        );
    }


    @Override
    public boolean process() {
        String textMsg;
        // 先判断是否更改群色图设置
        if (isBotAdmin(msgEvent.getSenderId())) {
            if (msgEvent.getPlainMsg().matches("[开关]群色图")) {
                groupFlag = msgEvent.getPlainMsg().startsWith("开");
                save();
                msgEvent.send("已" + (groupFlag ? "开启" : "关闭") + "群色图！");
                return true;
            } else if (msgEvent.getPlainMsg().matches("[开关]图片显示")) {
                showImageFlag = msgEvent.getPlainMsg().startsWith("开");
                save();
                if (showImageFlag) {
                    msgEvent.send("已开启图片显示！\n" +
                            "注意，开启此功能极有可能导致无法接收到消息！\n" +
                            "即使开启，r18图片也不会有缩略图显示~");
                } else {
                    msgEvent.send("已关闭图片显示！");
                }
                return true;
            }
        }
        String plainMsg = msgEvent.getPlainMsg();
        if (!plainMsg.matches("(来点)?([美色涩蛇]图|混合).*")) {
            return false;
        }
        if (plainMsg.startsWith("来点")) {
            plainMsg = plainMsg.substring(2);
        }
        R18Type type = R18Type.getR18TypeByStr(plainMsg.substring(0, 2));
        plainMsg = plainMsg.substring(2).trim();
        // 不带tag一张
        if ("".equals(plainMsg)) {
            setu(type, 1, (String) null);
            return true;
        }
        // 不带tag多张
        if (plainMsg.matches("[0-9]+")) {
            setu(type, Integer.parseInt(plainMsg), (String) null);
            return true;
        }
        // 带tag，判断结尾是否有数字
        int num = 5;
        if (plainMsg.matches(".*[0-9]+")) {
            String[] nums = plainMsg.split("\\D+");
            int num0 = Integer.parseInt(nums[nums.length - 1]);
            String num0Str = num0 + "";
            if (plainMsg.endsWith(num0Str)) {
                num = num0;
                plainMsg = plainMsg.substring(0, plainMsg.length() - num0Str.length()).trim();
            }
        }
        // 将tag以空白字符分割
        String[] tags = plainMsg.split("\\s+");
        setu(type, num, tags);
        return true;
    }

    private File getImgFile(String imgName) {
        return getFile(MyFileUtils.Dir.DATA, "lolicon", "img", imgName);
    }

    private void setu(R18Type type, int num, String... searchTags) {
        if (num <= 0) {
            msgEvent.send(msgEvent.getSenderId(), "图片数目至少为1！");
            return;
        }
        if (msgEvent.getMsgType() == MsgEvent.MsgType.GROUP && type != R18Type.NON_R18 && !groupFlag) {
            msgEvent.send("本群当前设置为群内只能查看非R18图片！\n请私聊发送指令QwQ");
            return;
        }
        Map<String, String> urlParams = new HashMap<>(16);
        urlParams.put("r18", type.getTypeIndex() + "");
        urlParams.put("size", "original");
        // map相同key会覆盖
//        urlParams.put("size", "regular");
//        urlParams.put("size", "small");
//        urlParams.put("size", "thumb");
//        urlParams.put("size", "mini");
        urlParams.put("num", Math.min(num, 20) + "");
        if (searchTags != null) {
            for (String tag : searchTags) {
                if (tag != null && !"".equals(tag)) {
                    urlParams.put("tag", tag);
                }
            }
        }
        msgEvent.send(msgEvent.getSenderId(), "正在查找图片，请耐心等候...");
        String s = getInfoFromUrl("https://api.lolicon.app/setu/v2", urlParams, null);
        try {
            JSONObject obj = JSONObject.parseObject(s);
            String code = obj.getString("error");
            if (!"".equals(code)) {
                msgEvent.send("Api 出错辣！\n" + code);
                return;
            }
            JSONArray data = obj.getJSONArray("data");
            if (data.size() == 0) {
                msgEvent.send("没有找到符合你要求的图片呢QAQ\n尝试减少一些tag吧！");
                return;
            }
            // 先用多线程下载所有图片
            // 构建下载信息，imagesUrl 不为 null 说明要下载
            String[] imagesUrl = new String[data.size()];
            File[] imagesFile = new File[data.size()];
            Image[] images = new Image[data.size()];
            for (int i = 0; i < data.size(); i++) {
                JSONObject img = data.getJSONObject(i);
                JSONObject urls = img.getJSONObject("urls");
                String originalImgUrl = unicodeToUtf8(urls.getString("original"));
                boolean showImage = msgEvent.getMsgType() != MsgEvent.MsgType.GROUP
                        || (!img.getBooleanValue("r18") && showImageFlag);
                if (showImage) {
                    imagesUrl[i] = originalImgUrl;
                    imagesFile[i] = getImgFile(img.getString("pid") + "." + img.getString("ext"));
                }
            }
            // 使用构建信息进行下载
            ExecutorService pool = new ThreadPoolExecutor(
                    THREAD_NUM, THREAD_NUM,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(),
                    new MyThreadFactory());
            long start = System.currentTimeMillis();
            for (int i = 0; i < THREAD_NUM; i++) {
                pool.execute(new UploadImgThread(i, msgEvent, imagesUrl, imagesFile, images));
            }
            pool.shutdown();
            while (!pool.isTerminated()) {
                if (System.currentTimeMillis() - start > 20000) {
                    break;
                }
                sleep(500);
            }

            for (int i = 0; i < data.size(); i++) {
                MessageChainBuilder chainBuilder = new MessageChainBuilder();
                chainBuilder.append("图片索引：").append(String.valueOf(i + 1))
                        .append(" / ").append(String.valueOf(data.size())).append("\n");
                JSONObject img = data.getJSONObject(i);
                JSONObject urls = img.getJSONObject("urls");
                String originalImgUrl = unicodeToUtf8(urls.getString("original"));
                if (images[i] != null) {
                    chainBuilder.append(images[i]);
                } else {
                    chainBuilder.append(originalImgUrl);
                }
                chainBuilder.append("\n")
                        .append(img.getString("title"))
                        .append("(PID ").append(img.getString("pid")).append(")\n")
                        .append("by ").append(img.getString("author"))
                        .append("(UID ").append(img.getString("uid")).append(")");
                msgEvent.send(chainBuilder.build());
                sleep(500);
            }
        } catch (JSONException e) {
            logError(e);
        }
    }
}
