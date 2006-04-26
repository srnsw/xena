package au.gov.naa.digipres.xena.plugin.email;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.help.NoMerge;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.PluginLocator;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.XmlList;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserDataStore;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.plugin.email.msg.MsgStore;
import au.gov.naa.digipres.xena.util.UrlEncoder;

/**
 * Normalise an email to a Xena email instance. We use the one normaliser for
 * all input email types for several reasons. Firstly, the Java Mail API makes
 * it easy. Secondly, it makes it much easier to change the format or fix bugs.
 * 
 * The same normaliser also caters for two rather different situations - the
 * case where one input file results in one output Xena file. And the case where
 * one input file (email folder(s)) results in multiple output files. In the
 * former case the main result of the normaliser is the Xena email file. In the
 * latter case, the main result of the normaliser is a summary file, and the
 * email files are created separately. Catering for these two situations
 * requires the check of the doMany flag.
 * 
 * @author Chris Bitmead.
 */
public class EmailToXenaEmailNormaliser extends AbstractNormaliser {
    final static String URI = "http://preservation.naa.gov.au/mailbox/1.0";

    public final static String MAILBOX_ROOT_TAG = "mailbox:mailbox";

    private Properties mailProperties = new Properties();

    public String hostName;

    public String userName;

    public String password;

    public int port = 143;

    private XmlList folders;

    private boolean doMany = true;

    // JRW - adding java logging
    Logger logger;

    public String getName() {
        return "Email";
    }

    public static void main(String[] args) throws Exception {
        EmailToXenaEmailNormaliser n = new EmailToXenaEmailNormaliser();
        n.parse(args[1]);
    }

    public EmailToXenaEmailNormaliser() {
        mailProperties.setProperty("mail.mbox.attemptfalback", "false");
        logger = Logger.getLogger(this.getClass().getName());
    }

    protected static XmlList allFolders(Store store, PluginManager pluginManager) throws MessagingException {
        XmlList rtn = new XmlList();
        Folder[] fdr = store.getPersonalNamespaces();
        for (int i = 0; i < fdr.length; i++) {
            addFolders(store, rtn, fdr[i]);
        }
        fdr = store.getSharedNamespaces();
        for (int i = 0; i < fdr.length; i++) {
            addFolders(store, rtn, fdr[i]);
        }
        return rtn;
    }

    private static void addFolders(Store store, List rtn, Folder fdr)
            throws MessagingException {
        try {
            if ((fdr.getType() & Folder.HOLDS_MESSAGES) != 0) {
                rtn.add(fdr.getFullName());
            } else if ((fdr.getType() & Folder.HOLDS_FOLDERS) != 0) {
                Folder[] ls = fdr.list();
                for (int i = 0; i < ls.length; i++) {
                    addFolders(store, rtn, ls[i]);
                }
            }
        } catch (FolderNotFoundException x) {
            // Nothing
        }
    }

    public Store getStore(Type type, InputSource input)
            throws NoSuchProviderException, MessagingException, XenaException {
        String mailboxType = null;
        URLName urln = null;
        File file = null;
        if (type instanceof MboxFileType || type instanceof MboxDirFileType) {
            try {
                file = new File(new URI(input.getSystemId()));
            } catch (URISyntaxException ex) {
                throw new XenaException(ex);
            }
            if (type instanceof MboxDirFileType) {
                mailProperties.setProperty("mail.mbox.mailhome", file
                        .toString());
            } else if (type instanceof MboxFileType) {
                mailProperties.setProperty("mail.mbox.mailhome", file
                        .getParent());
            }
            mailboxType = "mbox";
            urln = new URLName("mbox://" + input.getSystemId());
        } else if (type instanceof ImapType) {
            mailProperties.setProperty(IMAP_HOST, hostName);
            mailProperties.setProperty(IMAP_PORT, Integer.toString(port));
            mailboxType = "imap";
            urln = new URLName(input.getSystemId());
        } else if (type instanceof PstFileType) {
            mailboxType = "pst";
            mailProperties.setProperty("xena.util.pst.bin", PluginLocator.getBinDir().toString());
            urln = new URLName("pst://" + input.getSystemId());
        } else if (type instanceof MsgFileType) {
            mailboxType = "msg";
            urln = new URLName("msg://" + input.getSystemId());
            doMany = false;
        } else if (type instanceof TrimFileType) {
            mailboxType = "trim";
            urln = new URLName("trim://" + input.getSystemId());
            doMany = false;
        }

        mailProperties.setProperty("mail.store.protocol", mailboxType);
        Session session = Session.getInstance(mailProperties);

        Store store = session.getStore(urln);
        store.connect(hostName, userName, password);
        if (type instanceof MboxFileType) {
            folders = new XmlList();
            folders.add(store.getDefaultFolder().getFullName());
        }
        if (type instanceof MsgFileType) {
            ((MsgStore) store).setInputStream(input.getByteStream());
        }
        return store;
    }

