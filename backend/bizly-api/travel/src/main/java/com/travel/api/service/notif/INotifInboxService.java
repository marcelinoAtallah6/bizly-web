package com.travel.api.service.notif;

public interface INotifInboxService {

	boolean publish(NotifPublishRequest request);

	boolean publishOnce(NotifPublishRequest request);
}
