package de.maria_writes_code.mcsm.backend.features.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component 
public class AutostartHandler implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutostartHandler.class);
    @Autowired
    private ServerManager servers;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        for (
            var server
            : (Iterable<ActiveServer>)servers.stream()
            .filter(s -> s.getServer().isAutostart())
            ::iterator
        ) {
            try {
                server.start();
            } catch (IOException e) {
                LOGGER.error("Failed to start server", e);
            }
        }
        LOGGER.info("Started all autostart servers");
    }
}
