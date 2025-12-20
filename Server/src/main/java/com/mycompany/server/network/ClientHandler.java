package com.mycompany.server.network;

import com.mycompany.server.service.AuthService;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.json.JSONObject;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            String requestStr = in.readUTF();

            JSONObject request = new JSONObject(requestStr);
            JSONObject response = handleRequest(request);

           
            out.writeUTF(response.toString());
            out.flush();

        } catch (IOException e) {
            // System.err.println("[CLIENT] IO Error: " + e.getMessage());
        } finally {
            close();
        }
    }

    private JSONObject handleRequest(JSONObject request) {
        String type = request.optString("type", "UNKNOWN");

        switch (type) {
            case "REGISTER":
                return AuthService.register(
                        request.getString("username"),
                        request.getString("email"),
                        request.getString("password"));

            case "LOGIN":
                return AuthService.login(
                        request.getString("username"),
                        request.getString("password"));

            case "LOGOUT":
                return AuthService.logout(
                        request.getString("token"));

            default:
                JSONObject response = new JSONObject();
                response.put("success", false);
                response.put("message", "Request type not supported: " + type);
                return response;
        }
    }

    public void close() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
