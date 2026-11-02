package ca.mcgill.ecse420.a2.Q1;

// Observes overtaking in the Filter lock with n = 8 threads (Question 1.4)
public class OvertakingTest {

    // Initialize test parameters
    private static final int N = 8;
    private static final int ITERATIONS = 300; // lock() calls per thread
    private static final int WORK = 3000; // size of the light load done inside the critical section


    // Runs n threads through the tracked lock and prints overtaking statistics from lock implementation
    public static void main(String[] args) throws InterruptedException {

        // Initialize thread tasks to try and join a critical section ITERATIONS times
        FilterLock lock = new FilterLock(N, true, ITERATIONS);
        runThreads (lock);

        // Display the results of the tests 
        System.out.printf("Filter lock, n=%d, %d lock() calls per thread%n%n", N, ITERATIONS);
        System.out.println("Level   visits overtaken   max times one visit overtaken");

        for (int lvl = 1; lvl < N; lvl++) {
            reportLevel(lock, lvl, lvl, "  " + lvl + "  ");
        }

        System.out.println("\nWhole lock (doorway = arrive at level 1, CS = pass level n-1):");
        reportLevel(lock, 1, N - 1, " CS  ");

        // Initialize the Bakery lock, and perform similar test as done with FilterLock
        BakeryLock lock2 = new BakeryLock(N, true, ITERATIONS);
        runThreads (lock2);

        // Display the results of the tests 
        System.out.println("Level   visits overtaken   max times one visit overtaken \n");
        System.out.println("Whole lock (doorway = arrive at level 1, CS = pass level n-1):");
        reportBakery(lock2);
    }


    // Runs N threads that each call lock() ITERATIONS times 
    private static void runThreads(Lock lock) throws InterruptedException {
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
    }


    // Counts, for each visit, how many visits by other threads started at level
    // startLvl after it but finished level endLvl before it.
    // This only works for FilterLock type as it calls functions specific to that calss
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

    // Counts, for each visit, how many visits by other threads started their doorway after it
    // finished its doorway, but got past the wait loop before it
    // This only works for Bakery Lock implementation, as it calls functions specific to this class
    private static void reportBakery(BakeryLock lock) {
        // statistic variables
        int overtakenVisits = 0;
        int maxOvertakes = 0;
        int overlapReorders = 0;
        int total = N * ITERATIONS;

        // Iterate through object with overtaking enabled, and search for overtaking events
        // Similar to what was done for previous lock
        for (int a = 0; a < N; a++) {
            for (int va = 0; va < ITERATIONS; va++) {
                int count = 0;
                for (int b = 0; b < N; b++) {
                    if (b == a) {
                        continue;
                    }
                    for (int vb = 0; vb < ITERATIONS; vb++) {
                        // Check which passed first, and which actually finished first
                        boolean passedFirst = lock.passTicket(b, vb) < lock.passTicket(a, va);
                        if (passedFirst && lock.startTicket(b, vb) > lock.arrivalTicket(a, va)) {
                            count++; // B started after A's doorway finished, yet went first
                        } else if (passedFirst
                            && lock.arrivalTicket(b, vb) > lock.arrivalTicket(a, va)) {
                            overlapReorders++; // doorways overlapped, so either order is allowed
                        }
                    }
                }
                if (count > 0) {
                    overtakenVisits++;
                }
                maxOvertakes = Math.max(maxOvertakes, count);
            }
        }
        // printt out the results
        System.out.printf("Bakery lock: %d / %d visits overtaken (%4.1f%%), max %d%n",
            overtakenVisits, total, 100.0 * overtakenVisits / total, maxOvertakes);
        System.out.printf("Reorders with overlapping doorways (allowed): %d%n", overlapReorders);
    }
}
