package com.pe.allpafood.api.transaction.notification.bussiness;


public interface INotificationService<T> {
    void sendNotification(T message);
}
