package Game;

import java.util.ArrayList;
import java.util.List;

public class Model {

    private ArrayList<Observer> observers;
    public static final int BOARD_SIZE = 8;

    // Bitboards to represent the queens
    private long whiteQueens;
    private long blackQueens;
    private long walls;
    private int currentPlayer = Constants.WHITE;


    public Model() {
        this.observers = new ArrayList<>();
        initializeQueens();
    }

    // Initialize the queens on the board
    private void initializeQueens() {
        // Example positions for queens
        // Let's say the white queens are at A1, D4, and H8
        whiteQueens = (1L) | (1L << 27) | (1L << 63);

        // And the black queens are at B2, E5, and G7
        blackQueens = (1L << 9) | (1L << 36) | (1L << 54);
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

    public void movePiece(int oldPosition, int newPosition) {
        if (isWhiteQueen(oldPosition)) {
            whiteQueens &= ~(1L << oldPosition); // Remove from old position
            whiteQueens |= (1L << newPosition); // Add to new position
        } else if (isBlackQueen(oldPosition)) {
            blackQueens &= ~(1L << oldPosition); // Remove from old position
            blackQueens |= (1L << newPosition); // Add to new position
        }
        notifyObservers();
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


    public List<Integer> generatePossibleMoves(int playerColor) {
        List<Integer> possibleMoves = new ArrayList<>();

        for (int position = 0; position < BOARD_SIZE * BOARD_SIZE; position++) {
            int piece = getPiece(position);

            // Check if the piece is a queen of the current player's color
            if (piece == (playerColor == Constants.WHITE ? Constants.WHITE : Constants.BLACK)) {
                // Generate possible moves for this queen
                List<Integer> queenMoves = generateQueenMoves(position);
                possibleMoves.addAll(queenMoves);
            }
        }

        return possibleMoves;
    }

    private List<Integer> generateQueenMoves(int position) {
        List<Integer> moves = new ArrayList<>();

        // Extract row and column indices
        int row = position / BOARD_SIZE;
        int col = position % BOARD_SIZE;

        // Generate horizontal and vertical moves
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (i != col) {
                moves.add(row * BOARD_SIZE + i); // Horizontal moves
            }
            if (i != row) {
                moves.add(i * BOARD_SIZE + col); // Vertical moves
            }
        }

        // Generate diagonal moves
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (row - i >= 0 && col - i >= 0) {
                moves.add((row - i) * BOARD_SIZE + (col - i)); // Top-left diagonal
            }
            if (row - i >= 0 && col + i < BOARD_SIZE) {
                moves.add((row - i) * BOARD_SIZE + (col + i)); // Top-right diagonal
            }
            if (row + i < BOARD_SIZE && col - i >= 0) {
                moves.add((row + i) * BOARD_SIZE + (col - i)); // Bottom-left diagonal
            }
            if (row + i < BOARD_SIZE && col + i < BOARD_SIZE) {
                moves.add((row + i) * BOARD_SIZE + (col + i)); // Bottom-right diagonal
            }
        }

        return moves;
    }



    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void notifyObservers(){
        for(Observer observer : observers)
            observer.onBoardChanged();

        if(isGameOver())
            for(Observer observer : observers)
                observer.onGameOver();

    }
}
