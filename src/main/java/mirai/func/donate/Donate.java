package mirai.func.donate;

import mirai.core.FuncProcess;
import mirai.core.MsgEvent;
import mirai.core.ProcessMsgEvent;
import mirai.utils.MyFileUtils;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.File;

import static mirai.utils.MyFileUtils.getFile;
import static mirai.utils.Settings.AUTHOR_NAME;

/**
 * @author MengLeiFudge
 */
public class Donate extends FuncProcess {
    public Donate(MsgEvent msgEvent, ProcessMsgEvent.MyFunction myFunction) {
        super(msgEvent, myFunction);
    }

    @Override
    public void menu() {
    }

    @Override
    public boolean process() {
        if (msgEvent.getPlainMsg().matches("(/?(?i)donate)|捐献|支持")) {
            File donateImgFile = getFile(MyFileUtils.Dir.DATA, "zfb.jpg");
            MessageChain chain = new MessageChainBuilder()
                    .append(new At(msgEvent.getSenderId()))
                    .append("\n您的每一份捐赠都是对" + AUTHOR_NAME + "最大的支持！\n")
                    .append(msgEvent.uploadImage(donateImgFile))
                    .build();
            msgEvent.send(chain);
            return true;
        }
        return false;
    }
}
