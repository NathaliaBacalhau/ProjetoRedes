import server.Server;

import java.io.FileReader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class App {
    public static void main(String[] args) throws Exception {
        JsonReader configReader = new JsonReader(new FileReader("src\\config.json"));
        JsonParser jsonParser = new JsonParser();
        JsonObject config = jsonParser.parse(configReader).getAsJsonObject();
        final String ip = config.get("ip").getAsString();
        final int port = config.get("port").getAsInt();
        final String filesFolder = config.get("root").getAsString();
        Server server = new Server(ip, port, filesFolder);
        server.run();
    }
}
