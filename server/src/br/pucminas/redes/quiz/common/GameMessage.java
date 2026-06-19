package br.pucminas.redes.quiz.common;

import java.io.Serializable;

public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        LOGIN,              // Cliente -> Servidor (Nome do jogador e sua porta de escuta UDP)
        LOGIN_RESPONSE,     // Servidor -> Cliente (Confirmação de entrada)
        QUESTION,           // Servidor -> Cliente (Nova pergunta e opções)
        ROUND_RESULT,       // Servidor -> Cliente (Feedback se acertou, pontuação e placar)
        ANSWER,             // Cliente -> Servidor (Opção selecionada e tempo decorrido)
        GAME_OVER           // Servidor -> Cliente (Fim do jogo e ranking final)
     }

    private Type type;
    private String payload;      // Texto geral (ex: apelido, alternativa selecionada)
    private String[] options;    // Lista de alternativas (para tipo QUESTION)
    private int value;           // Valor numérico (ex: score, porta UDP, ou segundos)
    private String extraData;    // Dados adicionais (ex: ranking formatado)

    public GameMessage(Type type) {
        this.type = type;
    }

    public GameMessage(Type type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}

