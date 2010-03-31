import java.io.ByteArrayInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * 
 */

/**
 * Test the Java Sound SPI of the JFLAC decoder for consistency.
 * <p>
 * Usage: run this class. On error, it prints TEST FAILED as last output and
 * returns an exit code of 1. If everything is OK, it prints TEST OK as last
 * line and returns an exit code of 0. You need to have the root directory of
 * the workspace containing the META-INF directory in the classpath, or the full
 * jar in the classpath.
 * 
 * @author Florian Bomers
 */
public class TestJavaSoundSPI {

	private static boolean isSameBitsChannelSampleRate(AudioFormat af1,
			AudioFormat af2) {
		return (af1.getSampleSizeInBits() == af2.getSampleSizeInBits())
				&& (af1.getChannels() == af2.getChannels())
				&& (af1.getSampleRate() == af2.getSampleRate());
	}

	/**
	 * @param neg if true, then not being able to convert yields a successful
	 *            test
	 * @return true if test succeeded
	 */
	private static boolean checkConversion(AudioFormat srcFormat,
			AudioFormat targetFormat, boolean neg) {
		AudioInputStream srcStream = new AudioInputStream(
				new ByteArrayInputStream(new byte[0]), srcFormat, -1);
		boolean couldConvert = true;
		try {
			AudioInputStream targetStream = AudioSystem.getAudioInputStream(
					targetFormat, srcStream);
			// always a failure if src bits != target bits, or src channels !=
			// target channels
			targetFormat = targetStream.getFormat();
			if (!isSameBitsChannelSampleRate(srcFormat, targetFormat)) {
				System.out.println("ERROR");
				System.out.println("  converted stream has "
						+ targetFormat.getChannels() + " channels, "
						+ targetFormat.getSampleSizeInBits() + " bits, and "
						+ targetFormat.getSampleRate() + "Hz, "
						+ " but source stream had " + srcFormat.getChannels()
						+ " channels, " + srcFormat.getSampleSizeInBits()
						+ " bits, and " + srcFormat.getSampleRate() + "Hz");
				return false;
			}
		} catch (Exception e) {
			couldConvert = false;
		}
		if (couldConvert == neg) {
			System.out.println("ERROR");
			System.out.println("  can" + ((!couldConvert) ? "not" : "")
					+ " convert from " + srcFormat + " to " + targetFormat);
			return false;
		}
		System.out.println("OK");
		return true;
	}

	private static boolean checkConversion(AudioFormat srcFormat,
			AudioFormat.Encoding targetEncoding, boolean neg) {
		AudioInputStream srcStream = new AudioInputStream(
				new ByteArrayInputStream(new byte[0]), srcFormat, -1);
		boolean couldConvert = true;
		try {
			AudioInputStream targetStream = AudioSystem.getAudioInputStream(
					targetEncoding, srcStream);
			// always a failure if src bits != target bits, or src channels !=
			// target channels
			AudioFormat targetFormat = targetStream.getFormat();
			if (!isSameBitsChannelSampleRate(srcFormat, targetFormat)) {
				System.out.println("ERROR");
				System.out.println("  converted stream has "
						+ targetFormat.getChannels() + " channels, "
						+ targetFormat.getSampleSizeInBits() + " bits, and "
						+ targetFormat.getSampleRate() + "Hz, "
						+ " but source stream had " + srcFormat.getChannels()
						+ " channels, " + srcFormat.getSampleSizeInBits()
						+ " bits, and " + srcFormat.getSampleRate() + "Hz");
				return false;
			}
		} catch (Exception e) {
			couldConvert = false;
		}
		if (couldConvert == neg) {
			System.out.println("ERROR");
			System.out.println("  can" + ((!couldConvert) ? "not" : "")
					+ " convert from " + srcFormat + " to " + targetEncoding);
			return false;
		}
		System.out.println("OK");
		return true;
	}

	private static boolean checkDirect(AudioFormat srcFormat, boolean neg) {
		AudioFormat targetFormat = new AudioFormat(srcFormat.getSampleRate(),
				srcFormat.getSampleSizeInBits(), srcFormat.getChannels(), true,
				false);
		return checkConversion(srcFormat, targetFormat, neg);
	}

