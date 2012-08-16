/* Copyright (C) 2012 Justs Zarins & Andrew Rambaut
 *
 *This file is part of MASTodon.
 *
 *MASTodon is free software: you can redistribute it and/or modify
 *it under the terms of the GNU Lesser General Public License as
 *published by the Free Software Foundation, either version 3
 *of the License, or (at your option) any later version.
 *
 *MASTodon is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU Lesser General Public License for more details.
 *
 *You should have received a copy of the GNU Lesser General Public License
 *along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package mastodon;

import jam.framework.*;
import jam.mac.*;

import mastodon.util.OSType;

/**
 * @author Justs Zarins
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

