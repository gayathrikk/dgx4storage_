package dd.project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QuickUrlValidator {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void main(String[] args) {
        String testUrl = "https://apollo2.humanbrain.in/aiAgentServer/agent/recomendation?user_query=hi&page_context=%22%7B%5C%22ssid%5C%22%3A85%2C%5C%22seriesType%5C%22%3A%5C%22NISSL%5C%22%2C%5C%22secid%5C%22%3A1513%2C%5C%22biosampleId%5C%22%3A%5C%22201%5C%22%7D%22&page=Atlas%20Editor&first=true&action_context=null";
        
        System.out.println("🧪 Quick URL Validation Test");
        System.out.println("⏰ Time: " + LocalDateTime.now().format(TIME_FORMATTER));
        System.out.println("🔗 URL: " + testUrl);
        System.out.println("=" * 80);
        
        validateUrl("Apollo2 Recommendation API (secid=1513)", testUrl);
        
        // Test the original URL too for comparison
        String originalUrl = "https://apollo2.humanbrain.in/aiAgentServer/agent/recomendation?user_query=hi&page_context=%22%7B%5C%22ssid%5C%22%3A85%2C%5C%22seriesType%5C%22%3A%5C%22NISSL%5C%22%2C%5C%22secid%5C%22%3A52%2C%5C%22biosampleId%5C%22%3A%5C%22201%5C%22%7D%22&page=Atlas%20Editor&first=true&action_context=null";
        System.out.println("\n" + "=" * 80);
        System.out.println("🔍 Comparing with original URL (secid=52):");
        validateUrl("Apollo2 Recommendation API (secid=52)", originalUrl);
    }
    
    private static void validateUrl(String name, String url) {
        System.out.println("\n🌐 Testing: " + name);
        
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();
            
            int statusCode = response.statusCode();
            long responseTime = endTime - startTime;
            
            System.out.println("📊 Results:");
            System.out.println("   Status Code: " + statusCode + " " + getStatusDescription(statusCode));
            System.out.println("   Response Time: " + responseTime + "ms");
            System.out.println("   Content Length: " + response.body().length() + " characters");
            
            // Show first 200 characters of response for debugging
            String responseBody = response.body();
            if (responseBody.length() > 200) {
                System.out.println("   Response Preview: " + responseBody.substring(0, 200) + "...");
            } else {
                System.out.println("   Response Body: " + responseBody);
            }
            
            if (statusCode == 200) {
                System.out.println("✅ SUCCESS: " + name + " is working properly");
            } else {
                System.out.println("❌ FAILURE: " + name + " returned status " + statusCode);
                analyzeError(statusCode, responseBody);
            }
            
        } catch (Exception e) {
            System.out.println("❌ EXCEPTION: " + e.getMessage());
            System.out.println("   Error Type: " + e.getClass().getSimpleName());
            
            // Provide troubleshooting suggestions
            if (e.getMessage().contains("timeout")) {
                System.out.println("💡 Suggestion: Server might be slow or unresponsive");
            } else if (e.getMessage().contains("Connection refused")) {
                System.out.println("💡 Suggestion: Server might be down or port blocked");
            } else if (e.getMessage().contains("UnknownHostException")) {
                System.out.println("💡 Suggestion: DNS resolution issue or wrong hostname");
            }
        }
    }
    
    private static String getStatusDescription(int statusCode) {
        switch (statusCode) {
            case 200: return "(OK)";
            case 400: return "(Bad Request)";
            case 401: return "(Unauthorized)";
            case 403: return "(Forbidden)";
            case 404: return "(Not Found)";
            case 500: return "(Internal Server Error)";
            case 502: return "(Bad Gateway)";
            case 503: return "(Service Unavailable)";
            case 504: return "(Gateway Timeout)";
            default: return statusCode >= 500 ? "(Server Error)" : 
                    statusCode >= 400 ? "(Client Error)" : "(Unknown)";
        }
    }
    
    private static void analyzeError(int statusCode, String responseBody) {
        System.out.println("\n🔍 Error Analysis:");
        
        switch (statusCode) {
            case 502:
                System.out.println("   502 Bad Gateway - The server acting as a gateway received an invalid response");
                System.out.println("   💡 Possible causes:");
                System.out.println("      - Backend server is down");
                System.out.println("      - Nginx/proxy configuration issue");
                System.out.println("      - Network connectivity problem between proxy and backend");
                break;
                
            case 503:
                System.out.println("   503 Service Unavailable - Server temporarily unable to handle request");
                System.out.println("   💡 Possible causes:");
                System.out.println("      - Server overloaded");
                System.out.println("      - Maintenance mode");
                System.out.println("      - Database connection issues");
                break;
                
            case 504:
                System.out.println("   504 Gateway Timeout - Upstream server didn't respond in time");
                System.out.println("   💡 Possible causes:");
                System.out.println("      - Backend processing taking too long");
                System.out.println("      - Database query timeout");
                System.out.println("      - Network latency issues");
                break;
                
            case 500:
                System.out.println("   500 Internal Server Error - Something went wrong on the server");
                System.out.println("   💡 Check server logs for specific error details");
                break;
                
            default:
                if (statusCode >= 400 && statusCode < 500) {
                    System.out.println("   Client Error - Issue with the request");
                } else if (statusCode >= 500) {
                    System.out.println("   Server Error - Issue with the server");
                }
        }
        
        // Check if response contains error details
        if (responseBody != null && !responseBody.isEmpty()) {
            if (responseBody.toLowerCase().contains("error")) {
                System.out.println("   ⚠️ Error details found in response body");
            }
        }
    }
}