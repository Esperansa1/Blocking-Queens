package Game;

import Game.AI.Minimax;

import java.util.Arrays;

public class Controller {

    private int selectedPosition = -1;
    private int selectionMode = Constants.SELECTING_START;
    private final Model model;

    public Controller(){
        model = new Model();
        View view = new View(model, this);
        model.addObserver(view);
    }

    public void handleMouseClick(int row, int col) {
        int newPosition = row * Model.BOARD_SIZE + col;

        if (selectionMode == Constants.WALL_PLACING) {
//            model.generatePossibleWalls((short) selectedPosition);
            if(handleWallPlacement(newPosition))
                aiPlay();

        } else if (selectionMode == Constants.SELECTING_START && selectedPosition == -1) {
            selectPiece(newPosition);
        } else if (selectionMode == Constants.SELECTING_START) {
            movePiece(newPosition);
        }
    }

    public void aiPlay(){
        short[] bestMove = Minimax.minimax(model, 4);

        System.out.println(Arrays.toString(bestMove));

        model.movePiece(bestMove[0], bestMove[1]);
        model.placeWall(bestMove[2]);
    }


    private boolean handleWallPlacement(int newPosition) {
        if (model.isWalkable(newPosition) && model.isMoveValid(selectedPosition, newPosition)) {
                model.placeWall(newPosition);
                selectionMode = Constants.SELECTING_START;
                selectedPosition = -1;
                return true;
        }
        return false;
    }

    private void selectPiece(int newPosition) {
        int piece = model.getPiece(newPosition);
        if (piece != Constants.EMPTY && piece != Constants.WALL) {
            selectedPosition = newPosition;
        }
    }

    private void movePiece(int newPosition) {
        if (model.isMoveValid(selectedPosition, newPosition)) {
            model.movePiece((short)selectedPosition,(short)newPosition);
            selectionMode = Constants.WALL_PLACING;
            selectedPosition = newPosition;
        } else {
            selectionMode = Constants.SELECTING_START;
            selectedPosition = -1;
        }
    }




}