    /**
     * Note: mail.jar must be earlier in the classpath than the GNU mail
     * providers otherwise wierd things happen with attachments not being
     * recognized.
     * 
     * This method parse's an email input, and turns it into a XML.
     * 
     * 
     * @param input
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public void parse(InputSource input, NormaliserResults results)
            throws java.io.IOException, org.xml.sax.SAXException {
        // create a store - javax.mail.store
        Store store = null;
        try {
            ContentHandler ch = getContentHandler();
            Type type = ((XenaInputSource) input).getType();

            store = getStore(type, input);
            AttributesImpl empty = new AttributesImpl();

            // doMany is global for some reason... (passing parameters it too
            // hard perhaps?!?)
            if (doMany) {
                ch.startElement(URI, "mailbox", MAILBOX_ROOT_TAG, empty);
            }
            Iterator it = getFoldersOrAll(store).iterator();
            while (it.hasNext()) {
                String foldername = (String) it.next();

                // Foldername is produced from XIS system id,
                // so needs to be URL decoded
                foldername = URLDecoder.decode(foldername, "UTF-8");

                Folder folder = store.getFolder(foldername);
                doFolder(input, folder);
            }
            if (doMany) {
                ch.endElement(URI, "mailbox", MAILBOX_ROOT_TAG);
            }
        } catch (NoSuchProviderException x) {
            x.printStackTrace();
            throw new SAXException(x);
        } catch (MessagingException x) {
            throw new SAXException(x);
        } catch (XenaException x) {
            throw new SAXException(x);
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException x) {
                    throw new SAXException(x);
                }
            }
        }
    }

    void doFolder(InputSource input, Folder gofolder) 
            throws IOException, MessagingException, SAXException, XenaException {
        gofolder.open(Folder.READ_ONLY);
        ContentHandler contentHandler = getContentHandler();
        Message message[] = gofolder.getMessages();
        // XenaResultsLog log = (XenaResultsLog)getProperty("http://xena/log");
        AttributesImpl empty = new AttributesImpl();
        for (int i = 0, n = message.length; i < n; i++) {
            Message msg = message[i];
            List msgurls = null;
            String msgurl = null;
            XenaInputSource xis = null;
            if (doMany) {
                URLName url = new URLName(input.getSystemId());
                String fileName = url.getFile();
                if (fileName == null) {
                    fileName = "";
                } else if (fileName.length() == 0) {
                    // nothing
                } else if (fileName.charAt(fileName.length() - 1) != '/'
                        && !gofolder.getFullName().equals("")) {
                    fileName += "/";
                }
                fileName += UrlEncoder.encode(gofolder.getFullName()) + "/"
                        + msg.getMessageNumber();
                // The MailURL
                msgurl = new URLName(url.getProtocol(), 
                        url.getHost(),
                        url.getPort(),
                        fileName,
                        url.getUsername(),
                        url.getPassword()).toString();
                xis = new XenaInputSource(msgurl, null);
                xis.setParent((XenaInputSource) input);
            } else {
                xis = (XenaInputSource) input;
            }
            MessageNormaliser messageNormaliser = new MessageNormaliser(msg, normaliserManager);
            
            OutputStream outputStream = null;
            
            try {
                if (doMany) {
                    
                    AbstractFileNamer fileNamer = normaliserManager.getPluginManager().getFileNamerManager().getActiveFileNamer();
                    File messageOutputFile = fileNamer.makeNewXenaFile(xis, messageNormaliser);
                    
                    try {    
                        SAXTransformerFactory transformFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
                        TransformerHandler transformerHandler = transformFactory.newTransformerHandler();
                        messageNormaliser.setProperty("http://xena/url", xis.getSystemId());
                        messageNormaliser.setContentHandler(transformerHandler);
                        
                        messageNormaliser.setProperty("http://xena/file", messageOutputFile);
                        messageNormaliser.setProperty("http://xena/normaliser", messageNormaliser);
                        messageNormaliser.setProperty("http://xena/input", input);
                        
                        if (!messageOutputFile.exists()) {
                            outputStream = new FileOutputStream(messageOutputFile);
                            OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
                            StreamResult streamResult = new StreamResult(osw);
                            transformerHandler.setResult(streamResult);
                        }
                    
                    } catch (TransformerConfigurationException tce) {
                        throw new XenaException(tce);
                    }
                    
                    contentHandler.startElement(URI, "mailbox", "mailbox:item",empty);
                    char[] msgurlCharArray = msgurl.toString().toCharArray();
                    contentHandler.characters(msgurlCharArray, 0, msgurlCharArray.length);
                    contentHandler.endElement(URI, "mailbox", "mailbox:item");
                    
                    messageNormaliser.getContentHandler().startDocument();
                    XMLFilter wrapper = normaliserManager.getPluginManager().getMetaDataWrapperManager().getWrapNormaliser();
                    normaliserManager.parse(messageNormaliser, xis, wrapper);
                    messageNormaliser.getContentHandler().endDocument();
                    
                    //String msgOutputFilename = normaliserDataStore.getOutputFile().getAbsolutePath();
                    String msgOutputFilename = messageOutputFile.getAbsolutePath();
                    
                    
                    contentHandler.startElement(URI, "mailbox", "mailbox:item", empty);
                    char[] fileNameChars = msgOutputFilename.toCharArray();
                    contentHandler.characters(fileNameChars, 0, fileNameChars.length);
                    contentHandler.endElement(URI, "mailbox", "mailbox:item");
                } else {
                    messageNormaliser.setContentHandler(contentHandler);
                    messageNormaliser.parse(xis);
                }
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }
        gofolder.close(false);

    }
    

    public Properties getMailProperties() {
        return mailProperties;
    }

    final static String IMAP_USER = "mail.imap.user";

    final static String IMAP_HOST = "mail.imap.host";

    final static String IMAP_PORT = "mail.imap.port";

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public XmlList getFolders() throws MessagingException {
        return folders;
    }

    public XmlList getFoldersOrAll(Store store) throws MessagingException {
        if (folders == null) {
            folders = allFolders(store, normaliserManager.getPluginManager());
        }
        return folders;
    }

    public void setFolders(List folders) {
        this.folders = new XmlList(folders);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
