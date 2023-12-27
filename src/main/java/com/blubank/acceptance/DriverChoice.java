package com.github.mehrdad.falahati.acceptance;

public interface DriverChoice {
    /**
     * @param driverId1 ID of the first driver
     * @param driverId2 ID of the second driver
     * @return ID of the driver who first accepts the ride, or null if both reject
     */
    Integer getDriverAcceptance(int driverId1, int driverId2);
}
