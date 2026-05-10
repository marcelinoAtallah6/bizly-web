package com.settings.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SqlQueryValidationServiceTest {

	private SqlQueryValidationService svc;

	@BeforeEach
	void setUp() {
		svc = new SqlQueryValidationService();
	}

	@Test
	void acceptsSimpleSelect() {
		assertDoesNotThrow(() -> svc.validateSelectOnly("SELECT * FROM UM.KYC_CUSTOMER WHERE ROWNUM <= 10"));
	}

	@Test
	void acceptsWithSelect() {
		assertDoesNotThrow(() -> svc.validateSelectOnly("WITH t AS (SELECT 1 x FROM DUAL) SELECT * FROM t"));
	}

	@Test
	void rejectsInsert() {
		assertThrows(IllegalArgumentException.class, () -> svc.validateSelectOnly("INSERT INTO T VALUES (1)"));
	}

	@Test
	void rejectsDeleteInCommentIgnoredStillChecksBody() {
		assertThrows(IllegalArgumentException.class,
				() -> svc.validateSelectOnly("SELECT * FROM T; DELETE FROM T WHERE 1=1"));
	}

}
