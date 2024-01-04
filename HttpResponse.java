import java.io.PrintWriter;

public class HttpResponse {

    private int statusCode;
    private String contentType;
    private String body;

    private HttpResponse(int statusCode, String contentType, String body) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
    }

    public static void notFound(PrintWriter writer) {
        String notFoundHtml = "<html><head><title>404 Not Found</title></head>"
                + "<body><h1>404 Not Found</h1>"
                + "<p>The resource you are looking for might have been removed, had its name changed, or is temporarily unavailable.</p></body></html>";
        sendResponse(new HttpResponse(404, "text/html", notFoundHtml), writer);
    }

    public static void ok(PrintWriter writer, String body) {
        sendResponse(new HttpResponse(200, "text/html", body), writer);
    }

    private static void sendResponse(HttpResponse response, PrintWriter writer) {
        // Send the status line
        writer.println("HTTP/1.1 " + response.statusCode + " " + getStatusText(response.statusCode));

        // Send the headers
        writer.println("Content-Type: " + response.contentType);
        writer.println("Content-Length: " + response.body.length());
        writer.println(); // Blank line between headers and body

        // Send the body
        writer.println(response.body);
    }

    private static String getStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 404:
                return "Not Found";
            // Add other status codes as necessary
            default:
                return "Unknown";
        }
    }
}
