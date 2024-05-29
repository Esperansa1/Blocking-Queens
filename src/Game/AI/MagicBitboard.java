package Game.AI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.Random;

public class MagicBitboard {

    private static final long One = 1L;

    private static final long Ff = 0xFFL;

    private static final int MagicBishopDbLength = 512;

    private static final int MagicRookDbLength = 4096;

    private static final long[][] MagicBishopDb = new long[64][];

    private static final long[] MagicmovesBMagics = {
            0x0002020202020200L, 0x0002020202020000L, 0x0004010202000000L, 0x0004040080000000L, 0x0001104000000000L, 0x0000821040000000L, 0x0000410410400000L, 0x0000104104104000L,
            0x0000040404040400L, 0x0000020202020200L, 0x0000040102020000L, 0x0000040400800000L, 0x0000011040000000L, 0x0000008210400000L, 0x0000004104104000L, 0x0000002082082000L,
            0x0004000808080800L, 0x0002000404040400L, 0x0001000202020200L, 0x0000800802004000L, 0x0000800400A00000L, 0x0000200100884000L, 0x0000400082082000L, 0x0000200041041000L,
            0x0002080010101000L, 0x0001040008080800L, 0x0000208004010400L, 0x0000404004010200L, 0x0000840000802000L, 0x0000404002011000L, 0x0000808001041000L, 0x0000404000820800L,
            0x0001041000202000L, 0x0000820800101000L, 0x0000104400080800L, 0x0000020080080080L, 0x0000404040040100L, 0x0000808100020100L, 0x0001010100020800L, 0x0000808080010400L,
            0x0000820820004000L, 0x0000410410002000L, 0x0000082088001000L, 0x0000002011000800L, 0x0000080100400400L, 0x0001010101000200L, 0x0002020202000400L, 0x0001010101000200L,
            0x0000410410400000L, 0x0000208208200000L, 0x0000002084100000L, 0x0000000020880000L, 0x0000001002020000L, 0x0000040408020000L, 0x0004040404040000L, 0x0002020202020000L,
            0x0000104104104000L, 0x0000002082082000L, 0x0000000020841000L, 0x0000000000208800L, 0x0000000010020200L, 0x0000000404080200L, 0x0000040404040400L, 0x0002020202020200L
    };

    private static final long[] MagicmovesBMask = {
            0x0040201008040200L, 0x0000402010080400L, 0x0000004020100A00L, 0x0000000040221400L, 0x0000000002442800L, 0x0000000204085000L, 0x0000020408102000L, 0x0002040810204000L,
            0x0020100804020000L, 0x0040201008040000L, 0x00004020100A0000L, 0x0000004022140000L, 0x0000000244280000L, 0x0000020408500000L, 0x0002040810200000L, 0x0004081020400000L,
            0x0010080402000200L, 0x0020100804000400L, 0x004020100A000A00L, 0x0000402214001400L, 0x0000024428002800L, 0x0002040850005000L, 0x0004081020002000L, 0x0008102040004000L,
            0x0008040200020400L, 0x0010080400040800L, 0x0020100A000A1000L, 0x0040221400142200L, 0x0002442800284400L, 0x0004085000500800L, 0x0008102000201000L, 0x0010204000402000L,
            0x0004020002040800L, 0x0008040004081000L, 0x00100A000A102000L, 0x0022140014224000L, 0x0044280028440200L, 0x0008500050080400L, 0x0010200020100800L, 0x0020400040201000L,
            0x0002000204081000L, 0x0004000408102000L, 0x000A000A10204000L, 0x0014001422400000L, 0x0028002844020000L, 0x0050005008040200L, 0x0020002010080400L, 0x0040004020100800L,
            0x0000020408102000L, 0x0000040810204000L, 0x00000A1020400000L, 0x0000142240000000L, 0x0000284402000000L, 0x0000500804020000L, 0x0000201008040200L, 0x0000402010080400L,
            0x0002040810204000L, 0x0004081020400000L, 0x000A102040000000L, 0x0014224000000000L, 0x0028440200000000L, 0x0050080402000000L, 0x0020100804020000L, 0x0040201008040200L
    };

    private static final long[][] MagicRookDb = new long[64][];

