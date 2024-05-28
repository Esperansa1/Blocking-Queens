package Game;

import Game.AI.Minimax;

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
            handleWallPlacement(newPosition);
            aiPlay();
        } else if (selectionMode == Constants.SELECTING_START && selectedPosition == -1) {
            selectPiece(newPosition);
        } else if (selectionMode == Constants.SELECTING_START) {
            movePiece(newPosition);
        }
    }

    public void aiPlay(){
        int[] bestMove = Minimax.minimax(model, 2);
        model.movePiece(bestMove[0], bestMove[1]);
        model.placeWall(bestMove[2]);
    }


    private void handleWallPlacement(int newPosition) {
        if (model.isWalkable(newPosition)) {
            if (model.isMoveValid(selectedPosition, newPosition)) {
                model.placeWall(newPosition);
                selectionMode = Constants.SELECTING_START;
                selectedPosition = -1;
            }
        }
    }

    private void selectPiece(int newPosition) {
        int piece = model.getPiece(newPosition);
        if (piece != Constants.EMPTY && piece != Constants.WALL) {
            selectedPosition = newPosition;
        }
    }

    private void movePiece(int newPosition) {
        if (model.isMoveValid(selectedPosition, newPosition)) {
            model.movePiece(selectedPosition, newPosition);
            selectionMode = Constants.WALL_PLACING;
            selectedPosition = newPosition;
        } else {
            selectionMode = Constants.SELECTING_START;
            selectedPosition = -1;
        }
    }




}
