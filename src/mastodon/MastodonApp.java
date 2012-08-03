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
            final String versionString = "v1.0pre";
            String aboutString = "<html><center><p>MCMC Trace Analysis Tool<br>" +
                    "Version " + versionString + ", 2003-2012</p>" +
                    "<p>by<br>" +

                    "Andrew Rambaut, Trevor Bedford & Marc A. Suchard</p>" +

                    "<p>Institute of Evolutionary Biology, University of Edinburgh<br>" +
                    "<a href=\"mailto:a.rambaut@ed.ac.uk\">a.rambaut@ed.ac.uk</a></p>" +

                    "<p>Departments of Biomathematics, Biostatistics and Human Genetics, UCLA<br>" +
                    "<a href=\"mailto:msuchard@ucla.edu\">msuchard@ucla.edu</a></p>" +

                    "<p>Available from the BEAST site:<br>" +
                    "<a href=\"http://beast.bio.ed.ac.uk/\">http://beast.bio.ed.ac.uk/</a></p>" +
                    "<p>Source code distributed under the GNU LGPL:<br>" +
                    "<a href=\"http://code.google.com/p/beast-mcmc/\">http://code.google.com/p/beast-mcmc/</a></p>" +
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

            if (args.length > 0) {
                MastodonFrame frame = (MastodonFrame) app.getDefaultFrame();
                for (String fileName : args) {
                    File file = new File(fileName);
                    app.doOpenFile(file);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "Fatal exception: " + e,
                    "Please report this to the authors",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}