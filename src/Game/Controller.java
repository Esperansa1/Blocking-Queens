package Game;

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

        // Translation of row, col to correct board position
        int newPosition = row * Model.BOARD_SIZE + col;

        if(selectionMode == Constants.WALL_PLACING && model.isWalkable(newPosition)) {
            // Place wall
            if(model.isMoveValid(selectedPosition, newPosition)) {
                model.placeWall(newPosition);
                selectionMode = Constants.SELECTING_START;
                selectedPosition = -1;
            }
        } else if (selectionMode == Constants.SELECTING_START && selectedPosition == -1) {
            // Select piece
            int piece = model.getPiece(newPosition);
            if (piece != Constants.EMPTY && piece != Constants.WALL) {
                selectedPosition = newPosition;
            }
        } else  if(selectionMode == Constants.SELECTING_START){
            // Move piece
            if (model.isMoveValid(selectedPosition, newPosition)) {
                model.movePiece(selectedPosition, newPosition);
                selectionMode = Constants.WALL_PLACING;
                selectedPosition = newPosition;
            }else {
                selectionMode = Constants.SELECTING_START;
                selectedPosition = -1;
            }
        }
    }

}
