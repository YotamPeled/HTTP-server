import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HttpResponse {
    private int StatusCode;
    private ContentTypeHelper.ContentType ContentType;
    private PrintWriter Writer;
    private byte[] Body;
    private OutputStream output;
    private Boolean IsHeadOnly;
    private Boolean IsChunked;

    private HttpResponse(int statusCode, ContentTypeHelper.ContentType contentType, byte[] body, Boolean isHeadOnly, Boolean isChunked, OutputStream output) {
        this.Writer = new PrintWriter(output, true);
        this.output = output;
        this.StatusCode = statusCode;
        this.ContentType = contentType;
        this.Body = body;
        this.IsHeadOnly = isHeadOnly;
        this.IsChunked = isChunked;

        sendResponse();
    }

    public Boolean isChunked() {return IsChunked;}
    public PrintWriter getWriter() {return Writer;}
    public OutputStream getOutput() {return output;}
    public byte[] getBody() {
        return Body;
    }
    public int getStatusCode() {
        return StatusCode;
    }
    public String getContentType() {
        return ContentType.GetDescription();
    }
    public boolean isHeaderOnly() {return IsHeadOnly;}

    private void sendResponse(){
        if (this.ContentType == ContentTypeHelper.ContentType.html)
        {
            HTMLHelper.SendResponse(this);
        }
        else{
            ImageHelper.SendResponse(this);
        }
    }
    private static void notFound(OutputStream output, Boolean isChunked) {
        new HttpResponse(404, ContentTypeHelper.ContentType.html, BodyFactory.GetPredefinedBody(404), false, isChunked,output);
    }

    public static void serverError(OutputStream output) {
        new HttpResponse(500, ContentTypeHelper.ContentType.html, BodyFactory.GetPredefinedBody(500), false, false, output);
    }

    public static void ProcessRequest(OutputStream output, StringBuilder requestBuilder) {
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
                processPostRequest(output, requestedPage, requestedParams, isChunked);
                break;
            case TRACE:
                processTraceRequest(output, request);
                break;
            case HEAD:
                processHeadRequest(output, requestedPage, isChunked);
                break;
            case PUT:
            case PATCH:
            case DELETE:
            case CONNECT:
            case OPTIONS:
                notImplemented(output, isChunked);
                break;
            default:
                badRequest(output, isChunked);
                break;
        }
    }

    private static void processHeadRequest(OutputStream output, String filePath, Boolean isChunked) {
        try{
            readAndSendFile(output, filePath, isChunked, true);
        }
        catch(IOException exception) {
            notFound(output, isChunked);
        }
    }

    private static void processTraceRequest(OutputStream output, String request) {
        PrintWriter writer = new PrintWriter(output, true);
        writer.println(request);
    }

    private static void processPostRequest(OutputStream output, String filePath, String requestParams, Boolean isChunked) {
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
                readAndSendFile(output, filePath, isChunked, false);
            }
        }
        catch (IOException exception){
            notFound(output, isChunked);
        }
    }

    private static void badRequest(OutputStream output, Boolean isChunked) {
       new HttpResponse(400, ContentTypeHelper.ContentType.html, BodyFactory.GetPredefinedBody(400), false, isChunked,output);
    }

    private static void notImplemented(OutputStream output, Boolean isChunked) {
        new HttpResponse(501, ContentTypeHelper.ContentType.html, BodyFactory.GetPredefinedBody(501), false, isChunked,output);
    }

    private static void processGetRequest(OutputStream output, String filePath, Boolean isChunked) {
        try{
            readAndSendFile(output, filePath, isChunked, false);
        }
        catch(IOException exception) {
            notFound(output, isChunked);
        }
    }

    private static void readAndSendFile(OutputStream output, String filePath, Boolean isChunked, Boolean isHeadOnly) throws IOException{
        ContentTypeHelper.ContentType type = ContentTypeHelper.GetContentType(filePath);
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        new HttpResponse(200, type, fileBytes, isHeadOnly, isChunked, output);
    }
}
