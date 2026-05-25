package com.um.api.dto.workflow.engine;

public class EmailTemplateKeyDto {

	private String templateKey;
	private String subjectPreview;

	public EmailTemplateKeyDto() {
	}

	public EmailTemplateKeyDto(String templateKey, String subjectPreview) {
		this.templateKey = templateKey;
		this.subjectPreview = subjectPreview;
	}

	public String getTemplateKey() {
		return templateKey;
	}

	public void setTemplateKey(String templateKey) {
		this.templateKey = templateKey;
	}

	public String getSubjectPreview() {
		return subjectPreview;
	}

	public void setSubjectPreview(String subjectPreview) {
		this.subjectPreview = subjectPreview;
	}
}
