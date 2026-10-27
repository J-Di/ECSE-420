package ca.mcgill.ecse420.a2.Q1;


import java.util.concurrent.atomic.AtomicInteger;

// Test whetehr the filter lock provides n-thread mutual exclusion
public class TestFilterLock {

    private static final int ITERATIONS = 400; // critical sections per thread
    private static final int TRIALS = 3;       // Repeated runs per n

    // Variables needed for test
    private static final AtomicInteger presense = new AtomicInteger(0);
    private static final AtomicInteger violations = new AtomicInteger(0);
    private static volatile int maxPresense = 0;
    private static volatile long counter = 0;

    //Runs the test for n = 2 to 8 on the Filter lock
    public static void main(String[] args) throws InterruptedException {
        // Run the test for thread numbers for 2 to 8
        System.out.println("FilterLock tests");
        for (int n = 2; n <= 8; n++) {
            runTrials(n, true);
        }
    }

    private static void runTrials(int n, boolean correctLock) throws InterruptedException {

        // Test tracking variables
        int totalViolations = 0;
        int worstPresense = 0;
        int badCounts = 0;

        // For each iteration of N
        for (int t = 0; t < TRIALS; t++) {
            // create new filterlock to be tested, and run a trial
            FilterLock lock = new FilterLock(n);
            runOnce(lock, n);

            // Get metrics back after test
            totalViolations += violations.get();
            worstPresense = Math.max(worstPresense, maxPresense);
            if (counter != (long) n * ITERATIONS) {
                badCounts++;
            }
        }
        // Tpass or fail criteria
        boolean pass = totalViolations == 0 && badCounts == 0;
        System.out.printf(
            "n=%d  trials=%d  violations=%d  max threads in CS=%d  wrong counts=%d/%d  -> %s%n",
            n, TRIALS, totalViolations, worstPresense, badCounts, TRIALS, pass ? "PASS" : "FAIL");
    }

    // Starts n threads that each enter the critical section ITERATIONS times
    private static void runOnce(Lock lock, int n) throws InterruptedException {
        // reset class test metrics for each new test
        presense.set(0);
        violations.set(0);
        maxPresense = 0;
        counter = 0;

        // Code that each thread will run, trying to get inside the critical section
        Runnable task = () -> {
        for (int k = 0; k < ITERATIONS; k++) {
            lock.lock();
            try {
                criticalSection();
            } 
            finally {
            lock.unlock();  // release no matter what 
            }
        }
        };

        // launch threads
        IdThread[] threads = new IdThread[n];
        for (int id = 0; id < n; id++) {
            threads[id] = new IdThread(id, task);
        }
        for (IdThread thread : threads) {
            thread.start();
        }
        for (IdThread thread : threads) {
            thread.join();
        }
    }

    // The critical section, which record presense and do a non-atomic counter++
    private static void criticalSection() {
        int inside = presense.incrementAndGet(); // set to 1 and return

        // if there are two threads at the same time, mark the violation
        if (inside > 1) {
        violations.incrementAndGet();
        }

        // Record the max number of threads at the same time in the critical section
        if (inside > maxPresense) {
            maxPresense = inside;
        }

        // increment the counter to know the amount of times the critical section was entered
        long temp = counter;  
        Thread.yield(); // widen the window so overlapping threads interleave here
        counter = temp + 1;   
        presense.decrementAndGet();
    }
}
