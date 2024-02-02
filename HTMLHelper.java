import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class HTMLHelper {
    public static void SendHeader(HttpResponse response, PrintWriter writer){
        StringBuilder header = new StringBuilder();
        header.append("HTTP/1.1 " + response.getStatusCode() + " " + HttpResponse.GetStatusText(response.getStatusCode()));
        header.append(System.lineSeparator());
        header.append("Content-Type: " + response.getContentType());
        header.append(System.lineSeparator());
        header.append("Content-Length: " + response.getBody().length());
        header.append(System.lineSeparator());
        //send the headers
        System.out.println(header);
        writer.println(header);
    }

    public static void SendChunkedResponse(HttpResponse response, PrintWriter writer) {
        // Convert the response body to an array of bytes
        byte[] responseBodyBytes = response.getBody().getBytes(StandardCharsets.UTF_8);

        // Define a chunk size
        int chunkSize = 1024; // For example, 1024 bytes

        // Send the response body in chunks
        int start = 0;
        int counter = 0;
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
    }

    public static void SendResponse(HttpResponse response, PrintWriter writer) {
        HTMLHelper.SendHeader(response, writer);
        writer.println(response.getBody());
    }
}
