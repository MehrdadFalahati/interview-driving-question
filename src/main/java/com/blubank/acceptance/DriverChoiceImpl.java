package com.github.mehrdad.falahati.acceptance;



import com.github.mehrdad.falahati.service.DriverAcceptanceService;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DriverChoiceImpl implements DriverChoice {
    private final DriverAcceptanceService driverService;
    private final ExecutorService service = Executors.newFixedThreadPool(2);

    public DriverChoiceImpl(DriverAcceptanceService driverService) {
        this.driverService = driverService;
    }

    @Override
    public Integer getDriverAcceptance(int driverId1, int driverId2) {
        return getAllDriverAcceptance(List.of(driverId1, driverId2));
    }

    private Integer getAllDriverAcceptance(List<Integer> driverIds) {
        List<CompletableFuture<DriverAcceptanceResponse>> list = new ArrayList<>();
        for (int id: driverIds) {
            var response = CompletableFuture
                    .supplyAsync(() -> getResponse(id), service)
                    .completeOnTimeout(new DriverAcceptanceResponse(id, false, Integer.MAX_VALUE), 400,
                            TimeUnit.MILLISECONDS);
            list.add(response);
        }

        Optional<DriverAcceptanceResponse> result = getAllResponse(list).stream()
                .filter(DriverAcceptanceResponse::isAccepted)
                .min(Comparator.comparing(DriverAcceptanceResponse::delay));

        return result.isPresent() ? result.get().id() : null;
    }

    private List<DriverAcceptanceResponse> getAllResponse(List<CompletableFuture<DriverAcceptanceResponse>> list) {
        return CompletableFuture
                .allOf(list.toArray(CompletableFuture[]::new))
                .thenApply(result -> convertToList(list))
                .join();
    }

    private List<DriverAcceptanceResponse> convertToList(List<CompletableFuture<DriverAcceptanceResponse>> list) {
        return list.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    private DriverAcceptanceResponse getResponse(int id) {
        var start = LocalTime.now();
        boolean isAccepted = driverService.getDriverResponse(id);
        var end = LocalTime.now();
        return new DriverAcceptanceResponse(id, isAccepted, (int) Duration.between(start, end).toMillis());
    }
    
    private record DriverAcceptanceResponse(int id, boolean isAccepted, int delay) {}
}
