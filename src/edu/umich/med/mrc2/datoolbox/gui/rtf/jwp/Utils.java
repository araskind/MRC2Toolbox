package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;

public class Utils {
	public static final char[] WORD_SEPARATORS =
			new char[]{' ', '\t', '\n', '\r', '\f', '.', ',', ':', '-', '(', ')',
			'[', ']', '{', '}', '<', '>', '/', '|', '\\', '\'', '\"'};

	public static boolean isSeparator(char ch) {
		int k = 0;
		while (k < WORD_SEPARATORS.length) {
			if (ch == WORD_SEPARATORS[k]) {
				return true;
			}
			++k;
		}
		return false;
	}
}