    private static final long[] MagicmovesRMagics = {
            0x0080001020400080L, 0x0040001000200040L, 0x0080081000200080L, 0x0080040800100080L, 0x0080020400080080L, 0x0080010200040080L, 0x0080008001000200L, 0x0080002040800100L,
            0x0000800020400080L, 0x0000400020005000L, 0x0000801000200080L, 0x0000800800100080L, 0x0000800400080080L, 0x0000800200040080L, 0x0000800100020080L, 0x0000800040800100L,
            0x0000208000400080L, 0x0000404000201000L, 0x0000808010002000L, 0x0000808008001000L, 0x0000808004000800L, 0x0000808002000400L, 0x0000010100020004L, 0x0000020000408104L,
            0x0000208080004000L, 0x0000200040005000L, 0x0000100080200080L, 0x0000080080100080L, 0x0000040080080080L, 0x0000020080040080L, 0x0000010080800200L, 0x0000800080004100L,
            0x0000204000800080L, 0x0000200040401000L, 0x0000100080802000L, 0x0000080080801000L, 0x0000040080800800L, 0x0000020080800400L, 0x0000020001010004L, 0x0000800040800100L,
            0x0000204000808000L, 0x0000200040008080L, 0x0000100020008080L, 0x0000080010008080L, 0x0000040008008080L, 0x0000020004008080L, 0x0000010002008080L, 0x0000004081020004L,
            0x0000204000800080L, 0x0000200040008080L, 0x0000100020008080L, 0x0000080010008080L, 0x0000040008008080L, 0x0000020004008080L, 0x0000800100020080L, 0x0000800041000080L,
            0x0000102040800101L, 0x0000102040008101L, 0x0000081020004101L, 0x0000040810002101L, 0x0001000204080011L, 0x0001000204000801L, 0x0001000082000401L, 0x0000002040810402L
    };

    private static final long[] MagicmovesRMask = {
            0x000101010101017EL, 0x000202020202027CL, 0x000404040404047AL, 0x0008080808080876L, 0x001010101010106EL, 0x002020202020205EL, 0x004040404040403EL, 0x008080808080807EL,
            0x0001010101017E00L, 0x0002020202027C00L, 0x0004040404047A00L, 0x0008080808087600L, 0x0010101010106E00L, 0x0020202020205E00L, 0x0040404040403E00L, 0x0080808080807E00L,
            0x00010101017E0100L, 0x00020202027C0200L, 0x00040404047A0400L, 0x0008080808760800L, 0x00101010106E1000L, 0x00202020205E2000L, 0x00404040403E4000L, 0x00808080807E8000L,
            0x000101017E010100L, 0x000202027C020200L, 0x000404047A040400L, 0x0008080876080800L, 0x001010106E101000L, 0x002020205E202000L, 0x004040403E404000L, 0x008080807E808000L,
            0x0001017E01010100L, 0x0002027C02020200L, 0x0004047A04040400L, 0x0008087608080800L, 0x0010106E10101000L, 0x0020205E20202000L, 0x0040403E40404000L, 0x0080807E80808000L,
            0x00017E0101010100L, 0x00027C0202020200L, 0x00047A0404040400L, 0x0008760808080800L, 0x00106E1010101000L, 0x00205E2020202000L, 0x00403E4040404000L, 0x00807E8080808000L,
            0x007E010101010100L, 0x007C020202020200L, 0x007A040404040400L, 0x0076080808080800L, 0x006E101010101000L, 0x005E202020202000L, 0x003E404040404000L, 0x007E808080808000L,
            0x7E01010101010100L, 0x7C02020202020200L, 0x7A04040404040400L, 0x7608080808080800L, 0x6E10101010101000L, 0x5E20202020202000L, 0x3E40404040404000L, 0x7E80808080808000L
    };

    static {
        for (int i = 0; i < MagicBishopDb.length; i++)
            MagicBishopDb[i] = new long[MagicBishopDbLength];

        for (int i = 0; i < MagicRookDb.length; ++i)
            MagicRookDb[i] = new long[MagicRookDbLength];

        final int[] initMagicMovesDb = {
                63, 0, 58, 1, 59, 47, 53, 2,
                60, 39, 48, 27, 54, 33, 42, 3,
                61, 51, 37, 40, 49, 18, 28, 20,
                55, 30, 34, 11, 43, 14, 22, 4,
                62, 57, 46, 52, 38, 26, 32, 41,
                50, 36, 17, 19, 29, 10, 13, 21,
                56, 45, 25, 31, 35, 16, 9, 12,
                44, 24, 15, 8, 23, 7, 6, 5
        };

        final int[] squares = new int[64];

        int numSquares;

        for (int i = 0; i < squares.length; ++i) {
            numSquares = 0;
            long temp = MagicmovesBMask[i];
            while (temp != 0) {
                final long bit = (temp & -temp);
                squares[numSquares++] = initMagicMovesDb[(int) ((bit * 0x07EDD5E59A4E28C2L) >>> 58)];
                temp ^= bit;
            }

            for (temp = 0; temp < One << numSquares; ++temp) {
                final long tempocc = InitmagicmovesOcc(squares, numSquares, temp);
                MagicBishopDb[i][(int) ((tempocc * MagicmovesBMagics[i]) >>> 55)] = InitmagicmovesBmoves(i, tempocc);
            }
        }

        Arrays.fill(squares, 0);

        for (int i = 0; i < squares.length; ++i) {
            numSquares = 0;
            long temp = MagicmovesRMask[i];
            while (temp != 0) {
                final long bit = (temp & -temp);
                squares[numSquares++] = initMagicMovesDb[(int) ((bit * 0x07EDD5E59A4E28C2L) >>> 58)];
                temp ^= bit;
            }

            for (temp = 0; temp < One << numSquares; ++temp) {
                final long tempocc = InitmagicmovesOcc(squares, numSquares, temp);
                MagicRookDb[i][(int) ((tempocc * MagicmovesRMagics[i]) >>> 52)] = InitmagicmovesRmoves(i, tempocc);
            }
        }
    }

