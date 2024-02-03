import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ImageHelper {

    public static void sendChunkedResponse(HttpResponse response) throws IOException {
        OutputStream out = response.getOutput();

        int chunkSize = 1024; // Define a chunk size

        int start = 0;
        while (start < response.getBody().length) {
            // Determine the size of the current chunk
            int end = Math.min(response.getBody().length, start + chunkSize);
            int currentChunkSize = end - start;

            // Send the size of the current chunk in hexadecimal
            String sizeHeader = Integer.toHexString(currentChunkSize) + "\r\n";
            out.write(sizeHeader.getBytes(StandardCharsets.UTF_8));
            // Send the current chunk of the response body
            out.write(response.getBody(), start, currentChunkSize);
            out.write("\r\n".getBytes(StandardCharsets.UTF_8)); // End of the chunk

            // Move to the next chunk
            start = end;
        }

        // Send a zero-length chunk to indicate the end of the response
        out.write("0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public static void SendResponse(HttpResponse response){
        PrintWriter writer = response.getWriter();
        HTMLHelper.SendHeader(response, writer);

        try{
            if (response.isChunked())
            {
                sendChunkedResponse(response);
            }
            else{
                response.getOutput().write(response.getBody());
                response.getOutput().flush();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            //todo handle io error
            System.out.println("error sending image");
        }
    }

}
