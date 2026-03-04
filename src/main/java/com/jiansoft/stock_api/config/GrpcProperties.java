package com.jiansoft.stock_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * gRPC 連線設定。
 *
 * <p>此設定會對應到 {@code application.yaml} 中的 {@code app.grpc} 區段。
 */
@ConfigurationProperties(prefix = "app.grpc")
public class GrpcProperties {

    private String host = "";
    private int port;
    private boolean useTls = true;
    private boolean trustAllCertificates;

    /**
     * 取得 gRPC 主機名稱。
     *
     * @return gRPC 服務主機名稱
     */
    public String getHost() {
        return host;
    }

    /**
     * 設定 gRPC 主機名稱。
     *
     * @param host gRPC 服務主機名稱
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 取得 gRPC 連線埠號。
     *
     * @return gRPC 服務連線埠號
     */
    public int getPort() {
        return port;
    }

    /**
     * 設定 gRPC 連線埠號。
     *
     * @param port gRPC 服務連線埠號
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 是否使用 TLS 加密連線。
     *
     * @return {@code true} 表示使用 TLS；否則為 {@code false}
     */
    public boolean isUseTls() {
        return useTls;
    }

    /**
     * 設定是否使用 TLS 加密連線。
     *
     * @param useTls {@code true} 表示使用 TLS；否則為 {@code false}
     */
    public void setUseTls(boolean useTls) {
        this.useTls = useTls;
    }

    /**
     * 是否信任所有伺服器憑證。
     *
     * <p>通常僅建議在開發環境或內網環境使用。
     *
     * @return {@code true} 表示信任所有憑證；否則為 {@code false}
     */
    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

    /**
     * 設定是否信任所有伺服器憑證。
     *
     * @param trustAllCertificates {@code true} 表示信任所有憑證；否則為 {@code false}
     */
    public void setTrustAllCertificates(boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }
}
