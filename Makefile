# Makefile para agilizar a compilação e execução do projeto de Quiz (Redes I)

.PHONY: compile-client compile-server run-client-local run-client-docker run-server-local run-server-docker build-image clean help

help:
	@echo "Comandos úteis:"
	@echo "  make build-image       - Constrói/Reconstrói a imagem Docker da aplicação"
	@echo "  make run-client-local  - Executa o Cliente Swing localmente (Host)"
	@echo "  make run-client-docker - Executa o Cliente Swing dentro do Docker (GUI)"
	@echo "  make run-server-local  - Executa o Servidor do Quiz localmente (Host)"
	@echo "  make run-server-docker - Executa o Servidor do Quiz dentro do Docker (Console)"
	@echo "  make compile-client    - Compila o código do Cliente localmente (Host)"
	@echo "  make compile-server    - Compila o código do Servidor localmente (Host)"
	@echo "  make clean             - Remove os arquivos .class locais"

build-image:
	sudo docker build -t quiz-app .

compile-client:
	javac -sourcepath client/src -d client/src client/src/br/pucminas/redes/quiz/client/*.java client/src/br/pucminas/redes/quiz/common/*.java

compile-server:
	javac -sourcepath server/src -d server/src server/src/br/pucminas/redes/quiz/server/*.java server/src/br/pucminas/redes/quiz/common/*.java

# Execução Local (Host)
run-client-local:
	java -cp client/src br.pucminas.redes.quiz.client.ClientMain

run-server-local:
	java -cp server/src br.pucminas.redes.quiz.server.ServerMain

# Execução Conteinerizada (Docker)
run-client-docker:
	xhost +local:docker
	sudo docker run -it --rm --net=host -e DISPLAY=$${DISPLAY} -v /tmp/.X11-unix:/tmp/.X11-unix quiz-app

run-server-docker:
	xhost +local:docker
	sudo docker run -it --rm --net=host -e DISPLAY=$${DISPLAY} -v /tmp/.X11-unix:/tmp/.X11-unix quiz-app java -cp server/src br.pucminas.redes.quiz.server.ServerMain

clean:
	find . -name "*.class" -type f -delete

