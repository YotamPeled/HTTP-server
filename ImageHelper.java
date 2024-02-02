import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ImageHelper {

    public static void SendChunkedResponse(HttpResponse response, OutputStream out) throws IOException {
        PrintWriter writer = new PrintWriter(out, true);
        HTMLHelper.SendHeader(response, writer);
        byte[] responseBodyBytes = response.getBody().getBytes(); // Assuming getBodyBytes returns byte[]

        int chunkSize = 1024; // Define a chunk size

        int start = 0;
        while (start < responseBodyBytes.length) {
            // Determine the size of the current chunk
            int end = Math.min(responseBodyBytes.length, start + chunkSize);
            int currentChunkSize = end - start;

            // Send the size of the current chunk in hexadecimal
            String sizeHeader = Integer.toHexString(currentChunkSize) + "\r\n";
            out.write(sizeHeader.getBytes(StandardCharsets.UTF_8));
            // Send the current chunk of the response body
            out.write(responseBodyBytes, start, currentChunkSize);
            out.write("\r\n".getBytes(StandardCharsets.UTF_8)); // End of the chunk

            // Move to the next chunk
            start = end;
        }

        // Send a zero-length chunk to indicate the end of the response
        out.write("0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public static void SendResponse(HttpResponse response, OutputStream out) throws IOException {
        // Assuming SendHeader is adapted to use OutputStream and properly formats headers
        PrintWriter writer = new PrintWriter(out, true);
        HTMLHelper.SendHeader(response, writer);
        out.write(response.getBody().getBytes()); // Assuming getBodyBytes returns byte[]
        out.flush();
    }
}