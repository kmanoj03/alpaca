package com.rvy.scanner.client.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionChainResponse {

    @JsonProperty("next_page_token")
    private String nextPageToken;

    private Map<String, OptionSnapshotDto> snapshots;

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public Map<String, OptionSnapshotDto> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(Map<String, OptionSnapshotDto> snapshots) {
        this.snapshots = snapshots;
    }
}
