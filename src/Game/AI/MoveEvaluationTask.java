package Game.AI;

import Game.Model;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static Game.AI.Minimax.minimaxScore;

class MoveEvaluationTask implements Callable<MoveResult> {
    private final Model model;
    private final short[] move;
    private final short wallPlacement;
    private final int depth;

    public MoveEvaluationTask(Model model, short[] move, short wallPlacement, int depth) {
        this.model = model.deepCopy(); // Assuming you have a copy method to avoid modifying the original model
        this.move = move;
        this.wallPlacement = wallPlacement;
        this.depth = depth;
    }

    @Override
    public MoveResult call() {
        model.movePiece(move[0], move[1]);
        model.placeWall(wallPlacement);

        int score = minimaxScore(model, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

        model.unPlaceWall(wallPlacement);
        model.movePiece(move[1], move[0]);

        return new MoveResult(move, wallPlacement, score);
    }
}

class MoveResult {
    public final short[] move;
    public final short wallPlacement;
    public final int score;

    public MoveResult(short[] move, short wallPlacement, int score) {
        this.move = move;
        this.wallPlacement = wallPlacement;
        this.score = score;
    }

    @Override
    public String toString() {
        return "score=" + score;
    }
}
