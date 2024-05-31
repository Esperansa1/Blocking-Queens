package Game;

import Game.AI.MagicBitboard;

import java.io.*;
import java.util.*;

public class Model implements Serializable {

    private List<Observer> observers;
    public static final int BOARD_SIZE = 8;


    private final short[] blackQueenPositions;
    private final short[] whiteQueenPositions;

    // Bitboards to represent the queens
    private long whiteQueens;
    private long blackQueens;
    private long walls;
    private int currentPlayer = Constants.WHITE;

    private int whiteQueenArrayPosition = 0;
    private int blackQueenArrayPosition = 0;


    public Model() {
        this.observers = new ArrayList<>();

        blackQueenPositions = new short[3];
        whiteQueenPositions = new short[3];
        initializeQueens();
    }

    // Initialize the queens on the board
    private void initializeQueens() {

        putWhiteQueen(3);
        putWhiteQueen(4);
        putWhiteQueen(5);

        putBlackQueen(59);
        putBlackQueen(60);
        putBlackQueen(61);

//        setupWalls(new int[] {2,8,12,17,20,26,30,33,34,35,36,37,38,41,44,50,51,52,53,56,58});

    }

    private void putWhiteQueen(int position){
        if(whiteQueenArrayPosition >= 3) return;

        whiteQueens |= (1L << position);
        whiteQueenPositions[whiteQueenArrayPosition++] = (short) position;
    }

    private void putBlackQueen(int position){
        if(blackQueenArrayPosition >= 3) return;
        blackQueens |= (1L << position);
        blackQueenPositions[blackQueenArrayPosition++] = (short) position;
    }

    private void setupWalls(int[] positions){
        for(int pos : positions) {
            walls |= (1L << pos);
        }
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


    public boolean hasMoves(short[] positions){
        for(int queenPosition : positions) {
            for(int pos : Constants.POSSIBLE_MOVEMENTS_OFFSETS)
                    if (isWalkable(queenPosition + pos)) {
                        return true;
                    }
                }
        return false;
    }

    public boolean isGameOver(){
        boolean hasMovesBlack = hasMoves(blackQueenPositions);
        boolean hasMovesWhite = hasMoves(whiteQueenPositions);
        return !hasMovesBlack || !hasMovesWhite;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void movePiece(short oldPosition, short newPosition) {
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

    private void updateQueenPosition(short[] positions, short oldPosition, short newPosition) {
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


    public short[][] generatePossibleMoves(int playerColor) {
        short[][] possibleMovesList = new short[26 * 3][2];

        short[] queensPositions = playerColor == Constants.WHITE ? whiteQueenPositions : blackQueenPositions;
        long occupancy = getOccupancy();

        int k = 0;

        for (int position : queensPositions) {
            long queenMoves = MagicBitboard.getQueenAttacks(position, occupancy);
            while (queenMoves != 0) {
                int to = Long.numberOfTrailingZeros(queenMoves);
                queenMoves &= queenMoves - 1; // Remove the least significant bit
                possibleMovesList[k++] = new short[]{(short)position, (short)to};
            }
        }
        return Arrays.copyOf(possibleMovesList, k);
    }

    public short[] generatePossibleWalls(short lastMovedPosition) {
        short[] possibleMovesList = new short[28];

        long occupancy = getOccupancy();

        int k = 0;

        long queenMoves = MagicBitboard.getQueenAttacks(lastMovedPosition, occupancy);
        while (queenMoves != 0) {
            int to = Long.numberOfTrailingZeros(queenMoves);
            queenMoves &= queenMoves - 1; // Remove the least significant bit
            possibleMovesList[k] = (short) to;
            k++;
        }

//        System.out.println("moved="+lastMovedPosition);
//        System.out.println(Arrays.toString(possibleMovesList));
        return Arrays.copyOf(possibleMovesList, k);
    }


    public short[] getWhiteQueenPositions() {
        return whiteQueenPositions;
    }

    public short[] getBlackQueenPositions() {
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

    public void notifyObservers(){
        if(observers.isEmpty()) return;

        for(Observer observer : observers)
            observer.onBoardChanged();

        if(isGameOver()) {
            for (Observer observer : observers)
                observer.onGameOver();
        }

    }

    public Model deepCopy() {
        try {
            // Write object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            out.close();

            // Read object from byte array
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);
            return (Model) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isValidMove(int previousPosition, int newPosition) {

        int prevRow = previousPosition / BOARD_SIZE;
        int prevCol = previousPosition % BOARD_SIZE;

        int newRow = newPosition / BOARD_SIZE;
        int newCol = newPosition % BOARD_SIZE;

        // Check if the new position is within the board boundaries
        if (newRow < 0 || newRow >= BOARD_SIZE || newCol < 0 || newCol >= BOARD_SIZE) {
            return false;
        }

        // Check if the new position is one step away from the previous position
        int rowDiff = Math.abs(prevRow - newRow);
        int colDiff = Math.abs(prevCol - newCol);

        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1) || (rowDiff == 1 && colDiff == 1); // Valid move (one step horizontally or vertically)
    }


}
