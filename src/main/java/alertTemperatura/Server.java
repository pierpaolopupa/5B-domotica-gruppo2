package alertTemperatura;

import java.net.*;
import java.io.*;
import org.json.JSONObject;
import java.time.LocalTime;

class ServerThread extends Thread {
    private Socket client;
    private BufferedReader inDalClient;
    private DataOutputStream outVersoClient;
    private static LocalTime oraAllarmeMovimento = LocalTime.of(22, 0); // esempio: 22:00

    public ServerThread(Socket socket){
        this.client = socket;
    }

    public void run(){
        try {
            comunica();
        } catch (Exception e){
            e.printStackTrace(System.out);
        }
    }

    public void comunica() throws Exception {
        inDalClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
        outVersoClient = new DataOutputStream(client.getOutputStream());

        String messaggioJson;
        while ((messaggioJson = inDalClient.readLine()) != null) {

            if (messaggioJson.equalsIgnoreCase("FINE")) {
                outVersoClient.writeBytes("Chiusura connessione...\n");
                break;
            }

            try {
                JSONObject json = new JSONObject(messaggioJson);
                String tipo = json.getString("tipo").toLowerCase();

                switch (tipo) {
                    case "temperatura":
                        gestisciTemperatura(json);
                        break;
                    case "movimento":
                        gestisciMovimento(json);
                        break;
                    case "porta":
                        gestisciPorta(json);
                        break;
                    default:
                        outVersoClient.writeBytes("Tipo sensore non riconosciuto\n");
                        System.out.println("❓ Tipo sconosciuto: " + tipo);
                        break;
                }

            } catch (Exception e) {
                outVersoClient.writeBytes("Errore nel parsing del JSON\n");
                System.out.println("⚠️ JSON non valido: " + messaggioJson);
            }
        }

        outVersoClient.close();
        inDalClient.close();
        client.close();
        System.out.println("🔒 Chiusura socket: " + client);
    }

    private void gestisciTemperatura(JSONObject json) throws IOException {
        double valore = json.getDouble("valore");
        int id = json.getInt("id");

        System.out.println("🌡️ Temperatura #" + id + ": " + valore + "°C");

        if (valore > 35.0) {
            String alert = "⚠️ ALERT: Temperatura troppo alta (" + valore + "°C)";
            System.out.println(alert);
            outVersoClient.writeBytes(alert + "\n");
        } else {
            outVersoClient.writeBytes("Temperatura OK: " + valore + "°C\n");
        }
    }

    private void gestisciMovimento(JSONObject json) throws IOException {
        boolean valore = json.getBoolean("valore");
        String zona = json.optString("zona", "sconosciuta");
        String ora = json.optString("ora", "00:00");
        int id = json.getInt("id");

        System.out.println("🏃 Movimento #" + id +
                " | zona: " + zona +
                " | ora: " + ora +
                " | valore: " + valore);

        LocalTime oraAttuale = LocalTime.now();

        if (valore && oraAttuale.isAfter(oraAllarmeMovimento)) {
            String alert = "🚨 ALERT: Movimento rilevato nella zona " + zona +
                    " dopo l'ora impostata (" + oraAttuale + ")";
            System.out.println(alert);
            outVersoClient.writeBytes(alert + "\n");
        } else {
            outVersoClient.writeBytes("Movimento registrato, nessun alert.\n");
        }
    }

    private void gestisciPorta(JSONObject json) throws IOException {
        boolean valore = json.getBoolean("valore"); // true = aperta, false = chiusa
        String zona = json.optString("zona", "sconosciuta");
        int id = json.getInt("id");

        System.out.println("🚪 Porta #" + id + " | zona: " + zona + " | aperta: " + valore);

        if (valore) {
            String alert = "🚨 ALERT: Porta aperta nella zona " + zona;
            System.out.println(alert);
            outVersoClient.writeBytes(alert + "\n");
        } else {
            outVersoClient.writeBytes("Porta chiusa correttamente.\n");
        }
    }
}

public class MultiServer {
    public void start(){
        try {
            ServerSocket serverSocket = new ServerSocket(6789);
            System.out.println("✅ Server avviato sulla porta 6789...");

            for (;;) {
                System.out.println("💤 In attesa di connessioni...");
                Socket socket = serverSocket.accept();
                System.out.println("🔗 Connessione accettata: " + socket);
                ServerThread serverThread = new ServerThread(socket);
                serverThread.start();
            }
        } catch (Exception e) {
            System.out.println("❌ Errore durante l'istanza del server: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main (String[] args){
        MultiServer tcpServer = new MultiServer();
        tcpServer.start();
    }
}
