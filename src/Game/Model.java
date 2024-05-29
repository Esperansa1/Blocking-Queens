package Game;

import Game.AI.MagicBitboard;

import java.io.Serializable;
import java.util.*;

public class Model implements Serializable {

    private final List<Observer> observers;
    public static final int BOARD_SIZE = 8;


    private final int[] blackQueenPositions;
    private final int[] whiteQueenPositions;

    // Bitboards to represent the queens
    private long whiteQueens;
    private long blackQueens;
    private long walls;
    private int currentPlayer = Constants.WHITE;

    private final MagicBitboard magicBitboards;

    public Model() {
        this.observers = new ArrayList<>();
        magicBitboards = new MagicBitboard();

        blackQueenPositions = new int[3];
        whiteQueenPositions = new int[3];
        initializeQueens();
    }

    // Initialize the queens on the board
    private void initializeQueens() {
        // Example positions for queens
        // Let's say the white queens are at A1, D4, and H8
        whiteQueens = (1L << 0) | (1L << 27) | (1L << 60);
        whiteQueenPositions[0] = 0;
        whiteQueenPositions[1] = 27;
        whiteQueenPositions[2] = 60;


        // And the black queens are at B2, E5, and G7
        blackQueens = (1L << 9) | (1L << 36) | (1L << 54);
        blackQueenPositions[0] = 9;
        blackQueenPositions[1] = 36;
        blackQueenPositions[2] = 54;


    }

    public boolean isWhiteQueen(int position){
        return (whiteQueens & (1L << position)) != 0;
    }

    public boolean isBlackQueen(int position){
        return (blackQueens & (1L << position)) != 0;
    }

    public boolean isWall(int position){
        return (walls & (1L << position)) != 0;
    }

    public boolean isWalkable(int position){
        return ((walls | blackQueens | whiteQueens) & (1L << position)) == 0;
    }

    public long getOccupancy(){
        return walls | blackQueens | whiteQueens;
    }


