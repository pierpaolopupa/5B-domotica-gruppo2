package it.gruppo2b.domotica.server;

import it.gruppo2b.domotica.net.MessageListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DisKinServer {
    private final int tcpPort;
    private final int udpPort;
    private final ExecutorService pool;
    private volatile boolean running = true;
    private MessageListener listener;

    public DisKinServer(int tcpPort, int udpPort, int poolSize) {
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        this.pool = Executors.newFixedThreadPool(poolSize);
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public void start() {
        new Thread(this::startTCPServer, "Server-TCP-Acceptor").start();
        new Thread(this::startUDPServer, "Server-UDP").start();
    }

    public void stop() {
        running = false;
        pool.shutdownNow();
    }

    private void startTCPServer() {
        try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
            notify("INFO", "TCP attivo sulla porta " + tcpPort);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                notify("INFO", "Connessione da " + clientSocket.getRemoteSocketAddress());
                pool.submit(() -> handleTCPClient(clientSocket));
            }
        } catch (IOException e) {
            notify("ERROR", "TCP error: " + e.getMessage());
        }
    }

    private void handleTCPClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String line;
            while ((line = in.readLine()) != null) {
                String from = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
                notify("TCP " + from, line);
                out.println("{\"status\":\"OK\",\"msg\":\"Ricevuto\"}");
            }
        } catch (IOException e) {
            notify("ERROR", "client handler error: " + e.getMessage());
        }
    }

    private void startUDPServer() {
        try (DatagramSocket socket = new DatagramSocket(udpPort)) {
            notify("INFO", "UDP attivo sulla porta " + udpPort);
            byte[] buffer = new byte[2048];
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                String from = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                notify("UDP " + from, message);

                String response = "{\"status\":\"OK\",\"msg\":\"Ricevuto UDP\"}";
                byte[] responseBytes = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());
                socket.send(responsePacket);
            }
        } catch (IOException e) {
            notify("ERROR", "UDP server error: " + e.getMessage());
        }
    }

    private void notify(String from, String message) {
        if (listener != null) listener.onMessage(from, message);
        else System.out.println("[" + from + "] " + message);
    }
}
