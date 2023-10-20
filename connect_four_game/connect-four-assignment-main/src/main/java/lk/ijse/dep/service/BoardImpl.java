package lk.ijse.dep.service;

import java.util.*;
public class BoardImpl implements Board {
    private final Piece [][] pieces;
    private final BoardUI boardUI;
    public Piece piece = Piece.BLUE;
    public int cols;

    public BoardImpl(BoardUI boardUI) {
        this.boardUI = boardUI;
        this.pieces = new Piece[6][5];

        for (int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces[i].length; j++) {
                pieces[i][j] = Piece.EMPTY;
            }
        }
    }

    public BoardImpl(Piece[][] pieces, BoardUI boardUI) {
        this.pieces = new Piece[6][5];
        for (int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces[i].length; j++) {
                this.pieces[i][j] = pieces[i][j];
            }
        }
        this.boardUI = boardUI;
    }

    public Piece[][] getPieces() {
        return pieces;
    }
    public BoardUI getBoardUI() {
        return this.boardUI;
    }

    @Override
    public int findNextAvailableSpot(int col) {
        for (int i = 0; i < pieces[col].length; i++) {
            if (pieces[col][i] == Piece.EMPTY){
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isLegalMove(int col) {
        int index = findNextAvailableSpot(col);
        return index != -1;
    }

    @Override
    public boolean existLegalMoves() {
        for (int i = 0; i < pieces.length; i++) {
            if (isLegalMove(i)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateMove(int col, Piece move) {
        this.cols = col;
        this.piece = move;
        int index = findNextAvailableSpot(col);
        pieces[col][index] = move;
    }

    @Override
    public void updateMove(int col, int row, Piece move) {
        pieces[col][row]=move;
    }

    @Override
    public Winner findWinner() {
        int count = 0;
        for (int i = 0; i < pieces.length; i++){ //Check horizontally
            for (int j = 0; j < pieces[i].length-1; j++){
                if (pieces[i][j] == pieces[i][j+1]){
                    count++;
                    if (count == 3 && pieces[i][j] != Piece.EMPTY){
                        return new Winner(pieces[i][j], i, (j-2), i, (j+1));
                    }
                }
                else{
                    count = 0;
                }
            }
            count = 0;
        }

        count = 0;
        for (int i = 0; i < pieces[0].length; i++){ //Check vertically
            for (int j = 0; j < pieces.length-1; j++){
                if (pieces[j][i] == pieces[j+1][i]){
                    count++;
                    if (count == 3 && pieces[j][i] != Piece.EMPTY){
                        return  new Winner(pieces[j][i], (j-2), i, (j+1), i);
                    }
                }
                else{
                    count = 0;
                }
            }
            count = 0;
        }
        return new Winner(Piece.EMPTY);
    }

    @Override
    public BoardImpl getBoardImpl() {
        return this;
    }

    public List<BoardImpl> getAllLegalNextMoves() {
        Piece nextPiece = piece == Piece.BLUE ? Piece.GREEN : Piece.BLUE;
        List<BoardImpl> nextMoves = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            int raw = findNextAvailableSpot(i);
            if (raw != -1){
                BoardImpl legalMove = new BoardImpl(this.pieces, this.boardUI);
                legalMove.updateMove(i,nextPiece);
                nextMoves.add(legalMove);
            }
        }
        return  nextMoves;
    }

    public BoardImpl getRandomLeagalNextMove() {
        final List<BoardImpl> legalMoves = getAllLegalNextMoves();
        if (legalMoves.isEmpty()) {
            return null;
        }
        final int random = RANDOM_GENERATOR.nextInt(legalMoves.size());
        return legalMoves.get(random);
    }

    public boolean getStatus(){
        if (!existLegalMoves()){
            return false;
        }

        Winner winner = findWinner();

        if (winner.getWinningPiece() != Piece.EMPTY){
            return false;
        }
        return true;
    }
}
