package com.example.autoreconnect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ReconnectStateService {
    private static final AtomicInteger consecutiveAutoReconnectAttempts = new AtomicInteger(0);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "AutoReconnectStateScheduler");
        thread.setDaemon(true);
        return thread;
    });

    private static volatile ScheduledFuture<?> pendingReset;
    private static volatile String lastDecisionType = "none";
    private static volatile String lastDecisionDetails = "";

    private ReconnectStateService() {
    }

    public static int getCurrentAttempts() {
        return consecutiveAutoReconnectAttempts.get();
    }

    public static synchronized void recordAutoReconnectAttempt() {
        cancelPendingReset();
        int value = consecutiveAutoReconnectAttempts.incrementAndGet();
        recordDecision("attempt_incremented", "attempt=" + value);
    }

    public static synchronized void resetAttempts(String reason) {
        cancelPendingReset();
        int previous = consecutiveAutoReconnectAttempts.getAndSet(0);
        recordDecision("attempt_reset", "reason=" + reason + ", previous=" + previous);
    }

    public static synchronized void scheduleResetAfterStableConnection(String expectedServerAddress, int delaySeconds) {
        if (delaySeconds <= 0) {
            resetAttempts("stable_connection_immediate");
            return;
        }

        cancelPendingReset();
        pendingReset = scheduler.schedule(() -> {
            Minecraft client = Minecraft.getInstance();
            if (client == null) {
                return;
            }

            client.execute(() -> {
                if (client.getConnection() == null) {
                    return;
                }

                ServerData currentServer = client.getCurrentServer();
                if (currentServer == null || currentServer.ip == null || expectedServerAddress == null) {
                    return;
                }

                if (!currentServer.ip.equalsIgnoreCase(expectedServerAddress)) {
                    return;
                }

                resetAttempts("stable_connection_" + delaySeconds + "s");
            });
        }, delaySeconds, TimeUnit.SECONDS);

        recordDecision("attempt_reset_scheduled",
                "server=" + String.valueOf(expectedServerAddress) + ", delay=" + delaySeconds + "s");
    }

    public static synchronized void recordDecision(String decisionType, String details) {
        lastDecisionType = decisionType == null ? "unknown" : decisionType;
        lastDecisionDetails = details == null ? "" : details;
        DebugLog.log("reliability-decision type='" + lastDecisionType + "' details='" + lastDecisionDetails + "'");
    }

    public static String getLastDecisionType() {
        return lastDecisionType;
    }

    public static String getLastDecisionDetails() {
        return lastDecisionDetails;
    }

    private static void cancelPendingReset() {
        if (pendingReset != null) {
            pendingReset.cancel(false);
            pendingReset = null;
        }
    }
}
