package br.pucminas.redes.quiz.server;

import br.pucminas.redes.quiz.common.GameMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String nickname = "Desconhecido";
    private int udpPort;
    private int score = 0;
    private String currentAnswer = "";
    private boolean answeredThisRound = false;
    private long answerTimeMillis = 0;
    private boolean running = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getNickname() {
        return nickname;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public String getCurrentAnswer() {
        return currentAnswer;
    }

    public void resetRoundState() {
        this.currentAnswer = "";
        this.answeredThisRound = false;
        this.answerTimeMillis = 0;
    }

    public boolean hasAnsweredThisRound() {
        return answeredThisRound;
    }

    public long getAnswerTimeMillis() {
        return answerTimeMillis;
    }

    public void sendMessage(GameMessage message) {
        try {
            out.writeObject(message);
            out.flush();
            out.reset(); // Limpa cache para evitar enviar objetos antigos
        } catch (IOException e) {
            System.err.println("[SERVER] Erro ao enviar mensagem para " + nickname + ": " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            while (running) {
                Object obj = in.readObject();
                if (obj instanceof GameMessage) {
                    GameMessage msg = (GameMessage) obj;
                    handleMessage(msg);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[SERVER] Conexão encerrada com " + nickname + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleMessage(GameMessage msg) {
        switch (msg.getType()) {
            case LOGIN:
                this.nickname = msg.getPayload();
                this.udpPort = msg.getValue();
                System.out.println("[SERVER] Usuário '" + nickname + "' logado através do IP " 
                        + socket.getInetAddress().getHostAddress() + " e porta UDP: " + udpPort);
                
                ServerMain.addClient(this);
                sendMessage(new GameMessage(GameMessage.Type.LOGIN_RESPONSE, "Login efetuado com sucesso!"));
                break;
            case ANSWER:
                if (!answeredThisRound) {
                    this.currentAnswer = msg.getPayload();
                    this.answerTimeMillis = System.currentTimeMillis();
                    this.answeredThisRound = true;
                    System.out.println("[SERVER] '" + nickname + "' respondeu: " + currentAnswer);
                    ServerMain.checkAllAnswered();
                    if (ServerMain.window != null) {
                        ServerMain.window.updateGamePlayerStatus();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void cleanup() {
        running = false;
        ServerMain.removeClient(this);
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            // Ignorado
        }
    }
}

