package com.jiansoft.stock_api.service;

import com.jiansoft.stock_api.config.GrpcProperties;
import com.jiansoft.stock_api.dto.stock.HolidayScheduleDto;
import com.jiansoft.stock_api.grpc.HolidayScheduleRequest;
import com.jiansoft.stock_api.grpc.StockGrpc;
import com.jiansoft.stock_api.support.MessageException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

/**
 * 股票 gRPC 用戶端。
 *
 * <p>負責與外部 gRPC 服務建立連線並取得補充資料。
 */
@Component
public class StockGrpcClient {

    private final GrpcProperties grpcProperties;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile ManagedChannel channel;
    private volatile StockGrpc.StockBlockingStub stockBlockingStub;

    public StockGrpcClient(GrpcProperties grpcProperties) {
        this.grpcProperties = grpcProperties;
    }

    /**
     * 取得指定年度的休市日資料。
     *
     * @param year 年度
     * @return 休市日清單
     */
    public List<HolidayScheduleDto> fetchHolidaySchedule(int year) {
        try {
            var reply = blockingStub().fetchHolidaySchedule(
                HolidayScheduleRequest.newBuilder()
                    .setYear(year)
                    .build()
            );

            return reply.getHolidayList().stream()
                .map(item -> new HolidayScheduleDto(item.getDate(), item.getWhy()))
                .toList();
        } catch (Exception ex) {
            throw new MessageException("Failed to fetch holiday schedule from gRPC service.", ex);
        }
    }

    /**
     * 在 Bean 銷毀前關閉 gRPC 連線。
     */
    @PreDestroy
    void shutdown() {
        ManagedChannel currentChannel = channel;
        if (currentChannel != null) {
            currentChannel.shutdownNow();
        }
    }

    private StockGrpc.StockBlockingStub blockingStub() {
        StockGrpc.StockBlockingStub currentStub = stockBlockingStub;
        if (currentStub != null) {
            return currentStub;
        }

        lock.lock();
        try {
            if (stockBlockingStub == null) {
                channel = createChannel();
                stockBlockingStub = StockGrpc.newBlockingStub(channel);
            }

            return stockBlockingStub;
        } finally {
            lock.unlock();
        }
    }

    private ManagedChannel createChannel() {
        try {
            if (!grpcProperties.isUseTls()) {
                return ManagedChannelBuilder
                    .forAddress(grpcProperties.getHost(), grpcProperties.getPort())
                    .usePlaintext()
                    .build();
            }

            NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress(grpcProperties.getHost(), grpcProperties.getPort())
                .useTransportSecurity();

            if (grpcProperties.isTrustAllCertificates()) {
                builder.sslContext(
                    GrpcSslContexts.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build()
                );
            }

            return builder.build();
        } catch (Exception ex) {
            throw new MessageException("Failed to initialize gRPC client.", ex);
        }
    }
}
