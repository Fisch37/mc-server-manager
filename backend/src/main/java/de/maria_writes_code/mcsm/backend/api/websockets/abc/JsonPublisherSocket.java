package de.maria_writes_code.mcsm.backend.api.websockets.abc;

import java.io.IOException;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonPublisherSocket extends PublisherSocket {
    protected static final ObjectMapper MAPPER = new ObjectMapper();

    protected void publish(Object object) throws PublisherSocket.IOExceptionGroup, JsonProcessingException {
        publish(new TextMessage(
            MAPPER.writeValueAsString(object)
        ));
    }

    protected static void sendMessage(WebSocketSession session, Object message) throws IOException, JsonProcessingException {
        session.sendMessage(new TextMessage(
            MAPPER.writeValueAsString(message)
        ));
    }
}
