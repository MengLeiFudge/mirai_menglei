package mirai.func.arc.query;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.io.Serializable;

import static mirai.utils.LogUtils.logError;

/**
 * @author MengLeiFudge
 */
@Data
public class SongInfo implements Serializable, Comparable<SongInfo> {
    private static final long serialVersionUID = 1L;

    private String song_id;
    private String[] name_en = new String[4];
    private String[] name_jp = new String[4];
    private String[] artist = new String[4];
    private String[] bpm = new String[4];
    private double[] bpm_base = new double[4];
    private String[] set = new String[4];
    private String[] set_friendly = new String[4];
    private int[] time = new int[4];
    private int[] side = new int[4];
    private boolean[] world_unlock = new boolean[4];
    private boolean[] remote_download = new boolean[4];
    private String[] bg = new String[4];
    private long[] date = new long[4];
    private String[] version = new String[4];
    private int[] difficulty = new int[4];
    private int[] rating = new int[4];
    private int[] note = new int[4];
    private String[] chart_designer = new String[4];
    private String[] jacket_designer = new String[4];
    private boolean[] jacket_override = new boolean[4];
    private boolean[] audio_override = new boolean[4];

    /**
     * 指示是否通过content构建成功.
     */
    private boolean isOk = false;

    static SongInfo parse(JSONObject content) {
        SongInfo info = new SongInfo();
        try {
            info.song_id = content.getString("song_id");
            JSONArray difficulties = content.getJSONArray("difficulties");
            for (int i = 0; i < difficulties.size(); i++) {
                JSONObject obj = difficulties.getJSONObject(i);
                info.name_en[i] = obj.getString("name_en");
                info.name_jp[i] = obj.getString("name_jp");
                info.artist[i] = obj.getString("artist");
                info.bpm[i] = obj.getString("bpm");
                info.bpm_base[i] = obj.getDoubleValue("bpm_base");
                info.set[i] = obj.getString("set");
                info.set_friendly[i] = obj.getString("set_friendly");
                info.time[i] = obj.getIntValue("time");
                info.side[i] = obj.getIntValue("side");
                info.world_unlock[i] = obj.getBooleanValue("world_unlock");
                info.remote_download[i] = obj.getBooleanValue("remote_download");
                info.bg[i] = obj.getString("bg");
                info.date[i] = obj.getLongValue("date");
                info.version[i] = obj.getString("version");
                info.difficulty[i] = obj.getIntValue("difficulty");
                info.rating[i] = obj.getIntValue("rating");
                info.note[i] = obj.getIntValue("note");
                info.chart_designer[i] = obj.getString("chart_designer");
                info.jacket_designer[i] = obj.getString("jacket_designer");
                info.jacket_override[i] = obj.getBooleanValue("jacket_override");
                info.audio_override[i] = obj.getBooleanValue("audio_override");
            }
            info.isOk = true;
        } catch (Exception e) {
            logError(e);
        }
        return info;
    }

    @Override
    public int compareTo(SongInfo o) {
        return song_id.compareTo(o.song_id);
    }
}
