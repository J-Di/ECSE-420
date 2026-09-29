package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ca.mcgill.ecse420.a1.test.Q1_tests;

public class MatrixMultiplication {
	
	private static final int NUMBER_THREADS = 24; // Value that minimizes execution time
	private static final int MATRIX_SIZE = 3; // For testing

	public static void main(String[] args) {
		
		//  Different tests can be commented out / uncommented and ran below:
		
		// Direct testing method for manual testing:
		// runBasicTest();

		// Tests from the test suite in the test directory:
		// System.out.println("Test Suite Tests\n");
		// Q1_tests.testSequential();
		// Q1_tests.testParallel();

		// Sweeping tests for 1.4 and 1.5:
		//System.out.println("Sweeping Tests Start\n");
		// runThreadSweep(4000);
		// runSizeSweep(NUMBER_THREADS);
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
		int numColA = a[0].length;
		int numRowB = b.length;
		int numColB = b[0].length;

		if (numColA != numRowB){
			throw new IllegalArgumentException("The provided arrays cannot be multiplied");
		}

		// Create result matrix
		double [][] result = new double[numRowA][numColB];

		// Now multiply sequentially, going through rows of A and cols of B
		for (int i = 0; i < numRowA; i++){	
			for (int j = 0; j < numColB; j++){
				for (int k = 0; k < numColA; k++){
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
		int numColA = a[0].length;
		int numColB = b[0].length;

		// Do single row matrix multiplication
		for (int j = 0; j < numColB; j++) {
			for (int k = 0; k < numColA; k++){
				result[row][j] += a[row][k] * b[k][j];
			}
		}
	}


	/**
	 * Returns the result of a concurrent matrix multiplication
	 * The two matrices are randomly generated
	 * @param a is the first matrix
	 * @param b is the second matrix
	 * @param numThreads is the number of threads allowable for the execution
	 * @return the result of the multiplication
	 * */
	public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b, int numThreads) {
		// First get the rows / columns and verify if multiplication is valid
		int numRowA = a.length;
		int numColA = a[0].length;
		int numRowB = b.length;
		int numColB = b[0].length;

		if (numColA != numRowB){
			throw new IllegalArgumentException("The provided arrays cannot be multiplied");
		}

		// Create result matrix
		double [][] result = new double[numRowA][numColB];

		// Now solve in parallel, where the strategy used here is to create threads for each row of 
		// the matrix, and have each compute their respective row and come together to form the solved matrix

		//Create thread pool
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
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
	 * Returns the result of a concurrent matrix multiplication
	 * Funtion takes advantage of overloading to make questions 1.3-1.6 neater
	 * The two matrices are randomly generated
	 * @param a is the first matrix
	 * @param b is the second matrix
	 * @return the result of the multiplication
	 * */
	public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {
    	return parallelMultiplyMatrix(a, b, NUMBER_THREADS);
 	}

	
	/**
	 * Question 1.3 from the assignment is here:
	 * Returns a matrix of size 1x2 containing run times for sequential and parallel matrix multiplication methods respectively
	 * @param numRowA number of rows in A
	 * @param numColA number of columns in A, and therefore rows in B
	 * @param numRowB number of rows in B
	 * @param numColB number of columns in B
	 * @param numThreads number of threads
	 * @param compileFirst If the two solvers must be ran first to compile, to make timings more accurate and comparable
	 * @return times
	 */
	public static double [] getRunTimes (int numRowA, int numColA, int numRowB, int numColB, int numThreads, boolean compileFirst){

		// Input Check
		if (numColA != numRowB){
			throw new IllegalArgumentException("The provided arrays cannot be multiplied");
		}

		// Construct random matrices A and B for the test
		double[][] a = generateRandomMatrix(numRowA, numColA);
		double[][] b = generateRandomMatrix(numRowB, numColB);

		double [] times = new double[2]; // matrice to store times


		// Compile functions before timing to eliminate bias on first run
		if (compileFirst){
			for (int i = 0; i < 2; i++) {
				sequentialMultiplyMatrix(a, b);
				parallelMultiplyMatrix(a, b, numThreads);
			}
		}

		// Record the length of time for sequential
		long start = System.nanoTime();
		MatrixMultiplication.sequentialMultiplyMatrix(a, b);
		double deltaMs = (System.nanoTime() - start) / 1_000_000.0;
		times[0] = deltaMs;

		// Record length for parallel
		start = System.nanoTime();
		MatrixMultiplication.parallelMultiplyMatrix(a, b, numThreads);
		deltaMs = (System.nanoTime() - start) / 1_000_000.0;
		times[1] = deltaMs;

		return times;
	}


	/**
	 * 	Overloaded function above, so that for multiple consecutive runs it does not take as long
	 * Returns a matrix of size 1x2 containing run times for sequential and parallel matrix multiplication methods respectively
	 * @param numRows number of rows
	 * @param numCols number of cols
	 * @param numThreads number of threads
	 * @param compileFirst If the two solvers must be ran first to compile, to make timings more accurate and comparable
	 * @return times
	 */
	public static double[] getRunTimes(int numRowA, int numColA, int numRowB, int numColB, int numThreads) {
		return getRunTimes(numRowA, numColA, numRowB, numColB, numThreads, true);
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
	
	// Print function to visualize the matrices
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
		System.out.println("\n");
	}	
	
	/**
	 * Function to print out data for question 1.4
	 * It is only designed for square matrices
	 * @param numThreads the tmatrix sized to be used in each test
	 */
	public static void runThreadSweep(int matrixSize) {
		// Create an array of thread counts for trials, and corresponsing results
		int[] threadCounts = {1,1,2,3,4,6,8,10,12,16,18,24,32};
		double[][] results = new double[threadCounts.length][2];

		// print number of cores on the device being used for this questiom
		System.out.println("Logical cores available: " + Runtime.getRuntime().availableProcessors());

		// Compile step once
		getRunTimes(200, 200, 200, 200,1 );

		//  table
		System.out.printf("%-10s %16s %16s %10s%n","Threads", "Sequential (ms)", "Parallel (ms)", "Speedup");

		// loup to get information
		for (int i = 0; i < threadCounts.length; i++) {
			results[i] = getRunTimes(matrixSize, matrixSize, matrixSize, matrixSize, threadCounts[i], false);
			double speedup = results[i][0] / results[i][1];
			System.out.printf("%-10d %16.2f %16.2f %9.3fx%n", threadCounts[i], results[i][0], results[i][1], speedup);
		}
	}

	/**
	 * Runs the matrix-size sweep for question 1.5 and prints results as a table and as CSV.
	 * @param numThreads the thread count that minimised parallel execution time in 1.4
	 */
		public static void runSizeSweep(int numThreads) {
			int[] sizes = {100, 200, 500, 1000, 2000, 3000, 4000};
			double[][] results = new double[sizes.length][2];

			System.out.println("Logical cores available: " + Runtime.getRuntime().availableProcessors());

			// Compile step once
			 getRunTimes(200, 200, 200, 200, numThreads);

			// table
			System.out.printf("%-10s %16s %16s %10s%n", "Size", "Sequential (ms)", "Parallel (ms)", "Speedup");
			for (int i = 0; i < sizes.length; i++) {
				results[i] = getRunTimes(sizes[i], sizes[i], sizes[i], sizes[i], numThreads, false);
				System.out.printf("%-10d %16.2f %16.2f %9.3fx%n", sizes[i], results[i][0], results[i][1], results[i][0] / results[i][1]);
			}
		}

		// Runs a basic test that creates random matrices a and b, and computes sequential and parallel 
		// solver results, printing for manual inspection
		public static void runBasicTest() {
			System.out.println("Manual Tests\n");
			double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
			double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
			System.out.println("Matrix A: \n");
			printMatrix(a);
			System.out.println("Matrix B: \n");
			printMatrix(b);
			System.out.println("Sequential Solution: \n");
			printMatrix(sequentialMultiplyMatrix(a, b));
			System.out.println("Parallel Solution: \n");
			printMatrix(parallelMultiplyMatrix(a, b));
		}

}
