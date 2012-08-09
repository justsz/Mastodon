package mastodon;

import javax.swing.*;

/**
 * @author justs
 *
 */
public interface MastodonPruneMenuHandler {
	Action getAlgorithmAction();
	Action getRemoveRunAction();
	Action getManualPruneAction();
	Action getUndoAction();
	Action getRedoAction();
	Action getCommitAction();
}
