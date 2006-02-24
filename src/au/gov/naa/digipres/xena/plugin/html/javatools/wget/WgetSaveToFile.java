package au.gov.naa.digipres.xena.plugin.html.javatools.wget;
import java.io.*;
import java.net.*;

public class WgetSaveToFile implements WgetProcessUrl {
	File dir;

	public WgetSaveToFile(File dir) {
		this.dir = dir;
	}

	public void process(URLConnection connection) throws IOException, UnknownServiceException {
		if (Wget.isRedirected(connection)) {
			return;
		}
		URL url = connection.getURL();
//		System.out.println("URL: " + url);
		// Figure out the file name to save as.
		String path = url.getPath();
		if (url.getQuery() != null) {
			path += "?" + url.getQuery();
		}
		if (path.equals("") || path.endsWith("/")) {
			path = path + "index.html";
		}
		File newDir = new File(dir, url.getHost());
		File file = new File(newDir, path);
		// Make sure directory exists
		if (file.getParentFile().exists()) {
			if (!file.getParentFile().isDirectory()) {
				throw new IOException(file.getParent() + " is not a directory");
			}
		} else {
			file.getParentFile().mkdirs();
		}
		// Save the file
		BufferedOutputStream bos = null;
		try {
			InputStream is = connection.getInputStream();
			bos = new BufferedOutputStream(new FileOutputStream(file));
			byte[] buf = new byte[4096];
			int len;
			while (0 <= (len = is.read(buf))) {
				bos.write(buf, 0, len);
			}
		} finally {
			if (bos != null) {
				bos.close();
			}
		}
	}
}