package mirai.func.shapez;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.util.HashMap;
import java.util.regex.Pattern;

import static mirai.utils.WebUtils.getInfoFromUrl;

/**
 * @author MengLeiFudge
 */
public class ShapezUtils {
    /**
     * Shapez Token，用于下载谜题.
     * <p>
     * Token 通过抓包方式获取，每次登录谜题都会回传一个 TOKEN，下次登录该 TOKEN 将会失效。
     */
    private static final String TOKEN = "9e6fd85b-5175-473b-acac-c4b3af04e27d";

    /**
     * Shapez UA，用于下载谜题.
     * <p>
     * UA 通过抓包方式获取，且随版本更新而变化。
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "shapez/1.5.5 Chrome/96.0.4664.174 Electron/16.2.8 Safari/537.36";

    public static final Pattern PATTERN_CORNER = Pattern.compile("[CRWS][rgbypcuw]|--");
    public static final Pattern PATTERN_LAYER = Pattern.compile("([CRWS][rgbypcuw]|--){4}");
    public static final Pattern PATTERN_SHAPE = Pattern.compile("([CRWS][rgbypcuw]|--){4}(:([CRWS][rgbypcuw]|--){4}){0,3}");
    public static final Pattern PATTERN_SHAPE_IGNORE_CASE =
            Pattern.compile("(?i)([CRWS][rgbypcuw]|--){4}(:([CRWS][rgbypcuw]|--){4}){0,3}");

    /**
     * 通过序号或短代码获取指定谜题的 json.
     * <ul>
     *     <li>如果官方谜题库有该谜题，将谜题保存到谜题文件夹并返回谜题数据；否则从本地查找谜题</li>
     *     <li>如果谜题文件夹内有该谜题，将其移动到已删除文件夹并返回谜题数据；否则从已删除文件夹查找谜题</li>
     *     <li>如果已删除文件夹内有该谜题，返回谜题数据；否则返回 null</li>
     * </ul>
     *
     * @param shortKeyOrId 谜题序号或短代码
     * @return 获取到谜题时，返回谜题数据；否则返回 <code>null</code>
     */
    public static JSONObject getPuzzleJson(String shortKeyOrId) {
        String url = "https://api.shapez.io/v1/puzzles/download/" + shortKeyOrId;
        HashMap<String, String> headerParams = new HashMap<>(10);
        headerParams.put("Content-Type", "application/json");
        headerParams.put("User-Agent", USER_AGENT);
        headerParams.put("x-api-key", "d5c54aaa491f200709afff082c153ef2");
        headerParams.put("x-token", TOKEN);
        String puzzleStr = getInfoFromUrl(url, null, headerParams);
        if (puzzleStr == null) {
            throw new IllegalStateException("未能获取到官方谜题json，请检查TOKEN！");
        }
        JSONObject obj = JSON.parseObject(puzzleStr);
        if (!obj.containsKey("error")) {
            return obj;
        }
        return null;
    }

}
