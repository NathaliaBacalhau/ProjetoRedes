import server.Server;

public class App {

    public static void main(String[] args) throws Exception {
        Server server = new Server("192.168.1.11", 8080);
        server.run();
    }
}
