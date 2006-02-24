package au.gov.naa.digipres.xena.plugin.html.javatools.util;
import java.io.*;

public class CopyFile {

	public static void recursiveCopyFile(File from, File to) throws IOException {
		recursiveCopyFile(from, to, null);
	}

	public static void recursiveCopyFile(File from, File to, CopyFilter filter) throws
		IOException {
		if (from.isDirectory()) {
			File[] listing = from.listFiles();
			for (int i = 0; i < listing.length; i++) {
				File newTo = new File(to, listing[i].getName());
				recursiveCopyFile(listing[i], newTo);
			}
		} else {
			FileInputStream is = new FileInputStream(from);
			if (filter != null) {
				to = filter.filter(from, to);
			}
			to.getParentFile().mkdirs();
			FileOutputStream os = new FileOutputStream(to);
			try {
				byte[] buf = new byte[4096];
				int n;
				while (0 < (n = is.read(buf))) {
					os.write(buf, 0, n);
				}
			} finally {
				is.close();
				os.close();
			}
		}
	}

	public interface CopyFilter {
		public File filter(File from, File suggestedTo);
	}
}
