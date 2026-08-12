package com.rvy.scanner.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionContractsResponse {

    @JsonProperty("option_contracts")
    private List<OptionContractDto> optionContracts;

    @JsonProperty("next_page_token")
    @JsonAlias("page_token")
    private String nextPageToken;

    public List<OptionContractDto> getOptionContracts() {
        return optionContracts;
    }

    public void setOptionContracts(List<OptionContractDto> optionContracts) {
        this.optionContracts = optionContracts;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
}
