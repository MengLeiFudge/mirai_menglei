package mirai.func.shapez.base;

import mirai.func.shapez.calculate.Operate;
import mirai.utils.MyFileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static mirai.utils.MyFileUtils.getFile;

/**
 * @author MengLeiFudge
 */
public class ShapeDB {
    private static final boolean[] SHAPE_ID = new boolean[0x10000];
    private static final int[] MATERIAL_NUM = new int[0x10000];
    private static final int[] STEP_NUM = new int[0x10000];
    private static final Operate[] OPERATES = new Operate[0x10000];
    private static final int[] SHAPE_ID_1 = new int[0x10000];
    private static final int[] SHAPE_ID_2 = new int[0x10000];

    static {
        File db = getFile(MyFileUtils.Dir.DATA, "shapez", "db_2c1r_1+3_new.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(db))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 6 || !parts[0].matches("[0-9]+")) {
                    continue;
                }
                int id = Integer.parseInt(parts[0]);
                SHAPE_ID[id] = true;
                MATERIAL_NUM[id] = Integer.parseInt(parts[1]);
                STEP_NUM[id] = Integer.parseInt(parts[2]);
                OPERATES[id] = Operate.valueOf(parts[3]);
                SHAPE_ID_1[id] = Integer.parseInt(parts[4]);
                SHAPE_ID_2[id] = Integer.parseInt(parts[5]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean canMake(Shape shape) {
        return SHAPE_ID[shape.getId()];
    }

    public static int getMaterialNum(Shape shape) {
        return MATERIAL_NUM[shape.getId()];
    }

    public static int getStepNum(Shape shape) {
        return STEP_NUM[shape.getId()];
    }

    public static void getPath(Shape shape, int placeholderNum, List<String> list) {
        int id = shape.getId();
        StringBuilder sb = new StringBuilder();
        if (id < 16) {
            //基类显示shape的表达式
            for (int i = 0; i < placeholderNum; i++) {
                sb.append("| ");
            }
            sb.append(shape.toOneLine());
            list.add(sb.toString());
        } else {
            //非基类显示操作，然后调用自身，显示被操作shape合成路线
            switch (OPERATES[id]) {
                case LEFT:
                case RIGHT:
                case R90:
                    Shape shape0 = getShape(SHAPE_ID_1[id], OPERATES[id], shape);
                    getPath(shape0, placeholderNum + 1, list);
                    break;
                case STACK:
                    Shape[] shapes = getShape(SHAPE_ID_1[id], SHAPE_ID_2[id], shape);
                    getPath(shapes[0], placeholderNum + 1, list);
                    getPath(shapes[1], placeholderNum + 1, list);
                    break;
                default:
                    throw new IllegalStateException("");
            }
            for (int i = 0; i < placeholderNum; i++) {
                sb.append("| ");
            }
            sb.append(OPERATES[id]);
            list.add(sb.toString());
        }
    }

    /// <summary>
    /// 通过具体的目标图形以及合成方式，得到被操作图形的具体情况并返回。
    /// </summary>
    /// <param name="operateId">被操作图形的id</param>
    /// <param name="operate">操作类型，不是add</param>
    /// <param name="shape">目标图形</param>
    /// <returns>被操作图形</returns>
    public static Shape getShape(int operateId, Operate operate, Shape shape) {
        Shape ret = new Shape(operateId);
        switch (operate) {
            case LEFT:
            case RIGHT:
                // 这里默认切割后的产物无全空层
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        if (shape.corners[i][j].getShape() != Corner.CornerShape.NONE) {
                            ret.corners[i][j] = shape.corners[i][j];
                        }
                    }
                }
                break;
            case R90:
                ret = shape.rotate(270);
                break;
        }
        return ret;
    }

    /// <summary>
    /// 通过具体的目标图形以及合成方式，得到被操作图形的具体情况并返回。
    /// </summary>
    /// <param name="operateId1">堆叠的上方图形的id</param>
    /// <param name="operateId2">堆叠的下方图形的id</param>
    /// <param name="shape">目标图形</param>
    /// <returns>两个被操作图形</returns>
    public static Shape[] getShape(int operateId1, int operateId2, Shape shape) {
        // ret1堆叠到ret2上面，则得到shape
        Shape ret1 = new Shape(operateId1);
        Shape ret2 = new Shape(operateId2);
        // 计算ret1下落距离
        int distance = Integer.MAX_VALUE;
        for (int col = 0; col < 4; col++) {
            int aboveDistance = 4;
            for (int i = 0; i < 4; i++) {
                if (ret1.corners[i][col].getShape() != Corner.CornerShape.NONE) {
                    aboveDistance = i;
                    break;
                }
            }
            int belowDistance = 4;
            for (int i = 3; i >= 0; i--) {
                if (ret2.corners[i][col].getShape() != Corner.CornerShape.NONE) {
                    belowDistance = 3 - i;
                    break;
                }
            }
            distance = Math.min(distance, aboveDistance + belowDistance);
        }
        // 构造ret1
        for (int i = 4 - distance; i < 4; i++) {
            int kk = i - (4 - distance);
            for (int j = 0; j < 4; j++) {
                if (shape.corners[i][j].getShape() != Corner.CornerShape.NONE
                        && ret1.corners[kk][j].getShape() != Corner.CornerShape.NONE) {
                    ret1.corners[kk][j] = shape.corners[i][j];
                }
            }
        }
        // 构造ret2
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (shape.corners[i][j].getShape() != Corner.CornerShape.NONE
                        && ret2.corners[i][j].getShape() != Corner.CornerShape.NONE) {
                    ret2.corners[i][j] = shape.corners[i][j];
                }
            }
        }
        return new Shape[]{ret1, ret2};
    }
}

