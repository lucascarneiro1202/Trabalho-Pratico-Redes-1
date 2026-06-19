# Trabalho Prático - Redes de Computadores I: Quiz em Tempo Real

Este repositório contém o código-fonte do trabalho prático de Redes de Computadores I. A aplicação é um jogo de perguntas e respostas em tempo real (estilo *Kahoot*) baseado na arquitetura Cliente-Servidor.

---

## 📂 Estrutura do Projeto

O código está dividido de forma independente para facilitar a compilação isolada e a implantação em computadores diferentes:

```
.
├── client/                     # Código-fonte do Cliente (Frontend)
│   └── src/
│       └── br/pucminas/redes/quiz/
│           ├── client/         # Lógica da Interface e Conexão do Cliente
│           │   ├── ClientMain.java
│           │   ├── ClientSocketManager.java
│           │   └── QuizWindow.java
│           └── common/
│               └── GameMessage.java
├── server/                     # Código-fonte do Servidor (Backend)
│   ├── questions.txt           # Banco de perguntas local (Opção A)
│   └── src/
│       └── br/pucminas/redes/quiz/
│           ├── common/
│           │   └── GameMessage.java
│           └── server/         # Orquestrador do Jogo e Threads do Servidor
│               ├── ClientHandler.java
│               ├── QuizTimer.java
│               └── ServerMain.java
├── Dockerfile                  # Empacotamento do ambiente Java 17 + X11
├── Makefile                    # Automação de tarefas para Unix (Linux/macOS)
├── GEMINI.md                   # Documentação interna de contexto
└── README.md                   # Instruções de uso e documentação do projeto
```

---

## 🎨 Interface Gráfica (Java Swing)

A interface do Cliente em `QuizWindow.java` foi desenvolvida utilizando a biblioteca gráfica nativa **Java Swing**, adotando práticas de design modernas:
*   **CardLayout:** Usado para gerenciar e alternar telas sem abrir novas janelas. As transições cobrem: **Login/Conexão**, **Jogo (Pergunta + Opções + Timer)** e **Resultado da Rodada**.
*   **Estilização Premium:** Visual no estilo *Dark Mode* utilizando paleta de cores personalizada com efeitos visuais dinâmicos de *hover* nos botões.
*   **Leaderboard Customizado:** A exibição de pontuação utiliza a classe `LeaderboardCellRenderer`, que renderiza linhas personalizadas com cores para o pódio e fundos com cores diferenciadas.

---

## 📡 Protocolo e Conexão de Rede (TCP & UDP)

O projeto realiza a comunicação simultânea utilizando ambos os protocolos da camada de transporte para fins didáticos:

### 1. Canal de Controle e Jogabilidade (TCP)
*   **Objetivo:** Garantir a entrega confiável de mensagens críticas do jogo.
*   **Como funciona:** O servidor abre um `ServerSocket` TCP na porta `12345`. O cliente conecta-se a ele e cria uma instância de `ObjectOutputStream` / `ObjectInputStream` para serialização de objetos do tipo `GameMessage`.
*   **Ações trafegadas:** Login do jogador, envio da pergunta ativa aos clientes, envio da resposta selecionada pelo cliente e envio dos resultados/pontuações acumuladas.

### 2. Transmissão do Cronômetro (UDP)
*   **Objetivo:** Baixa latência e independência no envio periódico do tempo restante de cada rodada.
*   **Como funciona:** Durante a conexão TCP (Login), o cliente abre um `DatagramSocket` local e envia sua porta UDP aleatória para o servidor. A cada rodada, o servidor instancia uma thread `QuizTimer.java` que envia pacotes UDP unicast contendo `"TIMER;<segundos>"` diretamente ao IP e porta de cada jogador ativo a cada segundo.

### 3. Encerramento Antecipado de Rodada
*   Quando o servidor detecta que todos os jogadores ativos já submeteram suas respostas via TCP, ele interrompe imediatamente o cronômetro UDP, finalizando a rodada sem precisar esperar o tempo limite de 15 segundos expirar.

---

## 🚀 Guia de Execução (Do Zero)

### 📋 Pré-requisitos
Certifique-se de ter o **Java JDK 17 (ou mais recente)** instalado na sua máquina host.

---

### Opção 1: Execução Local (Recomendado para Windows, macOS e Linux)

Esta é a forma mais simples e direta de testar a interação em qualquer sistema operacional, pois evita a necessidade de configurar redirecionamento gráfico no Docker para o Swing.

#### **Como rodar no Linux / macOS**
Abra dois terminais na raiz do projeto e execute os comandos:

1.  **Terminal 1 (Servidor):**
    ```bash
    make compile-server
    make run-server-local
    ```
2.  **Terminal 2 (Cliente):**
    ```bash
    make compile-client
    make run-client-local
    ```
    *(Você pode abrir este comando em quantos terminais quiser para simular múltiplos jogadores)*

#### **Como rodar no Windows**
*   **Via Make (se utilizar Git Bash, WSL ou Make for Windows):**
    Abra os terminais e siga as mesmas instruções acima para Linux/macOS.
*   **Via CMD ou PowerShell convencional:**
    Abra os terminais na raiz do projeto e execute os comandos manuais:
    1.  **Terminal 1 (Servidor):**
        ```cmd
        javac -sourcepath server\src -d server\src server\src\br\pucminas\redes\quiz\server\*.java server\src\br\pucminas\redes\quiz\common\*.java
        java -cp server\src br.pucminas.redes.quiz.server.ServerMain
        ```
    2.  **Terminal 2 (Cliente):**
        ```cmd
        javac -sourcepath client\src -d client\src client\src\br\pucminas\redes\quiz\client\*.java client\src\br\pucminas\redes\quiz\common\*.java
        java -cp client\src br.pucminas.redes.quiz.client.ClientMain
        ```

---

### Opção 2: Execução via Docker (Recomendado para Linux)

O Docker garante a padronização das versões da JDK no ambiente de desenvolvimento.

1.  **Construir a Imagem:**
    ```bash
    make build-image
    ```
2.  **Iniciar o Servidor:**
    ```bash
    make run-server-docker
    ```
3.  **Iniciar o Cliente (Linux com servidor gráfico X11 habilitado):**
    ```bash
    make run-client-docker
    ```

> Para testar o cliente via Docker no Windows ou macOS, é necessário configurar um servidor X11 externo (como *VcXsrv* no Windows ou *XQuartz* no macOS) para receber a tela. Por conta disso, **a Opção 1 (Execução Local)** é a recomendada para membros do grupo com estes sistemas operacionais.