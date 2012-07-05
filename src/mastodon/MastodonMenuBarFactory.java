package mastodon;

import mastodon.PruningMenuFactory;
import jam.framework.*;
import jam.mac.*;

import mastodon.util.OSType;

/**
 * @author Just Zarins
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MastodonMenuBarFactory extends DefaultMenuBarFactory {

    public MastodonMenuBarFactory() {
        if (OSType.isMac()) {
            registerMenuFactory(new MastodonMacFileMenuFactory());
            registerMenuFactory(new PruningMenuFactory());
            registerMenuFactory(new DefaultEditMenuFactory());
            registerMenuFactory(new MacWindowMenuFactory());
            registerMenuFactory(new MacHelpMenuFactory());
        } else {
            registerMenuFactory(new MastodonDefaultFileMenuFactory());
            registerMenuFactory(new PruningMenuFactory());
            registerMenuFactory(new DefaultEditMenuFactory());
            registerMenuFactory(new DefaultHelpMenuFactory());
            
        }

    }
}

