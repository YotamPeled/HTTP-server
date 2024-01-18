import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    private int port;
    private File rootDirectory;
    private String defaultPage;
    private ExecutorService threadPool;

    public HttpServer(String configFilePath) {
        Properties config = new Properties();
        try {
            config.load(new FileInputStream(configFilePath));
            this.port = Integer.parseInt(config.getProperty("port"));
            this.rootDirectory = new File(config.getProperty("root"));
            this.defaultPage = config.getProperty("defaultPage");
            int maxThreads = Integer.parseInt(config.getProperty("maxThreads"));
            this.threadPool = Executors.newFixedThreadPool(maxThreads);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("failure during server configuration");
            System.exit(1);
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("HTTP Server started on port " + this.port);

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket, this.rootDirectory, this.defaultPage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private File rootDirectory;
        private String defaultPage;

        public ClientHandler(Socket clientSocket, File rootDirectory, String defaultPage) {
            this.clientSocket = clientSocket;
            this.rootDirectory = rootDirectory;
            this.defaultPage = defaultPage;
        }

        @Override
        public void run() {
            try {
                // Open input and output streams for the socket
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                // Read the request line by line
                String line;
                StringBuilder requestBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    requestBuilder.append(line + "\r\n");
                }

                String request = requestBuilder.toString();
                System.out.println("user request: \n" + request);
                request.replaceAll("../", "");
                String method = request.split(" ")[0];
                String[] requestedPage = request.split(" ")[1].split("\\?");
                Boolean isChunked = request.contains("chunked: yes");
                // get the request type
                String serverResponse = "";
                requestedPage[0] = rootDirectory + requestedPage[0];
                try {
                    eRequestType requestType = eRequestType.valueOf(method);
                    HttpResponse.ProcessRequest(writer, requestType, requestedPage, isChunked);
                } catch (IllegalArgumentException e) {
                    HttpResponse.serverError(writer, "");
                }
                finally {
                    System.out.println(serverResponse);
                }

                // Always close the client socket and streams after handling the request
                reader.close();
                output.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
