package ca.mcgill.ecse420.a3.Q2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


// Test class used to test the contains() method of a FineFrainedList class
// Contains smaller tests which are called in sequence to validate the methods implementation 
public class FineGrainedListTest {

    private static int THREADS = 8; // Number of threads used in testing
    private static int TIMEOUT = 60; // timeout used in s

    // Run all of the smaller tests for a complete verification of the contains() method
    public static void main(String[] args) throws Exception {
        basicTests();
        System.out.println("Sequential tests passed");
        concurrentAddsThenContains();
        System.out.println("Concurrent add + contains test passed");
        containsWithChangingList();
        System.out.println("Contains-under-churn test passed");
        ownedKeysTest();
        System.out.println("Owned-keys test passed");
        System.out.println("All tests passed!");
    }


    // Check function which is used to have print messages for test conditions
    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }


    // Method used to run all tasks concurrently with a timeout to detect deadlocks or other failures
    private static void runAll(List<Callable<Void>> tasks) throws Exception {
    // Create thread pool
    ExecutorService pool = Executors.newFixedThreadPool(tasks.size());

    try {
        List<Future<Void>> futures = pool.invokeAll(tasks, TIMEOUT, TimeUnit.SECONDS);
        for (Future<Void> f : futures) {
        f.get(); // CancellationException if timed out, ExecutionException if a check failed
        }
    } finally {
        pool.shutdownNow();
    }
    }


    // This test is used to test basic functionality of the FineGrainedList functions, in a
    // single thread being used environment
    private static void basicTests(){
        // Create a FineGrainedList from implemented class
        FineGrainedList<Integer> list = new FineGrainedList<>();

        // Test an empty list, which should return a error
        check(!list.contains(10), "Empty list should not contain 10");

        // Randomy add numbers to the list
        check(list.add(10), "Issue! Could not add 10");
        check(list.add(20), "Issue! Could not add 20");
        check(list.add(30), "Issue! Could not add 30");

        // Check if those numbers exist, they all should
        check(list.contains(10), "Issue! Should contain 10");
        check(list.contains(20), "Issue! Should contain 20");
        check(list.contains(30), "Issue! Should contain 30");

        // Test more numbers that do not exist
        check(!list.contains(5), "Issue! Should not contain 5");
        check(!list.contains(15), "Issue! Should not contain 15");
        check(!list.contains(35), "Issue! Should not contain 35");

        // Test contains after removing an item
        check(list.remove(20), "Issue! Could not remove 20");
        check(!list.contains(20), "Issue! Should not contain 20 after removal");
    }


    // Threads add nodes concurrently, and then all must be found with contains
    private static void concurrentAddsThenContains() throws Exception {
        // assign number of additions per thread, and create list and task array
        final int perThread = 500;
        FineGrainedList<Integer> list = new FineGrainedList<>();
        List<Callable<Void>> tasks = new ArrayList<>();

        // Create addition tasks for each thread
        for (int t = 0; t < THREADS; t++) {
            final int id = t;
            tasks.add(() -> {
                for (int j = 0; j < perThread; j++) {
                    check(list.add(id + THREADS * j), "Concurrent add failed");
                }
                return null;
            });
        }
        runAll(tasks); // concurrently add to the list
        
        // Check if list contains all the elements added
        for (int k = 0; k < THREADS * perThread; k++) {
            check(list.contains(k), "Lost elements!");
        }
        check(!list.contains(THREADS * perThread), "Unexpected element");
    }


    // This test involves using the contains method on existing elements which are not removed, 
    // in an environment where elements around are being added and removed at the same time
    private static void containsWithChangingList() throws Exception {
        // Number of additions and list declaration 
        final int range = 400;
        FineGrainedList<Integer> list = new FineGrainedList<>();

        // populate list with keys that should never be removed
        for (int k = 0; k < range; k += 2) {
            list.add(k);
        }

        // Create task array
        List<Callable<Void>> tasks = new ArrayList<>();
        
        // create threads which will add and remove odd numbers remove from the list
        for (int t = 0; t < THREADS / 2; t++) { 
            final long seed = t;
            tasks.add(() -> {
                // Add and remove odd value random items
                Random rnd = new Random(seed);

                for (int i = 0; i < 50000; i++) {
                    int odd = rnd.nextInt(range / 2) * 2 + 1;
                    if (rnd.nextBoolean()) {
                        list.add(odd);
                    } else {
                        list.remove(odd);
                    }
                    }
                return null;
            });
        }
        // Create threads which will call contains on even numbers
        for (int t = 0; t < THREADS / 2; t++) { // readers
            final long seed = 100 + t;
            tasks.add(() -> {
                Random rnd = new Random(seed);
                for (int i = 0; i < 50000; i++) {
                    int even = rnd.nextInt(range / 2) * 2;
                    check(list.contains(even), "Stable element missing!");
                }
                return null;
            });
        }
        runAll(tasks);
    }


    // Each thread has a keys corresponding to its id and mixes add/remove/contains
    // on them, so every return value can be checked against a baseline
    private static void ownedKeysTest() throws Exception {
        // test parameters and list initialization
        final int keysPerThread = 1000;
        FineGrainedList<Integer> list = new FineGrainedList<>();
        List<Callable<Void>> tasks = new ArrayList<>();

        // each thread will add, check contains, and remove their own set of interleaved keys
        for (int t = 0; t < THREADS; t++) {
            final int id = t;
            tasks.add(() -> {
                // add every key this thread owns and check that contains() finds it
                for (int j = 0; j < keysPerThread; j++) {
                    int key = id + THREADS * j;
                    check(list.add(key), "add(" + key + ") should succeed");
                    check(list.contains(key), "contains(" + key + ") should be true after add");
                    check(!list.add(key), "adding " + key + " a second time should fail");
                }
                // remove every second key and check that contains() no longer finds it
                for (int j = 1; j < keysPerThread; j += 2) {
                    int key = id + THREADS * j;
                    check(list.remove(key), "remove(" + key + ") should succeed");
                    check(!list.contains(key), "contains(" + key + ") should be false after remove");
                }
                return null;
            });
        }
        runAll(tasks);

        // Final check that even js were kept and odd removed
        for (int t = 0; t < THREADS; t++) {
            for (int j = 0; j < keysPerThread; j++) {
                int key = t + THREADS * j;
                boolean shouldBePresent = (j % 2 == 0);
                check(list.contains(key) == shouldBePresent, "Wrong final state for key ");
            }
        }
    }
}
