package com.notification.workflow;

public class CustomerBirthdayCandidate {

	private final long customerId;
	private final String email;
	private final String firstName;
	private final String lastName;
	private final String displayName;

	public CustomerBirthdayCandidate(long customerId, String email, String firstName, String lastName,
			String displayName) {
		this.customerId = customerId;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.displayName = displayName;
	}

	public long getCustomerId() {
		return customerId;
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

	public String getDisplayName() {
		return displayName;
	}
}
