package com.novamens.wildcard;

/**
 * @author lleggieri
 * @version $Id: Pattern.java,v 1.5 2008/05/12 22:47:00 alexis Exp $
 */
public class Pattern {
	private java.util.regex.Pattern pattern;

	private String wildcard;

	private Pattern(final String wildcard) {
		this.wildcard = wildcard;
		this.pattern = java.util.regex.Pattern
				.compile(wildcardToRegExp(wildcard));
	}

	/**
	 * Method wildcardToRegExp.
	 * 
	 * @param wildcard
	 * @return String
	 */
	static private String wildcardToRegExp(final String wildcard) {
		final StringBuilder tmp = new StringBuilder();
		int i = 0;
		char c;

		while (i < wildcard.length()) {
			c = wildcard.charAt(i);
			if (c == '*') {
				i++;
				if (i < wildcard.length() && wildcard.charAt(i) == '*') {
					tmp.append(".*"); //$NON-NLS-1$
					i++;
				} else {
					tmp.append("[.[^/]]*"); //$NON-NLS-1$
				}
			} else {
				tmp.append(c);
				i++;
			}
		}

		return tmp.toString();
	}

	public static Pattern compile(final String wildcard) {
		return new Pattern(wildcard);
	}

	public String pattern() {
		return this.wildcard;
	}

	public static boolean matches(final String wildcard,
			final CharSequence input) {
		final Matcher m = compile(wildcard).matcher(input);
		return m.matches();
	}

	/**
	 * Method matcher.
	 * 
	 * @param input
	 * @return Matcher
	 */
	public Matcher matcher(final CharSequence input) {
		return new Matcher(this.pattern.matcher(input));
	}
}
