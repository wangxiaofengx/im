package com.conference.service;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class IceService {

    @Value("${ice.stun-server-url}")
    String iceServerUrl;

    List<String> iceServers;

    private final RestTemplate restTemplate;

    public IceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Scheduled(cron = "0 0 3 * * *")
    @SneakyThrows
    public List<String> getIceUrl() {

        this.iceServers = FileUtils.readLines(new File(new URI(iceServerUrl)), StandardCharsets.UTF_8);

        return this.iceServers;
    }

    public List<String> getIceServers() {
        return iceServers;
    }

    public void setIceServers(List<String> iceServers) {
        this.iceServers = iceServers;
    }
}
