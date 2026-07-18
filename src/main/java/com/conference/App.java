package com.conference;

import com.conference.desktop.DesktopApplication;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@SpringBootApplication
@EnableWebSocket
public class App {
    public static void main(String[] args) {
        boolean consoleMode = Arrays.asList(args).contains("--console") || GraphicsEnvironment.isHeadless();
        if (!consoleMode) {
            DesktopApplication.launch(args);
            return;
        }

        SpringApplication app = new SpringApplication(App.class);
        app.run(args);
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
