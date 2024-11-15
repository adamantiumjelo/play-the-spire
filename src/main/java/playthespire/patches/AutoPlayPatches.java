package playthespire.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.watcher.PressEndTurnButtonAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import javassist.CtBehavior;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class AutoPlayPatches {

    public static final Logger logger = LogManager.getLogger("PlayTheSpire");

    @SpirePatch(
            clz = PressEndTurnButtonAction.class,
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoClashPlz {
        public static void Postfix(PressEndTurnButtonAction __instance) {
            OnRefreshHandCheckToPlayCardPatch.isEndingTurn = true;
        }
    }

    // Annotation to bind this patch to every time the game runs CardGroup.refreshHandLayout
    @SpirePatch(
        clz = CardGroup.class,
        method = "refreshHandLayout"
    )
    public static class OnRefreshHandCheckToPlayCardPatch {
        static ReflectionHacks.RMethod playCard;
        public static boolean isEndingTurn;
        public static boolean isCombatStarting = true;

        // Annotation to tell SpirePatch "Insert" how to locate the line to insert this function
        @SpireInsertPatch(
                locator = Locator.class
        )
        // Function that will be executed every time locator finds a line match
        public static void Insert(CardGroup __instance) {
            if(!AbstractDungeon.actionManager.actions.isEmpty())
            {
                logger.info("Other actions still going");

                return;
            }
            logger.info("Hand Reorder Triggered");
            logger.info(isCombatStarting);
//            logger.info(AbstractDungeon.isScreenUp);
//            logger.info(AbstractDungeon.actionManager.actions.isEmpty());
//            logger.info(AbstractDungeon.actionManager.currentAction);
//            logger.info(isEndingTurn);
//            logger.info(getFrontmostEnemy());
            if (!AbstractDungeon.isScreenUp                                 // battle screen is open
                    && AbstractDungeon.actionManager.actions.isEmpty()      // no pending actions
                    && AbstractDungeon.actionManager.currentAction == null  // no current action
                    && !isCombatStarting                                    // not starting combat
                    && !isEndingTurn) {                                     // not ending turn

                // will be null if all enemies are dead
                AbstractMonster target = getFrontmostEnemy();
                if (target == null) {
                    // if the enemies are dead, queue up an end turn
                    AbstractDungeon.actionManager.addToBottom(new PressEndTurnButtonAction());
                } else {
                    boolean foundACard = false;
                    for (AbstractCard cardInHand : AbstractDungeon.player.hand.group) {
                        // find a card that can be played with the current target
                        // Note: this uses each card's specific canUse implementation, some of which don't require a target (like self-targeted skills)
                        if (cardInHand.canUse(AbstractDungeon.player, target)) {
                            foundACard = true;

                            // Select the card, and select the target monster with "hover"
                            AbstractDungeon.player.hoveredCard = cardInHand;
                            ReflectionHacks.setPrivate(AbstractDungeon.player, AbstractPlayer.class, "hoveredMonster", target);
                            if (playCard == null) {
                                // grab a reference to "playCard" from the AbstractPlayer class via reflection
                                playCard = ReflectionHacks.privateMethod(AbstractPlayer.class, "playCard");
                            }

                            // Play the hovered card on the hovered target, then break, since we need to trigger a hand-reorder and start again
                            playCard.invoke(AbstractDungeon.player);
                            break;
                        }
                    }
                    // If we had no playable cards, end the turn
                    if (!foundACard) {
                        AbstractDungeon.actionManager.addToBottom(new PressEndTurnButtonAction());
                    }
                }
            }
        }
        private static ArrayList<AbstractMonster> getEnemies() {
            ArrayList<AbstractMonster> monsters = new ArrayList<>();
            MonsterGroup allMonsters = AbstractDungeon.getMonsters();
            if(allMonsters != null) {
                monsters = allMonsters.monsters;
            }
            monsters.removeIf(AbstractCreature::isDeadOrEscaped);
            return monsters;
        }

        private static AbstractMonster getFrontmostEnemy() {
            AbstractMonster foe = null;
            float bestPos = 10000F;
            for (AbstractMonster m : getEnemies()) {
                if (m.drawX < bestPos) {
                    foe = m;
                    bestPos = m.drawX;
                }
            }
            return foe;
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(AbstractPlayer.class, "relics");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }

    }

}

