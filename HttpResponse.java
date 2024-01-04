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

    public int getStatusCode() {
        return statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public String getBody() {
        return body;
    }

    // Static factory methods for standard responses
    public static HttpResponse ok(String body) {
        return new HttpResponse(200, "text/html", body);
    }

    public static void notFound(PrintWriter writer) {
        String notFoundHtml = "<html><head><title>404 Not Found</title></head>"
                + "<body><h1>404 Not Found</h1>"
                + "<p>The resource you are looking for might have been removed, had its name changed, or is temporarily unavailable.</p></body></html>";
         sendOutNotFound(new HttpResponse(404, "text/html", notFoundHtml), writer);
    }

    public static HttpResponse serverError() {
        return new HttpResponse(500, "text/html", "500 Server Error");
    }

    private static void sendOutNotFound(HttpResponse response ,PrintWriter writer) {
        // Send the status line
        writer.println("HTTP/1.1 " + response.statusCode + " Not Found");

        // Send the headers
        writer.println("Content-Type: " + response.contentType);
        writer.println("Content-Length: " + response.body.length());
        writer.println(); // Blank line between headers and body

        // Send the body
        writer.println(response.body);
    }
    // Other response types as needed...
}
