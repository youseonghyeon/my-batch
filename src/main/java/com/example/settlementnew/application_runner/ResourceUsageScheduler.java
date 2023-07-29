package com.example.settlementnew.application_runner;

import com.example.settlementnew.socket.SocketSender;
import com.sun.management.OperatingSystemMXBean;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class ResourceUsageScheduler {

    private final SocketSender socketSender;

    @Value("${os.memory-size}")
    private long memorySize;

    @Scheduled(fixedDelay = 1000)
    public void resourceUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double cpuLoad = osBean.getCpuLoad() * 100;
        Runtime runtime = Runtime.getRuntime();
        double usedMemory = (double) (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 / 1024 * 100 / memorySize;

        // 편의상 jvm mem 사용량을 전체 사용량으로 표현
        String memUse = String.format("%.2f", usedMemory) + "%";
        String cpuUse = String.format("%.2f", cpuLoad) + "%";
        socketSender.sendResourceMessage(cpuUse, memUse);
    }
}
