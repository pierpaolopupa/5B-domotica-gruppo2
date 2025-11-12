package it.gruppo2b.domotica.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final int UDP_PORT = 9999;
    private static final int TCP_PORT = 6789;

    public static void main(String[] args) {
        try {
            String serverIp = discoverServer();
            if (serverIp == null) {
                System.out.println("Nessun server trovato.");
                return;
            }
            try (Socket socket = new Socket(serverIp, TCP_PORT);
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 Scanner tastiera = new Scanner(System.in)) {
                while (true) {
                    String input = tastiera.nextLine();
                    if (input.trim().isEmpty()) break;
                    out.writeBytes(input + "\n");
                    String risposta = in.readLine();
                    if (risposta == null) break;
                    System.out.println("Server: " + risposta);
                }
            }
        } catch (IOException e) {
            System.out.println("Errore client: " + e.getMessage());
        }
    }

    private static String discoverServer() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            byte[] requestData = "DISCOVER_SERVER".getBytes();
            DatagramPacket packet = new DatagramPacket(requestData, requestData.length, InetAddress.getByName("255.255.255.255"), UDP_PORT);
            socket.send(packet);
            socket.setSoTimeout(3000);
            byte[] buffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            if (response.startsWith("SERVER_OK:")) return response.split(":")[1];
        } catch (IOException ignored) {}
        return null;
    }
}
