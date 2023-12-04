package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.io.*;

public class Server {
    final String ip;
    final int port;
    ArrayList<String> arquivos;

    String ok = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n" + "\r\n";

    String badRequest = "HTTP/1.1 404 BAD REQUEST\r\n" + "Content-Type: text/html\r\n" + "\r\n";

    String notImplemented = "HTTP/1.1 501 NOT IMPLEMENTED\r\n" + "Content-Type: text/html\r\n" + "\r\n";

    JsonParser jsonParser = new JsonParser();

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
        arquivos = new ArrayList();
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
            System.out.println(e);
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

            System.out.println(str);

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
            System.out.println(e.getMessage());
        }
    }

    // #region Manipuladores de métodos HTTP
    // Decide o que fazer baseado na rota acessada pelo usuário
    // Se a rota não existir, o retorno será um bad request
    private String getHandler(String route) {
        if (route.equals("/")) {
            return sendFileContent("src\\home.html");
        }
        if (route.equals("/getAll")) {
            return arquivos.toString(); 
        }
        if (route.contains("/get")) {
            String fileName = route.replace("/get/", "");
            return sendFileContent("src\\server\\files\\" + fileName);
        }
        
        return badRequest;
    }

    private String postHandler(String route, JsonObject object) {
        if (route.equals("/create")) {
            String fileName = object.get("name").getAsString();
            arquivos.add(fileName);
            String fileData = object.get("data").getAsString();
            Path filePath = Paths.get("src\\server\\files", fileName);
            try {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, fileData.getBytes(), StandardOpenOption.CREATE);
                return ok;
            } catch (Exception e) {
                return "HTTP/1.1 500 INTERNAL SERVER ERROR\r\n" + "Content-Type: text/html\r\n" + "\r\n";
            }
        }
        return badRequest;
    }

    private String deleteHandler(String route) {
        if (route.contains("/delete")) {
            String fileName = route.replace("/delete/", "");
            Path filePath = Paths.get("src\\server\\files", fileName);
            try {
                Files.delete(filePath);
                return ok;
            } catch (Exception e) {
                return "HTTP/1.1 500 INTERNAL SERVER ERROR\r\n" + "Content-Type: text/html\r\n" + "\r\n";
            }
        }
        return badRequest;
    }
    // #endregion

    private String sendFileContent(String fileName) {
        try {
            // Carrega o arquivo
            Path filePath = Paths.get(fileName);
            byte[] fileBytes = Files.readAllBytes(filePath);
            String file = new String(fileBytes);
            // Envia o arquivo
            return ok + file;
        } catch (Exception e) {
            return "HTTP/1.1 500 INTERNAL SERVER ERROR\r\n" + "Content-Type: text/html\r\n" + "\r\n";
        }
    }
}