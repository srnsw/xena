package au.gov.naa.digipres.xena.plugin.basic;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * Normalise a date/time to the Xena data-time type.
 *
 * @author Chris Bitmead
 */
public class DateTimeToXenaDateTimeNormaliser extends AbstractNormaliser {
	final static String URI = "http://preservation.naa.gov.au/date-time/1.0";

	JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(DateTimeToXenaDateTimePanel0.class);

	String inputFormat;

	String outputFormat;

	String zoneName = prefs.get("defaultTimeZone", "Australia/ACT");

	boolean showYear = true;

	boolean showMonth = true;

	boolean showDate = true;

	boolean showJulian = false;

	boolean showWeek = false;

	boolean showHour = true;

	boolean showMinute = true;

	boolean showSecond = true;

	boolean showFraction = true;

	boolean showTimeZone = true;

	boolean showDayOfWeek = false;

	public DateTimeToXenaDateTimeNormaliser() {
	}

	public String getName() {
		return "Date/Time";
	}

	public void setShowYear(boolean showYear) {
		this.showYear = showYear;
	}

	public void setShowWeek(boolean showWeek) {
		this.showWeek = showWeek;
	}

	public void setShowTimeZone(boolean showTimeZone) {
		this.showTimeZone = showTimeZone;
	}

	public void setShowSecond(boolean showSecond) {
		this.showSecond = showSecond;
	}

	public void setShowMonth(boolean showMonth) {
		this.showMonth = showMonth;
	}

	public void setShowMinute(boolean showMinute) {
		this.showMinute = showMinute;
	}

	public void setShowFraction(boolean showFraction) {
		this.showFraction = showFraction;
	}

	public void setShowJulian(boolean showJulian) {
		this.showJulian = showJulian;
	}

	public void setShowHour(boolean showHour) {
		this.showHour = showHour;
	}

	public void setShowDate(boolean showDate) {
		this.showDate = showDate;
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}

	public void setInputFormat(String inputFormat) {
		this.inputFormat = inputFormat;
	}

	public void setShowDayOfWeek(boolean showDayOfWeek) {
		this.showDayOfWeek = showDayOfWeek;
	}

	public boolean isShowDate() {
		return showDate;
	}

	public boolean isShowHour() {
		return showHour;
	}

	public boolean isShowJulian() {
		return showJulian;
	}

	public boolean isShowFraction() {
		return showFraction;
	}

	public boolean isShowMinute() {
		return showMinute;
	}

	public boolean isShowMonth() {
		return showMonth;
	}

	public boolean isShowSecond() {
		return showSecond;
	}

	public boolean isShowTimeZone() {
		return showTimeZone;
	}

	public boolean isShowWeek() {
		return showWeek;
	}

	public boolean isShowYear() {
		return showYear;
	}

	public String getZoneName() {
		return zoneName;
	}

	public String getInputFormat() {
		return inputFormat;
	}

	public boolean isShowDayOfWeek() {
		return showDayOfWeek;
	}

	public void parse(InputSource input, NormaliserResults results) 
	throws java.io.IOException, org.xml.sax.SAXException {
		if ((showJulian && showDate)
			|| (showDate && showWeek)
			|| (showJulian && showMonth)
			|| (showWeek && showJulian)
			|| (showDate && showDayOfWeek)
			|| (showMonth && showDayOfWeek)
			|| (showJulian && showDayOfWeek)
			|| (!showSecond && showFraction)) {
			throw new SAXException("Invalid Set of DateTime Options");
		}
		SimpleDateFormat df = new SimpleDateFormat(inputFormat);
		InputStream is = input.getByteStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String dateString = br.readLine();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		ContentHandler ch = getContentHandler();
		try {
			Date date = df.parse(dateString);
			System.out.println("DATE:" + date.toString());
			Calendar cal = Calendar.getInstance();
			TimeZone tz = null;
			if (showTimeZone) {
				tz = TimeZone.getTimeZone(zoneName);
				cal.setTimeZone(tz);
			}
			cal.setTime(date);
			AttributesImpl att = new AttributesImpl();
			ch.startElement(URI, "date-time", "date-time:date-time", att);
			String outString = "";
			if (showYear) {
				nf.setMinimumIntegerDigits(4);
				outString += nf.format(cal.get(Calendar.YEAR));
			} else {
				outString += "-";
			}
			outString += "/";
			if (showMonth) {
				nf.setMinimumIntegerDigits(2);
				outString += nf.format(cal.get(Calendar.MONTH) + 1);
			} else if (showJulian) {
				nf.setMinimumIntegerDigits(3);
				outString += nf.format(cal.get(Calendar.DAY_OF_YEAR));
			} else if (showWeek) {
				nf.setMinimumIntegerDigits(2);
				outString += nf.format(cal.get(Calendar.WEEK_OF_YEAR));
			} else {
				outString += "-";
			}
			if (!showJulian) {
				outString += "/";
			}
			nf.setMinimumIntegerDigits(2);
			if (showDate) {
				outString += nf.format(cal.get(Calendar.DAY_OF_MONTH));
			} else {
				outString += "-";
			}
			outString += "T";
			if (showHour) {
				outString += nf.format(cal.get(Calendar.HOUR_OF_DAY));
			} else {
				outString += "-";
			}
			outString += ":";
			if (showMinute) {
				outString += nf.format(cal.get(Calendar.MINUTE));
			} else {
				outString += "-";
			}
			outString += ":";
			if (showSecond) {
				outString += nf.format(cal.get(Calendar.SECOND));
			} else {
				outString += "-";
			}
			/*
			 *  PROBLEM - We are only supporting millisecond, not
			 *  microsecond etc etc. Probably good enough for the moment
			 */
			if (showFraction) {
				outString += "." + cal.get(Calendar.MILLISECOND);
			}
			if (showTimeZone && zoneName != null) {
				int milli = cal.get(Calendar.ZONE_OFFSET);
				int hoffset = milliToHour(milli);
				int moffset = milliToMinute(milli);
				if (hoffset == 0 && moffset == 0) {
					outString += "Z";
				} else {
					if (0 < hoffset) {
						outString += "+";
					}
					// The colon between hours and minutes is optional.
					// We don't use it because the RFC datetime doesn't use
					// it.
					outString += nf.format(hoffset) + nf.format(moffset);
				}
			}
			ch.characters(outString.toCharArray(), 0, outString.length());
			ch.endElement(URI, "date-time", "date-time:date-time");
		} catch (ParseException e) {
			throw new SAXException("Error Parsing Date: " + dateString, e);
		}
	}

	int milliToHour(int milli) {
		return milli / (1000 * 60 * 60);
	}

	int milliToMinute(int milli) {
		return (milli / (1000 * 60)) % 60;
	}
}
