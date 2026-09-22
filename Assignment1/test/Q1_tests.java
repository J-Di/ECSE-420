package Assignment1.test;

import ca.mcgill.ecse420.a1.MatrixMultiplication;

// This file contains all of the tests which were created to validate the implementationf for Q1
// DISCLAIMER: AI was used to help generate these test cases
public class Q1_tests {

    static double eps = 1e-6;

    // Function used to see if two matrices are equal manually
    private static boolean matricesEqual(double[][] x, double[][] y) {
    if (x.length != y.length) {
        return false;
    }

        // Check Size
        for (int i = 0; i < x.length; i++) {
            if (x[i].length != y[i].length) {
            return false;
            }
        //Then check value
        for (int j = 0; j < x[i].length; j++) {
            if (Math.abs(x[i][j] - y[i][j]) > eps) {
                return false;
            }
        }
    }
    return true;
    }

    // Runs one test case and prints result
    private static void check(String name, double[][] actual, double[][] expected) {
        boolean ok = matricesEqual(actual, expected);
        System.out.println((ok ? "PASS: " : "FAIL: ") + name);
        if (!ok) {
            System.out.println("  expected: " + java.util.Arrays.deepToString(expected));
            System.out.println("  actual:   " + java.util.Arrays.deepToString(actual));
        }
    }

