package mirai.func.arc.pubg;

import com.alibaba.fastjson2.JSONObject;
import mirai.core.FuncProcess;
import mirai.core.MsgEvent;
import mirai.core.ProcessMsgEvent;
import mirai.func.arc.query.ArcUtils;
import mirai.func.arc.query.User;

import java.text.DecimalFormat;

import static mirai.utils.DateUtils.LESS_THAN_ONE_MINUTE;
import static mirai.utils.DateUtils.getFullTimeStr;
import static mirai.utils.DateUtils.milliSecondToStr;
import static mirai.utils.MyFileUtils.ERR_STRING;

/**
 * @author MengLeiFudge
 */
public class Process extends FuncProcess {
    public Process(MsgEvent msgEvent, ProcessMsgEvent.MyFunction myFunction) {
        super(msgEvent, myFunction);
        menuList.add("arc吃鸡");
        menuList.add("arcaea吃鸡");
    }

    @Override
    public void menu() {
        msgEvent.send("标识 [g]群聊可用 [p]私聊可用\n" +
                "说明 [a]仅Bot管理员可用\n" +
                "[g]吃鸡：获取最近游玩的分数情况\n" +
                "[g]吃鸡 b30均值 定数 分数 P 大P F L：查询指定情况下分数，用空格连接每项\n" +
                "[g]加入吃鸡：加入吃鸡对局"
        );
    }

    @Override
    public boolean process() {
        if (msgEvent.getPlainMsg().matches("吃鸡")) {
            lastSongInfo();
            return true;
        } else if (msgEvent.getPlainMsg().matches("吃鸡 [0-9]+(\\.[0-9]+)? [0-9]+(\\.[0-9])? [0-9]+ [0-9]+ [0-9]+ [0-9]+ [0-9]+")) {
            String[] data = msgEvent.getPlainMsg().split(" ");
            double b30Avg = Double.parseDouble(data[1]);
            double songRate = Double.parseDouble(data[2]);
            int x1 = Integer.parseInt(data[3]);
            int x2 = Integer.parseInt(data[4]);
            int x3 = Integer.parseInt(data[5]);
            int x4 = Integer.parseInt(data[6]);
            int x5 = Integer.parseInt(data[7]);
            fullScoreInfo(b30Avg, songRate, x1, x2, x3, x4, x5);
            return true;
        } else if (msgEvent.getPlainMsg().matches("吃鸡 [0-9]+(\\.[0-9]+)? [0-9]+(\\.[0-9])?")) {
            String[] data = msgEvent.getPlainMsg().split(" ");
            pubgImg(Double.parseDouble(data[1]), Double.parseDouble(data[2]));
            return true;
        } else if (msgEvent.getPlainMsg().matches("吃鸡.+")) {
            msgEvent.send(msgEvent.getSenderId(), "指令为【吃鸡 b30均值 定数 分数 P 大P Far Lost】\n" +
                    "后三个可以交换位置");
            return true;
        } else if (msgEvent.getPlainMsg().matches("加入吃鸡")) {
            joinPUBG();
            return true;
        }
        return false;
    }

    DecimalFormat df1 = new DecimalFormat("0.0");
    DecimalFormat df2 = new DecimalFormat("0.00");
    DecimalFormat df3 = new DecimalFormat("0.000");
    DecimalFormat df4 = new DecimalFormat("0.0000");

