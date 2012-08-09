package mastodon;

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
            //might need to implement a Mac version of this menu later
            registerMenuFactory(new MastodonDefaultPruneMenuFactory());
            //registerMenuFactory(new DefaultEditMenuFactory());
            registerMenuFactory(new MacWindowMenuFactory());
            registerMenuFactory(new MacHelpMenuFactory());
        } else {
            registerMenuFactory(new MastodonDefaultFileMenuFactory());
            registerMenuFactory(new MastodonDefaultPruneMenuFactory());
            //registerMenuFactory(new DefaultEditMenuFactory());
            registerMenuFactory(new DefaultHelpMenuFactory());
            
        }

    }
}

