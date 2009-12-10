import javax.mail.NoSuchProviderException;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class SessionTest
  extends TestCase
{
	
  private Session session;
  
  public SessionTest(String name)
  {
    super(name);
  }
  
  protected void setUp()
  {
    session = Session.getInstance(System.getProperties());
  }
  
  protected void tearDown()
  {
    session = null;
  }
  
  public void testGetInstance()
  {
    assertNotNull(session);
  }
  
  public void testGetProvider()
  {
    Provider[] pp = session.getProviders();
    assertTrue(pp.length>0);
    try
      {
        Provider p = session.getProvider("imap");
        assertNotNull(p);
        assertEquals(p.getType(), Provider.Type.STORE);
        assertEquals(p.getProtocol(), "imap");
      }
    catch (NoSuchProviderException e)
      {
        fail(e.getMessage());
      }
  }

  public void testGetStore()
  {
    try
      {
        Store s = session.getStore("imap");
        assertNotNull(s);
      }
    catch (NoSuchProviderException e)
      {
        fail(e.getMessage());
      }
  }

  public void testGetTransport()
  {
    try
      {
        Transport t = session.getTransport("smtp");
        assertNotNull(t);
      }
    catch (NoSuchProviderException e)
      {
        fail(e.getMessage());
      }
  }
  
  public static Test suite()
  {
    return new TestSuite(SessionTest.class);
  }
  
}
