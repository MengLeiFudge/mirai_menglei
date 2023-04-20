package mirai.func.shapez;

import com.alibaba.fastjson2.JSONObject;
import mirai.core.FuncProcess;
import mirai.core.MsgEvent;
import mirai.core.ProcessMsgEvent;
import mirai.func.shapez.base.Shape;
import mirai.utils.MyFileUtils;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static jdk.internal.net.http.common.Log.logError;
import static mirai.func.shapez.ShapezUtils.PATTERN_SHAPE_IGNORE_CASE;
import static mirai.func.shapez.ShapezUtils.getPuzzleJson;
import static mirai.func.shapez.draw.ShapeViewer.drawShapeAndFullInfo;
import static mirai.utils.MyFileUtils.getFile;

/**
 * @author MengLeiFudge03
 */
public class Process extends FuncProcess {

    public Process(MsgEvent msgEvent, ProcessMsgEvent.MyFunction myFunction) {
        super(msgEvent, myFunction);
        menuList.add("shapez");
        menuList.add("异形工厂");
    }

    @Override
    public void menu() {
        msgEvent.send("标识 [g]群聊可用 [p]私聊可用\n" +
                "说明 [a]仅Bot管理员可用\n" +
                "[gp]i 短代码：返回短代码对应图形图片\n" +
                "[gp]p 短代码：返回短代码对应谜题图片\n" +
                "[gp]p 序号：返回序号对应谜题图片"
        );
    }


    @Override
    public boolean process() {
        String plainMsg = msgEvent.getPlainMsg();
        if (!plainMsg.matches("[ip] .*")) {
            return false;
        }
        if (plainMsg.matches("p *[0-9]+")) {
            showPuzzle(plainMsg.substring(2));
            return true;
        }
        String shortKey = plainMsg.substring(2).replace("：", ":").trim();
        if (!PATTERN_SHAPE_IGNORE_CASE.matcher(shortKey).matches()) {
            return false;
        }
        char[] chars = shortKey.toCharArray();
        int i = 0;
        while (i < chars.length) {
            boolean upper = (i % 9) % 2 == 0;
            if (upper) {
                chars[i] = Character.toUpperCase(chars[i]);
            } else {
                chars[i] = Character.toLowerCase(chars[i]);
            }
            i++;
        }
        shortKey = new String(chars);
        try {
            Shape shape = new Shape(shortKey);
            if (plainMsg.startsWith("i")) {
                showShape(shape);
            } else {
                showPuzzle(shape.getShortKey());
            }
            return true;
        } catch (Exception e) {
            // 短代码可能不合规
            return false;
        }
    }

    private void showShape(Shape shape) {
        BufferedImage bufferedImage = drawShapeAndFullInfo(shape);
        File file = getFile(MyFileUtils.Dir.DATA, "shapez", "img", "shape",
                shape.getShortKey().replace(":", "：") + ".png");
        try {
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            logError(e);
            return;
        }
        Image image = msgEvent.uploadImage(file);
        MessageChainBuilder chainBuilder = new MessageChainBuilder();
        chainBuilder.append(image);
        msgEvent.send(chainBuilder.build());
    }

    private void showPuzzle(String shortKeyOrId) {
        JSONObject obj = getPuzzleJson(shortKeyOrId);
        if (obj == null) {
            msgEvent.send("无法获取谜题json，需要萌泪更新token捏");
            return;
        }
        JSONObject meta = obj.getJSONObject("meta");
        int id = meta.getIntValue("id");
        String shortKey = meta.getString("shortKey");
        if (true) {
            msgEvent.send("功能还没有写好捏~");
            return;
        }
        File file = getFile(MyFileUtils.Dir.DATA, "shapez", "img", "puzzle",
                "[" + id + "]" + shortKey.replace(":", "：") + ".png");
        //createPuzzleImg(obj, file);
        Image image = msgEvent.uploadImage(file);
        MessageChainBuilder chainBuilder = new MessageChainBuilder();
        chainBuilder//.append(new At(msgEvent.getSenderId())).append("\n")
                .append(shortKeyOrId).append("\n")
                .append(image);
        msgEvent.send(chainBuilder.build());
    }

}