    // Test cases for sequential solving only. These matrices were randomly generated, and solutions were
    // added from an online matrix solver:
    // https://www.wolframalpha.com/input/?i=matrix+multiplication+calculator
    public static void testSequential() {
        // Test 1: small hand-computed 2x2 case
        double[][] a1 = {{1, 2}, {3, 4}};
        double[][] b1 = {{5, 6}, {7, 8}};
        double[][] expected1 = {{19, 22}, {43, 50}};
        check("2x2 hand-computed", MatrixMultiplication.sequentialMultiplyMatrix(a1, b1), expected1);

        // Test 2: A x I = A
        double[][] a2 = {{2, -1, 0}, {4, 3, 5}, {-2, 7, 1}};
        double[][] identity = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        check("3x3 times identity", MatrixMultiplication.sequentialMultiplyMatrix(a2, identity), a2);

        // Test 3: non-square (2x3) x (3x2) -> 2x2
        double[][] a3 = {{1, 2, 3}, {4, 5, 6}};
        double[][] b3 = {{7, 8}, {9, 10}, {11, 12}};
        double[][] expected3 = {{58, 64}, {139, 154}};
        check("2x3 times 3x2 (non-square)", MatrixMultiplication.sequentialMultiplyMatrix(a3, b3), expected3);

        // Test 4: 1x1, the smallest possible case, with a negative result
        double[][] a4 = {{7}};
        double[][] b4 = {{-3}};
        double[][] expected4 = {{-21}};
        check("1x1 smallest case", MatrixMultiplication.sequentialMultiplyMatrix(a4, b4), expected4);

        // Test 5: 4x4 square with negatives and zeros
        double[][] a5 = {{1, -2, 3, 0}, {4, 5, -1, 2}, {0, 3, 2, -4}, {-3, 1, 0, 6}};
        double[][] b5 = {{2, 0, -1, 3}, {1, 4, 2, -2}, {-3, 1, 0, 5}, {0, -2, 3, 1}};
        double[][] expected5 = {
        {-9, -5, -5, 22},
        {16, 15, 12, -1},
        {-3, 22, -6, 0},
        {-5, -8, 23, -5}
        };
        check("4x4 with negatives and zeros",
            MatrixMultiplication.sequentialMultiplyMatrix(a5, b5), expected5);

        // Test 6: non-square (3x4) x (4x2) -> 3x2, shrinking in both dimensions
        double[][] a6 = {{2, -1, 0, 3}, {1, 4, -2, 5}, {-3, 0, 6, 1}};
        double[][] b6 = {{1, 2}, {-4, 0}, {3, -1}, {2, 5}};
        double[][] expected6 = {{12, 19}, {-11, 29}, {17, -7}};
        check("3x4 times 4x2 (non-square)",
            MatrixMultiplication.sequentialMultiplyMatrix(a6, b6), expected6);

        // Test 7: (1x5) x (5x1) -> 1x1, an inner product; a single entry from many terms
        double[][] a7 = {{3, -1, 4, 1, -5}};
        double[][] b7 = {{2}, {6}, {-5}, {3}, {5}};
        double[][] expected7 = {{-42}};
        check("1x5 times 5x1 (inner product)",
            MatrixMultiplication.sequentialMultiplyMatrix(a7, b7), expected7);

        // Test 8: (4x1) x (1x3) -> 4x3, an outer product; many entries from one term each
        double[][] a8 = {{1}, {-2}, {3}, {4}};
        double[][] b8 = {{5, -6, 7}};
        double[][] expected8 = {{5, -6, 7}, {-10, 12, -14}, {15, -18, 21}, {20, -24, 28}};
        check("4x1 times 1x3 (outer product)",
            MatrixMultiplication.sequentialMultiplyMatrix(a8, b8), expected8);

        // Test 9: 5x5 square, the largest hand-checked case
        double[][] a9 = {
        {2, 0, 1, -3, 4},
        {-1, 5, 2, 0, 3},
        {4, -2, 0, 1, -1},
        {3, 1, -4, 2, 0},
        {0, 2, 3, -1, 5}
        };
        double[][] b9 = {
        {1, -2, 0, 3, 1},
        {4, 0, -1, 2, -3},
        {0, 3, 2, -1, 5},
        {-2, 1, 4, 0, 2},
        {3, -1, 1, 2, 0}
        };
        double[][] expected9 = {
        {20, -8, -6, 13, 1},
        {28, 5, 2, 11, -6},
        {-9, -6, 5, 6, 12},
        {3, -16, -1, 15, -16},
        {25, 3, 5, 11, 7}
        };
        check("5x5 square", MatrixMultiplication.sequentialMultiplyMatrix(a9, b9), expected9);

        // Test 10: 3x3 with fractional values, to confirm double arithmetic (not integer)
        double[][] a10 = {{0.5, -1.25, 2}, {3.5, 0, -0.75}, {-2, 1.5, 0.25}};
        double[][] b10 = {{4, -0.5, 1}, {2, 3, -2}, {-1, 0.5, 8}};
        double[][] expected10 = {
        {-2.5, -3.0, 19.0},
        {14.75, -2.125, -2.5},
        {-5.25, 5.625, -3.0}
        };
        check("3x3 fractional values", MatrixMultiplication.sequentialMultiplyMatrix(a10, b10), expected10);
    }

