package application;

import chess.ChessMatch;
import chess.ChessPosition;
import chess.ChessPiece;

import java.util.Scanner;

public class Program{
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        ChessMatch chessMatch = new ChessMatch();
        chessMatch.initialSetup();

        while(true){
            UI.printBoard(chessMatch.getPieces());
            System.out.println();
            System.out.print("Source: ");
            ChessPosition source = UI.readChessPosition(sc);
            System.out.println();
            System.out.print("Target: ");
            ChessPosition target = UI.readChessPosition(sc);

            ChessPiece captured = chessMatch.performChessMove(source, target);
        }
        
    }
}