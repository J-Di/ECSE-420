package ca.mcgill.ecse420.a2.Q1;

import java.util.concurrent.atomic.AtomicInteger;


// Filterlock class
// Implements a filter lock for n threads, as seen in Chapter 2
public class FilterLock implements Lock{

    private int n;
    /** For both level and victim, we need volatile behavior on each element of the 
        level and victim arrays, to do this we will use AtomicInteger, which is
        essentially a way to get a array full of volatile integers, which leads to using get()
        or set() in order to change these values */
    private AtomicInteger[] level; // level[i] is the level thread[i] trying to go to
    private AtomicInteger[] victim;

    // Extra fields added for question 1.4 to track overtaking
    private boolean trackOvertaking = false;
    private AtomicInteger clock; // Gives every recorded event an order
    private int[][][] arrive; // arrive[t][v][lvl] is thread t's ticket when arriving at lvl on its v-th lock()
    private int[][][] pass; // pass[t][v][lvl] is thread t's ticket when passing lvl on its v-th lock()
    private int[] visit; // visit[t] is how many times thread t has called lock()

    // constructor 
    public FilterLock(int n){
        this.n = n;
        level = new AtomicInteger[n];
        victim = new AtomicInteger[n];

        // Initialize arrays to all 0
        for (int i = 0; i < n; i++){
            level[i] = new AtomicInteger(0);
            victim[i] = new AtomicInteger(0);
        }
    }


    // Question 1.4 constructor with the same lock, with overtaking tracking turned on or off
    // maxVisits is the most times any single thread will call lock()
    public FilterLock(int n, boolean trackOvertaking, int maxVisits){
        this(n);
        this.trackOvertaking = trackOvertaking;
        if (trackOvertaking){
            clock = new AtomicInteger(0);
            arrive = new int[n][maxVisits][n];
            pass = new int[n][maxVisits][n];
            visit = new int[n];
        }
    }


    // New lock method used to lock a variable
    @Override 
    public void lock(){
        // Get the thread ID as to update correct level/victim for current thread
        int i = IdThread.currentId(); // Get current thread ID

        for (int lvl = 1; lvl < n; lvl++){
            level[i].set(lvl); // Announce intention to enter level lvl
            victim[lvl].set(i);  // always volunteer to be the victim

            // Now while there are others are at the same level, and the current thread
            // is the victim, wait
            while( otherThreadsPresent(i, lvl) && victim[lvl].get() == i){
                // Do nothing 
            }

            // Question 1.4, record when this thread passed level lvl
            if (trackOvertaking){
                pass[i][visit[i]][lvl] = clock.getAndIncrement();
            }
        }


    }

    // A helper function used to assess whether a thread can proceed to the next level in 
    // the filter or not
    private boolean otherThreadsPresent(int i, int lvl){
        // check through current, and superior levels for other threads
        for (int k = 0; k < n; k++){
            if (k != i && level[k].get() >= lvl){
                return true;
            }
        }

        return false;
    }

    @Override 
    public void unlock() {
        // Set the current thread interest back to 0
        level[IdThread.currentId()].set(0);

        // Question 1.4, move on to this thread's next visit
        if (trackOvertaking){
            visit[IdThread.currentId()]++;
        }
    }

    // Question 1.4, ticket for thread's x-th arrival at level lvl
    public int arrivalTicket(int t, int v, int lvl){
        return arrive[t][v][lvl];
    }

    // Question 1.4, ticket for thread's xth pass of level lvl 
    public int passTicket(int t, int v, int lvl){
        return pass[t][v][lvl];
    }
}



