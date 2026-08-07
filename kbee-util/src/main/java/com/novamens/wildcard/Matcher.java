package com.novamens.wildcard;

/**
 * @author lleggieri
 * @see java.util.regex.Matcher
 * @version $Id: Matcher.java,v 1.3 2007/03/08 00:46:47 alexis Exp $
 */
public class Matcher {

	/**
	 * the underlying Matcher.
	 */

	private java.util.regex.Matcher matcher;

	/**
	 * Matcher Constructor.
	 * 
	 * @param matcher
	 *            the underlying Matcher.
	 */
	public Matcher(final java.util.regex.Matcher matcher) {
		this.matcher = matcher;
	}

	/**
	 * Method matches.
	 * 
	 * @return true if matcher matches.
	 */
	public boolean matches() {
		return this.matcher.matches();
	}

	/**
	 * WildCard Matcher
	 * 
	 * @param str:
	 *            path expression ; matcher: wildcard expression
	 */
	/*
	 * private boolean match(String str, String matcher) { boolean backSlash; //
	 * true if previous character was a backSlash (so current character should
	 * be interpreted "literally" boolean dblAsterisk; // true if it is a double
	 * asterisk iteration (so it includes slashes) boolean asterisk; // true if
	 * it is an asterisk iteration boolean ret; // return value boolean go; //
	 * true while there are matches between matcher and path characters boolean
	 * condition; // summarizes various boolean values char charStr; // current
	 * character according to index in path string char charMatcher; // current
	 * character according to index in matcher string int strIndex; // current
	 * index in path string int matcherIndex; // current index in matcher string
	 * int strLength; // path string length int matcherLength; // matcher string
	 * length Stack iterators; // the stack for the asterisks iteration
	 * "algorithm"
	 * 
	 * iterators = new Stack(); strIndex = 0; matcherIndex = 0; strLength =
	 * str.length(); matcherLength = matcher.length(); go = true; backSlash =
	 * false; dblAsterisk = false; asterisk = false;
	 * 
	 * condition = (0 < matcherLength) && (0 < strLength);
	 * 
	 * while (asterisk || condition) { //while condition is valid, or condition
	 * isn't valid, but we're iterating in a *
	 * 
	 * if (condition) { // if condition, let's do the regular loop charMatcher =
	 * matcher.charAt(matcherIndex); charStr = str.charAt(strIndex); if
	 * (charMatcher == '*' && !backSlash) { // asterisk dblAsterisk =
	 * (matcherIndex != 0) && (matcher.charAt(matcherIndex - 1) == '*'); // lazy
	 * evaluation matcherIndex++; if ((matcherIndex < matcherLength) &&
	 * (matcher.charAt(matcherIndex) != '*')) { // lazy evaluation
	 * iterators.push( new StackStruct(matcherIndex, strIndex, dblAsterisk)); } }
	 * else { // not asterisk matcherIndex++; if (charMatcher == '\\' &&
	 * !backSlash) { go = true; backSlash = true; } else { strIndex++; backSlash =
	 * false; go = (charMatcher == charStr); } } } else { // if not condition,
	 * we must act if (go) { //found a match? if (matcherIndex == matcherLength) {
	 * if (strIndex == strLength) { // reached the end in both expressions
	 * iterators.clear(); } else { // reached the end in matcher if (asterisk) { //
	 * if we were in an iteration if (dblAsterisk || (str.lastIndexOf('/') <
	 * strIndex)) { // of a double asterisk, or no slashes left in path
	 * iterators.clear(); // then we're done } else { // a slash left, then
	 * incorrect match in this iteration go = false; } } else { // not in an
	 * iteration, then incorrect match go = false; } } } else { if (strIndex ==
	 * strLength) { // reached the end in path only for (int i = matcherIndex; i <
	 * matcherLength; i++) { // if what remains in matcher are only asterisks if
	 * (matcher.charAt(i) != '*') { go = false; break; } } if (go) {
	 * iterators.clear(); // then we're done } } else { go = false; } } } else {
	 * //not a match, so this iteration is over. Continue with next one. // (if
	 * we entered here, asterisk was true, so there is at least one left in the
	 * stack) StackStruct top = (StackStruct) iterators.pop(); matcherIndex =
	 * top.matcherIndex; strIndex = top.strIndex; dblAsterisk = top.dblAsterisk;
	 * 
	 * strIndex++; if (strIndex < strLength) { // continue with current loop if
	 * (!dblAsterisk && (str.charAt(strIndex) == '/')) { // found a slash? go =
	 * (matcher.charAt(matcherIndex) == '/'); } else { iterators.push( new
	 * StackStruct(matcherIndex, strIndex, dblAsterisk)); go = true; } } else { //
	 * reached last character, so a match wasn't found in this iteration
	 * dblAsterisk = false; go = false; } } } condition = go && (matcherIndex <
	 * matcherLength) && (strIndex < strLength); asterisk = !iterators.empty(); } //
	 * endwhile
	 * 
	 * if (go) { //found a match? if (matcherIndex == matcherLength) { if
	 * (strIndex == strLength) { // reached the end in both expressions ret =
	 * true; } else { // reached the end in matcher if (((matcherIndex - 1) > 0) &&
	 * (matcher.charAt(matcherIndex - 1) == '*')) { // if we were in an
	 * iteration if (dblAsterisk || (str.lastIndexOf('/') < strIndex)) { // of a
	 * double asterisk, or no slashes left in path ret = true; // then we're
	 * done } else { // a slash left, then incorrect match in this iteration ret =
	 * false; } } else { // not in an iteration, then incorrect match ret =
	 * false; } } } else { // reached the end in path only ret = true; for (int
	 * i = matcherIndex; i < matcherLength; i++) { // if what remains in matcher
	 * are only asterisks if (matcher.charAt(i) != '*') { ret = false; break; } } } }
	 * else { ret = false; }
	 * 
	 * return ret; }
	 * 
	 * private final class StackStruct {
	 * 
	 * StackStruct() { this.matcherIndex = 0; this.strIndex = 0;
	 * this.dblAsterisk = false; }
	 * 
	 * StackStruct(int matcherIndex, int strIndex, boolean dblAsterisk) {
	 * this.strIndex = strIndex; this.matcherIndex = matcherIndex;
	 * this.dblAsterisk = dblAsterisk; }
	 * 
	 * public int strIndex; public int matcherIndex; public boolean dblAsterisk;
	 *  }
	 */
}
