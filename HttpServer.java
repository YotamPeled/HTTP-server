import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private static final Object lock = new Object();
    private static ArrayList<FormData> Data;
    private int port;

    public static File getRootDirectory() {
        return rootDirectory;
    }

    private static File rootDirectory;
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
            this.Data = new ArrayList<FormData>();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("failure during server configuration");
            System.exit(1);
        }
    }

    public static void InsertData(FormData i_Data)
    {
        synchronized(lock) {
            Data.add(i_Data);
            System.out.println(Data.toString());
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
                //PrintWriter writer = new PrintWriter(output, true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                // Read the request line by line
                String line;
                StringBuilder requestBuilder = new StringBuilder();
                int contentLength = 0;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
                    }
                    requestBuilder.append(line + "\r\n");
                }

                // print the request headers
                System.out.println("user request: \n" + requestBuilder.toString());

                // Read body if Content-Length is present
                if (contentLength > 0) {
                    char[] body = new char[contentLength];
                    reader.read(body, 0, contentLength);
                    requestBuilder.append(new String(body));
                }


                // get the request type
                String serverResponse = "";

                try {
                    HttpResponse.ProcessRequest(output, requestBuilder);
                } catch (Exception e) {
                    e.printStackTrace();
                    HttpResponse.serverError(output);
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
