package com.upgrad.patterns.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stat {

    @JsonProperty("confirmed")
    private Float confirmed;

    @JsonProperty("deaths")
    private Integer deaths;

    @JsonProperty("recovered")
    private Integer recovered;

    public Float getConfirmed() {
        return this.confirmed;
    }

    public void setConfirmed(Float confirmed) {
        this.confirmed = confirmed;
    }

    public Integer getDeaths() {
        return this.deaths;
    }

    public void setDeaths(Integer deaths) {
        this.deaths = deaths;
    }

    public Integer getRecovered() {
        return this.recovered;
    }

    public void setRecovered(Integer recovered) {
        this.recovered = recovered;
    }
}