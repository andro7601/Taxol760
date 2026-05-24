    package com.taxol760.service.WebSocket;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.taxol760.service.auth.CurrentUserService;
    import com.taxol760.service.auth.JwtService;
    import com.taxol760.service.driver.DriverService;
    import com.taxol760.service.ride.RideService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Component;
    import org.springframework.web.socket.CloseStatus;
    import org.springframework.web.socket.TextMessage;
    import org.springframework.web.socket.WebSocketSession;
    import org.springframework.web.socket.handler.TextWebSocketHandler;

    import java.util.List;
    import java.util.Map;
    import java.util.concurrent.ConcurrentHashMap;


    @Component
    @RequiredArgsConstructor
    public class WebSocketHandler extends TextWebSocketHandler {
        private final Map<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();
        private final ObjectMapper objectMapper;
        private final DriverService driverService;
        private final RideService rideService;
        private final JwtService jwtService;

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            String query = session.getUri().getQuery(); // "token=eyJhbG..."
            String token = query.substring(6); // strip "token="

            int id = jwtService.extractUserId(token).intValue();
            session.getAttributes().put("userId", id);
            sessions.put(id, session);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session,
                                          CloseStatus status) {
            int id =(int) session.getAttributes().get("userId");
            sessions.remove(id);
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            try {
                String payload = message.getPayload();
                JsonNode json = objectMapper.readTree(payload);
                handleLocation(session, json);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        protected void handleLocation(WebSocketSession session, JsonNode json) {
            try {
                int userId = (int) session.getAttributes().get("userId");
                driverService.updateLocation(userId,
                        json.get("longitude").asDouble(),
                        json.get("latitude").asDouble());

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            } finally {
                SecurityContextHolder.clearContext(); // always clean up
            }
        }

        public void notifyDriver(int driverId, Object rideRequest) throws Exception {
            WebSocketSession session = sessions.get(driverId);
            if (session != null && session.isOpen()) {
                String json = objectMapper.writeValueAsString(rideRequest);
                session.sendMessage(new TextMessage(json));
            }
        }
    }