	/**
	 * test that the decoder discovery works correctly. This test is an
	 * end-to-end test, i.e. it uses AudioSystem for decoder discovery.
	 */
	public static boolean testDecoder() {
		boolean success = true;
		try {
			System.out.println("Positive tests that setting up a decoded stream works.");
			int[] bitsOK = {
					8, 16, 24
			};
			for (int channel = 1; channel <= 2; channel++) {
				for (int bit = 0; bit < bitsOK.length; bit++) {
					AudioFormat srcFormat = new AudioFormat(
							org.kc7bfi.jflac.sound.spi.FlacEncoding.FLAC,
							16000, bitsOK[bit], channel, -1, -1, false);
					System.out.print("can convert 1: " + channel + "-channel, "
							+ bitsOK[bit] + "-bit FLAC to PCM...");
					if (!checkDirect(srcFormat, false)) {
						success = false;
					}
					System.out.print("can convert 2: " + channel + "-channel, "
							+ bitsOK[bit] + "-bit FLAC to PCM...");
					if (!checkConversion(srcFormat,
							AudioFormat.Encoding.PCM_SIGNED, false)) {
						success = false;
					}
				}
			}

			System.out.println();
			System.out.println("Negative tests that the decoder does not claim to be able to convert non-supported formats.");
			int[] bitsCorrupt = {
					0, 4, 10, 20, 32
			};
			for (int channel = 2; channel <= 3; channel++) {
				for (int bit = 0; bit < bitsCorrupt.length; bit++) {
					AudioFormat srcFormat = new AudioFormat(
							org.kc7bfi.jflac.sound.spi.FlacEncoding.FLAC,
							16000, bitsCorrupt[bit], channel, -1, -1, false);
					System.out.print("cannot convert 1: " + channel
							+ "-channel, " + bitsCorrupt[bit]
							+ "-bit FLAC to PCM...");
					if (!checkDirect(srcFormat, true)) {
						success = false;
					}
					System.out.print("cannot convert 2: " + channel
							+ "-channel, " + bitsCorrupt[bit]
							+ "-bit FLAC to PCM...");
					if (!checkConversion(srcFormat,
							AudioFormat.Encoding.PCM_SIGNED, true)) {
						success = false;
					}
				}
			}
			int[] channelsCorrupt = {
					0, 3, 5, 10
			};
			for (int i = 0; i < channelsCorrupt.length; i++) {
				for (int bit = 16; bit < 40; bit += 16) {
					AudioFormat srcFormat = new AudioFormat(
							org.kc7bfi.jflac.sound.spi.FlacEncoding.FLAC,
							16000, bit, channelsCorrupt[i], -1, -1, false);
					System.out.print("cannot convert 1: " + channelsCorrupt[i]
							+ "-channel, " + bit + "-bit FLAC to PCM...");
					if (!checkDirect(srcFormat, true)) {
						success = false;
					}
					System.out.print("cannot convert 2: " + channelsCorrupt[i]
							+ "-channel, " + bit + "-bit FLAC to PCM...");
					if (!checkConversion(srcFormat,
							AudioFormat.Encoding.PCM_SIGNED, true)) {
						success = false;
					}
				}
			}
			System.out.println();
			System.out.println("Negative tests that the decoder does not claim to be able to convert bits, sample rate, or channels");

			float[] sampleRatesOK = {
					16000, 22050, 44100, 96000
			};

			for (int srcChannel = 1; srcChannel <= 2; srcChannel++) {
				for (int targetChannel = 1; targetChannel <= 2; targetChannel++) {
					for (int srcBitIndex = 0; srcBitIndex < bitsOK.length; srcBitIndex++) {
						for (int targetBitIndex = 0; targetBitIndex < bitsOK.length; targetBitIndex++) {
							for (int srcSampleRateIndex = 0; srcSampleRateIndex < sampleRatesOK.length; srcSampleRateIndex++) {
								for (int targetSampleRateIndex = 0; targetSampleRateIndex < sampleRatesOK.length; targetSampleRateIndex++) {
									int srcBit = bitsOK[srcBitIndex];
									int targetBit = bitsOK[targetBitIndex];
									float srcSampleRate = sampleRatesOK[srcSampleRateIndex];
									float targetSampleRate = sampleRatesOK[targetSampleRateIndex];
									if ((srcBit != targetBit)
											|| (srcChannel != targetChannel)
											|| (srcSampleRate != targetSampleRate)) {
										// OK, at least one combination of
										// src/target parameters is not the same
										AudioFormat srcFormat = new AudioFormat(
												org.kc7bfi.jflac.sound.spi.FlacEncoding.FLAC,
												srcSampleRate, srcBit,
												srcChannel, -1, -1, false);
										AudioFormat targetFormat = new AudioFormat(
												targetSampleRate, targetBit,
												targetChannel, true, false);
										System.out.print("cannot convert: "
												+ srcChannel + "-channel, "
												+ srcBit + "-bit, "
												+ srcSampleRate + "Hz FLAC to "
												+ targetChannel + "-channel, "
												+ targetBit + "-bit, "
												+ targetSampleRate
												+ "Hz PCM...");
										if (!checkConversion(srcFormat,
												targetFormat, true)) {
											success = false;
										}
									}
								}
							}
						}
					}
				}
			}
			System.out.println();
			System.out.println("Negative tests that the decoder does not claim to be able to decode to big endian");

			for (int srcChannel = 1; srcChannel <= 2; srcChannel++) {
				for (int srcBitIndex = 0; srcBitIndex < bitsOK.length; srcBitIndex++) {
					int srcBit = bitsOK[srcBitIndex];
					float srcSampleRate = 22050;
					AudioFormat srcFormat = new AudioFormat(
							org.kc7bfi.jflac.sound.spi.FlacEncoding.FLAC,
							srcSampleRate, srcBit, srcChannel, -1, -1, false);
					AudioFormat targetFormat = new AudioFormat(srcSampleRate,
							srcBit, srcChannel, true, true);
					System.out.print("cannot convert: " + srcChannel
							+ "-channel, " + srcBit + "-bit" + " FLAC to "
							+ srcChannel + "-channel, " + srcBit + "-bit,"
							+ " big-endian PCM...");
					if (!checkConversion(srcFormat, targetFormat, true)) {
						success = false;
					}
				}
			}

		} catch (Throwable t) {
			t.printStackTrace();
			success = false;
		}
		return success;
	}

	public static boolean runTest() {
		boolean success = true;
		if (!testDecoder()) {
			success = false;
		}
		return success;
	}

	public static void main(String[] args) {
		if (runTest()) {
			System.out.println("TEST OK");
			System.exit(0);
		} else {
			System.out.println("TEST FAILED");
			System.exit(1);
		}
	}
}
