package bartfastiel.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Component
@EnableScheduling
class WebSocketHandler extends TextWebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<WebSocketSession> activeSessions = new LinkedList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        activeSessions.add(session);
        logger.info("New WebSocket-Connection from {} established (now {} clients listening)", session.getRemoteAddress(), activeSessions.size());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        logger.info("Received text-message from {}:\n{}", session.getRemoteAddress(), message.getPayload());
    }

    @Scheduled(cron = "0/10 * * * * *")
    void sendRecurringMessage() {
        logger.debug("Sending message to " + activeSessions.size() + " listening clients");
        activeSessions.parallelStream().forEach(session -> {
            logger.trace("Sending message to client {}", session.getRemoteAddress());
            try {
                session.sendMessage(new TextMessage("At the third stroke, the time will be " + Instant.now() + " . . ."));
            } catch (IOException | RuntimeException e) {
                logger.warn("Cannot send message to {}", session.getRemoteAddress(), e);
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        activeSessions.remove(session);
        logger.info("Closed WebSocket-Connection from {} (now {} clients listening)", session.getRemoteAddress(), activeSessions.size());
    }
}
