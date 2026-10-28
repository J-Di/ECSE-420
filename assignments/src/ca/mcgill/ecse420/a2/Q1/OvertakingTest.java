package ca.mcgill.ecse420.a2.Q1;

// Observes overtaking in the Filter lock with n = 8 threads (Question 1.4)
public class OvertakingTest {

    // Initialize test parameters
    private static final int N = 8;
    private static final int ITERATIONS = 300; // lock() calls per thread
    private static final int WORK = 3000; // size of the light load done inside the critical section
    private static long sink = 0; // Stores the result of the light load so the compiler doesnt optimize


    // Runs n threads through the tracked lock and prints overtaking statistics from lock implementation
    public static void main(String[] args) throws InterruptedException {

        // Initialize thread tasks to try and join a critical section ITERATIONS times
        FilterLock lock = new FilterLock(N, true, ITERATIONS);

        Runnable task = () -> {
        for (int k = 0; k < ITERATIONS; k++) {
            lock.lock();
            try {
                // Light load, just a few small operations so each thread holds the lock briefly
                // and the others have time to line up behind it
                long sum = 0;
                for (int w = 0; w < WORK; w++) {
                    sum += w * k;
                }
                sink += sum;
            } finally {
                lock.unlock();
            }
        }
        };

        // Create the threads and run them
        IdThread[] threads = new IdThread[N];
        for (int id = 0; id < N; id++) {
            threads[id] = new IdThread(id, task);
        }
        for (IdThread thread : threads) {
            thread.start();
        }
        for (IdThread thread : threads) {
            thread.join();
        }

        // Display the results of the tests 
        System.out.printf("Filter lock, n=%d, %d lock() calls per thread%n%n", N, ITERATIONS);
        System.out.println("Level   visits overtaken   max times one visit overtaken");

        for (int lvl = 1; lvl < N; lvl++) {
            reportLevel(lock, lvl, lvl, "  " + lvl + "  ");
        }

        System.out.println();
        System.out.println("Whole lock (doorway = arrive at level 1, CS = pass level n-1):");
        reportLevel(lock, 1, N - 1, " CS  ");
    }

    // Counts, for each visit, how many visits by other threads started at level
    // startLvl after it but finished level endLvl before it.
    private static void reportLevel(FilterLock lock, int startLvl, int endLvl, String label) {

        // initialize parameters for each run
        int overtakenVisits = 0;
        int maxOvertakes = 0;
        int total = N * ITERATIONS;

        //extract number of overtakes going through all of the threadds
        for (int a = 0; a < N; a++) {
            for (int va = 0; va < ITERATIONS; va++) { // for each iteration
                int count = 0;
                for (int b = 0; b < N; b++) {
                    // Skip the case where comparing against itself 
                    if (b == a) {
                        continue;
                    }
                    for (int vb = 0; vb < ITERATIONS; vb++) {
                        // direct comparison here to check if either was started before, then 
                        // overtaken by the other thread
                        if (lock.arrivalTicket(b, vb, startLvl) > lock.arrivalTicket(a, va, startLvl)
                            && lock.passTicket(b, vb, endLvl) < lock.passTicket(a, va, endLvl)) {
                             count++;
                        }
                    }
                }   
                // count the overtake
                if (count > 0) {
                    overtakenVisits++;
                }
                maxOvertakes = Math.max(maxOvertakes, count);
            }   
        }
        // Print out results
        System.out.printf("%s | %5d / %d (%4.1f%%) | %d%n", label, overtakenVisits, total,
            100.0 * overtakenVisits / total, maxOvertakes);
    }
}
