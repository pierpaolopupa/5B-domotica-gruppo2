package it.gruppo2b.domotica.server;
import java.io.*;
import java.net.*;

public class DisKinServer {


    private static final int TCP_PORT = 5000;
    private static final int UDP_PORT = 5001;

    public static void main(String[] args) {
        System.out.println("Server multi-server (TCP + UDP) in ascolto su localhost...");

        new Thread(() -> startTCPServer()).start();
        new Thread(() -> startUDPServer()).start();
    }


    private static void startTCPServer() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("TCP attivo sulla porta " + TCP_PORT);

            while (true) {

                Socket clientSocket = serverSocket.accept();


                new Thread(() -> handleTCPClient(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleTCPClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("[TCP] Messaggio ricevuto: " + line);
                out.println("TCP dal server: " + line);
            }

        } catch (IOException e) {
        	
        }
    }


    private static void startUDPServer() {
        try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
            System.out.println("UDP attivo sulla porta " + UDP_PORT);

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); 

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("[UDP] Messaggio ricevuto: " + message);


                String response = "UDP dal server: " + message;
                byte[] responseBytes = response.getBytes();

                DatagramPacket responsePacket = new DatagramPacket(
                        responseBytes,
                        responseBytes.length,
                        packet.getAddress(),
                        packet.getPort()
                );

                socket.send(responsePacket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}