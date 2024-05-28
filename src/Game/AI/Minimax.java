package Game.AI;

import Game.Constants;
import Game.Model;
import Game.Observer;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static Game.Constants.BOARD_SIZE;

public class Minimax {

    static int count = 0;
    public static int[] minimax(Model model, int depth) {
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        List<Observer> observers = new ArrayList<>(model.getObservers());
        model.unregisterAllObservers();
        int[] value =  minimax(model, depth, alpha, beta, true);
        model.addAllObservers(observers);
        System.out.println(count);
        return value;
    }



    public static int[] minimax(Model model, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth <= 0 || model.isGameOver()) {
            return new int[]{evaluation(model), -1, -1, -1}; // Return the evaluation score and a dummy move (-1, -1, -1)
        }
        int[] bestMove = new int[3];
        if (isMaximizing) {
            int maxValue = Integer.MIN_VALUE;
            List<int[]> moves = model.generatePossibleMoves(Constants.BLACK);
            orderMoves(moves);
            for (int[] move : moves) {
                count++;
                model.movePiece(move[0], move[1]);
                model.placeWall(move[2]);
                int[] valueAndMove = minimax(model, depth - 1, alpha, beta, false);
                int value = valueAndMove[0];
                if (value > maxValue) {
                    maxValue = value;
                    bestMove = move;

                }
                alpha = Math.max(alpha, value);
                model.unPlaceWall(move[2]);
                model.movePiece(move[1], move[0]);
                if (value > beta) {
                    break; // Beta cutoff
                }
            }
            return new int[]{maxValue, bestMove[0], bestMove[1], bestMove[2]}; // Return the best score and the best move
        } else {
            int minValue = Integer.MAX_VALUE;
            List<int[]> moves = model.generatePossibleMoves(Constants.WHITE);
            count++;
            orderMoves(moves);
            for (int[] move : moves) {
                model.movePiece(move[0], move[1]);
                model.placeWall(move[2]);
                int[] valueAndMove = minimax(model, depth - 1, alpha, beta, true);
                int value = valueAndMove[0];
                if (value < minValue) {
                    minValue = value;
                    bestMove = move;
                }
                beta = Math.min(beta, value);
                model.unPlaceWall(move[2]);
                model.movePiece(move[1], move[0]);
                if (value < alpha) {
                    break; // Alpha cutoff
                }
            }
            return new int[]{minValue, bestMove[0], bestMove[1], bestMove[2]}; // Return the best score and the best move
        }
    }


    public static int evaluation(Model model){

        if(model.isGameOver()){
            return model.getCurrentPlayer() == Constants.WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }


        int positionEvaluation = 0;
        for(int whiteQueenPosition : model.getWhiteQueenPositions()){
            positionEvaluation += evaluateQueenPosition(model, whiteQueenPosition);
        }

        for(int blackQueenPosition : model.getBlackQueenPositions()){
            positionEvaluation -= evaluateQueenPosition(model, blackQueenPosition);
        }

        return positionEvaluation;
    }

    private static int evaluateQueenPosition(Model model, int position) {
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

    public static void orderMoves(List<int[]> moves){

        moves.sort(Comparator.comparingInt(o -> o[0]));
    }
}
