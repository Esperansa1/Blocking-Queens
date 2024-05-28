package Game;

import java.io.Serializable;
import java.util.*;

public class Model implements Serializable {

    private final List<Observer> observers;
    public static final int BOARD_SIZE = 8;

    private final Set<Integer> blackQueenPositions;
    private final Set<Integer> wallPositions;
    private final Set<Integer> whiteQueenPositions;

    // Bitboards to represent the queens
    private long whiteQueens;
    private long blackQueens;
    private long walls;
    private int currentPlayer = Constants.WHITE;


    public Model() {
        this.observers = new ArrayList<>();
        whiteQueenPositions = new HashSet<>();
        blackQueenPositions = new HashSet<>();
        wallPositions = new HashSet<>();
        initializeQueens();
    }

    // Initialize the queens on the board
    private void initializeQueens() {
        // Example positions for queens
        // Let's say the white queens are at A1, D4, and H8
        whiteQueens = (1L << 3) | (1L << 27) | (1L << 63);
        whiteQueenPositions.add(3);
        whiteQueenPositions.add(27);
        whiteQueenPositions.add(63);


        // And the black queens are at B2, E5, and G7
        blackQueens = (1L << 9) | (1L << 36) | (1L << 54);
        blackQueenPositions.add(9);
        blackQueenPositions.add(36);
        blackQueenPositions.add(54);



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

    public boolean isGameOver(){
        return generatePossibleMoves(currentPlayer).isEmpty();
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void movePiece(int oldPosition, int newPosition) {
        if (isWhiteQueen(oldPosition)) {
            whiteQueens &= ~(1L << oldPosition); // Remove from old position
            whiteQueens |= (1L << newPosition); // Add to new position
            whiteQueenPositions.remove(oldPosition);
            whiteQueenPositions.add(newPosition);
        } else if (isBlackQueen(oldPosition)) {
            blackQueens &= ~(1L << oldPosition); // Remove from old position
            blackQueens |= (1L << newPosition); // Add to new position
            blackQueenPositions.remove(oldPosition);
            blackQueenPositions.add(newPosition);
        }
        notifyObservers();
    }

    public void placeWall(int position) {
        walls |= (1L << position);
        wallPositions.add(position);
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
        wallPositions.remove(position);
        currentPlayer = currentPlayer == Constants.WHITE ? Constants.BLACK : Constants.WHITE;

        notifyObservers();
    }

    public void printBoard() {
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

    public List<int[]> generatePossibleMoves(int playerColor) {
        List<int[]> possibleMoves = new ArrayList<>();

        List<Integer> positionsToCheck = new ArrayList<>();
        positionsToCheck.addAll(getBlackQueenPositions());
        positionsToCheck.addAll(getWhiteQueenPositions());

        for (int position : positionsToCheck) {
            int piece = getPiece(position);

            // Check if the piece is a queen of the current player's color
            if (piece == playerColor) {
                // Generate possible moves for this queen
                List<Integer> queenMoves = generatePossibleMovements(position);
                for (int move : queenMoves) {
                    // For each move, generate possible wall placements
                    List<Integer> possibleWallPlacements = generatePossibleMovements(move);
                    for (int wallPos : possibleWallPlacements) {
                            possibleMoves.add(new int[]{position, move, wallPos});
                    }
                }
            }
        }

        return possibleMoves;
    }

    private List<Integer> generatePossibleMovements(int startPosition) {
        List<Integer> moves = new ArrayList<>();

        // Extract row and column indices

        for (int directionIndex = 0; directionIndex < BOARD_SIZE; directionIndex++) {
            for (int n = 0; n < 8; n++) {
                if(!isValidPosition(directionIndex, n)) continue;
                int position =  startPosition + Constants.directionOffsets[directionIndex] * (n + 1);
                if(isWalkable(position))
                    moves.add(position);
                else
                    break;
            }
        }



        return moves;
    }

    public Set<Integer> getWhiteQueenPositions() {
        return whiteQueenPositions;
    }

    public Set<Integer> getBlackQueenPositions() {
        return blackQueenPositions;
    }

    public Set<Integer> getWallPositions() {
        return wallPositions;
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


    public void notifyObservers(){
        if(observers.isEmpty()) return;

        for(Observer observer : observers)
            observer.onBoardChanged();

        if(isGameOver()) {
            for (Observer observer : observers)
                observer.onGameOver();
        }
    }

    public static boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }
}
