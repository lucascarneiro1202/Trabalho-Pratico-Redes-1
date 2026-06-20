package br.pucminas.redes.quiz.client;

import br.pucminas.redes.quiz.common.GameMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientSocketManager {

    public interface GameEventListener {
        void onConnected();
        void onConnectionFailed(String error);
        void onQuestionReceived(String text, String[] options, int questionNum);
        void onRoundResult(String feedback, int currentScore);
        void onLeaderboardUpdated(String leaderboard);
        void onTimerTick(int secondsLeft);
        void onGameOver(String finalRanking);
        void onDisconnected();
    }

    private Socket tcpSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private DatagramSocket udpSocket;
    private final GameEventListener listener;
    private boolean running = false;
    private String nickname;

    public ClientSocketManager(GameEventListener listener) {
        this.listener = listener;
    }

    public void connect(String ip, int port, String nickname) {
        this.nickname = nickname;
        new Thread(() -> {
            try {
                // 1. Abre porta UDP fixa (padrão: 12346) para escutar o cronômetro
                int localUdpPort = 12346;
                while (true) {
                    try {
                        udpSocket = new DatagramSocket(localUdpPort);
                        break;
                    } catch (java.net.SocketException e) {
                        localUdpPort++; // Tenta a próxima porta se a padrão estiver em uso
                        if (localUdpPort > 12360) {
                            throw e; // Limita a busca para evitar loop infinito
                        }
                    }
                }
                System.out.println("[CLIENT] Escutando timer na porta UDP local: " + localUdpPort);

                // 2. Estabelece a conexão TCP
                tcpSocket = new Socket(ip, port);
                out = new ObjectOutputStream(tcpSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(tcpSocket.getInputStream());

                running = true;

                // 3. Envia o pacote de login contendo a porta UDP do cliente
                GameMessage loginMsg = new GameMessage(GameMessage.Type.LOGIN, nickname);
                loginMsg.setValue(localUdpPort);
                out.writeObject(loginMsg);
                out.flush();

                // 4. Inicia as Threads receptoras
                startTCPReceiver();
                startUDPReceiver();

            } catch (Exception e) {
                listener.onConnectionFailed(e.getMessage());
                disconnect();
            }
        }).start();
    }

    public void sendAnswer(String answerChar) {
        if (!running) return;
        try {
            GameMessage answerMsg = new GameMessage(GameMessage.Type.ANSWER, answerChar);
            out.writeObject(answerMsg);
            out.flush();
        } catch (IOException e) {
            System.err.println("[CLIENT] Erro ao enviar resposta: " + e.getMessage());
        }
    }

    private void startTCPReceiver() {
        new Thread(() -> {
            try {
                while (running) {
                    Object obj = in.readObject();
                    if (obj instanceof GameMessage) {
                        GameMessage msg = (GameMessage) obj;
                        handleMessage(msg);
                    }
                }
            } catch (Exception e) {
                if (running) {
                    listener.onDisconnected();
                    disconnect();
                }
            }
        }).start();
    }

    private void startUDPReceiver() {
        new Thread(() -> {
            byte[] buffer = new byte[256];
            try {
                while (running && udpSocket != null && !udpSocket.isClosed()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    String data = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
                    
                    // Decodifica a mensagem "TIMER;<segundos>"
                    if (data.startsWith("TIMER;")) {
                        try {
                            int sec = Integer.parseInt(data.split(";")[1]);
                            listener.onTimerTick(sec);
                        } catch (NumberFormatException e) {
                            // Ignora erro de formatação
                        }
                    }
                }
            } catch (Exception e) {
                // Socket fechado de forma silenciosa na desconexão
            }
        }).start();
    }

    private void handleMessage(GameMessage msg) {
        switch (msg.getType()) {
            case LOGIN_RESPONSE:
                listener.onConnected();
                break;
            case QUESTION:
                listener.onQuestionReceived(msg.getPayload(), msg.getOptions(), msg.getValue());
                break;
            case ROUND_RESULT:
                if ("SCOREBOARD".equals(msg.getPayload())) {
                    listener.onLeaderboardUpdated(msg.getExtraData());
                } else {
                    listener.onRoundResult(msg.getExtraData(), msg.getValue());
                }
                break;
            case GAME_OVER:
                listener.onGameOver(msg.getExtraData());
                disconnect();
                break;
            default:
                break;
        }
    }

    public void disconnect() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (tcpSocket != null && !tcpSocket.isClosed()) tcpSocket.close();
            if (udpSocket != null && !udpSocket.isClosed()) udpSocket.close();
        } catch (IOException e) {
            // Silencioso
        }
    }
}

