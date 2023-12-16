package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                // Configurar leitura e escrita para o cliente
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Exemplo simples de comunicação
                out.println("Bem-vindo ao servidor!");

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received from " + clientSocket.getInetAddress() + ": " + inputLine);
                    out.println("Recebido: " + inputLine);

                    if (inputLine.equals("bye")) {
                        break;
                    }
                }

                // Fechar recursos
                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
