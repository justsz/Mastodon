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

import mastodon.util.OSType;
import jam.framework.*;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.util.Locale;

/**
 * This is the main entry point to the MASTadon graphical application.
 * @author Justs Zarins
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MastodonApp extends MultiDocApplication {

    public MastodonApp(String nameString, String aboutString, Icon icon,
                     String websiteURLString, String helpURLString) {
        super(new MastodonMenuBarFactory(), nameString, aboutString, icon, websiteURLString, helpURLString);

        addPreferencesSection(new GeneralPreferencesSection());
    }
    
    public DocumentFrame doOpenFile(File file) {
        DocumentFrame documentFrame = getUpperDocumentFrame();
        if (documentFrame != null && documentFrame.getFile() == null) {
            documentFrame.openFile(file);
            return documentFrame;
        } else {
            return super.doOpenFile(file);
        }
    }

    // Main entry point
    static public void main(String[] args) {
        // There is a major issue with languages that use the comma as a decimal separator.
        // To ensure compatibility between programs in the package, enforce the US locale.
        Locale.setDefault(Locale.US);

        boolean lafLoaded = false;

        if (OSType.isMac()) {
            System.setProperty("apple.awt.graphics.UseQuartz", "true");
            System.setProperty("apple.awt.antialiasing","true");
            System.setProperty("apple.awt.rendering","VALUE_RENDER_QUALITY");

            System.setProperty("apple.laf.useScreenMenuBar","true");
            System.setProperty("apple.awt.draggableWindowBackground","true");
            System.setProperty("apple.awt.showGrowBox","true");

            // set the Quaqua Look and Feel in the UIManager
            try {
                UIManager.setLookAndFeel(
                        "ch.randelshofer.quaqua.QuaquaLookAndFeel"
                );
                lafLoaded = true;


            } catch (Exception e) {
                //
            }

            UIManager.put("SystemFont", new Font("Lucida Grande", Font.PLAIN, 13));
            UIManager.put("SmallSystemFont", new Font("Lucida Grande", Font.PLAIN, 11));
        }

        if (!lafLoaded) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        try {
            java.net.URL url = MastodonApp.class.getResource("images/mastodon.png");
            Icon icon = null;

            if (url != null) {
                icon = new ImageIcon(url);
            }

            final String nameString = "MASTodon";
            final String versionString = "v0.3";
            String aboutString = "<html><center><p>Common subtree search tool<br>" +
            		"Made as part of Google SoC 2012.<br>" +
                    "Version " + versionString + ", 2012</p>" +
                    "<p>by<br>" +

                    "Justs Zarins<br>" +
                    "with supervision from<br>" +
                    "Andrew Rambaut, Karen Cranston and Benjamin Redelings<br>" +
                    "</p>" +

                    "<p>Mentoring organization: National Evolutionary Synthesis Center, NC<br>" +
                    "<a href=\"http://informatics.nescent.org/wiki/Main_Page\">Informatics at NESCent</a></p>" +
                    
                    "<p>Project wiki:<br>" +
                    "<a href=\"http://informatics.nescent.org/wiki/PhyloSoC:_Summary_and_visualization_of_phylogenetic_tree_sets\">informatics.nescent.org/wiki/PhyloSoC:_Summary_and_visualization_of_phylogenetic_tree_sets</a></p>" +
                    
					"<p>MASTodon is distributed under GNU LGPL. It comes WITHOUT WARRANTY.<br>" +
					"<a href=\"http://www.gnu.org/licenses/\">http://www.gnu.org/licenses/</a></p>" +
					
					"<p>Uses FreeHEP:<br>" +
					"<a href=\"http://www.freehep.org/\">http://www.freehep.org/</a></p>" +

                    "<p>Source and application available from GitHub site: <br>" +
                    "<a href=\"https://github.com/justsz/Mastodon/downloads\">https://github.com/justsz/Mastodon/downloads</a></p>" +
                    "</center></html>";

            String websiteURLString = "http://informatics.nescent.org/wiki/PhyloSoC:_Summary_and_visualization_of_phylogenetic_tree_sets";
            String helpURLString = "https://github.com/justsz/Mastodon/downloads";

            MastodonApp app = new MastodonApp(nameString, aboutString, icon, websiteURLString, helpURLString);
            app.setDocumentFrameFactory(new DocumentFrameFactory() {
                public DocumentFrame createDocumentFrame(Application app, MenuBarFactory menuBarFactory) {
                	return new MastodonFrame("MASTodon");
                }
            });
            app.initialize();

            app.doNew();

            //not accepting any arguments for now to simplify things
//            if (args.length > 0) {
//                MastodonFrame frame = (MastodonFrame) app.getDefaultFrame();
//                for (String fileName : args) {
//                    File file = new File(fileName);
//                    app.doOpenFile(file);
//                }
//            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "Fatal exception: " + e,
                    "Please report this to the authors",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}