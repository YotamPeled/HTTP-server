import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private eRequestType RequestType;
    private Boolean IsChunked;
    private String RequestedPage;
    public Boolean isChunked() {return IsChunked;}
    public String getRequestedPage() {return RequestedPage;}
    public eRequestType getRequestType() {return RequestType;}
    public HttpRequest(String request){
        parseRequest(request);
    }

    private void parseRequest(String request){
        request.replaceAll("../", "");
        String method = request.split(" ")[0].replaceAll("\\s+$", "");
        this.RequestType = eRequestType.valueOf(method);

        this.IsChunked = request.contains("chunked: yes");

        String requestedPage = request.split(" ").length > 1 ? request.split(" ")[1].split("\\?")[0].replaceAll("\\s+$", "") : "";
        this.RequestedPage = HttpServer.getRootDirectory() + requestedPage;

        if(this.RequestType == eRequestType.POST || this.RequestType == eRequestType.GET)
        {
            Map<String, String> map = new HashMap<String, String>();
            parseURLParams(request, map);
            if (this.RequestType == eRequestType.POST){
                parseBodyParams(request, map);
            }

            if (!map.isEmpty())
            {
                HttpServer.InsertData(map);
            }
        }
    }

    private void parseBodyParams(String request, Map<String, String> map) {
        String[] requestLines = request.split("\\r?\\n");
        String paramString = requestLines[requestLines.length - 1];
        String[] paramPairs = paramString.split("&"); // Split into key-value pairs
        insertData(paramPairs, map);
    }

    private void parseURLParams(String request, Map<String, String> map){
        if (request.split(" ").length > 1 && request.split(" ")[1].split("\\?").length > 1) {
            String paramString = request.split(" ")[1].split("\\?")[1]; // Get the part after '?'
            String[] paramPairs = paramString.split("&"); // Split into key-value pairs
            insertData(paramPairs, map);
        }
    }

    private void insertData(String[] paramPairs, Map<String, String> map)
    {
        for (String pair : paramPairs) {
            String[] keyValue = pair.split("=", 2); // Limit split to 2 in case value contains '='
            if (keyValue.length == 2) { // Ensure there's a key and a value
                // Decode the parameters to handle encoded characters like spaces (%20)
                String key = keyValue[0];
                String value = keyValue[1];
                map.put(key, value); // Add to map
            }
        }
    }
}
