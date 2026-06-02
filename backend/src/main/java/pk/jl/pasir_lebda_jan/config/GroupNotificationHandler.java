package pk.jl.pasir_lebda_jan.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pk.jl.pasir_lebda_jan.service.NotificationService;

import java.security.Principal;

@Component
public class GroupNotificationHandler extends TextWebSocketHandler {
    private final NotificationService notificationService;

    public GroupNotificationHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Principal principal = session.getPrincipal();
        if (principal != null) {
            notificationService.addSession(principal.getName(), session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Principal principal = session.getPrincipal();
        if (principal != null) {
            notificationService.removeSession(principal.getName());
        }
    }
}
