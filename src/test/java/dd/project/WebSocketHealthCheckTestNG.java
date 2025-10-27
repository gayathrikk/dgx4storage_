package dd.project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;
import java.util.HashMap;
import java.util.concurrent.*;

import javax.mail.*;
import javax.mail.internet.*;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

public class WebSocketHealthCheckTestNG {
    
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    private static final int WEBSOCKET_TIMEOUT = 15; // 15 seconds
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Configuration - externalize these in production
    private static final String SMTP_HOST = System.getProperty("smtp.host", "smtp.gmail.com");
    private static final String SMTP_PORT = System.getProperty("smtp.port", "587");
    private static final String EMAIL_USERNAME = System.getProperty("email.username", "automationsoftware25@gmail.com");
    private static final String EMAIL_PASSWORD = System.getProperty("email.password", "wjzcgaramsqvagxu");
    private static final String FROM_EMAIL = System.getProperty("from.email", "gayathri@htic.iitm.ac.in");
    
    private Map<String, String> webSockets;
    private Map<String, String> httpUrls;
    private SoftAssert softAssert;

    @BeforeClass
    public void setupTestData() {
        System.out.println("🚀 Starting AI Agent Health Check with TestNG at " + LocalDateTime.now().format(TIME_FORMATTER));
        
        webSockets = new HashMap<>();
        webSockets.put("apollo2.humanbrain.in WebSocket", "wss://apollo2.humanbrain.in/aiAgentServer/ws/ai_agent");
        // Add more WebSocket endpoints as needed
        // webSockets.put("dev2mani.humanbrain.in WebSocket", "wss://dev2mani.humanbrain.in/aiAgentServer/ws/ai_agent");

        httpUrls = new HashMap<>();
        httpUrls.put("Apollo2 Recommendation API", "https://apollo2.humanbrain.in/aiAgentServer/agent/recomendation?user_query=hi&page_context=%22%7B%5C%22ssid%5C%22%3A85%2C%5C%22seriesType%5C%22%3A%5C%22NISSL%5C%22%2C%5C%22secid%5C%22%3A52%2C%5C%22biosampleId%5C%22%3A%5C%22201%5C%22%7D%22&page=Atlas%20Editor&first=true&action_context=null");
        httpUrls.put("Apollo2 Recommendation API (secid=1513)", "https://apollo2.humanbrain.in/aiAgentServer/agent/recomendation?user_query=hi&page_context=%22%7B%5C%22ssid%5C%22%3A85%2C%5C%22seriesType%5C%22%3A%5C%22NISSL%5C%22%2C%5C%22secid%5C%22%3A1513%2C%5C%22biosampleId%5C%22%3A%5C%22201%5C%22%7D%22&page=Atlas%20Editor&first=true&action_context=null");
        
        System.out.println("📋 Test Configuration:");
        System.out.println("   WebSocket Endpoints: " + webSockets.size());
        System.out.println("   HTTP API Endpoints: " + httpUrls.size());
        System.out.println("=" * 60);
        
        softAssert = new SoftAssert();
    }

    @Test(priority = 1, groups = {"websocket", "smoke"})
    public void testApollo2WebSocket() {
        String serverName = "apollo2.humanbrain.in WebSocket";
        String webSocketUrl = webSockets.get(serverName);
        
        try {
            boolean isHealthy = testWebSocketConnection(serverName, webSocketUrl);
            Assert.assertTrue(isHealthy, "WebSocket connection to " + serverName + " failed");
        } catch (Exception e) {
            sendAlertMail(serverName, e.getMessage(), "222 1000", "Divya D", 193, "Neurovoyager");
            Assert.fail("Exception while testing " + serverName + ": " + e.getMessage());
        }
    }

    @Test(priority = 2, groups = {"http", "smoke"})
    public void testApollo2RecommendationAPISecid52() {
        String apiName = "Apollo2 Recommendation API";
        String url = httpUrls.get(apiName);
        
        boolean isHealthy = testHttpGetUrl(apiName, url);
        Assert.assertTrue(isHealthy, "HTTP API " + apiName + " health check failed");
    }

    @Test(priority = 2, groups = {"http", "smoke"})
    public void testApollo2RecommendationAPISecid1513() {
        String apiName = "Apollo2 Recommendation API (secid=1513)";
        String url = httpUrls.get(apiName);
        
        boolean isHealthy = testHttpGetUrl(apiName, url);
        Assert.assertTrue(isHealthy, "HTTP API " + apiName + " health check failed");
    }

