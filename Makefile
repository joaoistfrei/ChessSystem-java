JFLAGS = -d bin
JAVAC = javac
JAVA = java
SRC = $(wildcard application/*.java boardgame/*.java chess/*.java chess/pieces/*.java)
BIN = bin

default: compile run

compile:
	@mkdir -p $(BIN)
	$(JAVAC) $(JFLAGS) $(SRC)


run:
	$(JAVA) -cp $(BIN) application.Program


clean:
	@rm -rf $(BIN)

rebuild: clean default
