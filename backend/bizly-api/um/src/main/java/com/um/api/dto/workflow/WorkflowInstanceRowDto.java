package com.um.api.dto.workflow;

import java.time.LocalDateTime;

public class WorkflowInstanceRowDto {

	private Long id;
	private String screenName;
	private String actionName;
	private String triggeredByUsername;
	private Integer currentLevel;
	private String status;
	private LocalDateTime createdAt;
	private Long businessId;

	/** Levels configured on the workflow at creation time (from {@code UM_WORKFLOW_CONFIG}). */
	private Integer levelRequired;

	/** Progress through the approval chain (mirrors {@code UM_WORKFLOW_INSTANCE.current_level}). */
	private Integer levelCompleted;

	/** Raw JSON payload for review modals. */
	private String payloadJson;

	/** Short hint for checkers (roles / usernames) — not a security boundary. */
	private String checkerSummary;

	/** True when the current user may approve or reject this row. */
	private Boolean canApprove;

	/**
	 * True when the current user is the initiator or is listed on maker roles / maker users for this workflow
	 * (used by the UI to keep maker-side rows read-only even if the user also holds checker roles elsewhere).
	 */
	private Boolean makerParticipant;

	/** Resolution comment when rejected (or optional approve note). */
	private String checkerComment;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getTriggeredByUsername() {
		return triggeredByUsername;
	}

	public void setTriggeredByUsername(String triggeredByUsername) {
		this.triggeredByUsername = triggeredByUsername;
	}

	public Integer getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(Integer currentLevel) {
		this.currentLevel = currentLevel;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public Integer getLevelRequired() {
		return levelRequired;
	}

	public void setLevelRequired(Integer levelRequired) {
		this.levelRequired = levelRequired;
	}

	public Integer getLevelCompleted() {
		return levelCompleted;
	}

	public void setLevelCompleted(Integer levelCompleted) {
		this.levelCompleted = levelCompleted;
	}

	public String getPayloadJson() {
		return payloadJson;
	}

	public void setPayloadJson(String payloadJson) {
		this.payloadJson = payloadJson;
	}

	public String getCheckerSummary() {
		return checkerSummary;
	}

	public void setCheckerSummary(String checkerSummary) {
		this.checkerSummary = checkerSummary;
	}

	public Boolean getCanApprove() {
		return canApprove;
	}

	public void setCanApprove(Boolean canApprove) {
		this.canApprove = canApprove;
	}

	public Boolean getMakerParticipant() {
		return makerParticipant;
	}

	public void setMakerParticipant(Boolean makerParticipant) {
		this.makerParticipant = makerParticipant;
	}

	public String getCheckerComment() {
		return checkerComment;
	}

	public void setCheckerComment(String checkerComment) {
		this.checkerComment = checkerComment;
	}
}
