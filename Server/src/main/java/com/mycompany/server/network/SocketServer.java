package com.mycompany.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private boolean running = false;
    private ExecutorService executorService;
    private final Vector<ClientHandler> clients = new Vector<>();

    public void start() {
        if (running)
            return;
        System.out.println("Server Running on Port 5000");
        executorService = Executors.newCachedThreadPool();
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                running = true;

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();

                        ClientHandler handler = new ClientHandler(clientSocket);
                        clients.add(handler);
                        executorService.execute(handler);

                    } catch (IOException e) {

                    }
                }
            } catch (IOException e) {
            }
        }).start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null)
                serverSocket.close();
            for (ClientHandler client : clients)
                client.close();
            clients.clear();
            executorService.shutdownNow();
        } catch (IOException e) {
        }
    }

    public boolean isRunning() {
        return running;
    }
}