    private void lastSongInfo() {
        User user = ArcUtils.getUser(msgEvent.getSenderId(), msgEvent);
        if (user == null) {
            msgEvent.send(msgEvent.getSenderId(), "我又不是神奇海螺，你不绑我怎么查啊！\n" + "tips：绑定 + 9位arcID");
            return;
        }
        msgEvent.send("查询信息ing，请耐心等候...");
        String s = ArcUtils.getUserInfo(user.getArcId(), 1, true);
        if (ERR_STRING.equals(s)) {
            msgEvent.send(msgEvent.getSenderId(), "api获取信息超时啦！\n请稍后再试~");
            return;
        }
        JSONObject obj = JSONObject.parseObject(s);
        if (obj.getIntValue("status") != 0) {
            msgEvent.send(msgEvent.getSenderId(), "查询出错！\n" + obj.getString("message"));
            return;
        }

        JSONObject content = obj.getJSONObject("content");
        if (!content.containsKey("recent_score")) {
            msgEvent.send(msgEvent.getSenderId(), "未查找到你的近期游玩记录，先打一首歌再来查吧！");
            return;
        }

        JSONObject account_info = content.getJSONObject("account_info");
        String name = account_info.getString("name");// 昵称
        int character = account_info.getIntValue("character");// 角色
        int rating = account_info.getIntValue("rating");// ptt*100，隐藏时为-1
        String ratingStr = rating == -1 ? "隐藏" :
                new DecimalFormat("0.00").format(rating / 100.0);
        // 搭档技能是否锁定、搭档是否觉醒、是否为觉醒后又切换到原始态
        boolean isSkillSealed = account_info.getBooleanValue("is_skill_sealed");
        boolean isCharUncapped = account_info.getBooleanValue("is_char_uncapped");
        boolean isCharUncappedOverride = account_info.getBooleanValue("is_char_uncapped_override");

        // 最近游玩记录
        JSONObject recentScore = content.getJSONArray("recent_score").getJSONObject(0);
        String songID = recentScore.getString("song_id");// 歌曲id
        int difficulty = recentScore.getIntValue("difficulty");// 难度，0123
        int score = recentScore.getIntValue("score");// 分数
        int shinyPure = recentScore.getIntValue("shiny_perfect_count");// 大Pure
        int pure = recentScore.getIntValue("perfect_count");// Pure
        int far = recentScore.getIntValue("near_count");// Far
        int lost = recentScore.getIntValue("miss_count");// Lost
        int clearType = recentScore.getIntValue("clear_type");// 完成类型
        long timePlayed = recentScore.getLongValue("time_played");// 游玩时间
        double songPtt = recentScore.getDoubleValue("rating");// 单曲ptt
        String songRateStr = ArcUtils.scoreAndPttToRateStr(songID, difficulty, score, songPtt);// 单曲定数
        String fullTime = getFullTimeStr(timePlayed);
        String timeDiff = milliSecondToStr(timePlayed, System.currentTimeMillis(), false);
        if (!timeDiff.equals(LESS_THAN_ONE_MINUTE)) {
            timeDiff += "前";
        }

        JSONObject songinfo = content.getJSONArray("songinfo").getJSONObject(0);
        String songName = songinfo.getString("name_en");
        double songRate = songinfo.getIntValue("rating") / 10.0;

        double score1 = getScore1(rating / 100.0, songRate, score, shinyPure);
        double score2 = getScore2(shinyPure, pure, far, lost);
        double result = score1 * 0.8 + score2 * 0.2;
        s = name + " (" + user.getArcId() + ") - " + ratingStr + "\n" +
                //"best30 均值：" + df3.format(b30Avg) + "\n" +
                songName + " [" + ArcUtils.diffFormatStr[difficulty] + "] [" + songRateStr + "]\n" +
                ArcUtils.scoreToStr(score) + " -> " + ArcUtils.pttToStr(songPtt) + "\n\n" +
                "基础得分：" + df4.format(score1) + "\n" +
                "准度得分：" + df4.format(score2) + "\n" +
                "最终得分：" + df4.format(result);
        msgEvent.send(msgEvent.getSenderId(), s);
    }

    private void fullScoreInfo(double b30Avg, double songRate, int score,
                               int pure, int shinyPure, int far, int lost) {
        double score1 = getScore1(b30Avg, songRate, score, shinyPure);
        double score2 = getScore2(shinyPure, pure, far, lost);
        double result = score1 * 0.8 + score2 * 0.2;
        msgEvent.send(msgEvent.getSenderId(), "best30 均值：" + df3.format(b30Avg) + "\n" +
                "定数：" + df1.format(songRate) + "，分数：" + ArcUtils.scoreToStr(score) + "\n" +
                "Pure " + pure + "(+" + shinyPure + ")" + " Far " + far + " Lost " + lost + "\n" +
                "基础得分：" + df4.format(score1) + "\n" +
                "准度得分：" + df4.format(score2) + "\n" +
                "最终得分：" + df4.format(result));
    }

