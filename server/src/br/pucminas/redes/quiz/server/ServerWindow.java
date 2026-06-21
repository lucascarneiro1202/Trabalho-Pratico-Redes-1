package br.pucminas.redes.quiz.server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ServerWindow extends JFrame {

    // Cores modernas (tema escuro Dracula/pastel em harmonia com o cliente)
    private static final Color BG_DARK = new Color(30, 30, 46);
    private static final Color CARD_BG = new Color(45, 47, 72);
    private static final Color TEXT_WHITE = new Color(255, 255, 255);
    private static final Color TEXT_MUTED = new Color(166, 173, 200);
    private static final Color ACCENT_BLUE = new Color(137, 180, 250);
    private static final Color SUCCESS_GREEN = new Color(166, 227, 161);
    private static final Color DANGER_RED = new Color(243, 139, 168);
    private static final Color BUTTON_BG = new Color(49, 50, 68);
    private static final Color BUTTON_HOVER = new Color(69, 71, 90);

    // Fontes
    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 28);
    private static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 18);
    private static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FONT_TIMER = new Font("SansSerif", Font.BOLD, 80);

    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Componentes da Tela de Lobby
    private DefaultListModel<String> lobbyPlayersModel;
    private JList<String> listLobbyPlayers;
    private JLabel lblLobbyStatus;
    private JButton btnStartGame;

    // Componentes da Tela do Jogo (Quiz Control)
    private JLabel lblQuestion;
    private JLabel lblTimer;
    private DefaultListModel<String> gamePlayersModel;
    private JList<String> listGamePlayers;

    // Componentes da Tela de Classificação
    private JLabel lblResultTitle;
    private DefaultListModel<String> leaderboardModel;
    private JList<String> listLeaderboard;
    private JButton btnNextRound;

    public ServerWindow() {
        setTitle("Painel do Administrador - Quiz Redes I");
        setSize(850, 650);
        setMinimumSize(new Dimension(640, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_DARK);

        JPanel lobbyPanel = createLobbyPanel();
        JPanel gamePanel = createGamePanel();
        JPanel resultPanel = createResultPanel();

        mainPanel.add(lobbyPanel, "LOBBY");
        mainPanel.add(gamePanel, "GAME");
        mainPanel.add(resultPanel, "RESULT");

        add(mainPanel);
        cardLayout.show(mainPanel, "LOBBY");
    }

    // --- PAINEL DO LOBBY (AGUARDANDO CONEXÕES) ---
    private JPanel createLobbyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Bloco de Cabeçalho
        RoundedPanel headerCard = new RoundedPanel(new BorderLayout(), CARD_BG);
        headerCard.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitle = new JLabel("QUIZ REDES I - SERVIDOR", JLabel.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_WHITE);
        headerCard.add(lblTitle, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.15;
        panel.add(headerCard, gbc);

        // Bloco de Jogadores Conectados
        RoundedPanel playersCard = new RoundedPanel(new BorderLayout(5, 5), CARD_BG);
        playersCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        lblLobbyStatus = new JLabel("Jogadores Conectados (Lobby: 0)", JLabel.CENTER);
        lblLobbyStatus.setFont(FONT_SUBTITLE);
        lblLobbyStatus.setForeground(ACCENT_BLUE);
        lblLobbyStatus.setBorder(new EmptyBorder(0, 0, 15, 0));
        playersCard.add(lblLobbyStatus, BorderLayout.NORTH);

        lobbyPlayersModel = new DefaultListModel<>();
        listLobbyPlayers = new JList<>(lobbyPlayersModel);
        listLobbyPlayers.setBackground(CARD_BG);
        listLobbyPlayers.setForeground(TEXT_WHITE);
        listLobbyPlayers.setFont(FONT_BODY);
        listLobbyPlayers.setEnabled(false); // Lista apenas informativa
        
        JScrollPane scrollPane = new JScrollPane(listLobbyPlayers);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        playersCard.add(scrollPane, BorderLayout.CENTER);

        gbc.gridy = 1;
        gbc.weighty = 0.7;
        panel.add(playersCard, gbc);

        // Botão de Início
        btnStartGame = new RoundedButton("Iniciar Partida", ACCENT_BLUE);
        btnStartGame.setForeground(BG_DARK);
        btnStartGame.setFont(FONT_SUBTITLE);
        btnStartGame.setEnabled(false); // Habilitado apenas quando houver jogadores
        btnStartGame.addActionListener(e -> {
            btnStartGame.setEnabled(false);
            ServerMain.startGameLoop(); // Executa o loop do jogo em background
        });

        gbc.gridy = 2;
        gbc.weighty = 0.15;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(btnStartGame, gbc);

        return panel;
    }

    // --- PAINEL DE CONTROLE DA QUESTÃO ATIVA ---
    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Centro: Pergunta e Timer
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(BG_DARK);
        GridBagConstraints cGbc = new GridBagConstraints();
        cGbc.fill = GridBagConstraints.BOTH;
        cGbc.weightx = 1.0;

        // Card da Pergunta
        RoundedPanel questionCard = new RoundedPanel(new BorderLayout(), CARD_BG);
        questionCard.setBorder(new EmptyBorder(30, 20, 30, 20));
        
        lblQuestion = new JLabel("Pergunta Carregando...", JLabel.CENTER);
        lblQuestion.setFont(FONT_TITLE);
        lblQuestion.setForeground(TEXT_WHITE);
        questionCard.add(lblQuestion, BorderLayout.CENTER);

        cGbc.gridy = 0;
        cGbc.weighty = 0.4;
        cGbc.insets = new Insets(0, 0, 15, 0);
        centerContainer.add(questionCard, cGbc);

        // Timer de Contagem UDP
        lblTimer = new JLabel("--", JLabel.CENTER);
        lblTimer.setFont(FONT_TIMER);
        lblTimer.setForeground(ACCENT_BLUE);
        
        cGbc.gridy = 1;
        cGbc.weighty = 0.2;
        cGbc.insets = new Insets(5, 0, 15, 0);
        centerContainer.add(lblTimer, cGbc);

        // Lista de Status de Resposta dos Jogadores
        RoundedPanel statusCard = new RoundedPanel(new BorderLayout(5, 5), CARD_BG);
        statusCard.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel lblStatusTitle = new JLabel("Status das Respostas da Rodada", JLabel.CENTER);
        lblStatusTitle.setFont(FONT_SUBTITLE);
        lblStatusTitle.setForeground(ACCENT_BLUE);
        lblStatusTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        statusCard.add(lblStatusTitle, BorderLayout.NORTH);

        gamePlayersModel = new DefaultListModel<>();
        listGamePlayers = new JList<>(gamePlayersModel);
        listGamePlayers.setBackground(CARD_BG);
        listGamePlayers.setForeground(TEXT_WHITE);
        listGamePlayers.setFont(FONT_BODY);
        listGamePlayers.setEnabled(false);
        
        JScrollPane scrollPane = new JScrollPane(listGamePlayers);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        statusCard.add(scrollPane, BorderLayout.CENTER);

        cGbc.gridy = 2;
        cGbc.weighty = 0.4;
        cGbc.insets = new Insets(0, 0, 0, 0);
        centerContainer.add(statusCard, cGbc);

        panel.add(centerContainer, BorderLayout.CENTER);

        return panel;
    }

    // --- PAINEL DO SCOREBOARD (RANKING DA RODADA) ---
    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Card Cabeçalho do Resultado
        RoundedPanel feedbackCard = new RoundedPanel(new GridBagLayout(), CARD_BG);
        feedbackCard.setBorder(new EmptyBorder(20, 30, 20, 30));

        lblResultTitle = new JLabel("Classificação da Rodada", JLabel.CENTER);
        lblResultTitle.setFont(FONT_TITLE);
        lblResultTitle.setForeground(TEXT_WHITE);
        feedbackCard.add(lblResultTitle);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        panel.add(feedbackCard, gbc);

        // Bloco da Tabela de Classificação
        RoundedPanel leaderboardCard = new RoundedPanel(new BorderLayout(5, 5), CARD_BG);
        leaderboardCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        leaderboardModel = new DefaultListModel<>();
        listLeaderboard = new JList<>(leaderboardModel);
        listLeaderboard.setBackground(CARD_BG);
        listLeaderboard.setForeground(TEXT_WHITE);
        listLeaderboard.setFont(FONT_BODY);
        listLeaderboard.setCellRenderer(new LeaderboardCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(listLeaderboard);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        leaderboardCard.add(scrollPane, BorderLayout.CENTER);

        gbc.gridy = 1;
        gbc.weighty = 0.6;
        panel.add(leaderboardCard, gbc);

        // Botão Avançar Rodada
        btnNextRound = new RoundedButton("Carregando próxima pergunta...", ACCENT_BLUE);
        btnNextRound.setForeground(BG_DARK);
        btnNextRound.setFont(FONT_SUBTITLE);
        btnNextRound.setEnabled(false);
        
        gbc.gridy = 2;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(btnNextRound, gbc);

        return panel;
    }

    // --- MÉTODOS DE ATUALIZAÇÃO DA UI (EDT SAFE) ---
    
    public void updateLobbyPlayers() {
        SwingUtilities.invokeLater(() -> {
            List<ClientHandler> currentClients = ServerMain.getClientsList();
            lobbyPlayersModel.clear();
            for (ClientHandler client : currentClients) {
                lobbyPlayersModel.addElement("👤 " + client.getNickname() + " (Porta UDP: " + client.getUdpPort() + ")");
            }
            lblLobbyStatus.setText("Jogadores Conectados (Lobby: " + currentClients.size() + ")");
            btnStartGame.setEnabled(currentClients.size() > 0);
        });
    }

    public void showQuestion(String questionText, int qNum) {
        SwingUtilities.invokeLater(() -> {
            lblQuestion.setText("<html><center>Questão " + qNum + ":<br>" + questionText + "</center></html>");
            updateGamePlayerStatus();
            cardLayout.show(mainPanel, "GAME");
        });
    }

    public void updateTimer(int secondsLeft) {
        SwingUtilities.invokeLater(() -> {
            lblTimer.setText(String.valueOf(secondsLeft));
            if (secondsLeft <= 5) {
                lblTimer.setForeground(DANGER_RED);
            } else {
                lblTimer.setForeground(ACCENT_BLUE);
            }
        });
    }

    public void updateGamePlayerStatus() {
        SwingUtilities.invokeLater(() -> {
            List<ClientHandler> currentClients = ServerMain.getClientsList();
            gamePlayersModel.clear();
            for (ClientHandler client : currentClients) {
                String status = client.hasAnsweredThisRound() ? "✅ Respondeu" : "⏳ Pensando...";
                gamePlayersModel.addElement("👤 " + client.getNickname() + "   -   " + status);
            }
        });
    }

    public void showLeaderboard(String scoreboardData, boolean isGameOver) {
        SwingUtilities.invokeLater(() -> {
            leaderboardModel.clear();
            String[] lines = scoreboardData.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    leaderboardModel.addElement(line);
                }
            }

            if (isGameOver) {
                lblResultTitle.setText("RESULTADO FINAL!");
                btnNextRound.setText("Quiz Finalizado");
                btnNextRound.setEnabled(false);
            } else {
                lblResultTitle.setText("Classificação da Rodada");
                btnNextRound.setText("Próxima Pergunta carregando...");
                btnNextRound.setEnabled(false);
            }

            cardLayout.show(mainPanel, "RESULT");
        });
    }

    // --- COMPONENTES VISUAIS ARREDONDADOS REUTILIZADOS ---

    private static class RoundedPanel extends JPanel {
        private final int cornerRadius = 25;
        private final Color bgColor;

        public RoundedPanel(LayoutManager layout, Color bgColor) {
            super(layout);
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            
            g2.setColor(new Color(255, 255, 255, 15));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;
        private final int cornerRadius = 20;

        public RoundedButton(String text, Color bg) {
            super(text);
            this.normalBg = bg;
            this.hoverBg = bg.brighter();
            this.pressedBg = bg.darker();
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        setBackground(hoverBg);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (isEnabled()) {
                        setBackground(normalBg);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (!isEnabled()) {
                g2.setColor(new Color(normalBg.getRed(), normalBg.getGreen(), normalBg.getBlue(), 80));
            } else if (getModel().isPressed()) {
                g2.setColor(pressedBg);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverBg);
            } else {
                g2.setColor(normalBg);
            }
            
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class LeaderboardCellRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel lblRank = new JLabel();
        private final JLabel lblName = new JLabel();
        private final JLabel lblScore = new JLabel();

        public LeaderboardCellRenderer() {
            setLayout(new BorderLayout(15, 0));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(69, 71, 90), 1),
                    new EmptyBorder(8, 12, 8, 12)
            ));
            
            lblRank.setFont(new Font("SansSerif", Font.BOLD, 16));
            lblName.setFont(new Font("SansSerif", Font.BOLD, 14));
            lblScore.setFont(new Font("SansSerif", Font.BOLD, 14));
            
            add(lblRank, BorderLayout.WEST);
            add(lblName, BorderLayout.CENTER);
            add(lblScore, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            try {
                String rank = "";
                String name = value;
                String score = "";
                
                if (value.contains("º - ") && value.contains(" (") && value.contains(" pts)")) {
                    int dashIdx = value.indexOf("º - ");
                    int parenIdx = value.indexOf(" (");
                    int ptsIdx = value.indexOf(" pts)");
                    
                    rank = value.substring(0, dashIdx + 1); // "1º"
                    name = value.substring(dashIdx + 4, parenIdx);
                    score = value.substring(parenIdx + 2, ptsIdx) + " pts";
                }
                
                if (index == 0) {
                    lblRank.setText("🥇 " + rank);
                    lblRank.setForeground(new Color(249, 226, 175)); // Ouro
                    setBackground(new Color(69, 71, 90));
                } else if (index == 1) {
                    lblRank.setText("🥈 " + rank);
                    lblRank.setForeground(new Color(186, 194, 222)); // Prata
                    setBackground(new Color(55, 57, 80));
                } else if (index == 2) {
                    lblRank.setText("🥉 " + rank);
                    lblRank.setForeground(new Color(243, 139, 168)); // Bronze
                    setBackground(new Color(49, 50, 68));
                } else {
                    lblRank.setText("   " + rank);
                    lblRank.setForeground(TEXT_MUTED);
                    setBackground(new Color(30, 30, 46));
                }

                lblName.setText(name);
                lblScore.setText(score);
                
                lblName.setForeground(TEXT_WHITE);
                lblScore.setForeground(ACCENT_BLUE);
                
                if (isSelected) {
                    setBackground(ACCENT_BLUE);
                    lblName.setForeground(BG_DARK);
                    lblRank.setForeground(BG_DARK);
                    lblScore.setForeground(BG_DARK);
                }
                
            } catch (Exception e) {
                lblRank.setText("");
                lblName.setText(value);
                lblScore.setText("");
                lblName.setForeground(TEXT_WHITE);
                setBackground(new Color(49, 50, 68));
            }
            
            return this;
        }
    }
}
