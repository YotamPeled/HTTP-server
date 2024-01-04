public class Main {
    public static void main(String args[]){
        String configFilePath = "./config.ini"; // Replace with the path to your config.ini file
        HttpServer server = new HttpServer(configFilePath);
        server.start();
    }
}
