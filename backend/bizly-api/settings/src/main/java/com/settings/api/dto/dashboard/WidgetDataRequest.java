package com.settings.api.dto.dashboard;

import javax.validation.constraints.NotNull;

public class WidgetDataRequest {

	@NotNull
	private Long widgetId;

	public Long getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(Long widgetId) {
		this.widgetId = widgetId;
	}
}
