/**
 * 
 */
package mastodon.entryPoints;

import jam.framework.Application;
import jam.framework.DocumentFrame;
import jam.framework.DocumentFrameFactory;
import jam.framework.MenuBarFactory;
import jam.mac.Utils;

import java.awt.Font;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import ch.randelshofer.quaqua.QuaquaManager;
import figtree.application.Arguments;
import figtree.application.CopyOfFigTreeApplication;
import figtree.application.FigTreeApplication;
import figtree.application.FigTreeFrame;
import figtree.application.FigTreeMenuBarFactory;

/**
 * @author justs
 *
 */
public class MainGUI {
	
	public static FigTreeApplication application;
	
    static public void main(String[] args) {

        Arguments arguments = new Arguments(
                new Arguments.Option[] {
                        new Arguments.StringOption("graphic", new String[] {
                                "PDF", "SVG", "SWF", "PS", "EMF",
		                        // "PNG",
		                        "GIF",
		                        // "JPEG"
                        }, false, "produce a graphic with the given format"),
		                new Arguments.IntegerOption("width", "the width of the graphic in pixels"),
		                new Arguments.IntegerOption("height", "the height of the graphic in pixels"),
                        new Arguments.Option("help", "option to print this message")
                });

        try {
            arguments.parseArguments(args);
        } catch (Arguments.ArgumentException ae) {
            System.out.println();
            System.out.println(ae.getMessage());
            System.out.println();
            FigTreeApplication.printTitle();
            FigTreeApplication.printUsage(arguments);
            System.exit(1);
        }

        if (arguments.hasOption("help")) {
        	FigTreeApplication.printTitle();
        	FigTreeApplication.printUsage(arguments);
            System.exit(0);
        }

        if (arguments.hasOption("graphic")) {

	        int width = 800;
	        int height = 600;

	        if (arguments.hasOption("width")) {
		        width = arguments.getIntegerOption("width");
	        }

	        if (arguments.hasOption("height")) {
		        height = arguments.getIntegerOption("height");
	        }

            // command line version...
            String graphicFormat = arguments.getStringOption("graphic");
            String[] args2 = arguments.getLeftoverArguments();

            if (args2.length == 0) {
                // no tree file specified
            	FigTreeApplication.printTitle();
            	FigTreeApplication.printUsage(arguments);
                System.exit(0);
            } else if (args2.length == 1) {
                // no graphic file specified - write to stdout
            	FigTreeApplication.createGraphic(graphicFormat, width, height, args2[0], (args2.length > 1 ? args2[1] : null));
                System.exit(0);
            } else {
            	FigTreeApplication.printTitle();
            	FigTreeApplication.createGraphic(graphicFormat, width, height, args2[0], (args2.length > 1 ? args2[1] : null));
                System.exit(0);
            }
        }

        boolean lafLoaded = false;

        if (Utils.isMacOSX()) {
            if (Utils.getMacOSXMajorVersionNumber() >= 5) {
                System.setProperty("apple.awt.brushMetalLook","true");
            }

            System.setProperty("apple.laf.useScreenMenuBar","true");
            System.setProperty("apple.awt.draggableWindowBackground","true");
            System.setProperty("apple.awt.showGrowBox","true");
            System.setProperty("apple.awt.graphics.UseQuartz","true");

            // set the Quaqua Look and Feel in the UIManager
            try {
                // Only override the UI's necessary for ColorChooser and
                // FileChooser:
                Set includes = new HashSet();
                includes.add("ColorChooser");
                includes.add("FileChooser");
                includes.add("Component");
                includes.add("Browser");
                includes.add("Tree");
                includes.add("SplitPane");
                includes.add("TitledBorder");

                try {
                    QuaquaManager.setIncludedUIs(includes);
                } catch (java.lang.NoClassDefFoundError ncdfe) {
                    // this is to protect against the figtree.jar being
                    // run on Mac OS without Quaqua on the classpath
                }

                UIManager.setLookAndFeel(
                    "ch.randelshofer.quaqua.QuaquaLookAndFeel"
                );

                lafLoaded = true;
            } catch (Exception e) {
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

        java.net.URL url = FigTreeApplication.class.getResource("images/figtreeLogo.png");
        Icon icon = null;

        if (url != null) {
            icon = new ImageIcon(url);
        }

        final String nameString = "FigTree";
        String aboutString = "<html><center>Tree Figure Drawing Tool<br>Version " + FigTreeApplication.VERSION + "<br>" + FigTreeApplication.DATES + ", Andrew Rambaut<br>" +
                "Institute of Evolutionary Biology, University of Edinburgh.<br><br>" +
                "<a href=\"http://tree.bio.ed.ac.uk/\">http://tree.bio.ed.ac.uk/</a><br><br>" +
                "Uses the Java Evolutionary Biology Library (JEBL)<br>" +
                "<a href=\"http://sourceforge.net/projects/jebl/\">http://jebl.sourceforge.net/</a><br><br>" +
                "Thanks to Alexei Drummond, Joseph Heled, Philippe Lemey, <br>Tulio de Oliveira, Beth Shapiro & Marc Suchard</center></html>";

        String websiteURLString = "http://tree.bio.ed.ac.uk/software/figtree/";
        String helpURLString = "http://tree.bio.ed.ac.uk/software/figtree/";

        application = new FigTreeApplication(new FigTreeMenuBarFactory(), nameString, aboutString, icon,
                websiteURLString, helpURLString);

        
       
        //application.getDocumentFrame(arg0);
        
        application.setDocumentFrameFactory(new DocumentFrameFactory() {
            public DocumentFrame createDocumentFrame(Application app, MenuBarFactory menuBarFactory) {
            	DocumentFrame frame = new FigTreeFrame(nameString + " v" + FigTreeApplication.VERSION);
            	mainFrame = (FigTreeFrame) frame;
                return frame;
            }
        });

        application.initialize();

        if (args.length > 0) {
            for (String arg : args) {
                application.doOpen(arg);
            }
        }

//		if (!jam.mac.Utils.isMacOSX() && application.getUpperDocumentFrame() == null) {
//			// If we haven't opened any files by now, prompt for one...
//			application.doOpen();
//		}

        if (application.getUpperDocumentFrame() == null) {
            // If we haven't opened any files by now, open a blank window...
            application.doNew();
        }
        
    }
    static FigTreeFrame mainFrame;
}
