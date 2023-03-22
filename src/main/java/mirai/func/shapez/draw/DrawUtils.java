package mirai.func.shapez.draw;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author MengLeiFudge
 */
public class DrawUtils {
    /**
     * 旋转一个图像.
     *
     * @param image 被旋转的图像
     * @param angel 要旋转的角度
     * @return 旋转后的图像
     */
    public static BufferedImage rotate(BufferedImage image, double angel) {
        if (image == null) {
            return null;
        }
        if (angel < 0) {
            // 将负数角度，纠正为正数角度
            angel += 360;
        }
        int src_width = image.getWidth(null);
        int src_height = image.getHeight(null);
        // calculate the new image size
        Rectangle rect_des = calcRotatedSize(new Rectangle(new Dimension(src_width, src_height)), angel);
        BufferedImage res = new BufferedImage(rect_des.width, rect_des.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = res.createGraphics();
        // transform
        g2.translate((rect_des.width - src_width) / 2, (rect_des.height - src_height) / 2);
        g2.rotate(Math.toRadians(angel), src_width / 2, src_height / 2);
        g2.drawImage(image, null, null);
        g2.dispose();
        return res;
    }

    private static Rectangle calcRotatedSize(Rectangle src, double angel) {
        // if angel is greater than 90 degree, we need to do some conversion
        if (angel >= 90) {
            if (angel / 90 % 2 == 1) {
                int temp = src.height;
                src.height = src.width;
                src.width = temp;
            }
            angel = angel % 90;
        }
        double r = Math.sqrt(src.height * src.height + src.width * src.width) / 2;
        double len = 2 * Math.sin(Math.toRadians(angel) / 2) * r;
        double angel_alpha = (Math.PI - Math.toRadians(angel)) / 2;
        double angel_dalta_width = Math.atan((double) src.height / src.width);
        double angel_dalta_height = Math.atan((double) src.width / src.height);
        int len_dalta_width = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_width));
        len_dalta_width = len_dalta_width > 0 ? len_dalta_width : -len_dalta_width;
        int len_dalta_height = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_height));
        len_dalta_height = len_dalta_height > 0 ? len_dalta_height : -len_dalta_height;
        int des_width = src.width + len_dalta_width * 2;
        int des_height = src.height + len_dalta_height * 2;
        des_width = des_width > 0 ? des_width : -des_width;
        des_height = des_height > 0 ? des_height : -des_height;
        return new Rectangle(new Dimension(des_width, des_height));
    }

    /**
     * 将图片1放置于图片2上，参数xy是图片1左上角相对于图片2的坐标.
     * <p>
     * 传入前应已经缩放、旋转完毕，该方法只用于合并图像。
     *
     * @param upperImg 上层图像
     * @param lowerImg 下层图像
     * @param x        上层图像左上角x坐标
     * @param y        上层图像左上角y坐标
     * @return 合并后的图像
     */
    public static BufferedImage putAtLeftUp(BufferedImage upperImg, BufferedImage lowerImg, int x, int y) {
        Graphics2D g = lowerImg.createGraphics();
        g.drawImage(upperImg, x, y, upperImg.getWidth(), upperImg.getHeight(), null);
        g.dispose();
        return lowerImg;
    }

    public static void putAtLeftUp(BufferedImage upperImg, Graphics2D lowerImg, int x, int y) {
        lowerImg.drawImage(upperImg, x, y, upperImg.getWidth(), upperImg.getHeight(), null);
    }

    /**
     * 将图片1放置于图片2上，参数xy是图片1中心相对于图片2的坐标.
     * <p>
     * 传入前应已经缩放、旋转完毕，该方法只用于合并图像。
     *
     * @param upperImg 上层图像
     * @param lowerImg 下层图像
     * @param x        上层图像左上角x坐标
     * @param y        上层图像左上角y坐标
     * @return 合并后的图像
     */
    public static BufferedImage putAtCenter(BufferedImage upperImg, BufferedImage lowerImg, int x, int y) {
        Graphics2D g = lowerImg.createGraphics();
        g.drawImage(upperImg, x - upperImg.getWidth() / 2, y - upperImg.getHeight() / 2,
                upperImg.getWidth(), upperImg.getHeight(), null);
        g.dispose();
        return lowerImg;
    }

    /**
     * 将图片以指定比例缩放.
     *
     * @param img        要缩放的图像
     * @param scaleRatio 缩放比例
     * @return 缩放后的图像
     */
    public static BufferedImage scaleByRatio(BufferedImage img, double scaleRatio) {
        int width = (int) (img.getWidth() * scaleRatio);
        int height = (int) (img.getHeight() * scaleRatio);
        Image scaledInstance = img.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.drawImage(scaledInstance, 0, 0, null);
        g2.dispose();
        return img;
    }

    /**
     * 将图片以指定宽度缩放.
     *
     * @param img     要缩放的图像
     * @param targetX 目标宽度
     * @return 缩放后的图像
     */
    public static BufferedImage scaleByX(BufferedImage img, int targetX) {
        double ratio = (double) targetX / img.getWidth();
        return scaleByRatio(img, ratio);
    }

    /**
     * 将图片以指定高度缩放.
     *
     * @param img     要缩放的图像
     * @param targetY 目标高度
     * @return 缩放后的图像
     */
    public static BufferedImage scaleByY(BufferedImage img, int targetY) {
        double ratio = (double) targetY / img.getHeight();
        return scaleByRatio(img, ratio);
    }
}
