package br.pucminas.redes.quiz.server;

import br.pucminas.redes.quiz.common.GameMessage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import javax.swing.SwingUtilities;

public class ServerMain {
    private static final int PORT = 12345;
    private static final int TIMER_DURATION = 15; // Segundos por pergunta
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static final List<String[]> questions = new ArrayList<>();
    private static boolean gameStarted = false;
    private static QuizTimer activeTimer;
    public static ServerWindow window;


    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   SERVIDOR DO QUIZ DE REDES INICIADO    ");
        System.out.println("=========================================");
        
        // 1. Carregar perguntas do arquivo questions.txt
        loadQuestions("server/questions.txt");
        if (questions.isEmpty()) {
            System.err.println("[ERRO] O arquivo questions.txt está vazio ou indisponível.");
            return;
        }
        System.out.println("[INFO] " + questions.size() + " perguntas carregadas do arquivo local.");

        // 2. Iniciar escuta TCP para os clientes
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("[INFO] Escutando conexões TCP na porta: " + PORT);
                while (true) {
                    Socket socket = serverSocket.accept();
                    if (gameStarted) {
                        socket.close(); // Recusa novas conexões se a partida já começou
                    } else {
                        ClientHandler handler = new ClientHandler(socket);
                        handler.start();
                    }
                }
            } catch (IOException e) {
                System.err.println("[ERRO] Exceção no servidor TCP: " + e.getMessage());
            }
        }).start();

        // 3. Inicializar a Interface Gráfica do Servidor (Painel do Administrador)
        SwingUtilities.invokeLater(() -> {
            window = new ServerWindow();
            window.setVisible(true);
            System.out.println("[INFO] Interface gráfica do servidor carregada.");
        });
    }

    public static void startGameLoop() {
        new Thread(() -> {
            gameStarted = true;
            System.out.println("[GAME] Partida iniciada!");

            // Loop de Rodadas
            for (int i = 0; i < questions.size(); i++) {
                runRound(i);
            }

            // Finalizar Partida e Enviar Rankings
            finishGame();
        }).start();
    }

    private static void loadQuestions(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    questions.add(parts);
                } else {
                    System.err.println("[AVISO] Linha de pergunta malformada ignorada: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao carregar as perguntas: " + e.getMessage());
        }
    }

    private static void runRound(int qIdx) {
        String[] qData = questions.get(qIdx);
        String questionText = qData[0];
        String[] options = Arrays.copyOfRange(qData, 1, 5);
        String correctAnswer = qData[5].trim();

        System.out.println("\n-----------------------------------------");
        System.out.println("RODADA " + (qIdx + 1) + ": " + questionText);
        System.out.println("-----------------------------------------");

        // Atualiza a tela de controle do jogo no Administrador
        if (window != null) {
            window.showQuestion(questionText, qIdx + 1);
        }

        // Reseta estado de respostas da rodada para todos os clientes
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.resetRoundState();
            }
        }

        // Envia pergunta atual por TCP para todos os clientes logados
        GameMessage qMsg = new GameMessage(GameMessage.Type.QUESTION, questionText);
        qMsg.setOptions(options);
        qMsg.setValue(qIdx + 1); // Número da pergunta
        broadcastTCP(qMsg);

        // Dispara o timer UDP
        long roundStartTime = System.currentTimeMillis();
        activeTimer = new QuizTimer(clients, TIMER_DURATION, qIdx);
        activeTimer.start();

        // Aguarda a expiração do tempo limite da rodada (ou interrupção antecipada)
        try {
            activeTimer.join();
        } catch (InterruptedException e) {
            System.err.println("[ERRO] Thread principal interrompida durante a contagem.");
        }


        System.out.println("[GAME] Fim do tempo! Processando respostas...");

        // Processa acertos e calcula pontuações (inclui bônus por velocidade)
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.hasAnsweredThisRound()) {
                    boolean correct = client.getCurrentAnswer().equalsIgnoreCase(correctAnswer);
                    if (correct) {
                        long answerDelay = client.getAnswerTimeMillis() - roundStartTime;
                        double timeFactor = 1.0 - ((double) answerDelay / (TIMER_DURATION * 1000.0));
                        if (timeFactor < 0) timeFactor = 0;
                        int speedBonus = (int) (timeFactor * 100);
                        int earnedPoints = 100 + speedBonus;
                        client.addScore(earnedPoints);

                        GameMessage feedback = new GameMessage(GameMessage.Type.ROUND_RESULT, "CORRECT");
                        feedback.setValue(client.getScore());
                        feedback.setExtraData("Você acertou! +" + earnedPoints + " pts (" + String.format("%.2fs", answerDelay / 1000.0) + ")");
                        client.sendMessage(feedback);
                    } else {
                        GameMessage feedback = new GameMessage(GameMessage.Type.ROUND_RESULT, "WRONG");
                        feedback.setValue(client.getScore());
                        feedback.setExtraData("Você errou! A resposta correta era: " + correctAnswer);
                        client.sendMessage(feedback);
                    }
                } else {
                    GameMessage feedback = new GameMessage(GameMessage.Type.ROUND_RESULT, "TIMEOUT");
                    feedback.setValue(client.getScore());
                    feedback.setExtraData("Tempo esgotado! A resposta correta era: " + correctAnswer);
                    client.sendMessage(feedback);
                }
            }

            // Envia o ranking atualizado da rodada para todos os clientes e painel do administrador
            String scoreboard = generateScoreboard();
            System.out.println("\n[PLACAR ATUAL]\n" + scoreboard);
            
            if (window != null) {
                boolean isGameOver = (qIdx == questions.size() - 1);
                window.showLeaderboard(scoreboard, isGameOver);
            }

            for (ClientHandler client : clients) {
                GameMessage boardMsg = new GameMessage(GameMessage.Type.ROUND_RESULT, "SCOREBOARD");
                boardMsg.setExtraData(scoreboard);
                client.sendMessage(boardMsg);
            }
        }

        // Aguarda 5 segundos (tela de feedback no cliente) antes da próxima rodada
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // Ignorado
        }
    }

    private static void finishGame() {
        System.out.println("\n=========================================");
        System.out.println("             QUIZ TERMINADO!             ");
        System.out.println("=========================================");
        
        String finalRanking = generateScoreboard();
        System.out.println("[RANKING FINAL]\n" + finalRanking);

        GameMessage overMsg = new GameMessage(GameMessage.Type.GAME_OVER, "Fim do Quiz!");
        overMsg.setExtraData(finalRanking);
        broadcastTCP(overMsg);

        // Fecha sockets e limpa clientes
        synchronized (clients) {
            Iterator<ClientHandler> it = clients.iterator();
            while (it.hasNext()) {
                ClientHandler client = it.next();
                try {
                    client.getSocket().close();
                } catch (IOException e) {
                    // Silencioso
                }
                it.remove();
            }
        }
        System.out.println("[INFO] Servidor finalizado com sucesso.");
    }

    private static String generateScoreboard() {
        List<ClientHandler> sorted = new ArrayList<>(clients);
        sorted.sort((c1, c2) -> Integer.compare(c2.getScore(), c1.getScore()));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            ClientHandler c = sorted.get(i);
            sb.append((i + 1)).append("º - ").append(c.getNickname()).append(" (")
              .append(c.getScore()).append(" pts)\n");
        }
        return sb.toString();
    }

    private static void broadcastTCP(GameMessage message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    public static void checkAllAnswered() {
        synchronized (clients) {
            boolean allAnswered = true;
            for (ClientHandler client : clients) {
                if (!client.hasAnsweredThisRound()) {
                    allAnswered = false;
                    break;
                }
            }
            if (allAnswered && activeTimer != null && activeTimer.isAlive()) {
                System.out.println("[GAME] Todos os jogadores responderam! Encerrando a rodada antecipadamente...");
                activeTimer.interrupt();
            }
        }
    }

    public static List<ClientHandler> getClientsList() {
        synchronized (clients) {
            return new ArrayList<>(clients);
        }
    }

    public static void addClient(ClientHandler client) {
        clients.add(client);
        if (window != null) {
            window.updateLobbyPlayers();
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        if (window != null) {
            window.updateLobbyPlayers();
            window.updateGamePlayerStatus();
        }
    }

    public static void resetServer() {
        gameStarted = false;
        synchronized (clients) {
            clients.clear();
        }
        if (window != null) {
            window.resetToLobby();
        }
        System.out.println("[INFO] Servidor reiniciado. Pronto para novas conexões.");
    }
}