    /**
     * 获取第一部分目标分数为95时，对应的游玩分数.
     */
    private static double get95Score(double b30Avg, double songRate) {
        double diffValue = songRate - b30Avg;
        diffValue = Math.max(-2.5, Math.min(0, diffValue));
        // 不要问我参数怎么来的，matlab拟合告诉我的（
        double fitValue = -15260 * Math.pow(diffValue, 3) - 167800 * Math.pow(diffValue, 2)
                - 597400 * Math.pow(diffValue, 1) + 9289000;
        return Math.max(9500000, fitValue);
    }

    private static double getScore1(double b30Avg, double songRate, int score, int shinyPure) {
        score -= shinyPure;
        double standardScore = 95.0;
        double k = (100.0 - standardScore) / (10000000 - get95Score(b30Avg, songRate));
        double b = 100.0 - k * 10000000;
        if (score < 9800000) {
            // 分数低于980w时，分数损失上升
            double score1 = k * 9800000 + b;
            k = k * 1.5;
            b = score1 - k * 9800000;
            if (score < 9500000) {
                // 分数低于950w时，分数损失上升
                double score2 = k * 9500000 + b;
                k = k * 2;
                b = score2 - k * 9500000;
            }
        }
        return Math.max(0, Math.min(k * score + b, 100));
    }

    private static double getScore2(int shinyPure, int pure, int far, int lost) {
        int note = pure + far + lost;
        double score100 = note * 1.0;
        double score = shinyPure * 1.0 + (pure - shinyPure) * 0.7 + far * 0.3;
        return score / score100 * 100;
    }

    private void pubgImg(double b30Avg, double songRate) {
//        // 创建图片，画最外侧边框
//        Img img = new Img(600, 400);
//        img.setRgbColor(255, 255, 255);
//        img.drawRect(0, 0, 600, 400);
//        // 画分数曲线
//        float x1;
//        float y1;
//        float x2 = 0;
//        float y2 = (float) getScore1(b30Avg, songRate, 9000000, 0) * 3;
//        img.setRgbColor(0, 0, 0);
//        for (int score = 9004000; score <= 10000000; score += 4000) {
//            x1 = x2;
//            y1 = y2;
//            x2 = (score - 9000000) / 2000.0f;
//            y2 = (float) getScore1(b30Avg, songRate, score, 0) * 3;
//            img.drawLine(x1 + 50, 400 - (y1 + 50), x2 + 50, 400 - (y2 + 50));
//        }
//        // 画边框
//        img.setRgbColor(0x48, 0x76, 0xff);
//        for (float i = 0; i <= 500; i += 50) {
//            img.drawLine(i + 50, 50, i + 50, 350);
//            img.drawText(((int) i / 5 + 900) + "w", i + 50 - 20, 400f - 30);
//        }
//        for (float i = 0; i <= 300; i += 30) {
//            img.drawLine(50, i + 50, 550, i + 50);
//            img.drawText(((int) i / 3) + "", 20, 400f - (i + 45));
//        }
//        img.setRgbColor(0xcc, 0x32, 0x99);
//        File f = getFile(Dir.DATA, "test", getFullTimeStr(System.currentTimeMillis()) + ".jpg");
//        img.save(f);
//        addText("b30均值: " + df3.format(b30Avg) + "\n" +
//                "歌曲定数: " + df1.format(songRate) + "\n");
//        addImg(f);
//        msgEvent.send();
    }

    private void joinPUBG() {
        User user = ArcUtils.getUser(msgEvent.getSenderId(), msgEvent);
        if (user == null) {
            msgEvent.send(msgEvent.getSenderId(), "我又不是神奇海螺，你不绑我怎么查啊！\n" + "tips：绑定 + 9位arcID");
            return;
        }
        msgEvent.send("该功能还未写好！");
    }
}
