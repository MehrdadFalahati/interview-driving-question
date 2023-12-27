package com.github.mehrdad.falahati.service;

public interface DriverAcceptanceService {
    /**
     * @param driverId Requested driver id
     * @return true if driver accepted the request, false otherwise
     */
    boolean getDriverResponse(int driverId);
}
