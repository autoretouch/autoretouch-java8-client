package com.autoretouch.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.List;

public class Page<E> {
    @JsonProperty("entries")
    private List<E> entries;
    @JsonProperty("total")
    private BigInteger total;

    public List<E> getEntries() {
        return entries;
    }

    public void setEntries(List<E> entries) {
        this.entries = entries;
    }

    public BigInteger getTotal() {
        return total;
    }

    public void setTotal(BigInteger total) {
        this.total = total;
    }
}