    // Test cases for parallel solving only
    public static void testParallel() {
            // Test 1: small hand-computed 2x2 case
        double[][] a1 = {{1, 2}, {3, 4}};
        double[][] b1 = {{5, 6}, {7, 8}};
        double[][] expected1 = {{19, 22}, {43, 50}};
        check("2x2 hand-computed", MatrixMultiplication.parallelMultiplyMatrix(a1, b1), expected1);

        // Test 2: A x I = A
        double[][] a2 = {{2, -1, 0}, {4, 3, 5}, {-2, 7, 1}};
        double[][] identity = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        check("3x3 times identity", MatrixMultiplication.parallelMultiplyMatrix(a2, identity), a2);

        // Test 3: non-square (2x3) x (3x2) -> 2x2
        double[][] a3 = {{1, 2, 3}, {4, 5, 6}};
        double[][] b3 = {{7, 8}, {9, 10}, {11, 12}};
        double[][] expected3 = {{58, 64}, {139, 154}};
        check("2x3 times 3x2 (non-square)", MatrixMultiplication.parallelMultiplyMatrix(a3, b3), expected3);

        // Test 4: 1x1, the smallest possible case, with a negative result
        double[][] a4 = {{7}};
        double[][] b4 = {{-3}};
        double[][] expected4 = {{-21}};
        check("1x1 smallest case", MatrixMultiplication.parallelMultiplyMatrix(a4, b4), expected4);

        // Test 5: 4x4 square with negatives and zeros
        double[][] a5 = {{1, -2, 3, 0}, {4, 5, -1, 2}, {0, 3, 2, -4}, {-3, 1, 0, 6}};
        double[][] b5 = {{2, 0, -1, 3}, {1, 4, 2, -2}, {-3, 1, 0, 5}, {0, -2, 3, 1}};
        double[][] expected5 = {
        {-9, -5, -5, 22},
        {16, 15, 12, -1},
        {-3, 22, -6, 0},
        {-5, -8, 23, -5}
        };
        check("4x4 with negatives and zeros",
            MatrixMultiplication.parallelMultiplyMatrix(a5, b5), expected5);

        // Test 6: non-square (3x4) x (4x2) -> 3x2, shrinking in both dimensions
        double[][] a6 = {{2, -1, 0, 3}, {1, 4, -2, 5}, {-3, 0, 6, 1}};
        double[][] b6 = {{1, 2}, {-4, 0}, {3, -1}, {2, 5}};
        double[][] expected6 = {{12, 19}, {-11, 29}, {17, -7}};
        check("3x4 times 4x2 (non-square)",
            MatrixMultiplication.parallelMultiplyMatrix(a6, b6), expected6);

        // Test 7: (1x5) x (5x1) -> 1x1, an inner product; a single entry from many terms
        double[][] a7 = {{3, -1, 4, 1, -5}};
        double[][] b7 = {{2}, {6}, {-5}, {3}, {5}};
        double[][] expected7 = {{-42}};
        check("1x5 times 5x1 (inner product)",
            MatrixMultiplication.parallelMultiplyMatrix(a7, b7), expected7);

        // Test 8: (4x1) x (1x3) -> 4x3, an outer product; many entries from one term each
        double[][] a8 = {{1}, {-2}, {3}, {4}};
        double[][] b8 = {{5, -6, 7}};
        double[][] expected8 = {{5, -6, 7}, {-10, 12, -14}, {15, -18, 21}, {20, -24, 28}};
        check("4x1 times 1x3 (outer product)",
            MatrixMultiplication.parallelMultiplyMatrix(a8, b8), expected8);

        // Test 9: 5x5 square, the largest hand-checked case
        double[][] a9 = {
        {2, 0, 1, -3, 4},
        {-1, 5, 2, 0, 3},
        {4, -2, 0, 1, -1},
        {3, 1, -4, 2, 0},
        {0, 2, 3, -1, 5}
        };
        double[][] b9 = {
        {1, -2, 0, 3, 1},
        {4, 0, -1, 2, -3},
        {0, 3, 2, -1, 5},
        {-2, 1, 4, 0, 2},
        {3, -1, 1, 2, 0}
        };
        double[][] expected9 = {
        {20, -8, -6, 13, 1},
        {28, 5, 2, 11, -6},
        {-9, -6, 5, 6, 12},
        {3, -16, -1, 15, -16},
        {25, 3, 5, 11, 7}
        };
        check("5x5 square", MatrixMultiplication.parallelMultiplyMatrix(a9, b9), expected9);

        // Test 10: 3x3 with fractional values, to confirm double arithmetic (not integer)
        double[][] a10 = {{0.5, -1.25, 2}, {3.5, 0, -0.75}, {-2, 1.5, 0.25}};
        double[][] b10 = {{4, -0.5, 1}, {2, 3, -2}, {-1, 0.5, 8}};
        double[][] expected10 = {
        {-2.5, -3.0, 19.0},
        {14.75, -2.125, -2.5},
        {-5.25, 5.625, -3.0}
        };
        check("3x3 fractional values", MatrixMultiplication.parallelMultiplyMatrix(a10, b10), expected10);
    }

    
    
}
