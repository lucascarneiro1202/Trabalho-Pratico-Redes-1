package br.pucminas.redes.quiz.client;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ClientMain {
    public static void main(String[] args) {
        // Tenta aplicar um LookAndFeel limpo do sistema operacional
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Utiliza o padrão caso falhe
        }

        // Executa a janela na Thread de Eventos do Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            QuizWindow window = new QuizWindow();
            window.setVisible(true);
        });
    }
}

