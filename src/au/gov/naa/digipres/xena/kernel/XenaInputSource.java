package au.gov.naa.digipres.xena.kernel;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.InputSource;

import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * The standard XMLReader takes an InputSource as input. We enhance that to provide
 * some extra data, but we still retain compatibility with the XMLReader/InputSource
 * so that people can plug in standard XMLReaders.
 *
 * It's really hard to get the class hierarchy right for all the classes that
 * inherit from XenaInputSource. In theory, XenaInputSource is too specific and
 * has too much stuff dedicated to input sources that derive from Files, when
 * this should go in a sub-class to make sense with the existance of things
 * like ByteArrayInputSource.
 *
 * But in practice, most cases require regular Files, and it has been hard to
 * separate this out. At some time another effort should be made to do so however.
 *
 * @author Chris Bitmead
 */
public class XenaInputSource extends InputSource {
	
    private Type type;
	private String mimeType = "";
	private URLConnection conn;
    protected File file;
    private String unencodedRelativeFileName = "";
    private XenaInputSource parent;
    private boolean isTmpFile;
    private Date lastModified;
    
    
	protected List<InputStream> openedFiles = new ArrayList<InputStream>();

	public XenaInputSource(String systemId, Type type) {
		super(systemId);
		this.type = type;
	}

	public XenaInputSource(URL url, Type type) {
		this(getURI(url), type);
	}

    public XenaInputSource(URLConnection conn, Type type) throws IOException {
        this(getURI(conn.getURL()), type);
        this.conn = conn;
    }
    
    public XenaInputSource(File file, Type type) throws FileNotFoundException {
        this(file.toURI().toASCIIString(), type);
        this.file = file;
        if (!file.exists()) {
            throw new FileNotFoundException(file.toString() + " not  found");
        }
        this.lastModified = new Date(file.lastModified());
    }
    
    public XenaInputSource(File file) throws FileNotFoundException {
        //TODO: probably should have code in here to figure out the type or something...
        this(file.toURI().toASCIIString(), null);
        this.file = file;
        if (!file.exists()) {
            throw new FileNotFoundException(file.toString() + " not  found");
        }
        this.lastModified = new Date(file.lastModified());
    }
    

    protected static String getURI(URL url) {
        try {
            return new URI(url.getProtocol(), null, url.getHost(), url.getPort(), url.getPath(), url.getQuery(), null).toASCIIString();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
	public XenaInputSource getParent() {
		return parent;
	}

	public void setParent(XenaInputSource parent) {
		this.parent = parent;
	}

	public XenaInputSource getUltimateParent() {
		XenaInputSource rtn = null;
		if (parent == null) {
			rtn = this;
		} else {
			rtn = parent.getUltimateParent();
		}
        assert rtn != null;
		return rtn;
	}

	public File getUltimateFile() {
		File rtn = null;
		if (file != null) {
			rtn = file;
		} else if (parent == null) {
			return null;
		} else {
			rtn = parent.getUltimateFile();
		}
		return rtn;
	}

	public void delete() {
		if (file != null) {
			file.delete();
		}
	}

	public File getFile() {
		return file;
	}

	public Type getType() {
		return type;
	}
    
    public Type getType(PluginManager pluginManager, boolean forceInitialisation) throws XenaException { 
        if (forceInitialisation) {
            if (type == null) {
                initType(pluginManager);
            }
        }
        return type;
    }

	public void setType(Type type) {
		this.type = type;
	}

	public Reader getCharacterStream() {
		if (getEncoding() == null) {
			return new InputStreamReader(getByteStream());
		} else {
			try {
				return new InputStreamReader(getByteStream(), getEncoding());
			} catch (UnsupportedEncodingException x) {
				x.printStackTrace();
				return null;
			}
		}
	}

	protected URLConnection getConnection(URL url) throws IOException {
		if (conn == null) {
			return url.openConnection();
		} else {
			return conn;
		}
	}

	public InputStream getByteStream() {
		try {
			URL url = new URL(this.getSystemId());
			URLConnection conn = getConnection(url);
			//setType(conn.getContentType());
			InputStream rtn = null;
			InputStream is = null;
			try {
				is = conn.getInputStream();
			} catch (IOException x) {
				if (0 <= x.toString().indexOf("Too many open files")) {
					System.out.println("Trying to reclaim file descriptors");
					System.gc();
					is = conn.getInputStream();
				} else {
					throw x;
				}
			}
			rtn = new BufferedInputStream(is);
			openedFiles.add(rtn);
			return rtn;
		} catch (MalformedURLException x) {
			throw new RuntimeException(x);
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	
    
	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
    
	protected void setType(String content) {
		if (content == null) {
			return;
		}
		int i = content.indexOf(';');
		if (0 < i) {
			setMimeType(content.substring(0, i).trim());
			String charset = "charset=";
			String rest = content.substring(i + 1).trim();
			if (rest.startsWith(charset)) {
				setEncoding(rest.substring(charset.length()));
			}
		} else {
			setMimeType(content);
		}
	}

	public void close() throws IOException {
		Iterator it = openedFiles.iterator();
		while (it.hasNext()) {
			InputStream is = (InputStream)it.next();
			is.close();
		}
		openedFiles.clear();
		if (isTmpFile && file != null) {
			file.delete();
		}
	}

	public void setTmpFile(boolean v) {
		this.isTmpFile = v;
		if (file != null) {
			file.deleteOnExit();
		}
	}

	public String toString() {
		return "System id: " + this.getSystemId() + " and type(?): " + this.type + " and mime type: " + this.mimeType;
	}

    
    public void initType(PluginManager pluginManager) throws XenaException {
        Type guessedType;
        //TODO: this is sucky, since we shouldnt have to use the singleton here. oh well.
        try {
            guessedType = pluginManager.getGuesserManager().mostLikelyType(this);
        } catch (IOException e) {
            throw new XenaException("IOException caught whilst attempting to guess the type of this file.");
        }
        this.type = guessedType;
    }

    /**
     * @return Returns the unencodedRelativeFileName.
     */
    public String getUnencodedRelativeFileName() {
        return unencodedRelativeFileName;
    }

    /**
     * @return Returns the lastModified.
     */
    public Date getLastModified() {
        return lastModified;
    }

	@Override
	public boolean equals(Object obj)
	{	
		if (obj instanceof XenaInputSource)
		{
			XenaInputSource xis = (XenaInputSource)obj;
			return this.getSystemId().equals(xis.getSystemId());
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return this.getSystemId().hashCode();
	}

    
}
