package it.gruppo2b.domotica.client;

import it.gruppo2b.domotica.net.MessageListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;

public class DisKinClient {

    private static final Logger log = LogManager.getLogger(DisKinClient.class);
    private final String host;
    private final int tcpPort;
    private final int udpPort;
    private MessageListener listener;

    private Socket tcpSocket;
    private BufferedReader tcpIn;
    private PrintWriter tcpOut;
    private DatagramSocket udpSocket;

    public DisKinClient(String host, int tcpPort, int udpPort) {
        this.host = host;
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public boolean connectTCP() {
        try {
            tcpSocket = new Socket(host, tcpPort);
            tcpIn = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            tcpOut = new PrintWriter(tcpSocket.getOutputStream(), true);
            startTCPListener();
            log.info("TCP connesso al server.");
            if (listener != null) listener.onMessage("LOCAL", "TCP connesso a " + host + ":" + tcpPort);
            return true;
        } catch (IOException e) {
            log.error("Errore connessione TCP: " + e.getMessage());
            if (listener != null) listener.onMessage("ERROR", "TCP connect error: " + e.getMessage());
            return false;
        }
    }

    public void sendTCP(String message) {
        if (tcpOut != null) {
            tcpOut.println(message);
            if (listener != null) listener.onMessage("SENT-TCP", message);
        } else {
            if (listener != null) listener.onMessage("ERROR", "TCP non connesso");
        }
    }

    private void startTCPListener() {
        new Thread(() -> {
            try {
                String response;
                while ((response = tcpIn.readLine()) != null) {
                    if (listener != null) listener.onMessage("TCP " + tcpSocket.getRemoteSocketAddress(), response);
                }
            } catch (IOException e) {
                if (listener != null) listener.onMessage("INFO", "TCP listener chiuso");
            }
        }, "TCP-Client-Listener").start();
    }

    public boolean initializeUDP() {
        try {
            udpSocket = new DatagramSocket();
            udpSocket.setSoTimeout(2000);
            if (listener != null) listener.onMessage("LOCAL", "UDP socket pronta");
            return true;
        } catch (SocketException e) {
            if (listener != null) listener.onMessage("ERROR", "Errore UDP: " + e.getMessage());
            return false;
        }
    }

    public void sendUDP(String msg) {
        new Thread(() -> {
            try {
                byte[] buffer = msg.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(host), udpPort);
                udpSocket.send(packet);

                if (listener != null) listener.onMessage("SENT-UDP", msg);

                byte[] recvBuffer = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(recvBuffer, recvBuffer.length);
                udpSocket.receive(responsePacket);
                String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                if (listener != null) listener.onMessage("UDP " + responsePacket.getAddress() + ":" + responsePacket.getPort(), response);
            } catch (IOException e) {
                if (listener != null) listener.onMessage("ERROR", "UDP error: " + e.getMessage());
            }
        }, "UDP-Sender").start();
    }

    public void close() {
        try { if (tcpSocket != null) tcpSocket.close(); } catch (IOException ignored) {}
        if (udpSocket != null) udpSocket.close();
        if (listener != null) listener.onMessage("INFO", "Client chiuso");
    }
}
