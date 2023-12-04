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
        System.out.println(config.get("ip").getAsString());
        System.out.println(config.get("port").getAsInt());
        Server server = new Server(config.get("ip").getAsString(), config.get("port").getAsInt());
        server.run();
    }
}
