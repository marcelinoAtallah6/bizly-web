package com.notification.workflow;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Replaces {@code {{token}}} placeholders in template strings.
 */
@Component
public class TemplatePlaceholderRenderer {

	private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)\\}\\}");

	public String render(String template, Map<String, String> values) {
		if (template == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		Matcher m = PLACEHOLDER.matcher(template);
		while (m.find()) {
			String key = m.group(1);
			String replacement = values.getOrDefault(key, "");
			m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
