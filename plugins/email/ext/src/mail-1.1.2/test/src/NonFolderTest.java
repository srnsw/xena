import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.event.FolderEvent;
import javax.mail.event.FolderListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeMessage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Tests for (currently) nonexistenet folders.
 * This suite creates a folder, appends messages to it, renames it, deletes
 * it, etc.
 */
public class NonFolderTest
  extends TestCase
  implements FolderListener, MessageCountListener
{
	
	private Session session;
  private URLName url;
  private Store store;
  private Folder folder;

  private FolderEvent folderEvent;
  private MessageCountEvent countEvent;
	
	public NonFolderTest(String name, String url)
	{
    super(name);
    this.url = new URLName(url);
	}

	protected void setUp()
	{
		session = Session.getInstance(System.getProperties());
    try
    {
      store = session.getStore(url);
      assertNotNull(store);
      store.connect();
      folder = store.getFolder(url.getFile());
      assertNotNull(folder);
    }
    catch (MessagingException e)
    {
      fail(e.getMessage());
    }
	}

	protected void tearDown()
	{
    try
    {
      store.close();
    }
    catch (MessagingException e)
    {
    }
    url = null;
    folder = null;
    store = null;
		session = null;
	}

	public void testCreate()
	{
    String msg = url.toString();
    try
    {
      folder.addFolderListener(this);
      folder.create(Folder.HOLDS_MESSAGES);
      assertTrue(msg, folder.exists());
      assertNotNull(msg, folderEvent);
      assertEquals(msg, folderEvent.getType(), FolderEvent.CREATED);
    }
    catch (MessagingException e)
    {
      fail(msg+": "+e.getMessage());
    }
	}

	public void testAppend()
	{
    String msg = url.toString();
    try
    {
      assertTrue(msg, folder.exists());
      folder.addMessageCountListener(this);
      folder.open(Folder.READ_WRITE);
      Message[] messages = new Message[1];
      FileInputStream in = new FileInputStream("test.message");
      messages[0] = new MimeMessage(session, in);
      folder.appendMessages(messages);
      assertNotNull(msg, countEvent);
      assertEquals(msg, countEvent.getType(), MessageCountEvent.ADDED);
      folder.close(false);
    }
    catch (Exception e)
    {
      fail(url+": "+e.getMessage());
    }
	}

	public void testExpunge()
	{
    String msg = url.toString();
    try
    {
      assertTrue(msg, folder.exists());
      folder.addMessageCountListener(this);
      folder.open(Folder.READ_WRITE);
      Message[] messages = folder.getMessages();
      assertTrue(msg, messages.length>0);
      for (int i=0; i<messages.length; i++)
        messages[i].setFlag(Flags.Flag.DELETED, true);
      folder.expunge();
      assertNotNull(msg, countEvent);
      assertEquals(msg, countEvent.getType(), MessageCountEvent.REMOVED);
      folder.close(false);
    }
    catch (MessagingException e)
    {
      fail(url+": "+e.getMessage());
    }
	}

	public void testRename()
	{
    String msg = url.toString();
    try
    {
      assertTrue(msg, folder.exists());
      folder.addFolderListener(this);
      Folder parent = folder.getParent();
      assertNotNull(msg, parent);
      Folder newFolder = parent.getFolder(folder.getName()+".new");
      assertFalse(msg, newFolder.exists());
      assertTrue(msg, folder.renameTo(newFolder));
      assertTrue(msg, newFolder.exists());
      assertNotNull(msg, folderEvent);
      assertEquals(msg, folderEvent.getType(), FolderEvent.RENAMED);
      assertTrue(msg, newFolder.renameTo(folder));
    }
    catch (MessagingException e)
    {
      fail(url+": "+e.getMessage());
    }
	}

	public void testDelete()
	{
    String msg = url.toString();
    try
    {
      assertTrue(msg, folder.exists());
      folder.addFolderListener(this);
      folder.delete(true);
      assertFalse(msg, folder.exists());
      assertNotNull(msg, folderEvent);
      assertEquals(msg, folderEvent.getType(), FolderEvent.DELETED);
    }
    catch (MessagingException e)
    {
      fail(url+": "+e.getMessage());
    }
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

  public void messagesAdded(MessageCountEvent e)
  {
    countEvent = e;
  }

  public void messagesRemoved(MessageCountEvent e)
  {
    countEvent = e;
  }

  static Test suite(String url)
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new NonFolderTest("testCreate", url));
    suite.addTest(new NonFolderTest("testAppend", url));
    suite.addTest(new NonFolderTest("testExpunge", url));
    suite.addTest(new NonFolderTest("testRename", url));
    suite.addTest(new NonFolderTest("testDelete", url));
    return suite;
  }

	public static Test suite()
	{
		TestSuite suite = new TestSuite();
    try
    {
      BufferedReader r = new BufferedReader(new FileReader("non-folder-urls"));
      for (String line = r.readLine(); line!=null; line = r.readLine())
        suite.addTest(suite(line));
      r.close();
    }
    catch (FileNotFoundException e)
    {
      System.out.println("No non-folder URLs");
    }
    catch (IOException e)
    {
      throw new RuntimeException("Can't load non-folder URLs");
    }
    return suite;
	}
	
}
