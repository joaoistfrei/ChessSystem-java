package chess;

import boardgame.Board;
import chess.pieces.*;

public class ChessMatch {
    
    private Board board;

    public ChessMatch(){
        board = new Board(8, 8);
    }

    public ChessPiece[][] getPieces(){
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
        for(int i = 0; i < board.getRows(); i++){
            for(int j = 0; j < board.getColumns(); j++){

                mat[i][j] = (ChessPiece) board.piece(i, j);

            }
        }
        return mat;
    }

    private void placeNewPiece(char column, int row, ChessPiece piece){
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
    }

    public void initialSetup(){
        placeNewPiece('a', 1, new Rook(board, Color.WHITE));
        placeNewPiece('b', 1, new Knight(board, Color.WHITE));
        placeNewPiece('c', 1, new Bishop(board, Color.WHITE));


        for(int i = 0; i < board.getColumns(); i++){
            placeNewPiece((char)('a' + i), 2, new Pawn(board, Color.WHITE));
            placeNewPiece((char)('a' + i), 7, new Pawn(board, Color.BLACK));
        }







    }

}