    public static long getBishopAttacks(final int square, final long occupied) {
        return MagicBishopDb[square][(int) (((occupied & MagicmovesBMask[square]) * MagicmovesBMagics[square]) >>> 55)];
    }

    public static long getRookAttacks(final int square, final long occupied) {
        return MagicRookDb[square][(int) (((occupied & MagicmovesRMask[square]) * MagicmovesRMagics[square]) >>> 52)];
    }

    public static long getQueenAttacks(final int square, final long occupied) {
        return getBishopAttacks(square, occupied) | getRookAttacks(square, occupied);
    }

    private static long InitmagicmovesOcc(final int[] squares, final int squareNumber, final long linocc) {
        long ret = 0L;
        for (int i = 0; i < squareNumber; ++i)
            if ((linocc & (One << i)) != 0)
                ret |= One << squares[i];

        return ret;
    }

    private static long InitmagicmovesRmoves(final int square, final long occ) {
        long ret = 0L;
        final long rowbits = Ff << (8 * (square / 8));

        long bit = One << square;
        do {
            bit <<= 8;
            ret |= bit;
        }
        while (bit > 0 && (bit & occ) == 0);

        bit = One << square;
        do {
            bit >>>= 8;
            ret |= bit;
        }
        while (bit != 0 && (bit & occ) == 0);

        bit = One << square;
        do {
            bit <<= 1;
            if ((bit & rowbits) != 0)
                ret |= bit;
            else
                break;
        }
        while ((bit & occ) == 0);

        bit = One << square;
        do {
            bit >>>= 1;
            if ((bit & rowbits) != 0)
                ret |= bit;
            else
                break;
        }
        while ((bit & occ) == 0);

        return ret;
    }

    private static long InitmagicmovesBmoves(final int square, final long occ) {
        long ret = 0L;
        final long rowbits = Ff << (8 * (square / 8));

        long bit = One << square;
        long bit2 = bit;
        do {
            bit <<= 8 - 1;
            bit2 >>>= 1;
            if ((bit2 & rowbits) != 0)
                ret |= bit;
            else
                break;
        }
        while (bit != 0 && (bit & occ) == 0);

        bit = One << square;
        bit2 = bit;
        do {
            bit <<= 8 + 1;
            bit2 <<= 1;
            if ((bit2 & rowbits) != 0)
                ret |= bit;
            else
                break;
        }
        while (bit != 0 && (bit & occ) == 0);

        bit = One << square;
        bit2 = bit;
        do {
            bit >>>= 8 - 1;
            bit2 <<= 1;
            if ((bit2 & rowbits) != 0)
                ret |= bit;
            else
                break;
        }
        while (bit != 0 && (bit & occ) == 0);

        bit = One << square;
        bit2 = bit;
        do {
            bit >>>= 8 + 1;
            bit2 >>= 1;
            if ((bit2 & rowbits) != 0)
                ret |= bit;
            else
                break;
        }
        while (bit > 0 && (bit & occ) == 0);

        return ret;
    }
    public static void printBinaryBoard(String binaryString, int rows, int cols) {
        binaryString = String.format("%64s", binaryString).replace(' ', '0');
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(binaryString.charAt(i * cols + j) + " ");
            }
            System.out.println();
        }
    }
    public static int popLSB(long[] b) {
        int i = Long.numberOfTrailingZeros(b[0]);
        b[0] &= (b[0] - 1);
        return i;
    }
    public static void main(String[] args) {
        // Example usage:
        long[] queenMoves = new long[]{getQueenAttacks(36, 100001100101000L)};
        int i = popLSB(queenMoves);

        System.out.println(i);
        System.out.println(Long.toBinaryString(queenMoves[0]));
        printBinaryBoard(Long.toBinaryString(queenMoves[0]), 8, 8);
    }
}
