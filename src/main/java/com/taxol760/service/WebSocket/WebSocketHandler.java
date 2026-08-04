    package com.taxol760.service.WebSocket;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.taxol760.databaseANDcache.model.ride.RideModel;
    import com.taxol760.service.auth.JwtService;
    import com.taxol760.service.driver.DriverService;
    import com.taxol760.service.ride.RideService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Component;
    import org.springframework.web.socket.CloseStatus;
    import org.springframework.web.socket.TextMessage;
    import org.springframework.web.socket.WebSocketSession;
    import org.springframework.web.socket.handler.TextWebSocketHandler;

    import java.util.Map;
    import java.util.concurrent.ConcurrentHashMap;


    @Component
    @RequiredArgsConstructor
    public class WebSocketHandler extends TextWebSocketHandler {
        private final Map<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();
        private final ObjectMapper objectMapper;
        private final DriverService driverService;
        private final JwtService jwtService;

        @org.springframework.context.annotation.Lazy
        @org.springframework.beans.factory.annotation.Autowired
        private RideService rideService;

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
            System.out.println("went into session driver: id");
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
                Long driverId = driverService.getDriverByUserId((long) userId).getId();
                
                double lon = json.get("longitude").asDouble();
                double lat = json.get("latitude").asDouble();
                
                driverService.updateLocation(driverId.intValue(), lon, lat);

                // Broadcast location to rider if in an active ride
                RideModel activeRide = rideService.getActiveRideForDriver(driverId);
                if (activeRide != null) {
                    Long riderUserId = activeRide.getRider().getId();
                    WebSocketSession riderSession = sessions.get(riderUserId.intValue());
                    if (riderSession != null && riderSession.isOpen()) {
                        Map<String, Object> payload = Map.of(
                                "type", "DRIVER_LOCATION",
                                "rideId", activeRide.getId(),
                                "longitude", lon,
                                "latitude", lat
                        );
                        riderSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
                    }
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            } finally {
                SecurityContextHolder.clearContext(); // always clean up
            }
        }

        public void notifyDriverOfRide(Long driverUserId, RideModel ride) {
            System.out.println("notifyDriverOfRide called with driverUserId: " + driverUserId);
            System.out.println("ride id: " + ride.getId());
            System.out.println("sessions: " + sessions.keySet());
            try {
                WebSocketSession session = sessions.get(driverUserId.intValue());
                if (session != null && session.isOpen()) {
                    Map<String, Object> payload = Map.of(
                            "type", "RIDE_REQUEST",
                            "rideId", ride.getId(),
                            "pickupLat", ride.getPickupLatitude(),
                            "pickupLon", ride.getPickupLongitude(),
                            "dropoffLat", ride.getDropoffLatitude(),
                            "dropoffLon", ride.getDropoffLongitude()
                    );
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
                } else {
                    System.out.println("No session found for driverUserId: " + driverUserId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void notifyRiderOfReject(int driderUserId, RideModel ride) {
            try {
                WebSocketSession session = sessions.get(driderUserId);
                if (session != null && session.isOpen()) {
                    Map<String, Object> payload = Map.of(
                            "type", "RIDE_ACCEPTED",
                            "rideId", ride.getId(),
                            "driverId", ride.getDriver().getId()
                    );
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
                }
            } catch (Exception e) {
                System.out.println("Failed to notify rider: " + e.getMessage());
            }
        }
    }