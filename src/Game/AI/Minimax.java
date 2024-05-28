package Game.AI;

import Game.Constants;
import Game.Model;
import Game.Observer;

import java.util.ArrayList;
import java.util.List;

import static Game.Constants.BOARD_SIZE;

public class Minimax {

    static int counter = 0;
    public static int[] minimax(Model model, int depth) {

        List<Observer> observers = new ArrayList<>(model.getObservers());
        model.unregisterAllObservers();

        int[] value = bestMove(model, depth);

        model.addAllObservers(observers);

        System.out.println(counter);

        return value;
    }



    public static int[] bestMove(Model model, int depth) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMovePos = null;

        int[][] moves = model.generatePossibleMoves(Constants.BLACK);

        for (int[] move : moves) {
            model.movePiece(move[0], move[1]);
            model.placeWall(move[2]);

            counter++;
            int score = minimaxScore(model, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

            model.unPlaceWall(move[2]);
            model.movePiece(move[1], move[0]);

            if (score > bestScore) {
                bestScore = score;
                bestMovePos = move;
            }
        }

        return bestMovePos;
    }

    private static int minimaxScore(Model model, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0 || model.isGameOver()) {
            return evaluation(model);
        }


        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            int[][] moves = model.generatePossibleMoves(Constants.BLACK);
            for (int[] move : moves) {
                counter++;


                model.movePiece(move[0], move[1]);
                model.placeWall(move[2]);

                bestScore = Math.max(bestScore, minimaxScore(model, depth - 1, alpha, beta, false));

                model.unPlaceWall(move[2]);
                model.movePiece(move[1], move[0]);

                if(bestScore >= beta) {
                    break;
                }
                alpha = Math.max(alpha, bestScore);


            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            int[][] moves = model.generatePossibleMoves(Constants.WHITE);
            for (int[] move : moves) {

                counter++;


                model.movePiece(move[0], move[1]);
                model.placeWall(move[2]);

                bestScore = Math.min(bestScore, minimaxScore(model, depth - 1, alpha, beta, true));

                model.unPlaceWall(move[2]);
                model.movePiece(move[1], move[0]);


                if(bestScore <= alpha) {
                    break;
                }
                beta = Math.min(beta, bestScore);
            }
            return bestScore;
        }
    }


    public static int evaluation(Model model){

        if(model.isGameOver()){
            return model.getCurrentPlayer() == Constants.WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }

        int positionEvaluation = 0;
        for(int whiteQueenPosition : model.getWhiteQueenPositions()){
            positionEvaluation -= getQueenOptionsAmount(model, whiteQueenPosition);
        }

        for(int blackQueenPosition : model.getBlackQueenPositions()){
            positionEvaluation += getQueenOptionsAmount(model, blackQueenPosition);
        }

        return positionEvaluation;
    }

    private static int getQueenOptionsAmount(Model model, int position) {
        int row = position / BOARD_SIZE;
        int col = position % BOARD_SIZE;
        int possibleOptions = 0;

        // Check surrounding squares for walls
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // Skip the queen's own position
                int newRow = row + i;
                int newCol = col + j;
                if (model.isWalkable(newRow * BOARD_SIZE + newCol)) {
                    possibleOptions++;
                }
            }
        }

        return possibleOptions;
    }


}