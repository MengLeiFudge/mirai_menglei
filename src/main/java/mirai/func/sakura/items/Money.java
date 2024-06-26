package mirai.func.sakura.items;

import mirai.func.sakura.character.player.Player;

/**
 * @author MengLeiFudge
 */
public class Money extends Item {

    int num;

    public Money(int num) {
        super("樱币", -1);
        this.num = num;
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

    @Override
    public boolean use(Player player) {
        return false;
    }
}
