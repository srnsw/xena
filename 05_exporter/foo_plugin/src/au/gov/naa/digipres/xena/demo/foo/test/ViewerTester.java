/*
 * Created on 15/03/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.foo.test;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

public class ViewerTester {

    public static void main(String[] argv) {
        //create a Xena object
        Xena xena = new Xena();

        //load the foo plugin; our foo jar will already be on the class path, so we can load it by name.
        try {
            xena.loadPlugin("au/gov/naa/digipres/xena/demo/foo");
        } catch (XenaException xe) {
            xe.printStackTrace();
            return;
        }

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
        File viewFile = new File("example_file2.foo_Foo.xena");

        //get our view
        JPanel view = null;
        try {
            view = novf.getView(viewFile, null);
        } catch (XenaException e){
            e.printStackTrace();
        }

        //add it to our frame and display it!
        frame.setBounds(200,250,300,200);
        frame.getContentPane().add(view);
        frame.pack();
        frame.setVisible(true);
    }
}