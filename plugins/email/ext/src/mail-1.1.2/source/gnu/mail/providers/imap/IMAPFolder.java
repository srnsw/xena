/*
 * IMAPFolder.java
 * Copyright (C) 2003, 2004 Chris Burdess <dog@gnu.org>
 * 
 * This file is part of GNU JavaMail, a library.
 * 
 * GNU JavaMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GNU JavaMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */

package gnu.mail.providers.imap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.FolderEvent;
import javax.mail.internet.MimeMessage;

import javax.mail.search.AddressStringTerm;
import javax.mail.search.AddressTerm;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.DateTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.HeaderTerm;
import javax.mail.search.IntegerComparisonTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.MessageNumberTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.RecipientTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SizeTerm;
import javax.mail.search.StringTerm;
import javax.mail.search.SubjectTerm;

import gnu.inet.imap.IMAPConnection;
import gnu.inet.imap.IMAPConstants;
import gnu.inet.imap.ListEntry;
import gnu.inet.imap.MailboxStatus;
import gnu.inet.imap.MessageStatus;
import gnu.inet.imap.Quota;

/**
 * The folder class implementing the IMAP4rev1 mail protocol.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class IMAPFolder 
  extends Folder
  implements UIDFolder
{

  /**
   * The folder path.
   */
  protected String path;

  /**
   * The type of this folder (HOLDS_MESSAGES or HOLDS_FOLDERS).
   */
  protected int type;

  protected Flags permanentFlags = new Flags();

  protected char delimiter;

  protected int messageCount = -1;

  protected int newMessageCount = -1;

  protected long uidValidity = -1L;

  protected boolean subscribed;

  private static DateFormat searchdf = new SimpleDateFormat("d-MMM-yyyy");

  /**
   * Constructor.
   */
  protected IMAPFolder(Store store, String path) 
  {
    this(store, path, -1, '\u0000');
  }
  
  /**
   * Constructor.
   */
  protected IMAPFolder(Store store, String path, char delimiter) 
  {
    this(store, path, -1, delimiter);
  }
  
  /**
   * Constructor.
   */
  protected IMAPFolder(Store store, String path, int type, char delimiter) 
  {
    super(store);
    this.path = path;
    this.type = type;
    this.delimiter = delimiter;
  }

  /*
   * Updates this folder from the specified mailbox status object.
   */
  void update(MailboxStatus status, boolean fireEvents)
    throws MessagingException
  {
    if (status == null)
      {
        throw new FolderNotFoundException(this);
      }
    mode = status.readWrite ? Folder.READ_WRITE : Folder.READ_ONLY;
    if (status.permanentFlags != null)
      {
        permanentFlags = readFlags(status.permanentFlags);
      }
    // message counts
    int oldMessageCount = messageCount;
    messageCount = status.messageCount;
    newMessageCount = status.newMessageCount;
    // uidvalidity
    uidValidity = status.uidValidity;
    // fire events if necessary
    if (fireEvents)
      {
        if (messageCount > oldMessageCount)
          {
            Message[] m = new Message[messageCount - oldMessageCount];
            for (int i = oldMessageCount; i < messageCount; i++)
              {
                m[i - oldMessageCount] = getMessage(i);
              }
            notifyMessageAddedListeners(m);
          }
        else if (messageCount < oldMessageCount)
          {
            Message[] m = new Message[oldMessageCount - messageCount];
            for (int i = messageCount; i < oldMessageCount; i++)
              {
                m[i - messageCount] = getMessage(i);
              }
            notifyMessageRemovedListeners(false, m);
          }
      }
  }

  Flags readFlags(List sflags)
  {
    Flags flags = new Flags();
    int len = sflags.size();
    for (int i = 0; i < len; i++)
    {
      String flag = (String) sflags.get(i);
      if (flag == IMAPConstants.FLAG_ANSWERED)
        {
          flags.add(Flags.Flag.ANSWERED);
        }
      else if (flag == IMAPConstants.FLAG_DELETED)
        {
          flags.add(Flags.Flag.DELETED);
        }
      else if (flag == IMAPConstants.FLAG_DRAFT)
        {
          flags.add(Flags.Flag.DRAFT);
        }
      else if (flag == IMAPConstants.FLAG_FLAGGED)
        {
          flags.add(Flags.Flag.FLAGGED);
        }
      else if (flag == IMAPConstants.FLAG_RECENT)
        {
          flags.add(Flags.Flag.RECENT);
        }
      else if (flag == IMAPConstants.FLAG_SEEN)
        {
          flags.add(Flags.Flag.SEEN);
        }
      // user flags?
    }
    return flags;
  }

  /**
   * Returns the name of this folder.
   */
  public String getName() 
  {
    int di = path.lastIndexOf(delimiter);
    return (di == -1) ? path : path.substring(di + 1);
  }

  /**
   * Returns the full path of this folder.
   */
  public String getFullName()
  {
    return path;
  }

  /**
   * Returns the type of this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public int getType() 
    throws MessagingException 
  {
    if (type == -1)
      {
        int lsi = path.lastIndexOf(getSeparator());
        String parent = (lsi == -1) ? "" : path.substring(0, lsi);
        String name = (lsi == -1) ? path : path.substring(lsi+1);
        IMAPConnection connection = ((IMAPStore) store).getConnection();
        try
          {
            ListEntry[] entries = null;
            synchronized (connection)
              {
                entries = connection.list(parent, name);
              }
            if (connection.alertsPending())
              {
               ((IMAPStore) store).processAlerts();
              }
            type = Folder.HOLDS_FOLDERS;
            if (entries.length > 0)
              {
                if (!"".equals(path) && entries[0].isNoinferiors())
                  {
                    type |= Folder.HOLDS_MESSAGES;
                  }
              }
            else
              {
                throw new FolderNotFoundException(this);
              }
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
    return type;
  }

  /**
   * Indicates whether this folder exists.
   * @exception MessagingException if a messaging error occurred
   */
  public boolean exists() 
    throws MessagingException 
  {
    try
      {
        getType();
      }
    catch (FolderNotFoundException e)
      {
        return false;
      }
    return true;
  }

  /**
   * Indicates whether this folder contains new messages.
   * @exception MessagingException if a messaging error occurred
   */
  public boolean hasNewMessages() 
    throws MessagingException 
  {
    return getNewMessageCount() > 0; // TODO
  }

  /**
   * Opens this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public void open(int mode) 
    throws MessagingException 
  {
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        MailboxStatus status = null;
        synchronized (connection)
          {
            switch (mode)
              {
              case Folder.READ_WRITE:
                status = connection.select(path);
                break;
              case Folder.READ_ONLY:
                status = connection.examine(path);
                break;
              default:
                throw new MessagingException("No such mode: " + mode);
              }
            update(status, false);
          }
        s.setSelected(this);
        notifyConnectionListeners(ConnectionEvent.OPENED);
        if (connection.alertsPending())
          {
            s.processAlerts();
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * Create this folder.
   */
  public boolean create(int type) 
    throws MessagingException 
  {
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        String newPath = path;
        if ((type & HOLDS_FOLDERS) != 0)
          {
            getSeparator();
            if (delimiter == '\u0000') // this folder cannot be created
              {
                throw new FolderNotFoundException(this, newPath);
              }
            newPath = new StringBuffer(newPath)
              .append(delimiter)
              .toString();
          }
        boolean ret = false;
        synchronized (connection)
          {
            ret = connection.create(newPath);
          }
        if (ret)
          {
            type = -1;
            notifyFolderListeners(FolderEvent.CREATED);
          }
        if (connection.alertsPending())
          {
            s.processAlerts();
          }
        return ret;
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * Delete this folder.
   */
  public boolean delete(boolean flag) 
    throws MessagingException 
  {
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        boolean ret = false;
        synchronized (connection)
          {
            ret = connection.delete(path);
          }
        if (ret)
          {
            type = -1;
            notifyFolderListeners(FolderEvent.DELETED);
          }
        if (connection.alertsPending())
          {
            s.processAlerts();
          }
        return ret;
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * Rename this folder.
   */
  public boolean renameTo(Folder folder) 
    throws MessagingException 
  {
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        boolean ret = false;
        synchronized (connection)
          {
            ret = connection.rename(path, folder.getFullName());
          }
        if (ret)
          {
            type = -1;
            notifyFolderRenamedListeners(folder); // do we have to close?
          }
        if (connection.alertsPending())
          {
            s.processAlerts();
          }
        return ret;
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * Closes this folder.
   * @param expunge if the folder is to be expunged before it is closed
   * @exception MessagingException if a messaging error occurred
   */
  public void close(boolean expunge) 
    throws MessagingException 
  {
    if (mode == -1)
      {
        return;
      }
    IMAPStore s = (IMAPStore) store;
    boolean selected = s.isSelected(this);
    if (selected)
      {
        s.setSelected(null);
      }
    mode = -1;
    notifyConnectionListeners(ConnectionEvent.CLOSED);
    if (expunge)
      {
        if (!selected)
          {
            throw new FolderClosedException(this);
          }
        IMAPConnection connection = s.getConnection();
        try
          {
            boolean success = false;
            synchronized (connection)
              {
                success = connection.close();
              }
            if (connection.alertsPending())
              {
                s.processAlerts();
              }
            if (!success)
              {
                throw new IllegalWriteException();
              }
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
  }

  /**
   * Expunges this folder.
   * This deletes all the messages marked as deleted.
   * @exception MessagingException if a messaging error occurred
   */
  public Message[] expunge() 
    throws MessagingException 
  {
    if (!isOpen())
      {
        throw new MessagingException("Folder is not open");
      }
    if (mode == Folder.READ_ONLY)
      {
        throw new MessagingException("Folder was opened read-only");
      }
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        int[] messageNumbers = null;
        synchronized (connection)
          {
            messageNumbers = connection.expunge();
          }
        // construct empty IMAPMessages for the messageNumbers
        IMAPMessage[] messages = new IMAPMessage[messageNumbers.length];
        for (int i = 0; i < messages.length; i++)
          {
            messages[i] = new IMAPMessage(this, messageNumbers[i]);
          }
        // do we need to do this?
        notifyMessageRemovedListeners(true, messages);
        if (connection.alertsPending())
          {
            s.processAlerts();
          }
        return messages;
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * Indicates whether this folder is open.
   */
  public boolean isOpen() 
  {
    return (mode != -1);
  }

  /**
   * Returns the permanent flags for this folder.
   */
  public Flags getPermanentFlags() 
  {
    return permanentFlags;
  }

  /**
   * Returns the number of messages in this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public int getMessageCount() 
    throws MessagingException 
  {
    MailboxStatus ms = null;
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        if (mode == -1 || messageCount < 0)
          {
            String[] items = new String[1];
            items[0] = IMAPConstants.MESSAGES;
            synchronized (connection)
              {
                ms = connection.status(path, items);
              }
            update(ms, true);
          }
        else // NOOP
          {
            synchronized (connection)
              {
                ms = connection.noop();
              }
            if (ms != null)
              {
                update(ms, true);
              }
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
    if (connection.alertsPending())
      {
        s.processAlerts();
      }
    return messageCount;
  }

  /**
   * Returns the number of new messages in this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public int getNewMessageCount() 
    throws MessagingException 
  {
    MailboxStatus ms = null;
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        if (mode == -1 || newMessageCount < 0)
          {
            String[] items = new String[1];
            items[0] = IMAPConstants.RECENT;
            synchronized (connection)
              {
                ms = connection.status(path, items);
                update(ms, true);
              }
          }
        else // NOOP
          {
            synchronized (connection)
              {
                ms = connection.noop();
              }
            if (ms != null)
              {
                update(ms, true);
              }
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
    if (connection.alertsPending())
      {
        s.processAlerts();
      }
    return newMessageCount;
  }

  /**
   * Returns the specified message number from this folder.
   * The message is only retrieved once from the server.
   * Subsequent getMessage() calls to the same message are cached.
   * Since POP3 does not provide a mechanism for retrieving only part of
   * the message(headers, etc), the entire message is retrieved.
   * @exception MessagingException if a messaging error occurred
   */
  public Message getMessage(int msgnum) 
    throws MessagingException 
  {
    if (mode == -1)
      {
        throw new FolderClosedException(this);
      }
    return new IMAPMessage(this, msgnum);
  }

  /**
   * Appends the specified set of messages to this folder.
   * Only <code>MimeMessage</code>s are accepted.
   */
  public void appendMessages(Message[] messages) 
    throws MessagingException 
  {
    MimeMessage[] m = new MimeMessage[messages.length];
    try
      {
        for (int i = 0; i < messages.length; i++)
          {
            m[i] = (MimeMessage) messages[i];
          }
      }
    catch (ClassCastException e)
      {
        throw new MessagingException("Only MimeMessages can be appended to " +
                                      "this folder");
      }
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
    {
      for (int i = 0; i < m.length; i++)
      {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        m[i].writeTo(out);
        byte[] content = out.toByteArray();
        out = null;
        synchronized (connection)
        {
          connection.append(path, null, content);
        }
      }
      if (connection.alertsPending())
        {
          s.processAlerts();
        }
    }
    catch (IOException e)
    {
      throw new MessagingException(e.getMessage(), e);
    }
    notifyMessageAddedListeners(m);
  }

  /**
   * IMAP fetch routine.
   * This executes the fetch for the specified message numbers
   * and updates the messages according to the message statuses returned.
   */
  public void fetch(Message[] messages, FetchProfile fp) 
    throws MessagingException 
  {
    if (!isOpen())
      {
        throw new FolderClosedException(this);
      }
    // decide which commands to send
    String[] headers = fp.getHeaderNames();
    List l = new ArrayList();
    if (fp.contains(FetchProfile.Item.CONTENT_INFO))
      {
        l.add(IMAPMessage.FETCH_CONTENT);
      }
    else if (fp.contains(FetchProfile.Item.ENVELOPE))
      {
        l.add(IMAPMessage.FETCH_HEADERS);
      }
    else if (headers.length > 0)
      {
        // specified headers only
        StringBuffer hbuf = new StringBuffer("BODY.PEEK[HEADER.FIELDS(");
        for (int i = 0; i < headers.length; i++)
          {
            if (i > 0)
              {
                hbuf.append(' ');
              }
            hbuf.append(headers[i]);
          }
        hbuf.append(')');
        hbuf.append(']');
        l.add(hbuf.toString());
    }
    if (fp.contains(FetchProfile.Item.FLAGS))
      {
        l.add(IMAPConstants.FLAGS);
      }
    l.add(IMAPConstants.INTERNALDATE); // for received date
    int llen = l.size();
    if (llen == 0)
      {
        return; // no commands to send: don't bother the server
      }
    String[] commands = new String[llen];
    l.toArray(commands);
    l = null;
    // get casted imapmessages and message numbers
    IMAPMessage[] m = new IMAPMessage[messages.length];
    int[] msgnums = new int[messages.length];
    try
      {
        for (int i = 0; i < messages.length; i++)
          {
            m[i] = (IMAPMessage) messages[i];
            msgnums[i] = m[i].getMessageNumber();
          }
      }
    catch (ClassCastException e)
      {
        throw new MessagingException("Only IMAPMessages can be fetched");
      }
    // execute
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        synchronized (connection)
          {
            MessageStatus[] ms = connection.fetch(msgnums, commands);
            for (int i = 0; i < ms.length; i++)
              {
                int msgnum = ms[i].getMessageNumber();
                for (int j = 0; j < msgnums.length; j++)
                  {
                    if (msgnums[j] == msgnum)
                      {
                        m[j].update(ms[i]);
                        break;
                      }
                  }
              }
          }
        if (connection.alertsPending())
          {
            s.processAlerts();
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * IMAP search function.
   */
  public Message[] search(SearchTerm term)
    throws MessagingException
  {
    return search(term, null);
  }

  /**
   * IMAP search function.
   */
  public Message[] search(SearchTerm term, Message[] msgs)
    throws MessagingException
  {
    List list = new ArrayList();
    if (msgs != null)
      {
        // <message set>
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < msgs.length; i++)
          {
            int msgnum = msgs[i].getMessageNumber();
            if (i > 0)
              {
                buffer.append(',');
              }
            buffer.append(msgnum);
          }
        list.add(buffer.toString());
      }
    boolean isIMAPSearch = addTerm(term, list);
    String[] criteria = new String[list.size()];
    list.toArray(criteria);
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        Message[] messages;
        if (isIMAPSearch && criteria.length > 0)
          {
            int[] mn = null;
            synchronized (connection)
              {
                mn = connection.search(null, criteria);
              }
            messages = new Message[mn.length];
            for (int i = 0; i < mn.length; i++)
              messages[i] = new IMAPMessage(this, mn[i]);
            if (connection.alertsPending())
              s.processAlerts();
          }
        else
          messages = (msgs != null) ? msgs : getMessages();
        // Enforce final constraints
        return super.search(term, messages);
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * Possibly recursive search term add function.
   * Note that this is not sufficient to enforce all the constraints imposed
   * by the SearchTerm structures - this is why we finally call
   * <code>super.search()</code> in the search method.
   * @return true if all the terms can be represented in IMAP
   */
  private boolean addTerm(SearchTerm term, List list)
  {
    if (term instanceof AndTerm)
      {
        SearchTerm[] terms = ((AndTerm) term).getTerms();
        for (int i = 0; i < terms.length; i++)
          {
            if (!addTerm(terms[i], list))
              return false;
          }
      }
    else if (term instanceof OrTerm)
      {
        list.add(IMAPConstants.SEARCH_OR);
        SearchTerm[] terms = ((OrTerm) term).getTerms();
        for (int i = 0; i < terms.length; i++)
          {
            if (!addTerm(terms[i], list))
              return false;
          }
      }
    else if (term instanceof NotTerm)
      {
        list.add(IMAPConstants.SEARCH_NOT);
        if (!addTerm(((NotTerm) term).getTerm(), list))
          return false;
      }
    else if (term instanceof FlagTerm)
      {
        FlagTerm ft = (FlagTerm) term;
        Flags f = ft.getFlags();
        boolean set = ft.getTestSet();
        // System flags
        Flags.Flag[] sf = f.getSystemFlags();
        for (int i = 0; i < sf.length; i++)
          {
            Flags.Flag ff = sf[i];
            if (ff == Flags.Flag.ANSWERED)
              {
                list.add(set ? IMAPConstants.SEARCH_ANSWERED :
                          IMAPConstants.SEARCH_UNANSWERED);
              }
            else if (ff == Flags.Flag.DELETED)
              {
                list.add(set ? IMAPConstants.SEARCH_DELETED :
                          IMAPConstants.SEARCH_UNDELETED);
              }
            else if (ff == Flags.Flag.DRAFT)
              {
                list.add(set ? IMAPConstants.SEARCH_DRAFT :
                          IMAPConstants.SEARCH_UNDRAFT);
              }
            else if (ff == Flags.Flag.FLAGGED)
              {
                list.add(set ? IMAPConstants.SEARCH_FLAGGED :
                          IMAPConstants.SEARCH_UNFLAGGED);
              }
            else if (ff == Flags.Flag.RECENT)
              {
                list.add(set ? IMAPConstants.SEARCH_RECENT :
                          IMAPConstants.SEARCH_OLD);
              }
            else if (ff == Flags.Flag.SEEN)
              {
                list.add(set ? IMAPConstants.SEARCH_SEEN :
                          IMAPConstants.SEARCH_UNSEEN);
              }
          }
        // Keywords
        String[] uf = f.getUserFlags();
        for (int i = 0; i < uf.length; i++)
          {
            StringBuffer keyword = new StringBuffer();
            keyword.append(set ? IMAPConstants.SEARCH_KEYWORD :
                            IMAPConstants.SEARCH_UNKEYWORD);
            keyword.append('"');
            keyword.append(uf[i]);
            keyword.append('"');
            list.add(keyword.toString());
          }
      }
    else if (term instanceof AddressTerm)
      {
        Address address = ((AddressTerm) term).getAddress();
        StringBuffer criterion = new StringBuffer();
        if (term instanceof FromTerm)
          criterion.append(IMAPConstants.SEARCH_FROM);
        else if (term instanceof RecipientTerm)
          {
            Message.RecipientType rt =
              ((RecipientTerm) term).getRecipientType();
            if (rt == Message.RecipientType.TO)
              criterion.append(IMAPConstants.SEARCH_TO);
            else if (rt == Message.RecipientType.CC)
              criterion.append(IMAPConstants.SEARCH_CC);
            else if (rt == Message.RecipientType.BCC)
              criterion.append(IMAPConstants.SEARCH_BCC);
            else
              criterion = null;
          }
        else
          criterion = null;
        if (criterion != null)
          {
            criterion.append(' ');
            criterion.append('"');
            criterion.append(address.toString());
            criterion.append('"');
            list.add(criterion.toString());
          }
        else
          return false;
      }
    else if (term instanceof ComparisonTerm)
      {
        if (term instanceof DateTerm)
          {
            DateTerm dt = (DateTerm) term;
            Date date = dt.getDate();
            int comparison = dt.getComparison();
            StringBuffer criterion = new StringBuffer();
            switch (comparison)
              {
              case ComparisonTerm.NE:
              case ComparisonTerm.GE:
              case ComparisonTerm.LE:
                criterion.append(IMAPConstants.SEARCH_NOT);
                criterion.append(' ');
              }
            if (term instanceof SentDateTerm)
              criterion.append("SENT");
            switch (comparison)
              {
              case ComparisonTerm.EQ:
              case ComparisonTerm.NE:
                criterion.append(IMAPConstants.SEARCH_ON);
                break;
              case ComparisonTerm.LT:
              case ComparisonTerm.GE:
                criterion.append(IMAPConstants.SEARCH_BEFORE);
                break;
              case ComparisonTerm.GT:
              case ComparisonTerm.LE:
                criterion.append(IMAPConstants.SEARCH_SINCE);
                break;
              }
            criterion.append(' ');
            criterion.append(searchdf.format(date));
            list.add(criterion.toString());
          }
        else if (term instanceof IntegerComparisonTerm)
          {
            IntegerComparisonTerm it = (IntegerComparisonTerm) term;
            int number = it.getNumber();
            int comparison = it.getComparison();
            if (term instanceof SizeTerm)
              {
                StringBuffer criterion = new StringBuffer();
                switch (comparison)
                  {
                  case ComparisonTerm.EQ:
                  case ComparisonTerm.GE:
                  case ComparisonTerm.LE:
                    criterion.append(IMAPConstants.SEARCH_NOT);
                    criterion.append(' ');
                  }
                switch (comparison)
                  {
                  case ComparisonTerm.EQ:
                  case ComparisonTerm.NE:
                    criterion.append(IMAPConstants.SEARCH_OR);
                    criterion.append(' ');
                    criterion.append(IMAPConstants.SEARCH_SMALLER);
                    criterion.append(' ');
                    criterion.append(number);
                    criterion.append(' ');
                    criterion.append(IMAPConstants.SEARCH_LARGER);
                    criterion.append(' ');
                    criterion.append(number);
                    break;
                  case ComparisonTerm.LT:
                  case ComparisonTerm.GE:
                    criterion.append(IMAPConstants.SEARCH_SMALLER);
                    criterion.append(' ');
                    criterion.append(number);
                    break;
                  case ComparisonTerm.GT:
                  case ComparisonTerm.LE:
                    criterion.append(IMAPConstants.SEARCH_LARGER);
                    criterion.append(' ');
                    criterion.append(number);
                    break;
                  }
                list.add(criterion.toString());
              }
            else
              return false;
          }
      }
    else if (term instanceof StringTerm)
      {
        String pattern = ((StringTerm) term).getPattern();
        StringBuffer criterion = new StringBuffer();
        if (term instanceof BodyTerm)
          {
            criterion.append(IMAPConstants.SEARCH_BODY);
          }
        else if (term instanceof HeaderTerm)
          {
            criterion.append(IMAPConstants.SEARCH_HEADER);
            criterion.append(' ');
            criterion.append(((HeaderTerm) term).getHeaderName());
          }
        else if (term instanceof SubjectTerm)
          {
            criterion.append(IMAPConstants.SEARCH_SUBJECT);
          }
        else if (term instanceof MessageIDTerm)
          {
            criterion.append(IMAPConstants.SEARCH_HEADER);
            criterion.append(' ');
            criterion.append("Message-ID");
          }
        else
          criterion = null; // TODO StringAddressTerms?
        if (criterion != null)
          {
            criterion.append(' ');
            criterion.append('"');
            criterion.append(pattern);
            criterion.append('"');
            list.add(criterion.toString());
          }
        else
          return false;
      }
    else
      return false;
    return true;
  }

  public boolean isSubscribed()
  {
    return subscribed;
  }

  public void setSubscribed(boolean flag)
    throws MessagingException
  {
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        synchronized (connection)
          {
            if (flag)
              {
                connection.subscribe(path);
              }
            else
              {
                connection.unsubscribe(path);
              }
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }
  
  /**
   * Returns the subfolders for this folder.
   */
  public Folder[] list(String pattern) 
    throws MessagingException 
  {
    char sep = getSeparator();
    String spec = ("".equals(path)) ? path : 
      new StringBuffer(path).append(sep).append(pattern).toString();
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        ListEntry[] entries;
        synchronized (connection)
          {
            entries = connection.list("", spec);
          }
        if (connection.alertsPending())
          {
            s.processAlerts();
          }
        return getFolders(entries, false);
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }
  
  /**
   * Returns the subscribed subfolders for this folder.
   */
  public Folder[] listSubscribed(String pattern) 
    throws MessagingException 
  {
    char sep = getSeparator();
    String spec = ("".equals(path)) ? path :
      new StringBuffer(path).append(sep).append(pattern).toString();
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        ListEntry[] entries = null;
        synchronized (connection)
          {
            entries = connection.lsub("", spec);
          }
        if (connection.alertsPending())
          {
            s.processAlerts();
          }
        return getFolders(entries, true);
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /*
   * Returns a set of folders for a corresponding set of list entries.
   */
  Folder[] getFolders(ListEntry[] entries, boolean subscribed)
    throws MessagingException
  {
    List unique = new ArrayList(entries.length);
    for (int i = 0; i < entries.length; i++)
      {
        ListEntry entry = entries[i];
        int etype = entry.isNoinferiors() ?
          Folder.HOLDS_MESSAGES :
          Folder.HOLDS_FOLDERS;
        if (!entry.isNoselect())
          {
            IMAPFolder f = new IMAPFolder(store, entry.getMailbox(),
                                           etype, entry.getDelimiter());
            if (!unique.contains(f))
              {
                unique.add(f);
                f.subscribed = subscribed;
              }
          }
      }
    Folder[] folders = new Folder[unique.size()];
    unique.toArray(folders);
    return folders;
  }

  /**
   * Returns the parent folder of this folder.
   */
  public Folder getParent() 
    throws MessagingException 
  {
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    getSeparator();
    int di = path.lastIndexOf(delimiter);
    if (di == -1)
      {
        return s.getDefaultFolder();
      }
    return new IMAPFolder(store, path.substring(0, di), delimiter);
  }

  /**
   * Returns a subfolder with the specified name.
   */
  public Folder getFolder(String name) 
    throws MessagingException 
  {
    StringBuffer buf = new StringBuffer();
    if (path != null && path.length() > 0)
      {
        buf.append(path);
        buf.append(delimiter);
      }
    buf.append(name);
    return new IMAPFolder(store, buf.toString(), -1, getSeparator());
  }

  /**
   * Returns the path separator charcter.
   */
  public char getSeparator() 
    throws MessagingException 
  {
    if (delimiter == '\u0000')
      {
        IMAPStore s = (IMAPStore) store;
        IMAPConnection connection = s.getConnection();
        try
          {
            ListEntry[] entries = null;
            synchronized (connection)
              {
                entries = connection.list(path, null);
              }
            if (connection.alertsPending())
              {
                s.processAlerts();
              }
            if (entries.length > 0)
              {
                delimiter = entries[0].getDelimiter();
              }
            else
              {
                throw new FolderNotFoundException(this); 
              }
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
    return delimiter;
  }

  public boolean equals(Object other)
  {
    if (other instanceof IMAPFolder)
      {
        return ((IMAPFolder) other).path.equals(path);
      }
    return super.equals(other);
  }

  // -- UIDFolder --

  public long getUIDValidity()
    throws MessagingException
  {
    MailboxStatus ms = null;
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        if (mode == -1 || uidValidity < 0L)
          {
            String[] items = new String[1];
            items[0] = IMAPConstants.UIDVALIDITY;
            synchronized (connection)
              {
                ms = connection.status(path, items);
              }
            update(ms, true);
          }
        else // NOOP
          {
            synchronized (connection)
              {
                ms = connection.noop();
              }
            if (ms != null)
              {
                update(ms, true);
              }
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
    if (connection.alertsPending())
      {
        s.processAlerts();
      }
    return uidValidity;
  }

  public Message getMessageByUID(long uid)
    throws MessagingException
  {
    if (mode == -1)
      {
        throw new FolderClosedException(this);
      }
    MessageStatus ms = null;
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        String[] cmds = new String[] { IMAPConstants.FLAGS };
        synchronized (connection)
          {
            ms = connection.uidFetch(uid, cmds);
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
    if (connection.alertsPending())
      {
        s.processAlerts();
      }
    IMAPMessage message = new IMAPMessage(this, ms.getMessageNumber());
    message.update(ms);
    return message;
  }

  public Message[] getMessagesByUID(long start, long end)
    throws MessagingException
  {
    if (mode == -1)
      {
        throw new FolderClosedException(this);
      }
    MessageStatus[] ms = null;
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        String[] cmds = new String[] { IMAPConstants.FLAGS };
        synchronized (connection)
          {
            ms = connection.uidFetch(start, end, cmds);
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
    if (connection.alertsPending())
      {
        s.processAlerts();
      }
    Message[] messages = new Message[ms.length];
    for (int i = 0; i < messages.length; i++)
      {
        IMAPMessage message =
          new IMAPMessage(this, ms[i].getMessageNumber());
        message.update(ms[i]);
        messages[i] = message;
      }
    return messages;
  }

  public Message[] getMessagesByUID(long[] uids)
    throws MessagingException
  {
    if (mode == -1)
      {
        throw new FolderClosedException(this);
      }
    MessageStatus[] ms = null;
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        String[] cmds = new String[] { IMAPConstants.FLAGS };
        synchronized (connection)
          {
            ms = connection.uidFetch(uids, cmds);
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
    if (connection.alertsPending())
      {
        s.processAlerts();
      }
    Message[] messages = new Message[ms.length];
    for (int i = 0; i < messages.length; i++)
      {
        IMAPMessage message =
          new IMAPMessage(this, ms[i].getMessageNumber());
        message.update(ms[i]);
        messages[i] = message;
      }
    return messages;
  }

  public long getUID(Message message)
    throws MessagingException
  {
    if (mode == -1)
      {
        throw new FolderClosedException(this);
      }
    if (!(message instanceof IMAPMessage))
      {
        throw new MethodNotSupportedException("not an IMAPMessage");
      }
    IMAPMessage m = (IMAPMessage) message;
    if (m.uid == -1L)
      {
        m.fetchUID();
      }
    return m.uid;
  }

  /**
   * Returns the quotas for this folder.
   */
  public Quota[] getQuota()
    throws MessagingException
  {
    if (mode == -1)
      {
        throw new FolderClosedException(this);
      }
    IMAPStore s = (IMAPStore) store;
    synchronized (s)
      {
        try
          {
            return s.connection.getquotaroot(path);
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
  }

  /**
   * Returns the number of unread messages in this folder.
   * @see javax.mail.Folder#getUnreadMessageCount()
   */
  public synchronized int getUnreadMessageCount()
    throws MessagingException
  {
    return getMessageCountByCriteria("NOT SEEN");
  }

  /**
   * Returns the number of deleted messages in this folder.
   * @see javax.mail.Folder#getDeletedMessageCount()
   */
  public synchronized int getDeletedMessageCount()
    throws MessagingException
  {
    return getMessageCountByCriteria("DELETED");
  }

  /**
   * Convenience method for returning the number of messages in the
   * current folder that match the single criteria.
   */
  public int getMessageCountByCriteria(String criteria)
    throws MessagingException
  {
    if (!isOpen())
      {
        return -1;
      }
    
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    String[] criterias = new String[] { criteria };
    
    int[] ids = null;
    try
      { 
        ids = connection.search(null, criterias);
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
    
    return ids.length;
  }

  /**
   * Returns the access control list for this folder.
   * This is an array of access control entries.
   * @deprecated this API will probably change incompatibly soon
   */
  public ACL[] getACL()
    throws MessagingException
  {
    if (!isOpen())
      {
        return null;
      }
    
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    List acc = new ArrayList();
    try
      {
        Map acl = connection.getacl(path);
        for (Iterator i = acl.entrySet().iterator(); i.hasNext(); )
          {
            Map.Entry entry = (Map.Entry) i.next();
            String name = (String) entry.getKey();
            Integer rights = (Integer) entry.getValue();
            ACL ace = new ACL(name);
            if (rights != null)
              {
                ace.rights = new Rights();
                ace.rights.rights = rights.intValue();
              }
            acc.add(ace);
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
    ACL[] ret = new ACL[acc.size()];
    acc.toArray(ret);
    return ret;
  }

  /**
   * Adds the specified access control entry to this folder.
   * @param ace the access control entry
   * @deprecated this API will probably change incompatibly soon
   */
  public void addACL(ACL ace)
    throws MessagingException
  {
    if (!isOpen())
      {
        return;
      }
    
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        Rights aceRights = ace.getRights();
        if (aceRights != null)
          {
            int rights = connection.listrights(path, ace.name);
            rights |= aceRights.rights;
            if (!connection.setacl(path, ace.name, rights))
              {
                throw new MessagingException("can't set ACL");
              }
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * Removes the ACL for the given principal.
   * @param name the name of the principal
   * @deprecated this API will probably change incompatibly soon
   */
  public void removeACL(String name)
    throws MessagingException
  {
    if (!isOpen())
      {
        return;
      }
    
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        if (!connection.deleteacl(path, name))
          {
            throw new MessagingException("can't delete ACL");
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * Adds the rights for the specified access control entry.
   * @param ace the access control entry
   * @deprecated this API will probably change incompatibly soon
   */
  public void addRights(ACL ace)
    throws MessagingException
  {
    addACL(ace);
  }

  /**
   * Removes the rights specified in the given access control entry from the
   * principal.
   * @param ace the access control entry
   * @deprecated this API will probably change incompatibly soon
   */
  public void removeRights(ACL ace)
    throws MessagingException
  {
    if (!isOpen())
      {
        return;
      }
    
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        Rights aceRights = ace.getRights();
        if (aceRights != null)
          {
            int rights = connection.listrights(path, ace.name);
            rights -= aceRights.rights;
            if (!connection.setacl(path, ace.name, rights))
              {
                throw new MessagingException("can't set ACL");
              }
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * Returns the rights currently assigned to the given principal.
   * @param name the name of the principal
   * @deprecated this API will probably change incompatibly soon
   */
  public Rights listRights(String name)
    throws MessagingException
  {
    if (!isOpen())
      {
        return null;
      }
    
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        Rights rights = new Rights();
        rights.rights = connection.listrights(path, name);
        return rights;
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }
  
  /**
   * Returns the rights assigned to the currently authenticated principal.
   * @deprecated this API will probably change incompatibly soon
   */
  public Rights myRights()
    throws MessagingException
  {
    if (!isOpen())
      {
        return null;
      }
    
    IMAPStore s = (IMAPStore) store;
    IMAPConnection connection = s.getConnection();
    try
      {
        Rights rights = new Rights();
        rights.rights = connection.myrights(path);
        return rights;
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }
  
}
