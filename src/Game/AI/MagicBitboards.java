package Game.AI;

import java.util.Random;

public class MagicBitboards {
    private static final int BOARD_SIZE = 64;
    private static final int[] rookShifts = new int[BOARD_SIZE];
    private static final long[] rookMasks = new long[BOARD_SIZE];
    private static final long[][] rookAttacks = new long[BOARD_SIZE][];
    private static final long[] rookMagicNumbers = new long[BOARD_SIZE];

    private static final int[] bishopShifts = new int[BOARD_SIZE];
    private static final long[] bishopMasks = new long[BOARD_SIZE];
    private static final long[][] bishopAttacks = new long[BOARD_SIZE][];
    private static final long[] bishopMagicNumbers = new long[BOARD_SIZE];


    public MagicBitboards() {
        initRookMagicBitboards();
        initBishopMagicBitboards();
    }

    private void initRookMagicBitboards() {
        // Initialize rook masks, shifts, magic numbers, and attack tables
        for (int square = 0; square < BOARD_SIZE; square++) {
            rookMasks[square] = computeRookMask(square);
            rookShifts[square] = 64 - Long.bitCount(rookMasks[square]);
            rookMagicNumbers[square] = findMagicNumber(square, rookMasks[square], true);
            rookAttacks[square] = computeRookAttacks(square);
        }
    }

    private void initBishopMagicBitboards() {
        // Initialize bishop masks, shifts, magic numbers, and attack tables
        for (int square = 0; square < BOARD_SIZE; square++) {
            bishopMasks[square] = computeBishopMask(square);
            bishopShifts[square] = 64 - Long.bitCount(bishopMasks[square]);
            bishopMagicNumbers[square] = findMagicNumber(square, bishopMasks[square], false);
            bishopAttacks[square] = computeBishopAttacks(square);
        }
    }

    private long computeRookMask(int square) {
        // Compute rook occupancy mask for the given square
        long mask = 0L;
        // Add horizontal and vertical masks
        int rank = square / 8;
        int file = square % 8;
        for (int r = rank + 1; r <= 6; r++) mask |= (1L << (r * 8 + file));
        for (int r = rank - 1; r >= 1; r--) mask |= (1L << (r * 8 + file));
        for (int f = file + 1; f <= 6; f++) mask |= (1L << (rank * 8 + f));
        for (int f = file - 1; f >= 1; f--) mask |= (1L << (rank * 8 + f));
        return mask;
    }

    private long computeBishopMask(int square) {
        // Compute bishop occupancy mask for the given square
        long mask = 0L;
        int rank = square / 8;
        int file = square % 8;
        for (int r = rank + 1, f = file + 1; r <= 6 && f <= 6; r++, f++) mask |= (1L << (r * 8 + f));
        for (int r = rank + 1, f = file - 1; r <= 6 && f >= 1; r++, f--) mask |= (1L << (r * 8 + f));
        for (int r = rank - 1, f = file + 1; r >= 1 && f <= 6; r--, f++) mask |= (1L << (r * 8 + f));
        for (int r = rank - 1, f = file - 1; r >= 1 && f >= 1; r--, f--) mask |= (1L << (r * 8 + f));
        return mask;
    }

    private long findMagicNumber(int square, long mask, boolean isRook) {
        // Find a magic number for the given square and mask
        Random rand = new Random();
        long magicNumber;
        boolean success;
        do {
            magicNumber = (rand.nextLong() & rand.nextLong() & rand.nextLong());
            long[] used = new long[4096];
            success = true;
            for (int i = 0; i < (1 << Long.bitCount(mask)); i++) {
                long occupancy = generateOccupancyVariation(mask, i);
                int index = (int) ((occupancy * magicNumber) >>> (isRook ? rookShifts[square] : bishopShifts[square]));
                if (used[index] == 0L) {
                    used[index] = computeAttackMask(square, occupancy, isRook);
                } else if (used[index] != computeAttackMask(square, occupancy, isRook)) {
                    success = false;
                    break;
                }
            }
        } while (!success);
        return magicNumber;
    }

    private long generateOccupancyVariation(long mask, int index) {
        // Generate a variation of occupancy based on the mask and index
        long result = 0L;
        int bit = 0;
        for (int i = 0; i < 64; i++) {
            if ((mask & (1L << i)) != 0) {
                if ((index & (1 << bit)) != 0) {
                    result |= (1L << i);
                }
                bit++;
            }
        }
        return result;
    }

    private long computeAttackMask(int square, long occupancy, boolean isRook) {
        // Compute the attack mask for the given square and occupancy
        long attacks = 0L;
        int[] directions = isRook ? new int[]{-8, 8, -1, 1} : new int[]{-9, -7, 7, 9};
        for (int dir : directions) {
            for (int i = square + dir; i >= 0 && i < 64 && Math.abs((i % 8) - (square % 8)) <= 1; i += dir) {
                attacks |= (1L << i);
                if ((occupancy & (1L << i)) != 0) break;
            }
        }
        return attacks;
    }

    private long[] computeRookAttacks(int square) {
        // Compute rook attack table for the given square
        int variations = 1 << Long.bitCount(rookMasks[square]);
        long[] attacks = new long[variations];
        for (int i = 0; i < variations; i++) {
            long occupancy = generateOccupancyVariation(rookMasks[square], i);
            int index = (int) ((occupancy * rookMagicNumbers[square]) >>> rookShifts[square]);
            attacks[index] = computeAttackMask(square, occupancy, true);
        }
        return attacks;
    }

    private long[] computeBishopAttacks(int square) {
        // Compute bishop attack table for the given square
        int variations = 1 << Long.bitCount(bishopMasks[square]);
        long[] attacks = new long[variations];
        for (int i = 0; i < variations; i++) {
            long occupancy = generateOccupancyVariation(bishopMasks[square], i);
            int index = (int) ((occupancy * bishopMagicNumbers[square]) >>> bishopShifts[square]);
            attacks[index] = computeAttackMask(square, occupancy, false);
        }
        return attacks;
    }

    public long generateQueenMoves(int square, long occupancy) {
        // Generate queen moves by combining rook and bishop moves
        int rookIndex = (int) (((occupancy & rookMasks[square]) * rookMagicNumbers[square]) >>> rookShifts[square]);
        int bishopIndex = (int) (((occupancy & bishopMasks[square]) * bishopMagicNumbers[square]) >>> bishopShifts[square]);
        return rookAttacks[square][rookIndex] | bishopAttacks[square][bishopIndex];
    }

    public static void main(String[] args) {
        // Example usage
        MagicBitboards mb = new MagicBitboards();
        int square = 36; // e4
        long occupancy = 0x0000001008000000L; // Example occupancy

        long queenMoves = mb.generateQueenMoves(square, occupancy);
        System.out.println(Long.toBinaryString(queenMoves));
    }
}
