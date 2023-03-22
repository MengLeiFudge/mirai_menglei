package mirai.func.shapez.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mirai.func.shapez.base.Corner.CornerColor;

/**
 * 表示一个染料，包含染料的颜色信息.
 *
 * @author MengLeiFudge
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Color extends Item {
    private final CornerColor cornerColor;

    protected Color(String shortKey) {
        super();
        switch (shortKey) {
            case "uncolored":
                cornerColor = CornerColor.UNCOLORED;
                break;
            case "red":
                cornerColor = CornerColor.RED;
                break;
            case "green":
                cornerColor = CornerColor.GREEN;
                break;
            case "blue":
                cornerColor = CornerColor.BLUE;
                break;
            case "yellow":
                cornerColor = CornerColor.YELLOW;
                break;
            case "purple":
                cornerColor = CornerColor.PURPLE;
                break;
            case "cyan":
                cornerColor = CornerColor.CYAN;
                break;
            case "white":
                cornerColor = CornerColor.WHITE;
                break;
            default:
                throw new IllegalArgumentException("错误的颜色短代码：" + shortKey);
        }
        this.shortKey = shortKey;
    }
}
