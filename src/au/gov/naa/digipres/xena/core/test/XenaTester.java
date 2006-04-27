/*
 * Created on 28/10/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.core.test;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.xml.sax.XMLFilter;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperManager;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperPlugin;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

public class XenaTester {

    
    
    private static final String CLEAN_DESTINATION = "D:\\xena_data\\clean_destination\\";

    /**
     * @param args
     */
    public static void main(String[] args) throws XenaException {
        
        boolean cleanDestinationDir = false;
        
        boolean basicNormalising = true;

        boolean specNormaliser = false;

        boolean doview = false;

        boolean viewAllOutputs = true;

        boolean export = true;
        boolean exportToFileName = false;
        
        boolean listTypes = false;
        boolean listGuesses = false;
        
        boolean testMetaDataWrappers = true;

        
        /*
         * GUI STUFF! Hooray!
         */
        
        
        System.out.println("creating Xena!");
        
        Xena xena = new Xena();
        
        Vector<String> pluginList = new Vector<String>();

        // required for data set.
        //pluginList.add("au/gov/naa/digipres/xena/plugin/basic");
        pluginList.add("au/gov/naa/digipres/xena/plugin/plaintext");
        //pluginList.add("au/gov/naa/digipres/xena/plugin/html");
        //pluginList.add("au/gov/naa/digipres/xena/plugin/dataset");
        pluginList.add("au/gov/naa/digipres/xena/plugin/naa");

        try {
            xena.loadPlugins(pluginList);
        } catch (XenaException xe) {
            xe.printStackTrace();
            return;
        }

        Vector<String> morePlugins = new Vector<String>();
        //morePlugins.add("au/gov/naa/digipres/xena/plugin/image");
        try {
            xena.loadPlugins(morePlugins);
        } catch (XenaException xe) {
            xe.printStackTrace();
            return;
        }

        File imageJar = new File("D:\\workspace\\xena\\dist\\plugins\\image.jar");
        File htmlJar = new File("D:\\workspace\\xena\\dist\\plugins\\html.jar");
        File mailJar = new File("D:\\workspace\\xena\\dist\\plugins\\email.jar");
        File datasetJar = new File("D:\\workspace\\xena\\dist\\plugins\\dataset.jar");
        File officeJar = new File("D:\\workspace\\xena\\dist\\plugins\\office.jar");
        File xmlJar = new File("D:\\workspace\\xena\\dist\\plugins\\xml.jar");
        File csvJar = new File("D:\\workspace\\xena\\dist\\plugins\\csv.jar");
        
        List<File> pluginFiles = new ArrayList<File>();
        pluginFiles.add(imageJar);
        //pluginFiles.add(htmlJar);
        //pluginFiles.add(datasetJar);
        //pluginFiles.add(officeJar);
        //pluginFiles.add(xmlJar);
        pluginFiles.add(csvJar);
        
        for (File plugin : pluginFiles){
            try {
                System.out.println("loading plugin file: " + plugin.getName());
                xena.loadPlugins(plugin);
            } catch (Exception e) {
                System.err.println("Could not load plugin for file:" + plugin.getName());
                e.printStackTrace();
            }
            
        }
        pluginFiles = null;
        
        
        System.out.println("Plugins loaded!");

        System.out.println("-------------------------->>>><<<<<--------------------");
        
        List normalisers = xena.getPluginManager().getNormaliserManager().getAll();
        System.out.println("Here is a list of normalisers...");
        for (Iterator iter = normalisers.iterator(); iter.hasNext();) {
            System.out.println(iter.next().toString());
        }
        System.out.println("-------------------------->>>><<<<<--------------------");
        
//        List batchFilters = xena.getPluginManager().getBatchFilterManager().getFilters();
//        System.out.println("Here is a list of filters...");
//        for (Iterator iter = batchFilters.iterator(); iter.hasNext();) {
//            System.out.println(iter.next().toString());
//        }
//        System.out.println("-------------------------->>>><<<<<--------------------");

        System.out.println("Guessers...");
        for (Iterator iter = xena.getPluginManager().getGuesserManager()
                .getGuessers().iterator(); iter.hasNext();) {
            // System.out.println(iter.next().toString());
            Guesser foo = (Guesser) iter.next();
            System.out.println(foo.getName());
        }
        System.out.println("---------------------------->>>><<<<<--------------------");

        System.out.println("'types'");
        for (Iterator iter = xena.getPluginManager().getTypeManager()
                .allTypes().iterator(); iter.hasNext();) {
            Type foo = (Type) iter.next();
            System.out.println(foo.toString());
        }
        System.out.println("----------------------------->>>><<<<<--------------------");

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
        for (Iterator iter = xena.getPluginManager().getMetaDataWrapperManager().getMetaDataWrapperPlugins().iterator(); iter.hasNext();) {
            MetaDataWrapperPlugin foo = (MetaDataWrapperPlugin) iter.next();
            System.out.println(foo.toString());
        }
        System.out.println("--------------------------->>>><<<<<--------------------");

        System.out.println("and the results of getfilenamer...");
        MetaDataWrapperPlugin myMetaDataPlugin = xena.getPluginManager().getMetaDataWrapperManager().getActiveWrapperPlugin();
        if (myMetaDataPlugin== null) {
            System.out.println("meta data plugin is null. BURN!");
        } else {
            System.out.println("Selected meta data plugin:" + myMetaDataPlugin.toString());
        }
        
        /*
        try {
            myFileNamer.createHistoryFile("d:\\xena_data\\destination\\history.csv");
            myFileNamer.setKeepHistoryFile(true);            
        } catch (IOException e) {
            System.out.println("Could not create history file. burn!");
            myFileNamer.setKeepHistoryFile(false);
        }
        */
        myFileNamer.setOverwrite(true);
        
        
        System.out.println("-------------------------->>>><<<<<--------------------");

        System.out.println("viewers");
        for (Iterator iter = xena.getPluginManager().getViewManager().getAllViews().iterator(); iter.hasNext();) {
            XenaView view = (XenaView) iter.next();
            System.out.println("Viewer name: " + view.toString() +  " and type:" + view.getViewType() + " and finally, the class:" + view.getClass().toString());
        }
        System.out.println("----------------------------->>>><<<<<--------------------");

            
        
        System.out.println(" to stringing the normaliser manager...");
        System.out.println(xena.getPluginManager().getNormaliserManager().toString());

        System.out.println("-------------------------->>>><<<<<--------------------");

        List<File> fileList = new Vector<File>();

        XenaInputSource xis = null;


        fileList.add(new File("D:/xena_data/source/simple.txt"));
        //fileList.add(new File("D:/xena_data/source/foo_simple.csv"));
        fileList.add(new File("D:/xena_data/source/aniagls.gif"));
        //fileList.add(new File("D:/xena_data/source/the_collection.gif"));
        //fileList.add(new File("D:/xena_data/source/image002.gif"));
        //fileList.add(new File("D:/xena_data/source/data2_1.txt"));
        //fileList.add(new File("D:/xena_data/source/simple"));
       
        //fileList.add(new File("D:/xena_data/source/simple.txt"));
        //fileList.add(new File("D:/xena_data/source/the_collection.gif"));
        
        //fileList.add(new File("D:/xena_data/source/untitled.msg"));
        //fileList.add(new File("D:/xena_data/source/aniagls.gif"));
        //fileList.add(new File("D:/xena_data/source/seal.jpg"));
        //fileList.add(new File("D:/xena_data/source/csvfile.txt"));
        //fileList.add(new File("D:/xena_data/source/B6486_PF.csv"));
        //fileList.add(new File("D:/xena_data/source/exceptions.txt"));
        
        //fileList.add(new File("D:/xena_data/bad_data/declan.doc"));
         
        
        
        File destinationDir = new File(CLEAN_DESTINATION);
        
        if (!destinationDir.exists() || !destinationDir.isDirectory()) {
            throw new XenaException("DESTINATION DIR DOESNT EXIST!!!! " + CLEAN_DESTINATION);          
        }
        
        if (cleanDestinationDir) {
            // seeing as it is clean destination, we should clean it out!
            for (File file : destinationDir.listFiles()) {
                file.delete();
            }
        }        
        
        AbstractFileNamer activeFileNamer = xena.getPluginManager().getFileNamerManager().getActiveFileNamer();
        System.out.println("Active filenamer:" + activeFileNamer.toString());

        try {
            xena.setBasePath("D:\\xena_data");
        } catch (XenaException xe) {
            xe.printStackTrace();
        }
        
        if (basicNormalising) {
            for (Iterator iter = fileList.iterator(); iter.hasNext();) {
                File f = (File) iter.next();
                
                System.out.println("----------------------------------------------------");
                System.out.println("----------------------------------------------------");
                System.out.println("Our Filename:" + f.getName());
                try {
                    xis = new XenaInputSource(f);
                } catch (Exception e) {
                    System.out.println("WE HAD A BAD FILE! YOU SUCK!");
                    xis = null;
                    e.printStackTrace();
                }
                if (xis == null) {
                    System.out.println("xis null.");
                } else {
                    System.out.println("We have an xis.");
                    List<Guess> possibleTypes = null;
                    
                    try {
                        possibleTypes = xena.getGuesses(xis);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (possibleTypes == null) {
                        System.out.println("possible types null.");
                        return;
                    }
                    
                    if (listTypes) {
                        System.out.println("-----------------");
                        System.out.println("Listing possible types of our x.i.s.");
                        for (Iterator typesIterator = possibleTypes.iterator(); typesIterator.hasNext(); ){
                            System.out.println(typesIterator.next().toString());
                        }
                        System.out.println("-----------------");
                    }
                    
                    // lets get our first type.
                    Type type = ((Guess)possibleTypes.get(0)).getType();
                    //System.out.println("File is supposedly of type: " + type.toString());
                    
                    // sys out our best guesses....
                    
                    System.out.println("Best guess:" + possibleTypes.get(0).toString());
                    
                    if (listGuesses) { 
                        try {
                            System.out.println("Second Best guess:" + possibleTypes.get(1).toString());
                        } catch (ArrayIndexOutOfBoundsException e){
                            System.out.println("only one guess apparantly..");
                        }
                        try {
                            System.out.println("third Best guess:" + possibleTypes.get(2).toString());
                        } catch (ArrayIndexOutOfBoundsException e){
                            System.out.println("only two guesses apparantly..");
                        }
                    }
                    
                    // lets try and get a normaliser....
                    AbstractNormaliser normaliser = null;
                    try {
                        normaliser = xena.getNormaliser(type);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (normaliser != null) {
                        System.out.println("We have the normaliser for this type:" + normaliser.toString());
                        
                        try {
                            xena.normalise(xis, normaliser, destinationDir);
                            System.out.println("Normalised!");
                        } catch (Exception e) {
                            e.printStackTrace(System.out);
                        }
                        
                    } else {
                        System.out.println("Normaliser was null. No normalising for you. burn.");
                    }
                }
            }
            
            // end for loop//
        }
        
        // DEBUG Purposes...
        // Set foo = normaliserMap.entrySet();
        // System.out.println("HERE ARE OUR NORMALISERS");
        // for (Iterator iter = foo.iterator(); iter.hasNext();) {
        // Map.Entry newEntry= (Map.Entry) iter.next();
        // System.out.println("this is the key:"+newEntry.getKey().toString());
        // System.out.println("and this is the
        // value:"+newEntry.getValue().toString());
        // System.out.println("and the class of the value thingy:" +
        // newEntry.getValue().getClass().toString());
        // System.out.println();
        // }

        
        if (specNormaliser) {
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
            
            try {
                File f = new File("D:/xena_data/source/data2.doc");
                System.out.println("Our Filename:" + f.getName());
                try {
                    xis = new XenaInputSource(f);
                } catch (Exception e) {
                    System.out.println("WE HAD A BAD FILE!!! YOU SUCK!!!");
                    xis = null;
                    e.printStackTrace();
                    return;
                }
                System.out.println("XIS Type:" + xis.getType());
                File binaryDestinationDir = new File("d:\\xena_data\\binary_destination");
                if (!binaryDestinationDir.mkdir()){
                    System.out.println("Folder not created. You suck!");
                    if (binaryDestinationDir.exists() && binaryDestinationDir.isDirectory() ){
                        System.out.println("Oh. you had already created the folder. my bad.");
                    } else {
                        return;
                    }
                } else {
                    System.out.println("We have a destination folder.");
                }
                
                XMLFilter naaWrapper = null;
                
                naaWrapper = xena.getPluginManager().getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
                
                System.out.println(naaWrapper.toString());
                
                System.out.println("Time to normalise");
                System.out.flush();
                if (xis != null) {
                    normaliserResults = xena.normalise(xis, binaryNormaliser, binaryDestinationDir);
                }
                System.out.println("Normalisation done.");
                
            } catch (Exception e) {
                System.out.println("Binary normalisation failed. BURN!");
                e.printStackTrace(System.out);
                System.out.println("whew. Glad that is over.");
            }
            if (normaliserResults != null) {
                System.out.println("Our out file is: " + normaliserResults.getOutputFileName());
            } else {
                System.out.println("normaliser results were null.");
            }
        }
        
        
        if (testMetaDataWrappers) {
            // get our filenamer...
            AbstractFileNamer fileNamer = xena.getPluginManager().getFileNamerManager().getActiveFileNamer();
            fileNamer.setOverwrite(false);
            
            xena.getMetaDataWrappers();
            
            // get a list of all known meta data wrappers, and cycle through them, normalising files as we go :)
            for (MetaDataWrapperPlugin metaDataWrapperPlugin : xena.getMetaDataWrappers()) {
                
                for (Iterator iter = fileList.iterator(); iter.hasNext();) {
                    File f = (File) iter.next();
                    
                    System.out.println("----------------------------------------------------");
                    System.out.println("Our Filename:" + f.getName());
                    try {
                        xis = new XenaInputSource(f);
                    } catch (Exception e) {
                        System.out.println("WE HAD A BAD FILE! YOU SUCK!");
                        xis = null;
                        e.printStackTrace();
                    }
                    if (xis != null) {
                        System.out.println("We have an xis.");
                        List<Guess> possibleTypes = null;
                        
                        try {
                            possibleTypes = xena.getGuesses(xis);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (possibleTypes == null) {
                            System.out.println("possible types null.");
                            return;
                        }
                        
                        
                        try {
                            xena.normalise(xis, new File("d://xena_data//wrapper_test//"), fileNamer, metaDataWrapperPlugin.getWrapper());
                            System.out.println("Normalised with: " + metaDataWrapperPlugin.getName());
                        } catch (Exception e) {
                            e.printStackTrace(System.out);
                        }
                        
                    }
                }
            }
        }
        
        
        
        /*
         * GUI STUFF! Hooray!
         */
        
        if (doview) {
            
            System.out.println("---------------------------------------");
            
            System.out.println("doing stuff with view....");
            
            
            JFrame frame = new JFrame("XenaTester View");
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e){
                    System.exit(0);
                }
            });
            NormalisedObjectViewFactory novf = new NormalisedObjectViewFactory(xena);
            
            //for (int i = 0; i < 1000000000; i++);
            
            
            File viewFile = new File("D:\\xena_data\\destination\\roar.xena");
            
            JPanel view = null;
            try {
                view = novf.getView(viewFile, null);
            } catch (XenaException e){
                e.printStackTrace();
            }
            frame.setBounds(200,250,300,200);
            frame.getContentPane().add(view);
            frame.pack();
            frame.setVisible(true);
        }
     
        
        
        if (viewAllOutputs) {
            
            int edge = 10;
            
            for (File viewFile : new File(CLEAN_DESTINATION).listFiles() ) {
                System.out.println("---------------------------------------");
                System.out.println("doing stuff with view....");
                
                JFrame frame = new JFrame("XenaTester View");
                frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e){
                        System.exit(0);
                    }
                });
                NormalisedObjectViewFactory novf = new NormalisedObjectViewFactory(xena);
                
                JPanel view = null;
                try {
                    view = novf.getView(viewFile, null);
                    
                } catch (XenaException e){
                    e.printStackTrace();
                }
                frame.setBounds(200 + edge, 250, 300 + edge, 200);
                edge = edge + 10;
                
                frame.getContentPane().add(view);
                frame.pack();
                frame.setVisible(true);
            }
            
        }
        
        /*
         * Export stuff.....
         * 
         */
        if (export) {
        
            //let us imagine for a moment, we know our output file names, and they are in a list...
            //String[] exportFileNames = { "export1_txt.xena", "export2_png.xena", "export3_binary.xena"};
            
            String inputDirName = CLEAN_DESTINATION;
            String outputDirName = "D:\\xena_data\\export\\";
            
            File inputDir = new File(inputDirName);
            
            File[] exportFiles = inputDir.listFiles();
            
            for ( int i = 0; i < exportFiles.length; i++) {
                String filename = exportFiles[i].getAbsolutePath();
                System.out.println("Exporting: " + filename);
                try {
                    xena.export(new XenaInputSource(new File(filename)) , new File(outputDirName));
                } catch (XenaException xe) {
                    try {
                        System.out.println("OVERWRITING!!!");
                        xena.export(new XenaInputSource(new File(filename)) , new File(outputDirName), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        if (exportToFileName) {
            
            //let us imagine for a moment, we know our output file names, and they are in a list...
            //String[] exportFileNames = { "export1_txt.xena", "export2_png.xena", "export3_binary.xena"};
            
            String inputDirName = CLEAN_DESTINATION;
            String outputDirName = "D:\\xena_data\\export\\";
            
            File inputDir = new File(inputDirName);
            
            File[] exportFiles = inputDir.listFiles();
            
            for ( int i = 0; i < exportFiles.length; i++) {
                String filename = exportFiles[i].getAbsolutePath();
                System.out.println("Exporting: " + filename);
                try {
                    //xena.getPluginManager().getNormaliserManager().export( new XenaInputSource(new File(filename)) , new File(outputDirName) , false);
                    xena.export(new XenaInputSource(new File(filename)) , new File(outputDirName), "exported_Xena_file___" + i, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
    }

}
