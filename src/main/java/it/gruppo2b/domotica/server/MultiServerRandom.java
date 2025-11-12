package it.gruppo2b.domotica.server;

import java.io.*;
import java.net.*;

public class MultiServerRandom {
    private static final int TCP_PORT = 6789;
    private static final int UDP_PORT = 9999;

    public static void main(String[] args) {
        new Thread(MultiServerRandom::startDiscovery).start();
        new MultiServerRandom().startTCPServer();
    }

    private static void startDiscovery() {
        try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                if ("DISCOVER_SERVER".equals(message)) {
                    String reply = "SERVER_OK:" + InetAddress.getLocalHost().getHostAddress();
                    byte[] response = reply.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
                    socket.send(responsePacket);
                }
            }
        } catch (IOException e) {
            System.out.println("Errore discovery: " + e.getMessage());
        }
    }

    private void startTCPServer() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new ServerThread(socket).start();
            }
        } catch (IOException e) {
            System.out.println("Errore server TCP: " + e.getMessage());
        }
    }

    static class ServerThread extends Thread {
        private final Socket client;
        public ServerThread(Socket socket) {
            this.client = socket;
        }
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 DataOutputStream out = new DataOutputStream(client.getOutputStream())) {
                String msg;
                while ((msg = in.readLine()) != null) {
                    out.writeBytes("Ricevuto: " + msg + "\n");
                }
            } catch (IOException e) {
                System.out.println("Errore client " + client.getInetAddress() + ": " + e.getMessage());
            } finally {
                try {
                    client.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
