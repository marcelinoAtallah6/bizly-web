package com.notification.workflow;

public class WelcomePendingUser {

	private final long userId;
	private final String username;
	private final String email;
	private final String firstName;
	private final String lastName;

	public WelcomePendingUser(long userId, String username, String email, String firstName, String lastName) {
		this.userId = userId;
		this.username = username;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public long getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
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
