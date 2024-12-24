package com.conference.service;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IceService {

    private static final Logger log = LoggerFactory.getLogger(IceService.class);

    @Value("${ice.stun-server-url}")
    String iceServerUrl;

    List<String> iceServers;

    private final RestTemplate restTemplate;

    public IceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            this.fetchIceServers();
        } catch (Exception e) {
            log.error("Failed to fetch ice server urls", e);
            this.readIceFile();
        }
        log.info("ice server list: {}",iceServers);
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void trackIceServer() {
        try {
            this.fetchIceServers();
        } catch (Exception e) {
            log.error("Failed to fetch ice server urls", e);
        }
    }

    @SneakyThrows
    public void fetchIceServers() {
        log.info("Fetching ice server urls from {}", iceServerUrl);
        String response = restTemplate.getForObject(iceServerUrl, String.class);
        this.iceServers = Arrays.asList(response.split("\\r?\\n"));
        log.info("ice server urls {}", iceServers);
    }

    @SneakyThrows
    public void readIceFile() {
        ClassPathResource resource = new ClassPathResource("data/valid_hosts.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            this.iceServers = reader.lines().collect(Collectors.toList());
        }
    }

    public List<String> getIceServers() {
        return iceServers;
    }

    public void setIceServers(List<String> iceServers) {
        this.iceServers = iceServers;
    }
}
