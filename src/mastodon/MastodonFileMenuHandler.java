package mastodon;

import javax.swing.*;

/**
 * @author Just Zarins
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface MastodonFileMenuHandler {

	Action getExportDataAction();

	Action getExportPDFAction();
	
	Action getExportGraphicAction();

}