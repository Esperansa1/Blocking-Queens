package Game.AI;

import Game.Constants;
import Game.Model;
import Game.Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Minimax {

    static int counter = 0;
    public static short[] minimax(Model model, int depth) {

        List<Observer> observers = new ArrayList<>(model.getObservers());
        model.unregisterAllObservers();

        long startTime = System.currentTimeMillis();

        counter = 0;
        short[] value = bestMove(model, depth);

        model.addAllObservers(observers);

        System.out.println();
        System.out.println("Time took = "+ formatSecondsToMinutesSeconds( (float) (System.currentTimeMillis() - startTime) / 1000 ));
        System.out.println("Options tried = " +counter);
        System.out.println("Chosen = " + Arrays.toString(value));




        return value;
    }

    public static String formatSecondsToMinutesSeconds(float totalSeconds) {
        int minutes = (int) (totalSeconds / 60);
        float seconds = totalSeconds % 60;
        return String.format("%d minutes, %f seconds", minutes, seconds);
    }


    public static short[] bestMove(Model model, int depth) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<Future<MoveResult>> futures = new ArrayList<>();

        short[][] moves = model.generatePossibleMoves(Constants.BLACK);

        for (short[] move : moves) {
            model.movePiece(move[0], move[1]);
            for (short wallPlacement : model.generatePossibleWalls(move[1])) {
                MoveEvaluationTask task = new MoveEvaluationTask(model, move, wallPlacement, depth);
                futures.add(executor.submit(task));
            }
            model.movePiece(move[1], move[0]);
        }

        int bestScore = Integer.MIN_VALUE;
        short[] bestMovePos = null;
        short bestWallPlacement = -1;

        for (Future<MoveResult> future : futures) {
            try {
                MoveResult result = future.get();
                if (result.score > bestScore) {
                    bestScore = result.score;
                    bestMovePos = result.move;
                    bestWallPlacement = result.wallPlacement;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        return new short[]{bestMovePos[0], bestMovePos[1], bestWallPlacement};
    }

//    public static short[] bestMove(Model model, int depth) {
//        int bestScore = Integer.MIN_VALUE;
//        short[] bestMovePos = null;
//        short bestWallPlacement = -1;
//
//        short[][] moves = model.generatePossibleMoves(Constants.BLACK);
//
//        for (short[] move : moves) {
//            model.movePiece(move[0], move[1]);
//            for(short wallPlacement : model.generatePossibleWalls(move[1])){
//                model.placeWall(wallPlacement);
//
//                int score = minimaxScore(model, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
//
//                if (score > bestScore) {
//                    bestScore = score;
//                    bestMovePos = move;
//                    bestWallPlacement = wallPlacement;
//                }
//
//                model.unPlaceWall(wallPlacement);
//            }
//
//            model.movePiece(move[1], move[0]);
//
//
//        }
//
//        return new short[]{bestMovePos[0], bestMovePos[1], bestWallPlacement};
//    }

    public static int minimaxScore(Model model, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0 || model.isGameOver()) {
            return evaluation(model);
        }


        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            short[][] moves = model.generatePossibleMoves(Constants.BLACK);
            for (short[] move : moves) {
                model.movePiece(move[0], move[1]);

                for(int wallPlacement : model.generatePossibleWalls(move[1])){
                    counter++;


                    model.placeWall(wallPlacement);

                    bestScore = Math.max(bestScore, minimaxScore(model, depth - 1, alpha, beta, false));

                    model.unPlaceWall(wallPlacement);
                    alpha = Math.max(alpha, bestScore);
                    if(beta <= alpha) {
                        break;
                    }
                }

                model.movePiece(move[1], move[0]);

            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            short[][] moves = model.generatePossibleMoves(Constants.WHITE);
            for (short[] move : moves) {

                model.movePiece(move[0], move[1]);

                for(int wallPlacement : model.generatePossibleWalls(move[1])){
                    counter++;


                    model.placeWall(wallPlacement);

                    bestScore = Math.min(bestScore, minimaxScore(model, depth - 1, alpha, beta, true));

                    model.unPlaceWall(wallPlacement);
                    beta = Math.min(beta, bestScore);

                    if(beta <= alpha) {
                        break;
                    }

                }
                model.movePiece(move[1], move[0]);
            }
            return bestScore;
        }
    }


    public static int evaluation(Model model){

        if(model.isGameOver()){
            return model.getCurrentPlayer() == Constants.WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }

        short[] whiteQueenPositions = model.getWhiteQueenPositions();
        short[] blackQueenPositions = model.getBlackQueenPositions();


        int positionEvaluation = 0;
        for(int whiteQueenPosition : whiteQueenPositions){
            int whiteQueenOptions= getQueenOptionsAmount(model, whiteQueenPosition);
            positionEvaluation -= whiteQueenOptions;
        }

        for(int blackQueenPosition : blackQueenPositions){
            int blackQueenOptions= getQueenOptionsAmount(model, blackQueenPosition);
            positionEvaluation += blackQueenOptions;
        }


        return positionEvaluation;
    }

    private static int getQueenOptionsAmount(Model model, int position) {
        int possibleOptions = 0;

        // Check surrounding squares for walls
        int[] possibleMoves = Constants.POSSIBLE_MOVES[position];
        for(int newPos : possibleMoves){
            if(newPos == 0) break;
            if (model.isWalkable(newPos)) {
                possibleOptions++;
            }
        }

        return possibleOptions;
    }



}