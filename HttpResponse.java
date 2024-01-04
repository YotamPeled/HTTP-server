import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

    public static void okFromFile(PrintWriter writer, String filePath) {
        try {
            String body = readFile(filePath);
            sendResponse(new HttpResponse(200, "text/html", body), writer);
        } catch (IOException e) {
            e.printStackTrace();
            // If there's an error reading the file, send a server error response
            serverError(writer, "Internal Server Error");
        }
    }

    private static String readFile(String filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        }
        return contentBuilder.toString();
    }

    public static void serverError(PrintWriter writer, String message) {
        sendResponse(new HttpResponse(500, "text/html", "<html><body><h1>500 Server Error</h1><p>" + message + "</p></body></html>"), writer);
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
