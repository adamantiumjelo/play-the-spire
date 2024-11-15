package playthespire.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import playthespire.patches.AutoPlayPatches;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnableAutoPlayAction extends AbstractGameAction {
    public static final Logger logger = LogManager.getLogger("playthespire"); //Used to output to the console.
    public EnableAutoPlayAction() {
    }

    public void update() {
        logger.info("Inside EnableAutoPlayAction");
        AutoPlayPatches.OnRefreshHandCheckToPlayCardPatch.isCombatStarting = false;
        isDone = true;
    }
}

