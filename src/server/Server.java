package server;

import java.net.*;
import java.io.*;

public class Server {
    final String ip;
    final int port;

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    
    public void run() {
        //IP da máquina na rede no host do INetAddress.getByName
        try(ServerSocket httpServer = new ServerSocket(this.port, 0, InetAddress.getByName(this.ip))) {
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

            // Exibir a requisição no console
            System.out.println("Requisição:\n" + request.toString());

            // Resposta HTTP de exemplo
            String response = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "\r\n"
                    + "Hello, World!\r\n";

            // Enviar a resposta ao cliente
            out.write(response.getBytes());
            out.flush();

            clientSocket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}