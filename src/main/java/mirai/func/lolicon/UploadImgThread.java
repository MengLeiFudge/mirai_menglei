package mirai.func.lolicon;

import mirai.core.MsgEvent;
import net.mamoe.mirai.message.data.Image;

import java.io.File;

import static mirai.utils.Settings.THREAD_NUM;

/**
 * @author MengLeiFudge
 */
public class UploadImgThread extends Thread {
    private final int threadId;
    private final MsgEvent msgEvent;
    private final String[] imagesUrl;
    private final File[] imagesFile;
    private final Image[] images;

    public UploadImgThread(int threadId, MsgEvent msgEvent, String[] imagesUrl, File[] imagesFile, Image[] images) {
        this.threadId = threadId;
        this.msgEvent = msgEvent;
        this.imagesUrl = imagesUrl;
        this.imagesFile = imagesFile;
        this.images = images;
    }

    @Override
    public void run() {
        for (int i = 0; i < imagesUrl.length; i++) {
            if (i % THREAD_NUM == threadId) {
                if (imagesUrl[i] != null) {
                    images[i] = msgEvent.uploadImage(imagesUrl[i], imagesFile[i]);
                }
            }
        }
    }
}
