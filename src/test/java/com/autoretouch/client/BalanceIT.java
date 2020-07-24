package com.autoretouch.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class BalanceIT {

    @Test
    void shouldReturnAccountBalance() throws IOException {
        AutoRetouchClient underTest = DeviceAuthIT.createOrGetClient();

        BigInteger balance = underTest.getBalance();

        assertThat(balance).isGreaterThanOrEqualTo(BigInteger.ZERO);
    }
}
