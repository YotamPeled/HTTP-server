import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class HttpResponse {
    private int statusCode;
    private String contentType;
    private String body;

    private HttpResponse(int statusCode, String contentType, String body) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    private static void notFound(PrintWriter writer) {
        String notFoundHtml = "<html><head><title>404 Not Found</title></head>"
                + "<body><h1>404 Not Found</h1>"
                + "<p>The resource you are looking for might have been removed, had its name changed, or is temporarily unavailable.</p></body></html>";
        HTMLHelper.SendResponse(new HttpResponse(404, "text/html", notFoundHtml), writer);
    }

    private static void okFromFile(PrintWriter writer, String filePath, Boolean isChunked, boolean headerOnly) throws IOException {
        String body = readFile(filePath);

        if (headerOnly)
        {
            HTMLHelper.SendHeader(new HttpResponse(200, ContentTypeHelper.ContentType.html.GetDescription(), body), writer);
        }
        else if (isChunked){
            HTMLHelper.SendChunkedResponse(new HttpResponse(200, ContentTypeHelper.ContentType.html.GetDescription(), body), writer);
        }
        else {
            HTMLHelper.SendResponse(new HttpResponse(200, ContentTypeHelper.ContentType.html.GetDescription(), body), writer);
        }
    }

    private static String readFile(String filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = br.readLine()) != null) {
            contentBuilder.append(line).append("\n");
        }

        return contentBuilder.toString();
    }

    private static void okFromImage(OutputStream out, String filePath, Boolean isChunked, boolean headerOnly) throws IOException {
        String body = readFile(filePath);
        String type = ContentTypeHelper.GetContentType(filePath).GetDescription();
        if (headerOnly)
        {
            PrintWriter writer = new PrintWriter(out, true);
            HTMLHelper.SendHeader(new HttpResponse(200, type, body), writer);
        }
        else if (isChunked){
            ImageHelper.SendChunkedResponse(new HttpResponse(200, type, body), out);
        }
        else {
            ImageHelper.SendResponse(new HttpResponse(200, type, body), out);
        }
    }

    public static void serverError(PrintWriter writer, String message) {
        HTMLHelper.SendResponse(new HttpResponse(500, "text/html", "<html><body><h1>500 Server Error</h1><p>" + message + "</p></body></html>"), writer);
    }

// Note: Ensure the getStatusText method is defined to return the appropriate status text for the given status code.


    public static String GetStatusText(int statusCode) {
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

    public static void ProcessRequest(OutputStream output, StringBuilder requestBuilder) {
        PrintWriter writer = new PrintWriter(output, true);
        String request = requestBuilder.toString();
        request.replaceAll("../", "");
        String method = request.split(" ")[0].replaceAll("\\s+$", "");
        Boolean isChunked = request.contains("chunked: yes");
        eRequestType requestType = eRequestType.valueOf(method);
        String requestedPage = request.split(" ").length > 1 ? request.split(" ")[1].split("\\?")[0].replaceAll("\\s+$", "") : "";
        String URLParams = "";
        if (request.split(" ").length > 1 && request.split(" ")[1].split("\\?").length > 1)
        {
            URLParams = request.split(" ")[1].split("\\?")[1];
        }

        requestedPage = HttpServer.getRootDirectory() + requestedPage;

        switch (requestType){
            case GET:
                processGetRequest(output, requestedPage, isChunked);
                break;
            case POST:
                String[] requestLines = request.split("\\r?\\n");
                String requestedParams = requestLines[requestLines.length - 1];
                processPostRequest(writer, requestedPage, requestedParams, isChunked);
                break;
            case TRACE:
                processTraceRequest(writer, request);
                break;
            case HEAD:
                processHeadRequest(writer, request);
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

    private static void processHeadRequest(PrintWriter writer, String request) {
        try {
            okFromFile(writer, request, false, true);
        }
        catch(IOException exception) {
            notFound(writer);
        }
    }

    private static void processTraceRequest(PrintWriter writer, String request) {
        writer.println(request);
    }

    private static void processPostRequest(PrintWriter writer, String requestPage, String requestParams, Boolean isChunked) {
        try{
            try {
                FormData data = new FormData(requestParams);
                HttpServer.InsertData(data);
            }
            catch(IllegalArgumentException e){
                System.out.println("illegal post params");
            }
            finally
            {
                okFromFile(writer, requestPage, isChunked, false);
            }
        }
        catch (IOException exception){
            notFound(writer);
        }
    }

    private static void badRequest(PrintWriter writer) {
        String notImplementedHtml = "<html><head><title>400 Bad Request</title></head>"
                + "<body><h1>400 Bad Request</h1>"
                + "<p>add later</p></body></html>";
        HTMLHelper.SendResponse(new HttpResponse(400, "text/html", notImplementedHtml), writer);
    }

    private static void notImplemented(PrintWriter writer) {
        String notImplementedHtml = "<html><head><title>501 Not Implemented</title></head>"
                + "<body><h1>501 Not Implemented</h1>"
                + "<p>add later</p></body></html>";
        HTMLHelper.SendResponse(new HttpResponse(501, "text/html", notImplementedHtml), writer);
    }

    private static void processGetRequest(OutputStream output, String requestedPage, Boolean isChunked) {
        PrintWriter writer = new PrintWriter(output, true);

        try{
            ContentTypeHelper.ContentType type = ContentTypeHelper.GetContentType(requestedPage);
            if (type == ContentTypeHelper.ContentType.html)
            {
                okFromFile(writer, requestedPage, isChunked, false);
            }
            else{
                okFromImage(output, requestedPage, isChunked, false);
            }
        }
        catch(IOException exception) {
            notFound(writer);
        }
    }
}
