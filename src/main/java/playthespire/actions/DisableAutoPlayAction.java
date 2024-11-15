package playthespire.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import playthespire.patches.AutoPlayPatches;

public class DisableAutoPlayAction extends AbstractGameAction {
    public static final Logger logger = LogManager.getLogger("playthespire"); //Used to output to the console.
    public DisableAutoPlayAction() {
    }

    public void update() {
        logger.info("Inside EnableAutoPlayAction");
        AutoPlayPatches.OnRefreshHandCheckToPlayCardPatch.isCombatStarting = true;
        isDone = true;
    }
}
