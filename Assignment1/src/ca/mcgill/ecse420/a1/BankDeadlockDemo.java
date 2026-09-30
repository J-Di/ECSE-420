package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;


/*
This class is used to demonstrate a deadlock scenario similar to the 
example which was discussed in class with a transfer occuring beteen
2 bank acounts.
*/
class BankDeadlockDemo{

    // Class representing a bank account
    static class Account{
        private int id;
        private int balance;
        private Lock lock = new ReentrantLock();

        // constructor
        Account(int id, int balance){
            this.id = id;
            this.balance = balance;
        }

        // Function which is used to take money from this account
        void withdraw(int amount){
            this.balance -= amount;
        }

        // Function whic is used to deposit money into this account
        void deposit(int amount) {
            balance += amount;
        }

        // getter methods 
        int getId(){
            return this.id;
        }
        int getBalance (){
            return this.balance;
        }
        Lock getLock(){
            return this.lock;
        }

    }

    // Class that handlse interactions between accounts
    static class TransferTask implements Runnable{
        private Account sender; // Account which is loosing money
        private Account receiver; // Account which is going ot gain money
        private int amount; // Amount of money transferred
        private boolean locksOrdering; // Whether or not to properly order tasks to avoid 
                                       // deadlocks, for examples of working solution

        // Constructor
        TransferTask(Account sender, Account receiver, int amount, boolean locksOrdering){
            this.sender = sender;
            this.receiver = receiver;
            this.amount = amount;
            this.locksOrdering = locksOrdering;
        }

        @Override 
        public void run (){
            String name = Thread.currentThread().getName();

            // This part is where we can differienciate a deadlock from good operation

            // DEADLOCK PRONE: None consistent aquisition across threads, so if two threads 
            // sending to eachother, can cause deadlock!
            // Without any specific ordering, get the  sender and then the receiver
            Account first = sender;
            Account second = receiver;

            // If ordeing locks properly, we should have each task get the locks with a consistent
            // pattern, that way no deadlocks can occur even if two accounts sending to eachother
            if (locksOrdering) {
                if (sender.getId() > receiver.getId()){
                    first = receiver;
                    second = sender;
                }
            }
        
            // Initiate the transaction
            first.getLock().lock();
            try{
                System.out.println(name + " acquired the lock on account " + first.getId());

                // purposefully adding delay so other thread can aquire its first lock if it tries
                try{
                Thread.sleep(200);
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                // Now grab the second lock on the other account
                System.out.println(name + " waiting for lock on account " + second.getId());
                second.getLock().lock();
                try{
                    // proceed with the transfet
                    sender.withdraw(amount);
                    receiver.deposit(amount);
                    System.out.println(name + " completed transfer: " + amount);
                }
                finally{
                    second.getLock().unlock();
                }
            }
            finally{
                first.getLock().unlock();
            }
        }
    } 

    public static void main(String [] args) throws InterruptedException {

        // First, toggle if we want a deadlock Safe option or not
        boolean isDeadlockSafe = false; // Change this for deadlock safe or not

        // Create accounts for an example
        Account account1 = new Account(0, 100);
        Account account2 = new Account(1,   50);

        // Now create 2 threads
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(new TransferTask(account1, account2, 20, isDeadlockSafe));
        executor.execute(new TransferTask(account2, account1, 50, isDeadlockSafe));
        executor.shutdown(); // no new tasks

        // Using a timeout to determine if threads are in a deadlock
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            System.out.println("Program is hung after 5 seconds. Checking for deadlock");

            // Gather information on the threads that are in a deadlock here
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            long[] deadlockedIds = threadBean.findDeadlockedThreads();

            // Print it out for clarity using threadBean.getThreadInfo for more information and clarity
            if (deadlockedIds != null) {
                for (ThreadInfo info : threadBean.getThreadInfo(deadlockedIds)) {
                System.out.println(
                    info.getThreadName()+ " is waiting for a lock held by " 
                                                    + info.getLockOwnerName());
                }
            }
            System.exit(1);
        } 
        else {
            // no deadlock
            System.out.println("\nBoth transfers finished. Balances: account 1 = " + 
                      account1.getBalance() + ", account 2 = " + account2.getBalance());
            }

        }

}