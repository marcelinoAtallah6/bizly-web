package com.notification.workflow;

public class BirthdayCandidate {

	private final long userId;
	private final String email;
	private final String firstName;
	private final String lastName;

	public BirthdayCandidate(long userId, String email, String firstName, String lastName) {
		this.userId = userId;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public long getUserId() {
		return userId;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
}
