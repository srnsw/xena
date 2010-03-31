import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class StoreTest
  extends TestCase
  implements ConnectionListener
{
	
	private Session session;
  private URLName url;
  private Store store;

  private ConnectionEvent event;
	
	public StoreTest(String name, String url)
	{
    super(name);
    this.url = new URLName(url);
	}

	protected void setUp()
	{
		session = Session.getInstance(System.getProperties());
    //session.setDebug(true);
    try
    {
      store = session.getStore(url);
      assertNotNull(store);
    }
    catch (MessagingException e)
    {
      fail(e.getMessage());
    }
	}

	protected void tearDown()
	{
    if (store!=null)
    {
      try
      {
        store.close();
      }
      catch (MessagingException e)
      {
      }
    }
    url = null;
    store = null;
		session = null;
	}

	public void testConnect()
	{
    try
    {
      store.addConnectionListener(this);
      store.connect();
      assertNotNull(event);
      assertEquals(event.getType(), ConnectionEvent.OPENED);
    }
    catch (MessagingException e)
    {
      fail(e.getMessage());
    }
	}

	public void testClose()
	{
    try
    {
      store.addConnectionListener(this);
      store.connect();
      store.close();
      assertNotNull(event);
      assertEquals(event.getType(), ConnectionEvent.CLOSED);
    }
    catch (MessagingException e)
    {
      fail(e.getMessage());
    }
	}

	public void testGetDefaultFolder()
	{
    try
    {
      Folder f = store.getDefaultFolder();
      assertNotNull(f);
    }
    catch (MessagingException e)
    {
      fail(e.getMessage());
    }
	}

	public void testGetFolder()
	{
    try
    {
      Folder f = store.getFolder(url.getFile());
      assertNotNull(f);
    }
    catch (MessagingException e)
    {
      fail(e.getMessage());
    }
	}

  public void closed(ConnectionEvent e)
  {
    event = e;
  }

  public void opened(ConnectionEvent e)
  {
    event = e;
  }

  public void disconnected(ConnectionEvent e)
  {
    event = e;
  }

  static Test suite(String url)
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new StoreTest("testConnect", url));
    suite.addTest(new StoreTest("testClose", url));
    suite.addTest(new StoreTest("testGetDefaultFolder", url));
    suite.addTest(new StoreTest("testGetFolder", url));
    return suite;
  }

	public static Test suite()
	{
		TestSuite suite = new TestSuite();
    try
    {
      BufferedReader r = new BufferedReader(new FileReader("store-urls"));
      for (String line = r.readLine(); line!=null; line = r.readLine())
        suite.addTest(suite(line));
      r.close();
    }
    catch (IOException e)
    {
      System.err.println("No store URLs");
    }
    return suite;
	}
	
}