    @Test(priority = 3, groups = {"comprehensive"}, dependsOnGroups = {"smoke"})
    public void testAllEndpointsComprehensive() {
        System.out.println("🔍 Running comprehensive health check for all endpoints...");
        
        // Test all WebSocket connections
        for (Map.Entry<String, String> entry : webSockets.entrySet()) {
            String serverName = entry.getKey();
            String webSocketUrl = entry.getValue();
            try {
                boolean isHealthy = testWebSocketConnection(serverName, webSocketUrl);
                softAssert.assertTrue(isHealthy, "WebSocket " + serverName + " failed");
            } catch (Exception e) {
                sendAlertMail(serverName, e.getMessage(), "222 1000", "Divya D", 193, "Neurovoyager");
                softAssert.fail("Exception in WebSocket " + serverName + ": " + e.getMessage());
            }
        }

        // Test all HTTP endpoints
        for (Map.Entry<String, String> entry : httpUrls.entrySet()) {
            String apiName = entry.getKey();
            String url = entry.getValue();
            boolean isHealthy = testHttpGetUrl(apiName, url);
            softAssert.assertTrue(isHealthy, "HTTP API " + apiName + " failed");
        }
        
        softAssert.assertAll(); // This will report all failures at once
    }

    @Test(priority = 4, groups = {"performance"})
    public void testResponseTimes() {
        System.out.println("⏱️ Testing response times for all HTTP endpoints...");
        
        for (Map.Entry<String, String> entry : httpUrls.entrySet()) {
            String apiName = entry.getKey();
            String url = entry.getValue();
            
            long startTime = System.currentTimeMillis();
            boolean isHealthy = testHttpGetUrl(apiName, url);
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            System.out.println("📊 " + apiName + " response time: " + responseTime + "ms");
            
            // Assert response time is under 30 seconds
            Assert.assertTrue(responseTime < 30000, 
                apiName + " response time (" + responseTime + "ms) exceeded 30 seconds");
            
            if (isHealthy && responseTime > 10000) {
                System.out.println("⚠️ Warning: " + apiName + " is slow (" + responseTime + "ms)");
            }
        }
    }

    @DataProvider(name = "webSocketEndpoints")
    public Object[][] webSocketEndpoints() {
        return webSockets.entrySet().stream()
            .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
            .toArray(Object[][]::new);
    }

    @DataProvider(name = "httpEndpoints")
    public Object[][] httpEndpoints() {
        return httpUrls.entrySet().stream()
            .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "webSocketEndpoints", groups = {"websocket", "parallel"})
    public void testWebSocketEndpoint(String serverName, String webSocketUrl) {
        try {
            boolean isHealthy = testWebSocketConnection(serverName, webSocketUrl);
            Assert.assertTrue(isHealthy, "WebSocket " + serverName + " health check failed");
        } catch (Exception e) {
            sendAlertMail(serverName, e.getMessage(), "222 1000", "Divya D", 193, "Neurovoyager");
            Assert.fail("Exception while testing WebSocket " + serverName + ": " + e.getMessage());
        }
    }

    @Test(dataProvider = "httpEndpoints", groups = {"http", "parallel"})
    public void testHttpEndpoint(String apiName, String url) {
        boolean isHealthy = testHttpGetUrl(apiName, url);
        Assert.assertTrue(isHealthy, "HTTP API " + apiName + " health check failed");
    }

