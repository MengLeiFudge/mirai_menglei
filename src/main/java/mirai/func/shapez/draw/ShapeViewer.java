package mirai.func.shapez.draw;

import mirai.func.shapez.base.Corner;
import mirai.func.shapez.base.Shape;
import mirai.func.shapez.base.ShapeDB;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static mirai.func.shapez.draw.DrawUtils.putAtLeftUp;

/**
 * @author MengLeiFudge
 */
public class ShapeViewer {
    /**
     * 将传入的向右向上直角坐标系点位转为向右向下直角坐标系点位，并按照指定象限进行旋转.
     *
     * @param x
     * @param y
     * @param quadrant 最终要旋转到的象限（以向右向上直角坐标系为参照）
     */
    private static void rotate(double[] x, double[] y, int quadrant) {
        final int[][] rotationFactors = {
                {0, -1}, // tr
                {1, 0}, // br
                {0, 1}, // bl
                {-1, 0}, // tl
        };
        int[] factor = rotationFactors[quadrant];
        for (int i = 0; i < x.length; i++) {
            double x0 = x[i];
            double y0 = y[i];
            x[i] = x0 * factor[0] - y0 * factor[1];
            y[i] = x0 * factor[1] + y0 * factor[0];
        }
    }

    /**
     * 将传入的点位数组按照指定比例相乘.
     *
     * @param x
     * @param y
     * @param scale
     */
    private static void scale(double[] x, double[] y, double scale) {
        for (int i = 0; i < x.length; i++) {
            x[i] *= scale;
            y[i] *= scale;
        }
    }

    /**
     * 将以中心点为原点的坐标转为以左上角为原点的坐标.
     *
     * @param d
     * @param w
     * @return
     */
    static int convertCenterToLeftUp(double d, int w) {
        return (int) Math.round(d + w / 2.0);
    }

    /**
     * 将多个以中心点为原点的坐标转为以左上角为原点的坐标.
     *
     * @param dArr
     * @param w
     * @return
     */
    static int[] convertCenterToLeftUp(double[] dArr, int w) {
        return Arrays.stream(dArr).mapToInt(d -> convertCenterToLeftUp(d, w)).toArray();
    }

    /**
     * 灰色透明背景.
     */
    private static final Color SUBSTRATE_COLOR = new Color(40, 50, 65, (int) (0.1 * 255));

    /**
     * 边的颜色.
     */
    private static final Color EDGE_COLOR = new Color(0x555555);

