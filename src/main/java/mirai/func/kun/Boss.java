package mirai.func.kun;

import java.io.Serializable;

import static mirai.utils.RandomUtils.getRandomChineseStr;
import static mirai.utils.RandomUtils.getRandomDouble;
import static mirai.utils.RandomUtils.getRandomInt;

/**
 * @author MengLeiFudge
 */
public class Boss implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private int level;
    private int atk;
    private int def;
    private int hp;

    Boss() {
        newBoss();
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public int getAtk() {
        return atk;
    }

    public int getDef() {
        return def;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    /**
     * 以所有群等级最高者为基础，生成新的boss
     * 如果无人上榜，boss等级默认5000级
     */
    public void newBoss() {
        level = Math.max(5000, Rank.getMaxLevel());
        name = getRandomChineseStr(1, 4);
        atk = (int) (level * getRandomDouble(1.6, 2.8));
        def = (int) (level * getRandomDouble(0.8, 1.4));
        hp = level * getRandomInt(8000, 14000);
    }
}