    private boolean testHttpGetUrl(String name, String url) {
        System.out.println("🌐 Checking HTTP GET: " + name + " → " + url);
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(CONNECTION_TIMEOUT))
                    .build();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(CONNECTION_TIMEOUT))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            
            System.out.println("📊 " + name + " responded with HTTP status: " + statusCode);

            if (statusCode != 200) {
                String errorMessage = "HTTP GET returned status " + statusCode;
                if (statusCode == 502) {
                    errorMessage += " (Bad Gateway - Server/Proxy issue)";
                } else if (statusCode == 503) {
                    errorMessage += " (Service Unavailable)";
                } else if (statusCode == 504) {
                    errorMessage += " (Gateway Timeout)";
                } else if (statusCode >= 500) {
                    errorMessage += " (Server Error)";
                } else if (statusCode >= 400) {
                    errorMessage += " (Client Error)";
                }
                sendAlertMail(name, errorMessage, "hi", "Divya D", 193, "Atlas Editor");
                return false;
            }
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ HTTP GET failed for " + name + ": " + e.getMessage());
            sendAlertMail(name, e.getMessage(), "hi", "Divya D", 193, "Atlas Editor");
            return false;
        }
    }

    private boolean testWebSocketConnection(String serverName, String webSocketUrl) throws InterruptedException {
        System.out.println("🔍 Connecting to " + serverName + " → " + webSocketUrl);

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};
        final Exception[] connectionError = {null};
        
        ScheduledExecutorService globalScheduler = Executors.newSingleThreadScheduledExecutor();
        WebSocketClient client = null;

        try {
            client = new WebSocketClient(URI.create(webSocketUrl)) {
                private final StringBuilder responseBuffer = new StringBuilder();
                private ScheduledFuture<?> timeoutFuture;

                @Override
                public void onOpen(ServerHandshake handshake) {
                    try {
                        String testMessage = "{"
                                + "\"query\": \"222 1000\","
                                + "\"user\": \"Divya D\","
                                + "\"userId\": 193,"
                                + "\"page\": \"Neurovoyager\","
                                + "\"page_context\": {}"
                                + "}";
                        send(testMessage);
                        System.out.println("📤 Sent: " + testMessage);

                        timeoutFuture = globalScheduler.schedule(() -> {
                            System.err.println("❌ Timeout (" + WEBSOCKET_TIMEOUT + " seconds) waiting for response from " + serverName);
                            close();
                        }, WEBSOCKET_TIMEOUT, TimeUnit.SECONDS);
                        
                    } catch (Exception e) {
                        System.err.println("❌ Error sending message to " + serverName + ": " + e.getMessage());
                        connectionError[0] = e;
                        close();
                    }
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("📥 Received: " + message);
                    responseBuffer.append(message);
                    if (message.contains("###END")) {
                        if (timeoutFuture != null) timeoutFuture.cancel(true);
                        System.out.println("✅ Full AI Agent response received from " + serverName);
                        success[0] = true;
                        close();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("🔌 WebSocket Closed for " + serverName + ". Code: " + code + ", Reason: " + reason);
                    latch.countDown();
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("❌ Error in " + serverName + " WebSocket: " + ex.getMessage());
                    connectionError[0] = ex;
                    latch.countDown();
                }
            };

            client.setConnectionLostTimeout(CONNECTION_TIMEOUT / 1000);
            
            if (!client.connectBlocking(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Connection timeout after " + CONNECTION_TIMEOUT + "ms");
            }

        } catch (Exception e) {
            System.err.println("❌ Could not connect to " + serverName + ": " + e.getMessage());
            sendAlertMail(serverName, e.getMessage(), "222 1000", "Divya D", 193, "Neurovoyager");
            return false;
        } finally {
            try {
                latch.await(WEBSOCKET_TIMEOUT + 5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            if (client != null && !client.isClosed()) {
                client.close();
            }
            globalScheduler.shutdownNow();
        }

        if (connectionError[0] != null) {
            sendAlertMail(serverName, connectionError[0].getMessage(), "222 1000", "Divya D", 193, "Neurovoyager");
            return false;
        }

        if (success[0]) {
            System.out.println("✅ WebSocket connection to " + serverName + " succeeded.");
            return true;
        } else {
            sendAlertMail(serverName, "No complete response received within timeout.", "222 1000", "Divya D", 193, "Neurovoyager");
            return false;
        }
    }

    private void sendAlertMail(String serverName, String reason, String query, String user, int userId, String page) {
        if (EMAIL_USERNAME.isEmpty() || EMAIL_PASSWORD.isEmpty()) {
            System.out.println("⚠️ Email credentials not configured. Skipping email alert for: " + serverName);
            return;
        }
        
        String[] to = {"sriramv@htic.iitm.ac.in"};
        String[] cc = {"venip@htic.iitm.ac.in", "divya.d@htic.iitm.ac.in", "gayathri@htic.iitm.ac.in"};

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });

        session.setDebug(false);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));

            for (String recipient : to) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }
            for (String ccRecipient : cc) {
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(ccRecipient));
            }

            message.setSubject("AI Agent - TestNG Health Check Alert: " + serverName);

            String currentTime = LocalDateTime.now().format(TIME_FORMATTER);

            String content = "<div style='font-family: Arial, sans-serif; font-size: 14px; color: #333;'>"
                    + "<h3 style='color: #D9534F;'>🚨 AI Agent TestNG Health Check Alert</h3>"
                    + "<p>Hi Team,</p>"
                    + "<p><strong>TestNG Health Check detected failure for <span style='color:#5bc0de;'>" + serverName + "</span> at " + currentTime + "</strong></p>"
                    + "<p><u><strong>Test Details:</strong></u></p>"
                    + "<ul>"
                    + "<li><strong>Test Framework:</strong> TestNG</li>"
                    + "<li><strong>Page:</strong> " + page + "</li>"
                    + "<li><strong>User:</strong> " + user + " (ID: " + userId + ")</li>"
                    + "<li><strong>Query:</strong> " + query + "</li>"
                    + "<li><strong>Failure Reason:</strong> " + reason + "</li>"
                    + "</ul>"
                    + "<p><u><strong>Action Required:</strong></u></p>"
                    + "<p>Please check:</p>"
                    + "<ul>"
                    + "<li>WebSocket/HTTP server status</li>"
                    + "<li>Nginx configuration</li>"
                    + "<li>SSL certificate validity</li>"
                    + "<li>Network connectivity</li>"
                    + "<li>TestNG execution logs</li>"
                    + "</ul>"
                    + "<br><p style='color: #555;'>Regards,<br><b>TestNG Automated Health Check</b></p>"
                    + "</div>";

            message.setContent(content, "text/html");

            System.out.println("📧 Sending TestNG alert email for " + serverName + "...");
            Transport.send(message);
            System.out.println("✅ Alert email sent successfully.");

        } catch (MessagingException mex) {
            System.err.println("❌ Email sending failed: " + mex.getMessage());
        }
    }

    @AfterClass
    public void generateSummary() {
        System.out.println("🏁 TestNG Health Check Completed at " + LocalDateTime.now().format(TIME_FORMATTER));
        System.out.println("📊 Check TestNG reports for detailed results");
    }
}