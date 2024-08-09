package chess;

import boardgame.Position;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import chess.pieces.*;

public class ChessMatch {
    
    private Board board;
    private int turn;
    private Color currentPlayer;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();

    public ChessMatch(){
        board = new Board(8, 8);
        currentPlayer = Color.WHITE;
        check = false;
        turn = 1;
        initialSetup();
    }
    
    public int getTurn(){
        return turn;
    }

    public boolean getCheck(){
        return check;
    }

    public boolean getCheckMate(){
        return checkMate;
    }
    
    public Color getCurrentPlayer(){
        return currentPlayer;
    }

    public ChessPiece getEnPassantVulnerable(){
        return enPassantVulnerable;
    }

    public ChessPiece getPromoted(){
        return promoted;
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


    public boolean[][] possibleMoves(ChessPosition sourcePosition){
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }
    
    public ChessPiece performChessMove(ChessPosition fromPosition, ChessPosition toPosition){
        Position from = fromPosition.toPosition();
        Position to = toPosition.toPosition();
        validateSourcePosition(from);
        validateTargetPosition(from, to);
        Piece capturedPiece = makeMove(from, to);

        if(testCheck(currentPlayer)){
            undoMove(from, to, capturedPiece);
            throw new ChessException("You can't place yourself in check dumbass!");
        }

        ChessPiece movedPiece = (ChessPiece)board.piece(to);

        // testando promocao!!!
        promoted = null;
        if(movedPiece instanceof Pawn){
            if(movedPiece.getColor() == Color.WHITE && to.getRow() == 0 || movedPiece.getColor() == Color.BLACK && to.getRow() == 7){
                promoted = (ChessPiece)board.piece(to);
                promoted = replacePromotedPiece("Q");
            }
        }

        check = (testCheck(opponent(currentPlayer))) ? true : false;

        if(testCheckMate(opponent(currentPlayer))){
            checkMate = true;
        }
        else{
            nextTurn();
        }

        // marcando peao vulneravel ao en passant
        if(movedPiece instanceof Pawn && (to.getRow() == from.getRow() + 2) || to.getRow() == from.getRow() - 2){
            enPassantVulnerable = movedPiece;
        }
        else{
            enPassantVulnerable = null;
        }

        return (ChessPiece)capturedPiece;
    }

    public ChessPiece replacePromotedPiece(String type){
        if(promoted == null){
            throw new IllegalStateException("There is no piece to be promoted!");     
        }
        if(!type.equals("B") && !type.equals("G") && type.equals("R") && type.equals("Q")){
            throw new InvalidParameterException("Invalid type for promotion!");
        }

        Position pos = promoted.getChessPosition().toPosition();
        Piece p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);

        ChessPiece newPiece = newPiece(type, promoted.getColor());
        board.placePiece(newPiece, pos);
        piecesOnTheBoard.add(newPiece);

        return newPiece;
        
    }

    private ChessPiece newPiece(String type, Color color){
        if(type.equals("B")) return new Bishop(board, color);
        if(type.equals("G")) return new Knight(board, color);
        if(type.equals("R")) return new Rook(board, color);
        return new Queen(board, color);
    }
    
    private Piece makeMove(Position source, Position target){
        ChessPiece p = (ChessPiece)board.removePiece(source);   
        p.increaseMoveCount();
        Piece captured = board.removePiece(target);
        board.placePiece(p, target);
        
        if(captured != null){
            piecesOnTheBoard.remove(captured);
            capturedPieces.add(captured);
        }

        // roque do rei (pequeno)
        if(p instanceof King && target.getColumn() == source.getColumn() + 2){
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece)board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }
        // roque da rainha (grande)
        if(p instanceof King && target.getColumn() == source.getColumn() - 2){
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece)board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        // en passant
        if(p instanceof Pawn){
            if(source.getColumn() != target.getColumn() && captured == null){
                Position pawnPosition;
                if(p.getColor() == Color.WHITE){
                    pawnPosition = new Position(target.getRow() + 1, target.getColumn());
                }
                else{
                    pawnPosition = new Position(target.getRow() - 1, target.getColumn());
                }
                captured = board.removePiece(pawnPosition);
                capturedPieces.add(captured);
                piecesOnTheBoard.remove(captured);
            }
        }

