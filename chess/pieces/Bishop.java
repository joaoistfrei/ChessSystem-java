package chess.pieces;

import chess.ChessPiece;
import boardgame.Board;
import chess.Color;

public class Bishop extends ChessPiece{
    
    public Bishop(Board board, Color color){
        super(board, color);
    }

    @Override
    public String toString(){
        return "B";
    }

}
