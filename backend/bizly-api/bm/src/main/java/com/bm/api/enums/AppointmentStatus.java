package com.bm.api.enums;

public enum AppointmentStatus {

	PENDING,
	CONFIRMED,
	CANCELLED,
	COMPLETED;

	public String calendarColor() {
		switch (this) {
		case CONFIRMED:
			return "green";
		case CANCELLED:
			return "red";
		case PENDING:
			return "yellow";
		case COMPLETED:
			return "blue";
		default:
			return "gray";
		}
	}
}
