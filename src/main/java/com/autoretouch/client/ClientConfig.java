package com.autoretouch.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "autoretouch")
public class ClientConfig {
    @Value("${client.id:V8EkfbxtBi93cAySTVWAecEum4d6pt4J}") public String clientId;
    @Value("${client.audience:https://api.autoretouch.com/}") public String audience;
    @Value("${client.scope:offline_access openid}") public String scope;
    @Value("${client.auth.domain:https://auth.autoretouch.com/}") public String authDomain;
    @Value("${client.api.server:https://api.autoretouch.com/}") public String apiServer;
    @Value("${client.credentials:}") public String credentialsPath;
}
