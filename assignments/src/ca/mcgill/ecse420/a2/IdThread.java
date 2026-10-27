package ca.mcgill.ecse420.a2;


// A thread that carries an index in [0, n] for use by the lock implementations
public class IdThread extends Thread {
  private final int threadId;

  public IdThread(int threadId, Runnable task) {
    super(task);
    this.threadId = threadId;
  }

    // Returns the index of the calling thread
  public static int currentId() {
    return ((IdThread) Thread.currentThread()).threadId;
  }
}