package ca.mcgill.ecse420.a2.Q1;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


// Bakery lock class
// This class implements the bakery lock which is also discussed in chapter 2
public class BakeryLock implements Lock{

    // private class variables
    private int n;
    // Similar to Filter lock, every element of flag and label needs volatile behaviour,
    // so each element is an atomic object and get() + set() is used to assign and get back
    private AtomicBoolean[] flag; // flag[i] is true when thread i wants the lock
    private AtomicInteger[] label; // label[i] is the num thread i took, and lower nums go first

    // Extra fields added for question 1.8 to track overtaking
    private boolean trackOvertaking = false; 
    private AtomicInteger clock; // global ticket counter, gives every recorded event an order
    private int[][] start;  // start[t][v] is thread t's ticket before its doorway 
    private int[][] arrive; // arrive[t][v] is thread t's ticket after its doorway
    private int[][] pass;   // pass[t][v] is thread t's ticket after its wait loop
    private int[] visit;    // how many times thread t has called lock()

    // constructor
    public BakeryLock(int n) {
        this.n = n;
        flag = new AtomicBoolean[n];
        label = new AtomicInteger[n];

        // Initialize flags to false and labels to 0
        for (int i = 0; i < n; i++) {
            flag[i] = new AtomicBoolean(false);
            label[i] = new AtomicInteger(0);
        }
    }

    
    // Question 1.8 constructor, with overtaking tracking turned on or off
    public BakeryLock(int n, boolean trackOvertaking, int maxVisits) {
        // Similar to default constructor, but with tracking features
        this(n);
        this.trackOvertaking = trackOvertaking;
        if (trackOvertaking) {
            clock = new AtomicInteger(0);
            start = new int[n][maxVisits];
            arrive = new int[n][maxVisits];
            pass = new int[n][maxVisits];
            visit = new int[n];
        }
    }
    

    // Lock method used to acquire a lock
    @Override 
    public void lock(){
        // Get the thread ID as to update the correct class parameters
        int i = IdThread.currentId(); // Get current thread ID

        // Addition for Q1.8 tracking
        if (trackOvertaking) {
            start[i][visit[i]] = clock.getAndIncrement();
        }

        // Announce interest, and then take a label greater than one currently used
        flag[i].set(true);
        label[i].set(maxLabel() + 1);

        // Question 1.8, record when this thread finishes its doorway
        if (trackOvertaking) {
            arrive[i][visit[i]] = clock.getAndIncrement();
        }

        // Wait while other threads is interested and has a smaller label than current
        while (smallerLabelWaiting(i)) {
            // Do nothing
        }

        // Question 1.8, record when this thread gets past the wait loop
        if (trackOvertaking) {
            pass[i][visit[i]] = clock.getAndIncrement();
        }
    }


    // A helper function that finds the largest label currently held by any thread
    // (labels are read one at a time, in some arbitrary order, as in the slides)
    private int maxLabel() {
        int max = 0;
        // iterate through all tlabels to get current max
        for (int k = 0; k < n; k++) {
            int current = label[k].get();
            if (current > max) {
                max = current;
            }
        }
        return max;
    }


    // Helper function used to determine if a thread must keep waiting
    // It returns true if some other thread is interested and the label is before the current one
    private boolean smallerLabelWaiting(int i) {
        // retireve current label
        int myLabel = label[i].get();
        // determine if other threads has higher label, if it is interested
        for (int k = 0; k < n; k++) {
            if (k != i && flag[k].get()) {
                int otherLabel = label[k].get();
                // smaller label goes first adn ties broken by smaller thread ID
                if (otherLabel < myLabel || (otherLabel == myLabel && k < i)) {
                    return true;
                }
            }
        }
        return false; // Current thread can now run
    }

    
    // Unlock function used to release the lock back so that it can be aquired by other threads
    @Override
    public void unlock() {
        // No longer interested, label is untouched but interest no longer there
        flag[IdThread.currentId()].set(false);

        // Question 1.8, move on to this thread's next visit
        if (trackOvertaking) {
            visit[IdThread.currentId()]++;
        }
    }


    // Question 1.8, ticket for thread doorway start
    public int startTicket(int t, int v) {
        return start[t][v];
    }

    // Question 1.8, ticket for thread doorway end
    public int arrivalTicket(int t, int v) {
        return arrive[t][v];
    }

    // Question 1.8, ticket for thread  pass of the wait loop
    public int passTicket(int t, int v) {
        return pass[t][v];
    }

}
