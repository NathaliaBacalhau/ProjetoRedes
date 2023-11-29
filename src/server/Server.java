package server;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;

public class Server {
    // final String ip;
    final int port;

    public Server(int port) {
        this.port = port;
    }
   /*  public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
    } */
    
    public void run() {
        //IP da máquina na rede no host do INetAddress.getByName
        // try(ServerSocket httpServer = new ServerSocket(this.port, 0, InetAddress.getByName(this.ip)))
        try(ServerSocket httpServer = new ServerSocket(this.port)) {
            System.out.println("Aguardando requisiçoes...");

            while (true) {
                Socket clientsSocket = httpServer.accept();
                handler(clientsSocket);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void handler(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream()) {

            // Leitura da requisição HTTP
            StringBuilder request = new StringBuilder();
            String line;
            while (!(line = in.readLine()).isEmpty()) {
                request.append(line).append("\r\n");
            }

            String[] requestSplitted = request.toString().split(" ");
            String response = "";

            switch (requestSplitted[0]) {
                case "GET":
                    response = this.getRequest();
                    break;

                case "PUT":
                    
                    break;
            
                case "POST":
                    
                    break;

                case "DELETE":
                    
                    break;
                default:
                    throw new Exception("Requisição Inválida");
                
            }
            // Exibir a requisição no console
            System.out.println("Requisição:\n" + request.toString());

            // Enviar a resposta ao cliente
            out.write(response.getBytes());
            out.flush();

            clientSocket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    private String getRequest(){
        // Resposta HTTP de exemplo
        return sendFileContents("src/index.html");
    }

    private String sendFileContents(String fileName) {
        try {
            // Load the content of the HTML file
            Path filePath = Paths.get(fileName);
            byte[] fileBytes = Files.readAllBytes(filePath);

            String page = new String(fileBytes);
            // Send the file content
            return "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html\r\n"
                    + "\r\n"
                    + page;

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}