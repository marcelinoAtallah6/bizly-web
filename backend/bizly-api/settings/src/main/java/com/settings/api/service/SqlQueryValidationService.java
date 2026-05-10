package com.settings.api.service;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class SqlQueryValidationService {

	private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

	public void validateSelectOnly(String sql) {
		if (sql == null || sql.isBlank()) {
			throw new IllegalArgumentException("SQL is empty");
		}
		String stripped = stripCommentsAndNormalize(sql);
		if (stripped.isEmpty()) {
			throw new IllegalArgumentException("SQL is empty after removing comments");
		}
		String upper = stripped.toUpperCase(Locale.ROOT);
		if (!upper.startsWith("SELECT") && !upper.startsWith("WITH")) {
			throw new IllegalArgumentException("Only SELECT queries are allowed (WITH ... SELECT is permitted)");
		}
		String forbiddenCheck = upper.replaceAll("\\s+", " ");
		String[] banned = { " INSERT ", " UPDATE ", " DELETE ", " MERGE ", " DROP ", " ALTER ", " TRUNCATE ",
				" CREATE ", " GRANT ", " REVOKE ", " EXEC ", " EXECUTE ", " CALL " };
		for (String b : banned) {
			if (forbiddenCheck.contains(b)) {
				throw new IllegalArgumentException("Forbidden keyword in query");
			}
		}
		if (stripped.indexOf(';') >= 0 && stripped.lastIndexOf(';') < stripped.length() - 1) {
			throw new IllegalArgumentException("Multiple SQL statements are not allowed");
		}
	}

	public String stripCommentsAndNormalize(String sql) {
		String noBlock = BLOCK_COMMENT.matcher(sql).replaceAll(" ");
		StringBuilder sb = new StringBuilder();
		for (String line : noBlock.split("\n")) {
			int dash = line.indexOf("--");
			if (dash >= 0) {
				sb.append(line, 0, dash).append('\n');
			} else {
				sb.append(line).append('\n');
			}
		}
		return sb.toString().trim();
	}
}