    public boolean hasMoves(int[] positions){
        for(int queenPosition : positions) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue; // Skip the queen's own position

                    if (isWalkable(queenPosition + i * BOARD_SIZE + j)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isGameOver(){
        boolean hasMovesBlack = hasMoves(blackQueenPositions);
        boolean hasMovesWhite = hasMoves(whiteQueenPositions);
        return !hasMovesBlack && !hasMovesWhite;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void movePiece(int oldPosition, int newPosition) {
        if (isWhiteQueen(oldPosition)) {
            whiteQueens &= ~(1L << oldPosition); // Remove from old position
            whiteQueens |= (1L << newPosition); // Add to new position
            updateQueenPosition(whiteQueenPositions, oldPosition, newPosition);

        } else if (isBlackQueen(oldPosition)) {
            blackQueens &= ~(1L << oldPosition); // Remove from old position
            blackQueens |= (1L << newPosition); // Add to new position
            updateQueenPosition(blackQueenPositions, oldPosition, newPosition);

        }
        notifyObservers();
    }

    private void updateQueenPosition(int[] positions, int oldPosition, int newPosition) {
        for (int i = 0; i < 3; i++) {
            if (positions[i] == oldPosition) {
                positions[i] = newPosition;
                break;
            }
        }
    }

    public void placeWall(int position) {
        walls |= (1L << position);
        currentPlayer = currentPlayer == Constants.WHITE ? Constants.BLACK : Constants.WHITE;

        notifyObservers();
    }


    public int getPiece(int position){
        if(isWhiteQueen(position)) return Constants.WHITE;
        if(isBlackQueen(position)) return Constants.BLACK;
        if(isWall(position)) return Constants.WALL;
        return Constants.EMPTY;
    }

    public void unPlaceWall(int position){
        walls &= ~(1L << position);
        currentPlayer = currentPlayer == Constants.WHITE ? Constants.BLACK : Constants.WHITE;

        notifyObservers();
    }


    public boolean isMoveValid(int oldPosition, int newPosition) {
        // Check if the positions are within the boundaries of the board
        if((isWhiteQueen(oldPosition) && currentPlayer == Constants.BLACK) || (isBlackQueen(oldPosition) && currentPlayer == Constants.WHITE)) return false;


        if (oldPosition >= BOARD_SIZE * BOARD_SIZE || oldPosition < 0 ||
                newPosition >= BOARD_SIZE * BOARD_SIZE || newPosition < 0) {
            return false;
        }

        // Check if the new position is walkable
        if (!isWalkable(newPosition)) {
            return false;
        }

        int oldRow = oldPosition / BOARD_SIZE;
        int oldCol = oldPosition % BOARD_SIZE;
        int newRow = newPosition / BOARD_SIZE;
        int newCol = newPosition % BOARD_SIZE;

        int rowDiff = Math.abs(oldRow - newRow);
        int colDiff = Math.abs(oldCol - newCol);
        if (oldRow == newRow || oldCol == newCol || rowDiff == colDiff) {
            int rowStep = Integer.compare(newRow, oldRow);
            int colStep = Integer.compare(newCol, oldCol);
            int checkRow = oldRow + rowStep;
            int checkCol = oldCol + colStep;
            while (checkRow != newRow || checkCol != newCol) {
                int checkPosition = checkRow * BOARD_SIZE + checkCol;
                if (!isWalkable(checkPosition)) {
                    return false;
                }
                checkRow += rowStep;
                checkCol += colStep;
            }
            return true;
        }

        return false;
    }


    public int[][] generatePossibleMoves(int playerColor) {
        List<int[]> possibleMovesList = new ArrayList<>();

        int[] queensPositions = playerColor == Constants.WHITE ? whiteQueenPositions : blackQueenPositions;
        long occupancy = getOccupancy();

        for (int position : queensPositions) {
            long queenMoves = MagicBitboard.getQueenAttacks(position, occupancy);
            while (queenMoves != 0) {
                int to = Long.numberOfTrailingZeros(queenMoves);
                queenMoves &= queenMoves - 1; // Remove the least significant bit

                long newOccupancy = (occupancy & ~(1L << position)) | (1L << to);
                long wallMoves = MagicBitboard.getQueenAttacks(to, newOccupancy);

                while (wallMoves != 0) {
                    int wall = Long.numberOfTrailingZeros(wallMoves);
                    wallMoves &= wallMoves - 1; // Remove the least significant bit

                    possibleMovesList.add(new int[]{position, to, wall});
                }
            }
        }

        return possibleMovesList.toArray(new int[0][0]);
    }


    private static void printBinaryBoard(String binaryString, int rows, int cols) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Calculate the index in the binary string
                int index = i * cols + j;
                // Print the binary digit
                if(index < binaryString.length())
                    System.out.print(binaryString.charAt(index) + " ");
                else
                    System.out.print(0 + " ");

            }
            // Print a new line after each row
            System.out.println();
        }
    }



    public int[] getWhiteQueenPositions() {
        return whiteQueenPositions;
    }

    public int[] getBlackQueenPositions() {
        return blackQueenPositions;
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void unregisterAllObservers(){
        observers.clear();
    }

    public List<Observer> getObservers() {
        return observers;
    }

    public void addAllObservers(List<Observer> newObservers){
        observers.addAll(newObservers);
    }

    private void printBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int position = row * BOARD_SIZE + col;
                if ((whiteQueens & (1L << position)) != 0) {
                    System.out.print("W ");
                } else if ((blackQueens & (1L << position)) != 0) {
                    System.out.print("B ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
    }

    public void notifyObservers(){
        if(observers.isEmpty()) return;

        for(Observer observer : observers)
            observer.onBoardChanged();

        if(isGameOver()) {
            for (Observer observer : observers)
                observer.onGameOver();
        }

    }


}
