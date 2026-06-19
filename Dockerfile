# Dockerfile para padronizar o ambiente Java (JDK 17 LTS)
# Instala também as bibliotecas do sistema necessárias para renderizar a interface gráfica Swing (X11/AWT)

FROM eclipse-temurin:17-jdk-jammy

# Instalação de dependências do X11 e fontes para suporte a interface gráfica Swing do Java
RUN apt-get update && apt-get install -y \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    fontconfig \
    x11-apps \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia todo o código-fonte para dentro do contêiner
COPY . .

# Compila o cliente e o servidor de forma isolada para evitar duplicidade de classes comuns
RUN javac -sourcepath client/src -d client/src client/src/br/pucminas/redes/quiz/client/*.java client/src/br/pucminas/redes/quiz/common/*.java
RUN javac -sourcepath server/src -d server/src server/src/br/pucminas/redes/quiz/server/*.java server/src/br/pucminas/redes/quiz/common/*.java


# Comando padrão para executar o cliente Swing
CMD ["java", "-cp", "client/src", "br.pucminas.redes.quiz.client.ClientMain"]
