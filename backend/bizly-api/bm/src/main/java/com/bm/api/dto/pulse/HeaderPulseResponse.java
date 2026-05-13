package com.bm.api.dto.pulse;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeaderPulseResponse {

	private Long appointmentsToday;
	private Long appointmentsOpenToday;
	private Long appointmentsUpcoming24h;
	private Long salesCountToday;
	private Double salesAmountToday;

	public Long getAppointmentsToday() {
		return appointmentsToday;
	}

	public void setAppointmentsToday(Long appointmentsToday) {
		this.appointmentsToday = appointmentsToday;
	}

	public Long getAppointmentsOpenToday() {
		return appointmentsOpenToday;
	}

	public void setAppointmentsOpenToday(Long appointmentsOpenToday) {
		this.appointmentsOpenToday = appointmentsOpenToday;
	}

	public Long getAppointmentsUpcoming24h() {
		return appointmentsUpcoming24h;
	}

	public void setAppointmentsUpcoming24h(Long appointmentsUpcoming24h) {
		this.appointmentsUpcoming24h = appointmentsUpcoming24h;
	}

	public Long getSalesCountToday() {
		return salesCountToday;
	}

	public void setSalesCountToday(Long salesCountToday) {
		this.salesCountToday = salesCountToday;
	}

	public Double getSalesAmountToday() {
		return salesAmountToday;
	}

	public void setSalesAmountToday(Double salesAmountToday) {
		this.salesAmountToday = salesAmountToday;
	}
}
