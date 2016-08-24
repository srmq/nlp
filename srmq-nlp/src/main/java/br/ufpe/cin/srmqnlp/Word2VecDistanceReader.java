package br.ufpe.cin.srmqnlp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Word2VecDistanceReader {

	public static void main(String[] args) throws IOException {
		BufferedInputStream googleData = new BufferedInputStream(new FileInputStream(new File("/home/srmq/devel/word2vec/trunk/GoogleNews-vectors-negative300.bin")));
		
		long words = readLongText(googleData);
		int embedSize = (int)readLongText(googleData);
		System.out.print(words + " " + embedSize);

		byte floatBytes[] = new byte[4];

		for (long w = 0; w < words; w++) {
			String word = readWord(googleData);
			System.out.print("\n" + word);
			float[] embed = new float[embedSize];
			for (int i = 0; i < embedSize; i++) {
				for (int k = 0; k < floatBytes.length ; k++) {
					floatBytes[k] = (byte)googleData.read();
				}
				final float f = ByteBuffer.wrap(floatBytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
				embed[i] = f;
				System.out.print(" " + f);
			}
			//System.out.println(" " + Arrays.toString(embed));
		}

	
	}

	private static long readLongText(BufferedInputStream googleData) throws IOException {
		String longText = readCharToken(googleData);
		return Long.parseLong(longText);
	}

	private static String readCharToken(BufferedInputStream googleData)
			throws IOException {
		StringBuffer buf = new StringBuffer();
		char c = (char)googleData.read();
		while(Character.isWhitespace(c)) {
			c = (char)googleData.read();
		}
		while(!Character.isWhitespace(c)) {
			buf.append(c);
			c = (char)googleData.read();
		}
		return buf.toString();
	}
	
	private static String readWord(BufferedInputStream googleData)
			throws IOException {
		StringBuffer buf = new StringBuffer();
		char c = (char)googleData.read();
		while(c != ' ') {
			if (c != '\n') buf.append(c);
			c = (char)googleData.read();
		}
		return buf.toString();
	}
	

}