        return captured;
    }
    
    private void undoMove(Position source, Position target, Piece captured){
        ChessPiece p = (ChessPiece)board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);

        if(captured != null){
            board.placePiece(captured, target);
            capturedPieces.remove(captured);
            piecesOnTheBoard.add(captured);
        }

        // roque do rei (pequeno)
        if(p instanceof King && target.getColumn() == source.getColumn() + 2){
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece)board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }
        // roque da rainha (grande)
        if(p instanceof King && target.getColumn() == source.getColumn() - 2){
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece)board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }
        
        // en passant
        if(p instanceof Pawn){
            if(source.getColumn() != target.getColumn() && captured == enPassantVulnerable){
                ChessPiece pawn = (ChessPiece)board.removePiece(target);
                Position pawnPosition;
                if(p.getColor() == Color.WHITE){
                    pawnPosition = new Position(3, target.getColumn());
                }
                else{
                    pawnPosition = new Position(4, target.getColumn());
                }
                board.placePiece(pawn, pawnPosition);
            }
        }
    }

    private void validateSourcePosition(Position position){
        if(!board.thereIsAPiece(position)){
            throw new ChessException("Error moving piece! Source position empty!");
        }
        if(currentPlayer != ((ChessPiece)board.piece(position)).getColor()){
            throw new ChessException("Chosen piece is not yours!");
        }
        if(!board.piece(position).isThereAnyPossibleMove()){
            throw new ChessException("There is no possible move for chosen piece!");
        }
    }
    
    private void validateTargetPosition(Position from, Position to){
        if(!board.piece(from).possibleMove(to)){
            throw new ChessException("The chosen piece can't move to target position!");
        }
    }

    private void nextTurn(){
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE ? Color.BLACK : Color.WHITE);
    }

    private Color opponent(Color color){
        return (color == Color.WHITE ? Color.BLACK : Color.WHITE);
    }

    private ChessPiece king(Color color){
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
        for (Piece p : list){
            if (p instanceof King){
                return (ChessPiece)p;
            }
        }
        throw new IllegalStateException("There is no " + color + " king on the board!");
    }

    private boolean testCheck(Color color){
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList()); 
        for(Piece p : opponentPieces){
            boolean[][] mat = p.possibleMoves();
            if(mat[kingPosition.getRow()][kingPosition.getColumn()] == true){
                return true;
            }
        }
        return false;
    }

    private boolean testCheckMate(Color color){
        if(!testCheck(color)){
            return false;
        }
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList()); 
        for(Piece p : list){
            boolean[][] mat = p.possibleMoves();
            for(int i = 0; i < board.getRows(); i++){
                for(int j = 0; j < board.getColumns(); j++){
                    if(mat[i][j]){
                        Position source = ((ChessPiece)p).getChessPosition().toPosition();
                        Position target = new Position(i, j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean testCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);
                        if(!testCheck){
                            return false;
                        }
                    }
                }
            }

        }
        return true;
    }
    
    private void placeNewPiece(char column, int row, ChessPiece piece){
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }
    
    public void initialSetup(){
        // TORRE
        placeNewPiece('a', 1, new Rook(board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK)); 
        
        // CAVALO
        placeNewPiece('b', 1, new Knight(board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        
        // BISPO
        placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
     
        // REI
        placeNewPiece('e', 1, new King(board, Color.WHITE, this));
        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        
        // RAINHA
        placeNewPiece('d', 1, new Queen(board, Color.WHITE));
        placeNewPiece('d', 8, new Queen(board, Color.BLACK));

        // PEAO
        for(int i = 0; i < board.getColumns(); i++){
            placeNewPiece((char)('a' + i), 2, new Pawn(board, Color.WHITE, this));
            placeNewPiece((char)('a' + i), 7, new Pawn(board, Color.BLACK, this));
        }
    }

}
