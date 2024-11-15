package playthespire.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import playthespire.actions.EnableAutoPlayAction;


@SpirePatch(
        clz = AbstractPlayer.class,
        method = "applyStartOfCombatLogic"
)
public class GlobalAtCombatStartHookPatches {
    public static final Logger logger = LogManager.getLogger("PlayTheSpire");


    @SpirePrefixPatch(
    )
    public static void Prefix(AbstractPlayer __instance) {
        logger.info("combat starting - 1");
        AutoPlayPatches.OnRefreshHandCheckToPlayCardPatch.isCombatStarting = true;
    }

    @SpirePostfixPatch(
    )
    public static void Postfix(AbstractPlayer __instance) {
        logger.info("combat starting - 2");
        //AbstractDungeon.actionManager.addToBottom(new EnableAutoPlayAction());
        //AutoPlayPatches.OnRefreshHandCheckToPlayCardPatch.isCombatStarting = false;
    }
}