/*
 * Created on 15/03/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx.test;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.demo.orgx.DemoInfoProvider;
import au.gov.naa.digipres.xena.demo.orgx.OrgXFileNamer;
import au.gov.naa.digipres.xena.demo.orgx.OrgXMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperPlugin;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

public class ViewerTester {

    public static void main(String[] argv) {
        //create a Xena object
        Xena xena = new Xena();

        //load the foo plugin; our foo jar will already be on the class path, so we can load it by name.
        try {
            xena.loadPlugin("au/gov/naa/digipres/xena/demo/foo");
            xena.loadPlugin("au/gov/naa/digipres/xena/demo/orgx");
        } catch (XenaException xe) {
            xe.printStackTrace();
            return;
        }

        //normalise something first up...
        
        NormaliserResults normaliserResults;
        
        MetaDataWrapperPlugin activeWrapper = xena.getActiveMetaDataWrapperPlugin();
        System.out.println(activeWrapper.getName());
        if (activeWrapper.getName().equals("orgxFilter")) {
            try {
                OrgXMetaDataWrapper orgxMetaDataWrapper = (OrgXMetaDataWrapper)activeWrapper.getWrapper();
                orgxMetaDataWrapper.setMyInfoProvider(new DemoInfoProvider());
                System.out.println("demo info provider set.");
            } catch (XenaException xe) {
                xe.printStackTrace();
                return;
            }
        }
        
        AbstractFileNamer activeFileNamer = xena.getActiveFileNamer();
        System.out.println(activeFileNamer.getName());
        if (activeFileNamer instanceof OrgXFileNamer) {
                OrgXFileNamer orgXFileNamer = (OrgXFileNamer)activeFileNamer;
                orgXFileNamer.setMyInfoProvider(new DemoInfoProvider());
        }
        
        
        try {
            File sampleFile = new File("example_file1.foo");
            XenaInputSource xis = new XenaInputSource(sampleFile);
            normaliserResults = xena.normalise(xis);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        System.out.println(normaliserResults.toString());
        
        String outputFileName = normaliserResults.getOutputFileName();
        
        //create the view factory
        NormalisedObjectViewFactory novf = new NormalisedObjectViewFactory(xena);
        
        //create our frame
            JFrame frame = new JFrame("XenaTester View");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });
        
        
        
        //create our view file
        File viewFile = new File(outputFileName);

        //get our view
        JPanel view = null;
        try {
            view = novf.getView(viewFile, null);
        } catch (XenaException e){
            e.printStackTrace();
            return;
        }

        //add it to our frame and display it!
        frame.setBounds(200,250,300,200);
        frame.getContentPane().add(view);
        frame.pack();
        frame.setVisible(true);
    }
}