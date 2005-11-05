/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 10:19:59 PM
 */
package com.sahi.playback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sahi.config.Configuration;
import com.sahi.util.Utils;

public abstract class SahiScript {
	private static Logger logger = Configuration
	.getLogger("com.sahi.playback.SahiScript");
	protected String script;
	private static final String PREFIX = "sahiAdd(\"";
	private static final String CONJUNCTION = "\", \"";
	private static final String SUFFIX = "\")";
	protected String scriptName;
	protected String fqn;
	protected String base = "";
	protected ArrayList parents;

	public SahiScript() {
	}
	
	public SahiScript(String fileName) {
		parents = new ArrayList();
		init(fileName);
	}

	public SahiScript(String fileName, ArrayList parents) {
		this.parents = parents;
		init(fileName);
	}

	protected void setScript(String s) {
		script = modify(s);
	}

	public String modifiedScript() {
		return script == null ? "" : script;
	}

	String modify(String s) {
		StringBuffer sb = new StringBuffer();
		StringTokenizer tokenizer = new StringTokenizer(s, "\n");
		int lineNumber = 0;
		while (tokenizer.hasMoreTokens()) {
			lineNumber++;
			String line = tokenizer.nextToken().trim();
			if ("".equals(line))
				continue;
			if (line.startsWith("_")) {
				if (line.startsWith("_include")) {
					sb.append(processInclude(line));
				} else {
					sb.append(PREFIX);
					sb.append(modifyFunctionNames(separateVariables(Utils
							.escapeDoubleQuotes(line))));
					sb.append(CONJUNCTION);
					sb.append(scriptName);
					sb.append(" : ");
					sb.append(lineNumber);
					sb.append(SUFFIX);
					sb.append("\r\n");
				}
			} else {
				sb.append(modifyFunctionNames(line));
				sb.append("\r\n");
			}
		}
		String toString = sb.toString();
		logger.fine(toString);
		return toString;
	}

	private String processInclude(String line) {
		final String include = getInclude(line);
		if (include != null && !parents.contains(getFQN(include))) {
			ArrayList clone = (ArrayList) parents.clone();
			clone.add(this.fqn);
			return getNewInstance(include, clone).modifiedScript();
		}
		return "";
	}

	abstract String getFQN(String include);

	abstract SahiScript getNewInstance(String scriptName,
			ArrayList parentScriptName);

	static String getInclude(String line) {
		final String re = ".*_include\\([\"|'](.*)[\"|']\\).*";
		Pattern p = Pattern.compile(re);
		Matcher m = p.matcher(line.trim());
		if (m.matches()) {
			return line.substring(m.start(1), m.end(1));
		}
		return null;
	}

	public static String modifyFunctionNames(String unmodified) {
		unmodified = stripSahiFromFunctionNames(unmodified);
		String modified = unmodified.replaceAll(getRegExp(false), "sahi$0");
		return modified;
	}

	public static String stripSahiFromFunctionNames(String unmodified) {
		String modified = unmodified.replaceAll(getRegExp(true), "$1");
		return modified;
	}

	private static String getRegExp(boolean isForStripping) {
		ArrayList keywords = getKeyWords();
		StringBuffer sb = new StringBuffer();
		int size = keywords.size();
		if (isForStripping)
			sb.append("sahi");
		sb.append("(");
		for (int i = 0; i < size; i++) {
			String keyword = (String) keywords.get(i);
			sb.append(keyword);
			if (i != size - 1)
				sb.append("|");
		}
		sb.append(")");
		return sb.toString();
	}

	private static ArrayList getKeyWords() {
		ArrayList keywords = new ArrayList();
		keywords.add("_accessor");
		keywords.add("_alert");
		keywords.add("_assertEqual");
		keywords.add("_assertNotEqual");
		keywords.add("_assertNotNull");
		keywords.add("_assertNull");
		keywords.add("_assertTrue");
		keywords.add("_assertNotTrue");
		keywords.add("_button");
		keywords.add("_check");
		keywords.add("_checkbox");
		keywords.add("_click");
		keywords.add("_clickLinkByAccessor");
		keywords.add("_getCellText");
		keywords.add("_getSelectedText");
		keywords.add("_image");
		keywords.add("_link");
		keywords.add("_password");
		keywords.add("_radio");
		keywords.add("_select");
		keywords.add("_setSelected");
		keywords.add("_setValue");
		keywords.add("_simulateEvent");
		keywords.add("_submit");
		keywords.add("_textarea");
		keywords.add("_textbox");
		keywords.add("_event");
		keywords.add("_eval");
		keywords.add("_setGlobal");
		keywords.add("_getGlobal");
		keywords.add("_wait");
		keywords.add("_random");
		keywords.add("_savedRandom");
		keywords.add("_cell");
		keywords.add("_table");
		keywords.add("_containsText");
		keywords.add("_containsHTML");
		keywords.add("_popup");
		return keywords;
	}

	String separateVariables(String s) {
		StringBuffer sb = new StringBuffer();
		char c = ' ';
		char prev = ' ';
		boolean isVar = false;
		int len = s.length();
		for (int i = 0; i < len; i++) {
			c = s.charAt(i);
			if (c == '$' && prev != '\\' && i + 1 < len
					&& Character.isJavaIdentifierStart(s.charAt(i + 1))) {
				isVar = true;
				sb.append("\"+s_v(");
			}
			if (isVar && !(Character.isJavaIdentifierPart(c) || c == '.')) { 
				sb.append(")+\"");
				isVar = false;
			}
			sb.append(c);
			prev = c;
		}
		return sb.toString();
	}

	protected String read(InputStream in) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int c = ' ';
		try {
			while ((c = in.read()) != -1) {
				out.write(c);
			}
		} catch (IOException ste) {
			ste.printStackTrace();
		}
		return new String(out.toByteArray());
	}

	public String getScriptName() {
		return scriptName;
	}

	protected void setNameAndBase(String url) {
		this.fqn = url;
		int lastIndexOfSlash = url.lastIndexOf("/");
		if (lastIndexOfSlash != -1) {
			this.scriptName = url.substring(lastIndexOfSlash + 1);
			this.base = url.substring(0, lastIndexOfSlash + 1);
		}
	}

	protected void init(String url) {
		setNameAndBase(url);
		loadScript(url);
	}

	protected abstract void loadScript(String url);
}