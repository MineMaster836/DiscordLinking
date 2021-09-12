package me.discordlinking.utils;

import apple.utilities.request.AppleRequestService;

public class QueueNowService extends AppleRequestService {
    private final static QueueNowService instance = new QueueNowService();

    public static QueueNowService get() {
        return instance;
    }


    @Override
    public int getRequestsPerTimeUnit() {
        return 10;
    }

    @Override
    public int getTimeUnitMillis() {
        return 0;
    }

    @Override
    public int getSafeGuardBuffer() {
        return 0;
    }
}
