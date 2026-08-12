package com.rvy.scanner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "alpaca")
public class AlpacaProperties {

    private String dataBaseUrl = "https://data.alpaca.markets";
    private String tradeBaseUrl = "https://paper-api.alpaca.markets";
    private String apiKey = "";
    private String apiSecret = "";
    private int chainPageLimit = 1000;
    private int contractsPageLimit = 10000;

    public String getDataBaseUrl() {
        return dataBaseUrl;
    }

    public void setDataBaseUrl(String dataBaseUrl) {
        this.dataBaseUrl = dataBaseUrl;
    }

    public String getTradeBaseUrl() {
        return tradeBaseUrl;
    }

    public void setTradeBaseUrl(String tradeBaseUrl) {
        this.tradeBaseUrl = tradeBaseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public int getChainPageLimit() {
        return chainPageLimit;
    }

    public void setChainPageLimit(int chainPageLimit) {
        this.chainPageLimit = chainPageLimit;
    }

    public int getContractsPageLimit() {
        return contractsPageLimit;
    }

    public void setContractsPageLimit(int contractsPageLimit) {
        this.contractsPageLimit = contractsPageLimit;
    }

    public boolean hasCredentials() {
        return apiKey != null && !apiKey.isBlank() && apiSecret != null && !apiSecret.isBlank();
    }
}
