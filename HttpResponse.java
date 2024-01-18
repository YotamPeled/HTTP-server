import java.io.*;
import java.nio.charset.StandardCharsets;

public class HttpResponse {

    private int statusCode;
    private String contentType;
    private String body;

    private HttpResponse(int statusCode, String contentType, String body) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
    }

    private static void notFound(PrintWriter writer) {
        String notFoundHtml = "<html><head><title>404 Not Found</title></head>"
                + "<body><h1>404 Not Found</h1>"
                + "<p>The resource you are looking for might have been removed, had its name changed, or is temporarily unavailable.</p></body></html>";
        sendResponse(new HttpResponse(404, "text/html", notFoundHtml), writer);
    }

    private static void okFromFile(PrintWriter writer, String filePath, Boolean isChunked) throws IOException {
        String body = readFile(filePath);
        if (isChunked){
            sendChunkedResponse(new HttpResponse(200, "text/html", body), writer);
        }
        else {
            sendResponse(new HttpResponse(200, "text/html", body), writer);
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
        StringBuilder header = new StringBuilder();
        header.append("HTTP/1.1 " + response.statusCode + " " + getStatusText(response.statusCode));
        header.append(System.lineSeparator());
        header.append("Content-Type: " + response.contentType);
        header.append(System.lineSeparator());
        header.append("Content-Length: " + response.body.length());
        header.append(System.lineSeparator());
        //send the headers
        System.out.println(header);
        writer.println(header);
        // Send the body
        writer.println(response.body);
    }

    private static void sendChunkedResponse(HttpResponse response, PrintWriter writer) {
        StringBuilder header = new StringBuilder();

        // Build the HTTP start line and headers
        header.append("HTTP/1.1 ").append(response.statusCode).append(" ").append(getStatusText(response.statusCode)).append(System.lineSeparator());
        header.append("Content-Type: ").append(response.contentType).append(System.lineSeparator());
        header.append("Transfer-Encoding: chunked").append(System.lineSeparator());
        header.append(System.lineSeparator()); // End of headers

        // Send the headers
        System.out.println(header);
        writer.print(header);

        // Convert the response body to an array of bytes
        byte[] responseBodyBytes = response.body.getBytes(StandardCharsets.UTF_8);

        // Define a chunk size
        int chunkSize = 1024; // For example, 1024 bytes

        // Send the response body in chunks
        int start = 0;
        while (start < responseBodyBytes.length) {
            // Determine the size of the current chunk
            int end = Math.min(responseBodyBytes.length, start + chunkSize);
            int currentChunkSize = end - start;

            // Send the size of the current chunk in hexadecimal
            writer.println(Integer.toHexString(currentChunkSize));

            // Send the current chunk of the response body
            writer.write(new String(responseBodyBytes, start, currentChunkSize, StandardCharsets.UTF_8));
            writer.println(); // End of the chunk

            // Move to the next chunk
            start = end;
        }

        // Send a zero-length chunk to indicate the end of the response
        writer.println("0");
        writer.println(); // End of chunks

        // Flush the writer to ensure all data is sent
        writer.flush();
    }

// Note: Ensure the getStatusText method is defined to return the appropriate status text for the given status code.


    private static String getStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 400:
                return "Bad Request";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            case 501:
                return "Not Implemented";
            default:
                return "Unknown";
        }
    }

    public static void ProcessRequest(PrintWriter writer, eRequestType requestType, String[] request, Boolean isChunkedResponse) {
        switch (requestType){
            case GET:
               processGetRequest(writer, request[0], isChunkedResponse);
               break;
            case POST:
                processPostRequest(writer, request, isChunkedResponse);
                break;
            case PUT:
            case PATCH:
            case DELETE:
            case CONNECT:
            case OPTIONS:
                notImplemented(writer);
                break;
            default:
                badRequest(writer);
                break;
        }
    }

    private static void processPostRequest(PrintWriter writer, String[] request, Boolean isChunked) {

    }

    private static void badRequest(PrintWriter writer) {
        String notImplementedHtml = "<html><head><title>400 Bad Request</title></head>"
                + "<body><h1>400 Bad Request</h1>"
                + "<p>add later</p></body></html>";
        sendResponse(new HttpResponse(400, "text/html", notImplementedHtml), writer);
    }

    private static void notImplemented(PrintWriter writer) {
        String notImplementedHtml = "<html><head><title>501 Not Implemented</title></head>"
                + "<body><h1>501 Not Implemented</h1>"
                + "<p>add later</p></body></html>";
        sendResponse(new HttpResponse(501, "text/html", notImplementedHtml), writer);
    }

    private static void processGetRequest(PrintWriter writer, String requestedPage, Boolean isChunked) {
        try{
            okFromFile(writer, requestedPage, isChunked);
        }
        catch(IOException exception) {
            notFound(writer);
        }
    }
}
