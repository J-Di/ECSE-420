package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Assignment1.test.Q1_tests;

public class MatrixMultiplication {
	
	private static final int NUMBER_THREADS = 1;
	private static final int MATRIX_SIZE = 3;

	public static void main(String[] args) {
		
		// Direct tests
		
		// Generate two random matrices, same size
		double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
		double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
		// sequentialMultiplyMatrix(a, b);
		// parallelMultiplyMatrix(a, b);

		// printMatrix(a);
		// System.out.println("\n");
		// printMatrix(b);
		// System.out.println("\n");
		// printMatrix(parallelMultiplyMatrix(a, b));
		
		// Testing using test suite

		Q1_tests.testSequential();
		Q1_tests.testParallel();
	}
	
	/**
	 * Returns the result of a sequential matrix multiplication
	 * The two matrices are randomly generated
	 * @param a is the first matrix
	 * @param b is the second matrix
	 * @return the result of the multiplication
	 * */
	public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {	

		// First get the rows / columns and verify if multiplication is valid
		int numRowA = a.length;
		int numColumnA = a[0].length;
		int numRowB = b.length;
		int numColumnB = b[0].length;

		if (numColumnA != numRowB){
			throw new IllegalArgumentException("The provided arrays cannot be multiplied");
		}

		// Create result matrix
		double [][] result = new double[numRowA][numColumnB];

		// Now multiply sequentially, going through rows of A and cols of B
		for (int i = 0; i < numRowA; i++){	
			for (int j = 0; j < numColumnB; j++){
				for (int k = 0; k < numColumnA; k++){
					// Calculate dot product of the row
					result[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return result;
	}
	
	// Runnable function that each seperate thread will do (matrix multiplying one row)
	// This function is used in the below parallel solving strategy
	private static void computeRowSum(double[][] a, double [][] b, double[][] result, int row) {

		// Declare var for the row sum
		int numColumnA = a[0].length;
		int numColumnB = b[0].length;

		// Do single row matrix multiplication
		for (int j = 0; j < numColumnB; j++) {
			for (int k = 0; k < numColumnA; k++){
				result[row][j] += a[row][k] * b[k][j];
			}
		}
	}

	/**
	 * Returns the result of a concurrent matrix multiplication
	 * The two matrices are randomly generated
	 * @param a is the first matrix
	 * @param b is the second matrix
	 * @return the result of the multiplication
	 * */
	public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {
		// First get the rows / columns and verify if multiplication is valid
		int numRowA = a.length;
		int numColumnA = a[0].length;
		int numRowB = b.length;
		int numColumnB = b[0].length;

		if (numColumnA != numRowB){
			throw new IllegalArgumentException("The provided arrays cannot be multiplied");
		}

		// Create result matrix
		double [][] result = new double[numRowA][numColumnB];

		// Now solve in parallel, where the strategy used here is to create threads for each row of 
		// the matrix, and have each compute their respective row and come together to form the solved matrix

		//Create thread pool
		ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
		// Create threads and have them each solve their respective row
		for (int i = 0; i < numRowA; i++) {
			final int row = i;
			executor.execute(() -> computeRowSum(a, b, result, row));
		}

		 // Stop accepting tasks
		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.HOURS); // wait for all tasks to finish, with timeout
		} catch (InterruptedException e) {
			executor.shutdownNow();             // Stop any tasks still running
			throw new IllegalStateException("Matrix multiplication was interrupted", e);
		}

		return result;
	}

        /**
         * Populates a matrix of given size with randomly generated integers between 0-10.
         * @param numRows number of rows
         * @param numCols number of cols
         * @return matrix
         */
	private static double[][] generateRandomMatrix (int numRows, int numCols) {
             double matrix[][] = new double[numRows][numCols];
        for (int row = 0 ; row < numRows ; row++ ) {
            for (int col = 0 ; col < numCols ; col++ ) {
                matrix[row][col] = (double) ((int) (Math.random() * 10.0));
            }
        }
        return matrix;
    }
	
	// AI used to create this print function to visualize the matrices
	public static void printMatrix(double[][] matrix) { 
		// Edge case check
		if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
			System.out.println("[] (Empty or null matrix)");
			return;
		}

		// Loop through every row and column
		for (int i = 0; i < matrix.length; i++) {
			System.out.print("[ "); // Left border 
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.printf("%8.2f ", matrix[i][j]);
			}
			System.out.println(" ]"); // end border
		}
	}
}
