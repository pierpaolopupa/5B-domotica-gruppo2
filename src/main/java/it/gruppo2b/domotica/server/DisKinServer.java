package it.gruppo2b.domotica.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DisKinServer {

    private static final Logger log = LogManager.getLogger(DisKinServer.class);

    private final int tcpPort;
    private final int udpPort;
    private final ExecutorService pool;

    private volatile boolean running = false;
    private ServerSocket tcpServerSocket;
    private DatagramSocket udpSocket;

    private ServerListener listener;

    public interface ServerListener {
        void onMessage(String from, String message);
    }

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }

    public DisKinServer(int tcpPort, int udpPort, int threads) {
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        this.pool = Executors.newFixedThreadPool(threads);
    }

    public void start() {
        running = true;

        startTCP();
        startUDP();
    }

    private void startTCP() {
        pool.execute(() -> {
            try (ServerSocket server = new ServerSocket(tcpPort)) {
                tcpServerSocket = server;
                log.info("TCP server listening on " + tcpPort);

                while (running) {
                    Socket client = server.accept();
                    pool.execute(() -> handleTCPClient(client));
                }

            } catch (IOException e) {
                if (running) log.error("TCP server error: ", e);
            }
        });
    }

    private void handleTCPClient(Socket client) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out = new PrintWriter(client.getOutputStream(), true)
        ) {
            String line;
            while (running && (line = in.readLine()) != null) {

                if (listener != null) {
                    listener.onMessage(client.getRemoteSocketAddress().toString(), line);
                }

                String jsonResponse = processMessage(line);

                out.println(jsonResponse);
            }

        } catch (Exception ignored) {}
    }

    private String processMessage(String json) {
        try {
            String type = extract(json, "type");
            String value = extract(json, "value");

            String result;

            switch (type) {
                case "TEMPERATURA" -> result = "Temperatura ricevuta: " + value;
                case "MOVIMENTO" -> result = "Movimento rilevato: " + value;
                case "UMIDITA" -> result = "Umidità ricevuta: " + value;
                default -> result = "Tipo sconosciuto: " + type;
            }

            return
                    "{ \"status\": \"OK\", \"ack\": \"" + result + "\", \"timestamp\": " + System.currentTimeMillis() + " }";

        } catch (Exception e) {
            return "{ \"status\": \"ERROR\", \"msg\": \"JSON non valido\" }";
        }
    }

    private String extract(String json, String key) {
        String k = "\"" + key + "\":";
        int start = json.indexOf(k);
        if (start == -1) return "";
        start = json.indexOf("\"", start + k.length()) + 1;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private void startUDP() {
        pool.execute(() -> {
            try (DatagramSocket socket = new DatagramSocket(udpPort)) {
                udpSocket = socket;
                log.info("UDP server listening on " + udpPort);

                byte[] buffer = new byte[1024];

                while (running) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String msg = new String(packet.getData(), 0, packet.getLength());

                    if (listener != null) {
                        listener.onMessage(packet.getAddress() + ":" + packet.getPort(), msg);
                    }

                    if (msg.toUpperCase().contains("TEMPERATURA")) {
                        double temp = Double.parseDouble(msg.substring(5));
                        if (temp > 35) {
                            String coolCmd = "COOL";
                            byte[] coolBytes = coolCmd.getBytes();
                            DatagramPacket coolPacket = new DatagramPacket(
                                    coolBytes, coolBytes.length,
                                    packet.getAddress(), packet.getPort()
                            );
                            socket.send(coolPacket);
                        }
                    }

                    String response = "{ \"udpAck\": \"" + msg + "\" }";
                    byte[] respBytes = response.getBytes();

                    DatagramPacket resp = new DatagramPacket(
                            respBytes, respBytes.length,
                            packet.getAddress(), packet.getPort()
                    );
                    socket.send(resp);
                }

            } catch (IOException e) {
                if (running) log.error("UDP server error", e);
            }
        });
    }

    public void stop() {
        running = false;

        try {
            if (tcpServerSocket != null) tcpServerSocket.close();
        } catch (Exception ignored) {}

        try {
            if (udpSocket != null) udpSocket.close();
        } catch (Exception ignored) {}

        pool.shutdownNow();
    }
}
