package Game;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static final int BOARD_SIZE = 8;
    public static final int WHITE = 1;
    public static final int BLACK = 2;
    public static final int EMPTY = 0;
    public static final int WALL = 3;

    public static final int SELECTING_START = -1;
    public static final int WALL_PLACING = -2;
    public static final int[] POSSIBLE_MOVEMENTS_OFFSETS = {
            -BOARD_SIZE -1, -BOARD_SIZE, -BOARD_SIZE + 1, -1, +1, BOARD_SIZE - 1, BOARD_SIZE, BOARD_SIZE + 1
    };

    public static int[][] POSSIBLE_MOVES;

    static {
        POSSIBLE_MOVES = new int[BOARD_SIZE*BOARD_SIZE][8];


        for (int position = 0; position < 64; position++) {
            int k = 0;
            for(int offset : POSSIBLE_MOVEMENTS_OFFSETS){
                int newPos = position + offset;
                if (Model.isValidMove(position, newPos)) {
                    POSSIBLE_MOVES[position][k++] = newPos;
                }
            }
        }

    }




}
