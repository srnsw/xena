/*
 * Created on 28/10/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.core.test;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperPlugin;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.ExportResult;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

public class XenaTester {

    private static final String CLEAN_DESTINATION_NAME = "d:\\xena_data\\clean_destination\\";
    private static final String BINARY_DIR_NAME = "d:\\xena_data\\binary_destination";
    private static final String SIMPLE_FILE_NAME = "d:\\xena_data\\source\\simple.txt";
    private static final String META_DATA_DEST_DIR_NAME = "d:\\xena_data\\wrapper_test\\";
    private static final String VIEW_FILE_NAME = "d:\\xena_data\\destination\\roar.xena";
    private static final String EXPORT_OUTPUT_DIR_NAME = "d:\\xena_data\\export\\";
    private static final String BASE_PATH_NAME = "d:\\xena_data\\source";

    /**
     * @param args
     */
    public static void main(String[] args) {
        XenaTester xenaTester = new XenaTester();
        try {
            xenaTester.runTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Test Flags.
    private boolean showPluginDetails = false;
    private boolean cleanDestinationDir = true;
    private boolean basicNormalising = true;
    private boolean specifyBinaryNormaliser = false;
    private boolean specOfficeNormaliser = false;
    private boolean doview = false;
    private boolean viewAllOutputs = false;
    private boolean export = true;
    private boolean exportToFileName = false;
    private boolean listTypes = false;
    private boolean listGuesses = false;
    private boolean testMetaDataWrappers = false;
    
    //Initialise our test file list, then use static init to add some stuff.
    private List<File> fileList = new Vector<File>();
    {
        fileList.add(new File("D:/xena_data/source/simple.txt"));
        //fileList.add(new File("D:/xena_data/source/foo_simple.csv"));
        //fileList.add(new File("D:/xena_data/source/aniagls.gif"));
        // fileList.add(new File("D:/xena_data/source/the_collection.gif"));
        // fileList.add(new File("D:/xena_data/source/image002.gif"));
        //fileList.add(new File("D:/xena_data/source/test1.doc"));
        // fileList.add(new File("D:/xena_data/source/simple"));
        // fileList.add(new File("D:/xena_data/source/simple.txt"));
        // fileList.add(new File("D:/xena_data/source/the_collection.gif"));
        // fileList.add(new File("D:/xena_data/source/untitled.msg"));
        // fileList.add(new File("D:/xena_data/source/aniagls.gif"));
        // fileList.add(new File("D:/xena_data/source/seal.jpg"));
        // fileList.add(new File("D:/xena_data/source/csvfile.txt"));
        // fileList.add(new File("D:/xena_data/source/B6486_PF.csv"));
        // fileList.add(new File("D:/xena_data/source/exceptions.txt"));
        // fileList.add(new File("D:/xena_data/bad_data/declan.doc"));
        fileList.add(new File("D:/xena_data/source/jet.JPG"));
    }
    
    // Initialise our first plugin list, by name, and add some entries. These must plugins already be on the class path.
    private Vector<String> pluginList = new Vector<String>();
    {
        pluginList.add("au/gov/naa/digipres/xena/plugin/plaintext");
        // pluginList.add("au/gov/naa/digipres/xena/plugin/html");
        // pluginList.add("au/gov/naa/digipres/xena/plugin/naa");
    }
    
    // Initialise our second list of plugins, and add some if we want...
    private Vector<String> morePlugins = new Vector<String>();
    {
        // pluginList.add("au/gov/naa/digipres/xena/plugin/naa");
    }

    // Create File object for various plugins
    private File imageJar = new File("D:\\workspace\\xena\\dist\\plugins\\image.jar");
    private File htmlJar = new File("D:\\workspace\\xena\\dist\\plugins\\html.jar");
    private File mailJar = new File("D:\\workspace\\xena\\dist\\plugins\\email.jar");
    private File datasetJar = new File("D:\\workspace\\xena\\dist\\plugins\\dataset.jar");
    private File officeJar = new File("D:\\workspace\\xena\\dist\\plugins\\office.jar");
    private File xmlJar = new File("D:\\workspace\\xena\\dist\\plugins\\xml.jar");
    private File csvJar = new File("D:\\workspace\\xena\\dist\\plugins\\csv.jar");

    // Initialise plugins file list, add some of our files
    private List<File> pluginFiles = new ArrayList<File>();
    {
        pluginFiles.add(imageJar);
        // pluginFiles.add(htmlJar);
        // pluginFiles.add(datasetJar);
        // pluginFiles.add(officeJar);
        // pluginFiles.add(xmlJar);
        // pluginFiles.add(csvJar);
    }
    
    // Our very own Xena object!!!
    private Xena xena;

    public XenaTester() {
        System.out.println("creating Xena!");
        xena = new Xena();
    }

    public void runTest() {
        System.out.print("starting tests.");
        try {
            loadPlugins();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (showPluginDetails) {
            showPluginDetails();
        }

        File destinationDir = new File(CLEAN_DESTINATION_NAME);
        if (!destinationDir.exists() || !destinationDir.isDirectory()) {
            System.out.println("DESTINATION DIR DOESNT EXIST!!!! " + CLEAN_DESTINATION_NAME);
            return;
        }
        if (cleanDestinationDir) {
            // seeing as it is clean destination, we should clean it out!
            for (File file : destinationDir.listFiles()) {
                file.delete();
            }
        }

        try {
            xena.setBasePath(BASE_PATH_NAME);
        } catch (XenaException xe) {
            System.out.println("Couldnt set base path.");
            xe.printStackTrace();
            return;
        }

        if (basicNormalising) {
            try {
                doBasicNormalising(fileList, destinationDir);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        if (specifyBinaryNormaliser) {
            try {
                doBinaryNormalisation();
            } catch (Exception e) {
                System.out.println("Binary normalisation failed. BURN!");
                e.printStackTrace(System.out);
                System.out.println("whew. Glad that is over.");
                return;
            }
        }

        if (specOfficeNormaliser) {
            try {
                doOfficeNormalising();
            } catch (Exception e) {
                System.out.println("Office normalisation failed. Nooooo Good.");
                e.printStackTrace(System.out);
                return;
            }
        }

        if (testMetaDataWrappers) {
            try {
                testMetaDataWrappers();
            } catch (Exception e) {
                System.out.println("EXCEPTIONED'D!!!");
                e.printStackTrace(System.out);
                return;
            }
        }

        if (doview) {
            try {
                doView();
            } catch (Exception e) {
                System.out.println("EXCEPTIONED'D!!!");
                e.printStackTrace(System.out);
                return;
            }
        }

        if (viewAllOutputs) {
            try {
                viewAllOutputs();
            } catch (Exception e) {
                System.out.println("EXCEPTIONED'D!!!");
                e.printStackTrace(System.out);
                return;
            }
        }

        if (export) {
            try {
                doExport();
            } catch (Exception e) {
                System.out.println("exxxxxxxxPORT FAILURE!");
                e.printStackTrace();
                return;
            }
        }

        if (exportToFileName) {
            try {
                exportToFileName();
            } catch (Exception e) {
                System.out.println("EXCEPTIONED'D!!!");
                e.printStackTrace(System.out);
                return;
            }
        }
    }
    
    
    /**
     * Load plugins for xena.
     * @throws XenaExceptio - if error loading plugins for some reason.
     * @throws IOException - if plugin files cant be loaded
     */

    private void loadPlugins() throws XenaException, IOException {
        xena.loadPlugins(pluginList);

        xena.loadPlugins(morePlugins);

        for (File plugin : pluginFiles) {
            xena.loadPlugins(plugin);
        }
        pluginFiles = null;
        System.out.println("Plugins loaded!");
        System.out.println("-------------------------->>>><<<<<--------------------");
    }

    /**
     * Show plugin details for loaded plugins.
     */
    private void showPluginDetails() {
        List normalisers = xena.getPluginManager().getNormaliserManager().getAll();
        System.out.println("Here is a list of normalisers...");
        for (Iterator iter = normalisers.iterator(); iter.hasNext();) {
            System.out.println(iter.next().toString());
        }
        System.out.println("-------------------------->>>><<<<<--------------------");

        // List batchFilters =
        // xena.getPluginManager().getBatchFilterManager().getFilters();
        // System.out.println("Here is a list of filters...");
        // for (Iterator iter = batchFilters.iterator(); iter.hasNext();) {
        // System.out.println(iter.next().toString());
        // }
        // System.out.println("-------------------------->>>><<<<<--------------------");

        System.out.println("Guessers...");
        for (Iterator iter = xena.getPluginManager().getGuesserManager().getGuessers().iterator(); iter.hasNext();) {
            // System.out.println(iter.next().toString());
            Guesser foo = (Guesser) iter.next();
            System.out.println(foo.getName());
        }
        System.out
                .println("---------------------------->>>><<<<<--------------------");

        System.out.println("'types'");
        for (Iterator iter = xena.getPluginManager().getTypeManager().allTypes().iterator(); iter.hasNext();) {
            Type foo = (Type) iter.next();
            System.out.println(foo.toString());
        }
        System.out
                .println("----------------------------->>>><<<<<--------------------");

        System.out.println("List of filenames....");
        for (Iterator iter = xena.getPluginManager().getFileNamerManager().getFileNamers().iterator(); iter.hasNext();) {
            AbstractFileNamer foo = (AbstractFileNamer) iter.next();
            System.out.println(foo.toString());
        }
        System.out.println("--------------------------->>>><<<<<--------------------");

        System.out.println("and the results of getfilenamer...");
        AbstractFileNamer myFileNamer = xena.getPluginManager().getFileNamerManager().getActiveFileNamer();
        if (myFileNamer == null) {
            System.out.println("filenamer is null. BURN!");
        } else {
            System.out.println("Selected filenamer:" + myFileNamer.toString());
        }

        System.out.println("----------------------------->>>><<<<<--------------------");

        System.out.println("List of meta data wrappers....");
        List<MetaDataWrapperPlugin> metaDataWrapperPlugins = xena.getPluginManager().getMetaDataWrapperManager().getMetaDataWrapperPlugins();
        for (Iterator<MetaDataWrapperPlugin> iter = metaDataWrapperPlugins.iterator(); iter.hasNext();) {
            MetaDataWrapperPlugin foo = iter.next();
            System.out.println(foo.toString());
        }
        System.out.println("--------------------------->>>><<<<<--------------------");

        System.out.println("and the results of getfilenamer...");
        MetaDataWrapperPlugin myMetaDataPlugin = xena.getPluginManager().getMetaDataWrapperManager().getActiveWrapperPlugin();
        if (myMetaDataPlugin == null) {
            System.out.println("meta data plugin is null. BURN!");
        } else {
            System.out.println("Selected meta data plugin:" + myMetaDataPlugin.toString());
        }

        System.out.println("-------------------------->>>><<<<<--------------------");

        System.out.println("viewers");
        for (Iterator iter = xena.getPluginManager().getViewManager().getAllViews().iterator(); iter.hasNext();) {
            XenaView view = (XenaView) iter.next();
            System.out.println("Viewer name: " + view.toString() + 
                               " and type:" + view.getViewType() + 
                               " and finally, the class:" + view.getClass().toString());
        }
        System.out.println("----------------------------->>>><<<<<--------------------");

        System.out.println(" to stringing the normaliser manager...");
        System.out.println(xena.getPluginManager().getNormaliserManager().toString());

        System.out.println("-------------------------->>>><<<<<--------------------");
        
        AbstractFileNamer activeFileNamer = xena.getPluginManager().getFileNamerManager().getActiveFileNamer();
        System.out.println("Active filenamer:" + activeFileNamer.toString());

    }
    
    /**
     * Do basic normalising - guess the files, and use whichever wrapper / filename is active,
     * normalise to provided dest dir.
     * @param fileList - list of files to normalise
     * @param destinationDir - place for output
     * @throws XenaException - Thrown in the case of problems guessing or normalising.
     * @throws IOException - thrown if xis cant be created or error with dest dir.
     */

    private void doBasicNormalising(List<File> fileList, File destinationDir) 
        throws XenaException, FileNotFoundException, IOException {

        XenaInputSource xis;
        for (Iterator iter = fileList.iterator(); iter.hasNext();) {
            File f = (File) iter.next();

            System.out.println("----------------------------------------------------");
            System.out.println("----------------------------------------------------");
            System.out.println("Our Filename:" + f.getName());

            xis = new XenaInputSource(f);

            System.out.println("We have an xis.");
            List<Guess> possibleTypes = null;

            possibleTypes = xena.getGuesses(xis);

            if (possibleTypes == null) {
                System.out.println("possible types null.");
            }

            if (listTypes) {
                System.out.println("-----------------");
                System.out.println("Listing possible types of our x.i.s.");
                for (Iterator typesIterator = possibleTypes.iterator(); typesIterator.hasNext();) {
                    System.out.println(typesIterator.next().toString());
                }
                System.out.println("-----------------");
            }

            // lets get our first type.
            Type type = possibleTypes.get(0).getType();
            xis.setType(type);

            // sys out our best guesses....
            System.out.println("Best guess:" + possibleTypes.get(0).toString());

            if (listGuesses) {
                try {
                    System.out.println("Second Best guess:" + possibleTypes.get(1).toString());
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("only one guess apparantly..");
                }
                try {
                    System.out.println("third Best guess:" + possibleTypes.get(2).toString());
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("only two guesses apparantly..");
                }
            }

            // lets try and get a normaliser....
            AbstractNormaliser normaliser = null;
            normaliser = xena.getNormaliser(type);
            if (normaliser != null) {
                System.out.println("We have the normaliser for this type:"  + normaliser.toString());
                xena.normalise(xis, normaliser, destinationDir);
                System.out.println("Normalised!");
            } else {
                System.out .println("Normaliser was null. No normalising for you! Burn.");
            }
        }
    }
    
    /**
     * Binary normalise some stuff.
     * @throws XenaException -  thrown in the case of problems with normalisation or error getting wrapper 
     * @throws IOException - thrown if xis cant be created or dest dir is broken
     */

    private void doBinaryNormalisation() throws XenaException, IOException {
        XenaInputSource xis;
        // so now... get a 'binary' normaliser....
        // okay, now try to get a binrary normaliser.

        System.out.println("---------------------------------------");
        System.out.println("Time to attempt binary normalisation...");

        AbstractFileNamer fileNamer = xena.getPluginManager().getFileNamerManager().getActiveFileNamer();
        fileNamer.setOverwrite(true);

        Map<String, AbstractNormaliser> normaliserMap = xena.getPluginManager().getNormaliserManager().getNormaliserMap();
        // now... try to normalise with the binary normaliser. this could be
        // exciting!!!
        NormaliserResults normaliserResults = null;
        AbstractNormaliser binaryNormaliser = normaliserMap.get("Binary");
        System.out.println("binaryNormaliser info-> name: " + binaryNormaliser.getName() + " class: " + binaryNormaliser.getClass());

        File f = new File(SIMPLE_FILE_NAME);
        System.out.println("Our Filename:" + f.getName());

        xis = new XenaInputSource(f);

        System.out.println("XIS Type:" + xis.getType());
        File binaryDestinationDir = new File(BINARY_DIR_NAME);
        if (binaryDestinationDir.mkdir()) {
            System.out.println("We have a destination folder.");
        } else {
            System.out.println("Folder not created. You suck!");
            if (binaryDestinationDir.exists() && binaryDestinationDir.isDirectory()) {
                System.out.println("Oh. you had already created the folder. my bad.");
            } else {
                throw new IOException("Binary destination broken");
            }
        }

        AbstractMetaDataWrapper myWrapper = null;
        myWrapper = xena.getPluginManager().getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();

        System.out.println(myWrapper.toString());
        System.out.println("Time to normalise");
        System.out.flush();
        if (xis != null) {
            normaliserResults = xena.normalise(xis, binaryNormaliser, binaryDestinationDir);
        }
        System.out.println("Normalisation done.");

        if (normaliserResults != null) {
            System.out.println("Our out file is: " + normaliserResults.getOutputFileName());
        } else {
            System.out.println("normaliser results were null.");
        }
    }
    
    /**
     * Normalise something with the office normaliser.
     * @throws IOException - thrown in the case of unable to create XIS, or cant get destination dir.
     * @throws XenaException - thrown if we cant get the wrapper, set the xis type, or have problems normalising.
     */
    private void doOfficeNormalising() throws IOException, XenaException {
        XenaInputSource xis;
        // so now... get a 'binary' normaliser....
        // okay, now try to get a binrary normaliser.

        System.out.println("---------------------------------------");
        System.out.println("Time to attempt office normalisation...");

        AbstractFileNamer fileNamer = xena.getPluginManager().getFileNamerManager().getActiveFileNamer();
        fileNamer.setOverwrite(true);

        Map<String, AbstractNormaliser> normaliserMap = xena.getPluginManager().getNormaliserManager().getNormaliserMap();
        // now... try to normalise with the binary normaliser. this could be
        // exciting!!!
        NormaliserResults normaliserResults = null;
        AbstractNormaliser officeNormaliser = normaliserMap.get("Office");
        System.out.println("officeNormaliser info-> name: " + officeNormaliser.getName() + " class: " + officeNormaliser.getClass());

        File f = new File(SIMPLE_FILE_NAME);
        System.out.println("Our Filename:" + f.getName());

        xis = new XenaInputSource(f);
        System.out.println("XIS Type:" + xis.getType());
        File binaryDestinationDir = new File(
        "d:\\xena_data\\binary_destination");
        if (binaryDestinationDir.mkdir()) {
            System.out.println("We have a destination folder.");
        } else {
            System.out.println("Folder not created. You suck!");
            if (binaryDestinationDir.exists()
                    && binaryDestinationDir.isDirectory()) {
                System.out.println("Oh. you had already created the folder. my bad.");
            } else {
                throw new IOException();
            }
        }
        
        AbstractMetaDataWrapper myWrapper = null;
        myWrapper = xena.getPluginManager().getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();

        System.out.println(myWrapper.toString());
        System.out.println("Time to normalise");
        System.out.flush();
        if (xis != null) {
            xis.setType(xena.getPluginManager().getTypeManager().lookup("Word Processor"));
            normaliserResults = xena.normalise(xis, officeNormaliser, binaryDestinationDir);
        }
        System.out.println("Normalisation done.");

        if (normaliserResults != null) {
            System.out.println("Our out file is: " + normaliserResults.getOutputFileName());
        } else {
            System.out.println("normaliser results were null.");
        }
    }
    
    /**
     * Test the meta data wrappers by normalising an object with each of the different wrappers.
     * @throws XenaException - thrown if we cant guess, normalise, or get a wrapper
     * @throws IOException - thrown if we fail to create the XIS, or have problem guessing
     */
    private void testMetaDataWrappers() throws XenaException, IOException{
        XenaInputSource xis;
        // get our filenamer...
        AbstractFileNamer fileNamer = xena.getPluginManager().getFileNamerManager().getActiveFileNamer();
        fileNamer.setOverwrite(false);

        xena.getMetaDataWrappers();

        // get a list of all known meta data wrappers, and cycle through
        // them, normalising files as we go :)
        for (MetaDataWrapperPlugin metaDataWrapperPlugin : xena.getMetaDataWrappers()) {

            for (Iterator iter = fileList.iterator(); iter.hasNext();) {
                File f = (File) iter.next();

                System.out.println("----------------------------------------------------");
                System.out.println("Our Filename:" + f.getName());
                xis = new XenaInputSource(f);
                System.out.println("We have an xis.");
                List<Guess> possibleTypes = null;

                possibleTypes = xena.getGuesses(xis);
                if (possibleTypes == null) {
                    System.out.println("possible types null.");
                    return;
                }

                xena.normalise(xis, new File(META_DATA_DEST_DIR_NAME), fileNamer, metaDataWrapperPlugin.getWrapper());
                System.out.println("Normalised with: " + metaDataWrapperPlugin.getName());

            }
        }
    }
    
    /**
     * Show view for file named in the VIEW_FILE_NAME constant.
     * @throws XenaException - if view cannot be found for this file.
     */

    private void doView() throws XenaException {
        System.out.println("---------------------------------------");

        System.out.println("doing stuff with view....");

        JFrame frame = new JFrame("XenaTester View");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        NormalisedObjectViewFactory novf = new NormalisedObjectViewFactory(
                xena);

        // for (int i = 0; i < 1000000000; i++);

        File viewFile = new File(VIEW_FILE_NAME);

        JPanel view = null;
        
        view = novf.getView(viewFile, null);
        
        frame.setBounds(200, 250, 300, 200);
        frame.getContentPane().add(view);
        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * Show jframes with a view for all the objects in the clean destination dir.
     * @throws XenaException - if we cant get a view for one of the files.
     */

    private void viewAllOutputs() throws XenaException {
        int edge = 10;

        for (File viewFile : new File(CLEAN_DESTINATION_NAME).listFiles()) {
            System.out.println("---------------------------------------");
            System.out.println("doing stuff with view....");

            JFrame frame = new JFrame("XenaTester View");
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            NormalisedObjectViewFactory novf = new NormalisedObjectViewFactory(xena);

            JPanel view = null;
            
            view = novf.getView(viewFile, null);

            frame.setBounds(200 + edge, 250, 300 + edge, 200);
            edge = edge + 10;

            frame.getContentPane().add(view);
            frame.pack();
            frame.setVisible(true);
        }
    }
    

    /**
     * Do Export. Attempt without overwrite first.
     * @throws FileNotFoundException - cant create a xena input source
     * @throws XenaException - cant do export for some reason.
     */
    private void doExport() throws FileNotFoundException, XenaException {
        // let us imagine for a moment, we know our output file names, and
        // they are in a list...
        // String[] exportFileNames = { "export1_txt.xena",
        // "export2_png.xena", "export3_binary.xena"};

        String inputDirName = CLEAN_DESTINATION_NAME;

        File inputDir = new File(inputDirName);

        File[] exportFiles = inputDir.listFiles();

        for (int i = 0; i < exportFiles.length; i++) {
            String filename = exportFiles[i].getAbsolutePath();
            System.out.println("Exporting: " + filename);
            try {
                ExportResult er = xena.export(new XenaInputSource(new File(filename)), new File(EXPORT_OUTPUT_DIR_NAME));
                System.out.println(er.toString());
            } catch (XenaException xe) {
                System.out.println("Xena exception thrown, attempting overwrite!!!");
                ExportResult er = xena.export(new XenaInputSource(new File(filename)), new File(EXPORT_OUTPUT_DIR_NAME),true);
                System.out.println(er.toString());
            }
        }
    }

    
    /**
     * Export to given file name.
     * @throws XenaException - if error exporting
     * @throws FileNotFoundException - if xis cant be created for some reason.
     */
    private void exportToFileName() throws XenaException, FileNotFoundException {
        String inputDirName = CLEAN_DESTINATION_NAME;
        String outputDirName = EXPORT_OUTPUT_DIR_NAME;

        File inputDir = new File(inputDirName);

        File[] exportFiles = inputDir.listFiles();

        for (int i = 0; i < exportFiles.length; i++) {
            String filename = exportFiles[i].getAbsolutePath();
            System.out.println("Exporting: " + filename);
                xena.export(new XenaInputSource(new File(filename)), new File(outputDirName), "exported_Xena_file___" + i, true);
            
        }
    }

    
}
