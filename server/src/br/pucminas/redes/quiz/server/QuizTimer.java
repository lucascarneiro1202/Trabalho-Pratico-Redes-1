package br.pucminas.redes.quiz.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class QuizTimer extends Thread {
    private final List<ClientHandler> clients;
    private final int durationSeconds;
    private final int currentQuestionIndex;

    public QuizTimer(List<ClientHandler> clients, int durationSeconds, int currentQuestionIndex) {
        this.clients = clients;
        this.durationSeconds = durationSeconds;
        this.currentQuestionIndex = currentQuestionIndex;
    }

    @Override
    public void run() {
        System.out.println("[TIMER] Cronômetro UDP iniciado para a Questão " + (currentQuestionIndex + 1));
        
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            for (int sec = durationSeconds; sec >= 0; sec--) {
                if (isInterrupted()) {
                    break;
                }

                // Payload simples no formato "TIMER;<tempo_restante>"
                String payload = "TIMER;" + sec;
                byte[] data = payload.getBytes(StandardCharsets.UTF_8);

                synchronized (clients) {
                    for (ClientHandler client : clients) {
                        try {
                            InetAddress address = client.getSocket().getInetAddress();
                            int port = client.getUdpPort();
                            
                            DatagramPacket packet = new DatagramPacket(
                                    data,
                                    data.length,
                                    address,
                                    port
                            );
                            udpSocket.send(packet);
                        } catch (Exception ex) {
                            System.err.println("[TIMER] Erro ao enviar UDP para " + client.getNickname() + ": " + ex.getMessage());
                        }
                    }
                }

                // Dorme por 1 segundo entre envios
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("[TIMER] Cronômetro UDP interrompido.");
        } catch (Exception e) {
            System.err.println("[TIMER] Erro no socket UDP: " + e.getMessage());
        }
        System.out.println("[TIMER] Cronômetro UDP finalizado.");
    }
}

