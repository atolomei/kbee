package com.novamens.email;

import com.novamens.service.SystemService;

import java.time.OffsetDateTime;

public interface EmailFetchingService extends SystemService {
    void start();

    OffsetDateTime getStartDateTime();

    boolean isStarted();
}
