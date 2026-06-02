package pk.jl.pasir_lebda_jan.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addSession(String email, WebSocketSession session) {
        sessions.put(email, session);
    }

    public void removeSession(String email) {
        sessions.remove(email);
    }

    public void sendNotification(String email, Object notification) {
        WebSocketSession session = sessions.get(email);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                System.err.println("Error sending notification to " + email + ": " + e.getMessage());
            }
        }
    }
}
