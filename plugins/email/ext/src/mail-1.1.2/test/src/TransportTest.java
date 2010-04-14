
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.MimeMessage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransportTest
  extends TestCase
  implements ConnectionListener, TransportListener
{

  static Logger logger = Logger.getLogger("test");

  private Session session;
  private URLName url;
  private Transport transport;

  private ConnectionEvent connectionEvent;
  private TransportEvent transportEvent;

  public TransportTest(String name, String url)
    {
      super(name);
      this.url = new URLName(url);
    }

  protected void setUp()
    {
      session = Session.getInstance(System.getProperties());
      try
        {
          transport = session.getTransport(url);
          assertNotNull(transport);
        }
      catch (MessagingException e)
        {
          logger.log(Level.SEVERE, e.getMessage(), e);
          fail(e.getMessage());
        }
    }

  protected void tearDown()
    {
      url = null;
      transport = null;
      session = null;
    }

  public void testConnect()
    {
      try
        {
          transport.addConnectionListener(this);
          transport.connect();
          assertNotNull(connectionEvent);
          assertEquals(connectionEvent.getType(), ConnectionEvent.OPENED);
        }
      catch (MessagingException e)
        {
          System.out.println(url.toString());
          e.printStackTrace(System.out);
          logger.log(Level.SEVERE, e.getMessage(), e);
          fail(e.getMessage());
        }
    }

  public void testClose()
    {
      try
        {
          transport.addConnectionListener(this);
          transport.connect();
          transport.close();
          assertNotNull(connectionEvent);
          assertEquals(connectionEvent.getType(), ConnectionEvent.CLOSED);
        }
      catch (MessagingException e)
        {
          logger.log(Level.SEVERE, e.getMessage(), e);
          fail(e.getMessage());
        }
    }

  public void testSendMessage()
    {
      try
        {
          FileInputStream in = new FileInputStream("test.message");
          MimeMessage message = new MimeMessage(session, in);
          in.close();
          Address[] addresses = message.getAllRecipients();

          transport.connect();
          transport.addTransportListener(this);
          transport.sendMessage(message, addresses);
          assertNotNull(transportEvent);
        }
      catch (MessagingException e)
        {
          logger.log(Level.SEVERE, e.getMessage(), e);
          fail(e.getMessage());
        }
      catch (IOException e)
        {
          logger.log(Level.SEVERE, e.getMessage(), e);
          fail(e.getMessage());
        }
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

  public void messageDelivered(TransportEvent e)
    {
      transportEvent = e;
    }

  public void messageNotDelivered(TransportEvent e)
    {
      transportEvent = e;
    }

  public void messagePartiallyDelivered(TransportEvent e)
    {
      transportEvent = e;
    }

  static Test suite(String url)
    {
      TestSuite suite = new TestSuite();
      suite.addTest(new TransportTest("testConnect", url));
      suite.addTest(new TransportTest("testClose", url));
      suite.addTest(new TransportTest("testSendMessage", url));
      return suite;
    }

  public static Test suite()
    {
      TestSuite suite = new TestSuite();
      try
        {
          BufferedReader r = new BufferedReader(new FileReader("transport-urls"));
          for (String line = r.readLine(); line!=null; line = r.readLine())
            suite.addTest(suite(line));
          r.close();
        }
      catch (IOException e)
        {
          logger.log(Level.SEVERE, e.getMessage(), e);
          System.err.println("No transport URLs");
        }
      return suite;
    }

}
