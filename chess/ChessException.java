package chess;

import boardgame.BoardException;

// essa excessao personalisada serve para representar erros da logica de movimentacao do xadrez!

public class ChessException extends BoardException{
    private static final long serialVersionUID = 1L;

    public ChessException(String msg){
        super(msg);
    }
    
}
