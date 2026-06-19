package br.pucminas.redes.quiz.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class QuizWindow extends JFrame implements ClientSocketManager.GameEventListener {

    // Cores modernas (tema escuro/pastel)
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
    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 26);
    private static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 18);
    private static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FONT_TIMER = new Font("SansSerif", Font.BOLD, 36);

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
    private JButton btnNextMock;

    // Controlador de Rede
    private ClientSocketManager socketManager;
    private String selectedAnswer = "";

    public QuizWindow() {
        setTitle("Quiz em Tempo Real - Redes I");
        setSize(800, 600);
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

        // Container Centralizado (Card)
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                new EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints cGbc = new GridBagConstraints();
        cGbc.insets = new Insets(8, 8, 8, 8);
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

        txtName = new JTextField("Jogador 1");
        styleTextField(txtName);
        cGbc.gridy = 4;
        card.add(txtName, cGbc);

        // Input IP
        JLabel lblIP = new JLabel("IP do Servidor");
        lblIP.setFont(FONT_LABEL);
        lblIP.setForeground(ACCENT_BLUE);
        cGbc.gridy = 5;
        cGbc.gridwidth = 1;
        card.add(lblIP, cGbc);

        txtIP = new JTextField("127.0.0.1");
        styleTextField(txtIP);
        cGbc.gridy = 6;
        card.add(txtIP, cGbc);

        // Input Porta
        JLabel lblPort = new JLabel("Porta TCP");
        lblPort.setFont(FONT_LABEL);
        lblPort.setForeground(ACCENT_BLUE);
        cGbc.gridx = 1;
        cGbc.gridy = 5;
        card.add(lblPort, cGbc);

        txtPort = new JTextField("12345");
        styleTextField(txtPort);
        cGbc.gridx = 1;
        cGbc.gridy = 6;
        card.add(txtPort, cGbc);

        // Botão Conectar
        btnConnect = new JButton("Conectar e Jogar");
        styleButton(btnConnect, ACCENT_BLUE, BG_DARK);
        cGbc.gridx = 0;
        cGbc.gridy = 7;
        cGbc.gridwidth = 2;
        cGbc.insets = new Insets(20, 8, 8, 8);
        card.add(btnConnect, cGbc);

        // Ação de conexão
        btnConnect.addActionListener(e -> handleConnect());

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(card, gbc);

        return panel;
    }

    // --- PAINEL PRINCIPAL DO JOGO ---
    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Cabeçalho: Info do Jogador e Timer
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_DARK);

        lblPlayerInfo = new JLabel("Conectando...", JLabel.LEFT);
        lblPlayerInfo.setFont(FONT_SUBTITLE);
        lblPlayerInfo.setForeground(TEXT_WHITE);
        headerPanel.add(lblPlayerInfo, BorderLayout.WEST);

        lblTimer = new JLabel("--", JLabel.RIGHT);
        lblTimer.setFont(FONT_TIMER);
        lblTimer.setForeground(ACCENT_BLUE);
        headerPanel.add(lblTimer, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Centro: Pergunta
        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.setBackground(CARD_BG);
        questionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_BG, 1),
                new EmptyBorder(30, 20, 30, 20)
        ));

        lblQuestion = new JLabel("Aguardando início do jogo pelo administrador...", JLabel.CENTER);
        lblQuestion.setFont(FONT_TITLE);
        lblQuestion.setForeground(TEXT_WHITE);
        questionPanel.add(lblQuestion, BorderLayout.CENTER);

        panel.add(questionPanel, BorderLayout.CENTER);

        // Inferior: Opções e Status da Submissão
        JPanel footerPanel = new JPanel(new BorderLayout(10, 10));
        footerPanel.setBackground(BG_DARK);

        // Grid de Opções (2x2)
        JPanel optionsGrid = new JPanel(new GridLayout(2, 2, 15, 15));
        optionsGrid.setBackground(BG_DARK);

        btnOptions = new JButton[4];
        for (int i = 0; i < 4; i++) {
            btnOptions[i] = new JButton();
            styleButton(btnOptions[i], BUTTON_BG, TEXT_WHITE);
            btnOptions[i].setFont(FONT_SUBTITLE);
            btnOptions[i].setEnabled(false);
            final int index = i;
            btnOptions[i].addActionListener(e -> handleSelectAnswer(index));
            optionsGrid.add(btnOptions[i]);
        }
        footerPanel.add(optionsGrid, BorderLayout.CENTER);

        // Barra de Status
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

        // Bloco Principal de Feedback
        JPanel feedbackCard = new JPanel(new GridBagLayout());
        feedbackCard.setBackground(CARD_BG);
        feedbackCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_BG, 1),
                new EmptyBorder(20, 30, 20, 30)
        ));

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

        // Bloco de Placar Liderança (Leaderboard)
        JPanel leaderboardCard = new JPanel(new BorderLayout(5, 5));
        leaderboardCard.setBackground(CARD_BG);
        leaderboardCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_BG, 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblLdbTitle = new JLabel("Classificação Geral da Partida", JLabel.CENTER);
        lblLdbTitle.setFont(FONT_SUBTITLE);
        lblLdbTitle.setForeground(ACCENT_BLUE);
        lblLdbTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        leaderboardCard.add(lblLdbTitle, BorderLayout.NORTH);

        leaderboardModel = new DefaultListModel<>();
        listLeaderboard = new JList<>(leaderboardModel);
        listLeaderboard.setBackground(BUTTON_BG);
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

        // Botão Informativo (Sem ação de avanço manual)
        btnNextMock = new JButton("Aguardando próxima rodada...");
        styleButton(btnNextMock, ACCENT_BLUE, TEXT_WHITE);
        btnNextMock.setEnabled(false);
        
        gbc.gridy = 2;
        gbc.weighty = 0.1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(btnNextMock, gbc);

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
        btnOptions[optionIndex].setBackground(ACCENT_BLUE);
        btnOptions[optionIndex].setForeground(BG_DARK);

        lblGameStatus.setText("Resposta '" + selectedAnswer + "' enviada via TCP!");
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
                btn.setBackground(BUTTON_BG);
                btn.setForeground(TEXT_WHITE);
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

    // --- MÉTODOS DE ESTILIZAÇÃO VISUAL (DESIGN PREMIUM) ---
    private void styleTextField(JTextField field) {
        field.setBackground(BUTTON_BG);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(TEXT_WHITE);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_BG, 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(FONT_BODY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg, 1),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    if (bg.equals(BUTTON_BG)) {
                        button.setBackground(BUTTON_HOVER);
                    } else {
                        button.setBackground(bg.brighter());
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(bg);
                }
            }
        });
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


