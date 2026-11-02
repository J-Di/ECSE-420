package ca.mcgill.ecse420.a2.Q1;

// This is an interface which will be implemented by the FilterLock and BakeryLock implementations
public interface Lock {
    void lock();
    void unlock();
}