    public static BufferedImage drawShape(Shape shape, int w) {
        BufferedImage image = new BufferedImage(w, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        // 设置抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 设置绘制边的粗细
        g2.setStroke(new BasicStroke((int) (w * 0.03)));

        // 画背景灰色透明圆
        g2.setColor(SUBSTRATE_COLOR);
        g2.fillOval(0, 0, w, w);

        // 画每一层图形
        Corner[][] layers = shape.getCorners();
        for (int layerIndex = 0; layerIndex < layers.length; layerIndex++) {
            Corner[] layer = layers[layerIndex];
            // 层缩放比例
            double layerScale = 0.8 - layerIndex * 0.2;
            for (int quadrantIndex = 0; quadrantIndex < 4; quadrantIndex++) {
                Corner.CornerShape cs = layer[quadrantIndex].getShape();
                if (cs == Corner.CornerShape.NONE) {
                    continue;
                } else if (cs == Corner.CornerShape.NOT_NONE) {
                    cs = Corner.CornerShape.CIRCLE;
                }
                Corner.CornerColor cc = layer[quadrantIndex].getColor();
                if (cc == Corner.CornerColor.NONE) {
                    continue;
                } else if (cc == Corner.CornerColor.NOT_NONE) {
                    cc = Corner.CornerColor.UNCOLORED;
                }
                // 构建一象限的一个角的坐标
                double[] x;
                double[] y;
                double originX = 0, originY = 0, sideLen = w / 2.0;
                if (cs == Corner.CornerShape.CIRCLE) {
                    // 圆比较特殊，此处只画两边
                    x = new double[]{originX, originX, originX + sideLen};
                    y = new double[]{originY + sideLen, originY, originY};
                } else if (cs == Corner.CornerShape.RECTANGLE) {
                    x = new double[]{originX, originX + sideLen, originX + sideLen, originX};
                    y = new double[]{originY, originY, originY + sideLen, originY + sideLen};
                } else if (cs == Corner.CornerShape.STAR) {
                    x = new double[]{originX, originX + sideLen / 2, originX + sideLen, originX};
                    y = new double[]{originY, originY, originY + sideLen, originY + sideLen / 2};
                } else if (cs == Corner.CornerShape.WINDMILL) {
                    x = new double[]{originX, originX + sideLen / 2, originX + sideLen, originX};
                    y = new double[]{originY, originY, originY + sideLen, originY + sideLen};
                } else {
                    throw new RuntimeException("unknown shape");
                }
                // 将一象限的角旋转并缩放
                rotate(x, y, quadrantIndex);
                scale(x, y, layerScale);
                // 根据变换后的坐标绘制这个角
                if (cs == Corner.CornerShape.CIRCLE) {
                    double radius = sideLen * layerScale;
                    // 使用填充圆弧工具绘制内部
                    g2.setColor(cc.getColor());
                    g2.fillArc(convertCenterToLeftUp(-radius, w), convertCenterToLeftUp(-radius, w),
                            (int) (radius * 2), (int) (radius * 2),
                            90 - quadrantIndex * 90, -90);
                    // 使用画圆弧工具绘制边
                    g2.setColor(EDGE_COLOR);
                    g2.drawArc(convertCenterToLeftUp(-radius, w), convertCenterToLeftUp(-radius, w),
                            (int) (radius * 2), (int) (radius * 2),
                            90 - quadrantIndex * 90, -90);
                    g2.drawPolyline(convertCenterToLeftUp(x, w), convertCenterToLeftUp(y, w), x.length);
                } else {
                    // 使用填充多边形工具绘制内部
                    g2.setColor(cc.getColor());
                    g2.fillPolygon(convertCenterToLeftUp(x, w), convertCenterToLeftUp(y, w), x.length);
                    // 使用画多边形工具绘制边
                    g2.setColor(EDGE_COLOR);
                    g2.drawPolygon(convertCenterToLeftUp(x, w), convertCenterToLeftUp(y, w), x.length);
                }
            }
        }

        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return image;
    }


    /**
     * 边的颜色.
     */
    private static final Color BACKGROUND_COLOR = new Color(250, 240, 230);

    public static BufferedImage drawShapeAndFullInfo(Shape shape) {
        int width = 600, height = 565;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        // 设置抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 设置绘制边的粗细
        g2.setStroke(new BasicStroke(2));
        // 设置字体
        g2.setFont(new Font("YaHei Consolas Hybrid", Font.PLAIN, 24));
        // 填充背景
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, width, height);
        // 绘制内容
        g2.setColor(Color.BLACK);

        g2.drawString("短代码：" + shape.getShortKey(), 20, 35);

        g2.drawString("多层表达式", 20, 70);
        String[] multipleLines = shape.toMultipleLines().split("\n");
        for (int i = 0; i < multipleLines.length; i++) {
            g2.drawString(multipleLines[i], 28, 70 + (i + 1) * 25);
        }

        g2.drawString("ID：" + shape.getId(), 160, 90);
        boolean canMake = ShapeDB.canMake(shape);
        if (canMake) {
            g2.drawString("共计" + ShapeDB.getMaterialNum(shape) + "个基材", 150, 120);
            g2.drawString("合成需要" + ShapeDB.getStepNum(shape) + "步", 150, 150);
        } else {
            g2.drawString("该图形不可合成", 150, 120);
        }

        BufferedImage shapeImg = drawShape(shape, 280);
        putAtLeftUp(shapeImg, g2, 20, 260);

        if (canMake) {
            List<String> makePathList = new ArrayList<>();
            ShapeDB.getPath(shape, 0, makePathList);
            for (int i = 0; i < makePathList.size(); i++) {
                g2.drawString(makePathList.get(i), 320, 50 + (i + 1) * 25);
            }
        } else {
            g2.drawString("该图形无法通过基材合成", 320, 50 + 25);
        }

        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return image;
    }
}
