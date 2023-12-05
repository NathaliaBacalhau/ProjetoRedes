package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.*;

public class Server {
    final String ip, root;
    final int port;

    // #region http responses
    String ok = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n" + "\r\n";

    String created = "HTTP/1.1 201 CREATED\r\n" + "Content-Type: text/html\r\n" + "\r\n";

    String badRequest = "HTTP/1.1 400 BAD REQUEST\r\n" + "Content-Type: text/html\r\n" + "\r\n";

    String notFound = "HTTP/1.1 404 NOT FOUND\r\n" + "Content-Type: text/html\r\n" + "\r\n";

    String notImplemented = "HTTP/1.1 501 NOT IMPLEMENTED\r\n" + "Content-Type: text/html\r\n" + "\r\n";

    String internalError = "HTTP/1.1 500 INTERNAL SERVER ERROR\r\n" + "Content-Type: text/html\r\n" + "\r\n";
    // #endregion

    JsonParser jsonParser = new JsonParser();

    public Server(String ip, int port, String root) {
        this.ip = ip;
        this.port = port;
        this.root = root;
    }

    public void run() {
        // IP da máquina na rede no host do INetAddress.getByName
        try (ServerSocket httpServer = new ServerSocket(this.port, 0, InetAddress.getByName(this.ip))) {
            System.out.println("Waiting requests...");

            while (true) {
                Socket clientsSocket = httpServer.accept();
                handler(clientsSocket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Manipulador das requisições para decidir o que será feito
    private void handler(Socket clientSocket) {
        try {
            // Requisição enviada pelo cliente
            InputStreamReader request = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader data = new BufferedReader(request);

            // Corpo de resposta a ser preenchido
            OutputStream response = clientSocket.getOutputStream();

            // Leitura da requisição e carriage return
            StringBuilder str = new StringBuilder();
            String line;
            while (!(line = data.readLine()).isEmpty()) {
                str.append(line).append("\r\n");
            }

            // Captura do tipo de método HTTP, rota
            // Capturar o body nessa etapa era conflitante
            // Com a requisição GET devido a ausência de body
            String method = str.toString().split(" ")[0];
            String route = str.toString().split(" ")[1];

            if (method.equals("GET")) {
                response.write(getHandler(route).getBytes());
                response.flush();
            } else if (method.equals("POST")) {
                // Captura do body
                StringBuilder body = new StringBuilder();
                while (data.ready()) {
                    body.append((char) data.read());
                }
                JsonObject jsonBody = jsonParser.parse(body.toString()).getAsJsonObject();
                response.write(postHandler(route, jsonBody).getBytes());
                response.flush();
            } else if (method.equals("DELETE")) {
                response.write(deleteHandler(route).getBytes());
                response.flush();
            } else {
                response.write(notImplemented.getBytes());
                response.flush();
            }
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // #region Manipuladores de métodos HTTP
    // Decide o que fazer baseado na rota acessada pelo usuário
    // Se a rota não existir, o retorno será um bad request
    private String getHandler(String route) {
        if (route.equals("/")) {
            Path home = Paths.get("src\\home.html");
            return get(home);
        }
        if (route.contains("/getAll")) {
            String filesFolder = route.replace("/getAll/", "");
            Path filesPath = Paths.get(filesFolder);
            return getAll(filesPath);
        }
        if (route.contains("/get")) {
            String fileName = route.replace("/get/", "");
            Path filePath = Paths.get(fileName);
            return get(filePath);
        }
        return notFound;
    }

    private String get(Path filePath) {
        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            String file = new String(fileBytes);
            return ok + file;
        } catch (Exception e) {
            return internalError;
        }
    }

    private String getAll(Path filesPath) {
        if (Files.exists(filesPath)) {
            try {
                var filesPaths = Files.walk(filesPath, 1).toList();
                String fileList = filesPaths.subList(1, filesPaths.size()).stream()
                        .map(file -> file.getFileName()
                                .toString())
                        .toList()
                        .toString();
                return ok + fileList;
            } catch (IOException e) {
                return internalError;
            }
        }
        return notFound;
    }

    private String postHandler(String route, JsonObject object) {
        if (route.equals("/create")) {
            String fileName = object.get("name").getAsString();
            String fileData = object.get("data").getAsString();
            Path filePath = Paths.get(fileName);
            return create(filePath, fileData);
        }
        return notFound;
    }

    private String create(Path filePath, String fileData) {
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, fileData.getBytes(), StandardOpenOption.CREATE);
            return created;
        } catch (Exception e) {
            return badRequest;
        }
    }

    private String deleteHandler(String route) {
        if (route.contains("/delete")) {
            String fileName = route.replace("/delete/", "");
            Path filePath = Paths.get(fileName);
            return delete(filePath);
        }
        return notFound;
    }

    private String delete(Path filePath) {
        try {
            Files.delete(filePath);
            return ok;
        } catch (Exception e) {
            return notFound;
        }
    }
    // #endregion
}