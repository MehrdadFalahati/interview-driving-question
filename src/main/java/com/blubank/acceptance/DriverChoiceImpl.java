package com.blubank.acceptance;

import com.blubank.service.DriverAcceptanceService;

public class DriverChoiceImpl implements DriverChoice {
    private final DriverAcceptanceService driverService;

    public DriverChoiceImpl(DriverAcceptanceService driverService) {
        this.driverService = driverService;
    }

    @Override
    public Integer getDriverAcceptance(int driverId1, int driverId2) {
        return 0;
    }
}
