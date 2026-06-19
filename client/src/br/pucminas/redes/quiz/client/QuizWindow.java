package br.pucminas.redes.quiz.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class QuizWindow extends JFrame implements ClientSocketManager.GameEventListener {

    // Cores modernas (tema escuro/pastel lúdico inspirado no Kahoot/Dracula)
    private static final Color BG_DARK = new Color(30, 30, 46);
    private static final Color CARD_BG = new Color(45, 47, 72);
    private static final Color TEXT_WHITE = new Color(255, 255, 255);
    private static final Color TEXT_MUTED = new Color(166, 173, 200);
    private static final Color ACCENT_BLUE = new Color(137, 180, 250);
    private static final Color SUCCESS_GREEN = new Color(166, 227, 161);
    private static final Color DANGER_RED = new Color(243, 139, 168);
    private static final Color BUTTON_BG = new Color(49, 50, 68);

    // Cores das 4 Alternativas (Kahoot Colors)
    private static final Color RED_COLOR = new Color(226, 27, 60);
    private static final Color BLUE_COLOR = new Color(19, 104, 206);
    private static final Color YELLOW_COLOR = new Color(216, 158, 0);
    private static final Color GREEN_COLOR = new Color(38, 137, 12);

    // Fontes
    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 28);
    private static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 18);
    private static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FONT_TIMER = new Font("SansSerif", Font.BOLD, 80); // Tamanho monumental

    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Componentes de Login
    private JTextField txtName;
    private JTextField txtIP;
    private JTextField txtPort;
    private JButton btnConnect;

    // Componentes do Jogo
    private JLabel lblQuestion;
    private JLabel lblTimer;
    private JLabel lblPlayerInfo;
    private JButton[] btnOptions;
    private JLabel lblGameStatus;
    
    // Componentes do Resultado
    private JLabel lblResultTitle;
    private JLabel lblResultFeedback;
    private JList<String> listLeaderboard;
    private DefaultListModel<String> leaderboardModel;
    private JButton btnNextRound;

    // Controlador de Rede
    private ClientSocketManager socketManager;
    private String selectedAnswer = "";

    public QuizWindow() {
        setTitle("Quiz em Tempo Real - Redes I");
        setSize(850, 650);
        setMinimumSize(new Dimension(640, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Garante fechamento limpo das sockets ao fechar a janela
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (socketManager != null) {
                    socketManager.disconnect();
                }
            }
        });

        // Inicializa CardLayout para gerenciar as telas
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_DARK);

        // Criação dos painéis (telas)
        JPanel loginPanel = createLoginPanel();
        JPanel gamePanel = createGamePanel();
        JPanel resultPanel = createResultPanel();

        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(gamePanel, "GAME");
        mainPanel.add(resultPanel, "RESULT");

        add(mainPanel);

        // Define a tela inicial
        cardLayout.show(mainPanel, "LOGIN");
    }

    // --- PAINEL DE LOGIN / CONEXÃO ---
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Container Centralizado (Card Arredondado)
        RoundedPanel card = new RoundedPanel(new GridBagLayout(), CARD_BG);
        card.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints cGbc = new GridBagConstraints();
        cGbc.insets = new Insets(10, 10, 10, 10);
        cGbc.fill = GridBagConstraints.HORIZONTAL;

        // Título do Quiz
        JLabel lblTitle = new JLabel("QUIZ REDES I", JLabel.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_WHITE);
        cGbc.gridx = 0;
        cGbc.gridy = 0;
        cGbc.gridwidth = 2;
        card.add(lblTitle, cGbc);

        // Subtítulo
        JLabel lblSub = new JLabel("Conecte-se à sessão para jogar", JLabel.CENTER);
        lblSub.setFont(FONT_BODY);
        lblSub.setForeground(TEXT_MUTED);
        cGbc.gridy = 1;
        card.add(lblSub, cGbc);

        // Separador Espaçador
        cGbc.gridy = 2;
        card.add(Box.createVerticalStrut(15), cGbc);

        // Input Nome do Jogador
        JLabel lblName = new JLabel("Apelido / Nickname");
        lblName.setFont(FONT_LABEL);
        lblName.setForeground(ACCENT_BLUE);
        cGbc.gridy = 3;
        cGbc.gridwidth = 2;
        card.add(lblName, cGbc);

        txtName = new RoundedTextField("Jogador 1", BUTTON_BG);
        txtName.setFont(FONT_BODY);
        cGbc.gridy = 4;
        card.add(txtName, cGbc);

        // Input IP
        JLabel lblIP = new JLabel("IP do Servidor");
        lblIP.setFont(FONT_LABEL);
        lblIP.setForeground(ACCENT_BLUE);
        cGbc.gridy = 5;
        cGbc.gridwidth = 1;
        card.add(lblIP, cGbc);

        txtIP = new RoundedTextField("127.0.0.1", BUTTON_BG);
        txtIP.setFont(FONT_BODY);
        cGbc.gridy = 6;
        card.add(txtIP, cGbc);

        // Input Porta
        JLabel lblPort = new JLabel("Porta TCP");
        lblPort.setFont(FONT_LABEL);
        lblPort.setForeground(ACCENT_BLUE);
        cGbc.gridx = 1;
        cGbc.gridy = 5;
        card.add(lblPort, cGbc);

        txtPort = new RoundedTextField("12345", BUTTON_BG);
        txtPort.setFont(FONT_BODY);
        cGbc.gridx = 1;
        cGbc.gridy = 6;
        card.add(txtPort, cGbc);

        // Botão Conectar (Rounded)
        btnConnect = new RoundedButton("Conectar e Jogar", ACCENT_BLUE);
        btnConnect.setForeground(BG_DARK);
        btnConnect.setFont(FONT_SUBTITLE);
        cGbc.gridx = 0;
        cGbc.gridy = 7;
        cGbc.gridwidth = 2;
        cGbc.insets = new Insets(25, 10, 10, 10);
        card.add(btnConnect, cGbc);

        // Ação de conexão
        btnConnect.addActionListener(e -> handleConnect());

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(card, gbc);

        return panel;
    }

    // --- PAINEL PRINCIPAL DO JOGO (QUIZ) ---
    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Cabeçalho: Info do Jogador
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_DARK);

        lblPlayerInfo = new JLabel("Conectando...", JLabel.LEFT);
        lblPlayerInfo.setFont(FONT_SUBTITLE);
        lblPlayerInfo.setForeground(TEXT_WHITE);
        headerPanel.add(lblPlayerInfo, BorderLayout.WEST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Centro: Pergunta e Timer Centralizado
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(BG_DARK);
        GridBagConstraints cGbc = new GridBagConstraints();
        cGbc.fill = GridBagConstraints.BOTH;
        cGbc.weightx = 1.0;

        // Card da Pergunta
        RoundedPanel questionCard = new RoundedPanel(new BorderLayout(), CARD_BG);
        questionCard.setBorder(new EmptyBorder(30, 20, 30, 20));
        
        lblQuestion = new JLabel("Aguardando início do jogo pelo administrador...", JLabel.CENTER);
        lblQuestion.setFont(FONT_TITLE);
        lblQuestion.setForeground(TEXT_WHITE);
        questionCard.add(lblQuestion, BorderLayout.CENTER);

        cGbc.gridy = 0;
        cGbc.weighty = 0.6;
        cGbc.insets = new Insets(0, 0, 15, 0);
        centerContainer.add(questionCard, cGbc);

        // Timer Monumental Ticking
        lblTimer = new JLabel("--", JLabel.CENTER);
        lblTimer.setFont(FONT_TIMER);
        lblTimer.setForeground(ACCENT_BLUE);
        
        cGbc.gridy = 1;
        cGbc.weighty = 0.4;
        cGbc.insets = new Insets(5, 0, 5, 0);
        centerContainer.add(lblTimer, cGbc);

        panel.add(centerContainer, BorderLayout.CENTER);

        // Inferior: Opções do Kahoot
        JPanel footerPanel = new JPanel(new BorderLayout(10, 10));
        footerPanel.setBackground(BG_DARK);

        // Grid de Opções (2x2) de tamanho massivo (Fitts's Law)
        JPanel optionsGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        optionsGrid.setBackground(BG_DARK);
        optionsGrid.setPreferredSize(new Dimension(800, 250)); // Altura enorme dos botões

        btnOptions = new JButton[4];
        Color[] optionColors = { RED_COLOR, BLUE_COLOR, YELLOW_COLOR, GREEN_COLOR };
        for (int i = 0; i < 4; i++) {
            btnOptions[i] = new RoundedButton("", optionColors[i]);
            btnOptions[i].setFont(FONT_SUBTITLE);
            btnOptions[i].setEnabled(false);
            final int index = i;
            btnOptions[i].addActionListener(e -> handleSelectAnswer(index));
            optionsGrid.add(btnOptions[i]);
        }
        footerPanel.add(optionsGrid, BorderLayout.CENTER);

        // Barra de Status de envio
        lblGameStatus = new JLabel("Selecione a alternativa correta contra o relógio!", JLabel.CENTER);
        lblGameStatus.setFont(FONT_BODY);
        lblGameStatus.setForeground(TEXT_MUTED);
        lblGameStatus.setBorder(new EmptyBorder(10, 0, 0, 0));
        footerPanel.add(lblGameStatus, BorderLayout.SOUTH);

        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- PAINEL DE RESULTADOS E SCOREBOARD ---
    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Card Principal de Feedback (Arredondado)
        RoundedPanel feedbackCard = new RoundedPanel(new GridBagLayout(), CARD_BG);
        feedbackCard.setBorder(new EmptyBorder(25, 30, 25, 30));

        GridBagConstraints fGbc = new GridBagConstraints();
        fGbc.insets = new Insets(8, 8, 8, 8);
        fGbc.fill = GridBagConstraints.HORIZONTAL;

        lblResultTitle = new JLabel("Fim da Rodada!", JLabel.CENTER);
        lblResultTitle.setFont(FONT_TITLE);
        lblResultTitle.setForeground(TEXT_WHITE);
        fGbc.gridx = 0;
        fGbc.gridy = 0;
        fGbc.gridwidth = 2;
        feedbackCard.add(lblResultTitle, fGbc);

        lblResultFeedback = new JLabel("Aguardando resposta do servidor...", JLabel.CENTER);
        lblResultFeedback.setFont(FONT_SUBTITLE);
        lblResultFeedback.setForeground(SUCCESS_GREEN);
        fGbc.gridy = 1;
        feedbackCard.add(lblResultFeedback, fGbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        panel.add(feedbackCard, gbc);

        // Bloco de Placar de Liderança (Arredondado)
        RoundedPanel leaderboardCard = new RoundedPanel(new BorderLayout(5, 5), CARD_BG);
        leaderboardCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblLdbTitle = new JLabel("Classificação Geral da Partida", JLabel.CENTER);
        lblLdbTitle.setFont(FONT_SUBTITLE);
        lblLdbTitle.setForeground(ACCENT_BLUE);
        lblLdbTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        leaderboardCard.add(lblLdbTitle, BorderLayout.NORTH);

        leaderboardModel = new DefaultListModel<>();
        listLeaderboard = new JList<>(leaderboardModel);
        listLeaderboard.setBackground(CARD_BG);
        listLeaderboard.setForeground(TEXT_WHITE);
        listLeaderboard.setFont(FONT_BODY);
        listLeaderboard.setSelectionBackground(ACCENT_BLUE);
        listLeaderboard.setSelectionForeground(BG_DARK);
        listLeaderboard.setCellRenderer(new LeaderboardCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(listLeaderboard);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        leaderboardCard.add(scrollPane, BorderLayout.CENTER);

        gbc.gridy = 1;
        gbc.weighty = 0.5;
        panel.add(leaderboardCard, gbc);

        // Botão Informativo de transição (Não clicável)
        btnNextRound = new RoundedButton("Aguardando próxima rodada...", ACCENT_BLUE);
        btnNextRound.setForeground(BG_DARK);
        btnNextRound.setFont(FONT_SUBTITLE);
        btnNextRound.setEnabled(false);
        
        gbc.gridy = 2;
        gbc.weighty = 0.1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(btnNextRound, gbc);

        return panel;
    }

    // --- TRATAMENTO DOS INPUTS E AÇÕES ---
    private void handleConnect() {
        String nickname = txtName.getText().trim();
        String ip = txtIP.getText().trim();
        String portStr = txtPort.getText().trim();

        if (nickname.isEmpty() || ip.isEmpty() || portStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Porta de rede inválida!", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnConnect.setEnabled(false);
        btnConnect.setText("Conectando...");

        if (socketManager == null) {
            socketManager = new ClientSocketManager(this);
        }
        socketManager.connect(ip, port, nickname);
    }

    private void handleSelectAnswer(int optionIndex) {
        // Desativa cliques adicionais
        for (JButton btn : btnOptions) {
            btn.setEnabled(false);
        }

        String answerChar = "";
        switch (optionIndex) {
            case 0: answerChar = "A"; break;
            case 1: answerChar = "B"; break;
            case 2: answerChar = "C"; break;
            case 3: answerChar = "D"; break;
        }

        selectedAnswer = answerChar;
        
        // Aplica o feedback visual de clique (borda espessa no botão selecionado)
        if (btnOptions[optionIndex] instanceof RoundedButton) {
            ((RoundedButton) btnOptions[optionIndex]).setSelected(true);
        }

        lblGameStatus.setText("Resposta '" + selectedAnswer + "' registrada! Enviando via TCP...");
        socketManager.sendAnswer(selectedAnswer);
    }

    // --- CALLBACKS DO CLIENTESOCKETMANAGER (EVENTOS DE REDE) ---
    @Override
    public void onConnected() {
        SwingUtilities.invokeLater(() -> {
            lblPlayerInfo.setText("Jogador: " + txtName.getText().trim() + " | Pontos: 0");
            lblGameStatus.setText("Conectado com sucesso! Aguardando o início do jogo...");
            cardLayout.show(mainPanel, "GAME");
        });
    }

    @Override
    public void onConnectionFailed(String error) {
        SwingUtilities.invokeLater(() -> {
            btnConnect.setEnabled(true);
            btnConnect.setText("Conectar e Jogar");
            JOptionPane.showMessageDialog(this, "Não foi possível conectar: " + error, "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        });
    }

    @Override
    public void onQuestionReceived(String text, String[] options, int questionNum) {
        SwingUtilities.invokeLater(() -> {
            lblQuestion.setText("<html><center>" + text + "</center></html>");
            btnOptions[0].setText(options[0]);
            btnOptions[1].setText(options[1]);
            btnOptions[2].setText(options[2]);
            btnOptions[3].setText(options[3]);

            selectedAnswer = "";
            lblGameStatus.setText("Selecione a alternativa correta contra o relógio!");

            for (JButton btn : btnOptions) {
                btn.setEnabled(true);
                if (btn instanceof RoundedButton) {
                    ((RoundedButton) btn).setSelected(false);
                }
            }
            cardLayout.show(mainPanel, "GAME");
        });
    }

    @Override
    public void onRoundResult(String feedback, int currentScore) {
        SwingUtilities.invokeLater(() -> {
            lblPlayerInfo.setText("Jogador: " + txtName.getText().trim() + " | Pontos: " + currentScore);
            lblResultFeedback.setText(feedback);
            if (feedback.contains("acertou")) {
                lblResultFeedback.setForeground(SUCCESS_GREEN);
            } else {
                lblResultFeedback.setForeground(DANGER_RED);
            }
            cardLayout.show(mainPanel, "RESULT");
        });
    }

    @Override
    public void onLeaderboardUpdated(String leaderboard) {
        SwingUtilities.invokeLater(() -> {
            leaderboardModel.clear();
            String[] lines = leaderboard.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    leaderboardModel.addElement(line);
                }
            }
        });
    }

    @Override
    public void onTimerTick(int secondsLeft) {
        SwingUtilities.invokeLater(() -> {
            lblTimer.setText(String.valueOf(secondsLeft));
            if (secondsLeft <= 5) {
                lblTimer.setForeground(DANGER_RED);
            } else {
                lblTimer.setForeground(ACCENT_BLUE);
            }
        });
    }

    @Override
    public void onGameOver(String finalRanking) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "FIM DO QUIZ!\n\n" + finalRanking, "Resultado Final", JOptionPane.INFORMATION_MESSAGE);
            btnConnect.setEnabled(true);
            btnConnect.setText("Conectar e Jogar");
            cardLayout.show(mainPanel, "LOGIN");
        });
    }

    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "A conexão com o servidor foi perdida.", "Desconectado", JOptionPane.WARNING_MESSAGE);
            btnConnect.setEnabled(true);
            btnConnect.setText("Conectar e Jogar");
            cardLayout.show(mainPanel, "LOGIN");
        });
    }

    // --- COMPONENTES ARREDONDADOS E ESTILIZADOS PERSONALIZADOS (UI PREMIUM) ---
    
    // 1. Painel com Cantos Arredondados
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
            
            // Desenha contorno fino sutil
            g2.setColor(new Color(255, 255, 255, 15));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // 2. Campo de Texto Arredondado
    private static class RoundedTextField extends JTextField {
        private final int cornerRadius = 15;
        private final Color bgColor;

        public RoundedTextField(String text, Color bg) {
            super(text);
            this.bgColor = bg;
            setOpaque(false);
            setCaretColor(Color.WHITE);
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // 3. Botão com Cantos Arredondados e Efeitos Visuais
    private static class RoundedButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;
        private final int cornerRadius = 20;
        private boolean selected = false;

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
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (!isEnabled()) {
                if (selected) {
                    g2.setColor(normalBg);
                } else {
                    // Opacidade reduzida para botões não selecionados
                    g2.setColor(new Color(normalBg.getRed(), normalBg.getGreen(), normalBg.getBlue(), 80));
                }
            } else if (getModel().isPressed()) {
                g2.setColor(pressedBg);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverBg);
            } else {
                g2.setColor(normalBg);
            }
            
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            
            // Contorno de destaque branco no botão selecionado
            if (selected) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(4));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, cornerRadius, cornerRadius);
            }
            
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // --- RENDERIZADOR PERSONALIZADO PARA LISTA DE LIDERANÇA ---
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
                    setBackground(new Color(69, 71, 90)); // Destaque escuro
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
