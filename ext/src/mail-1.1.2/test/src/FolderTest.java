import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.FolderEvent;
import javax.mail.event.FolderListener;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FolderTest
  extends TestCase
  implements ConnectionListener
{
	
  private Session session;
  private URLName url;
  private Store store;
  private Folder folder;

  private ConnectionEvent connectionEvent;
  private FolderEvent folderEvent;
	
  public FolderTest(String name, String url)
  {
    super(name);
    this.url = new URLName(url);
  }
  
  protected void setUp()
    throws Exception
  {
    session = Session.getInstance(System.getProperties());
    //session.setDebug(true);
    store = session.getStore(url);
    assertNotNull(store);
  }

  void getFolder()
    throws Exception
  {
    String file = url.getFile();
    if (file != null && file.length() > 0)
      {
        folder = store.getFolder(url);
      }
    else
      {
        folder = store.getDefaultFolder();
      }
    assertNotNull(folder);
  }

  protected void tearDown()
    throws Exception
  {
    if (store!=null)
      {
        store.close();
      }
    url = null;
    folder = null;
    store = null;
    session = null;
  }
  
  public void testOpen()
    throws Exception
  {
    store.connect();
    getFolder();
    folder.addConnectionListener(this);
    folder.open(Folder.READ_ONLY);
    assertNotNull(connectionEvent);
    assertEquals(connectionEvent.getType(), ConnectionEvent.OPENED);
  }
  
  public void testClose()
    throws Exception
  {
    store.connect();
    getFolder();
    folder.addConnectionListener(this);
    folder.open(Folder.READ_ONLY);
    folder.close(false);
    assertNotNull(connectionEvent);
    assertEquals(connectionEvent.getType(), ConnectionEvent.CLOSED);
  }

  public void closed(ConnectionEvent e)
  {
    connectionEvent = e;
  }

  public void opened(ConnectionEvent e)
  {
    connectionEvent = e;
  }

  public void disconnected(ConnectionEvent e)
  {
    connectionEvent = e;
  }

  public void folderCreated(FolderEvent e)
  {
    folderEvent = e;
  }

  public void folderDeleted(FolderEvent e)
  {
    folderEvent = e;
  }

  public void folderRenamed(FolderEvent e)
  {
    folderEvent = e;
  }

  static Test suite(String url)
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new FolderTest("testOpen", url));
    suite.addTest(new FolderTest("testClose", url));
    return suite;
  }

  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    try
      {
        BufferedReader r = new BufferedReader(new FileReader("folder-urls"));
        for (String line = r.readLine(); line!=null; line = r.readLine())
          {
            if (!line.startsWith("#"))
              {
                suite.addTest(suite(line));
              }
          }
        r.close();
      }
    catch (FileNotFoundException e)
      {
        System.err.println("No folder URLs");
      }
    catch (IOException e)
      {
        e.printStackTrace(System.err);
      }
    return suite;
  }
  
}
