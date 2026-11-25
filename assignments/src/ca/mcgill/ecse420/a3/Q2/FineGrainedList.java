package ca.mcgill.ecse420.a3.Q2;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

// This class implements a simple genric linked list, which is needed to properly 
// test the functionality of the contains method in the created tests
public class FineGrainedList <T> {
    
    // private fields for the linked list
    private final Node<T> head;
    private final Node<T> tail;

    // Create the node subclass
    private static class Node <T>{
        private T item; 
        private int key;
        private Node<T> next;
        private ReentrantLock lock = new ReentrantLock(); // each node has its own lock as it is 
                                                          // fine grained implementation
        
        // contructor for the node
        private Node (T item, int key){
            this.item = item;
            this.key = key;
        }

        // method to lock the node
        private void lock() {
            lock.lock();
        }

        // method to unlock the node
        private void unlock(){
            lock.unlock();
        }
    }

    
    // Constructor for the class
    public FineGrainedList() {
        head = new Node<>(null, Integer.MIN_VALUE);
        tail = new Node<>(null, Integer.MAX_VALUE);
        head.next = tail;

    }


    // Here is the contains method for question 2.1 of the assignment
    // This function returns a boolean representing whether or not the list contains the provided 
    // item, following a similar format to the lecture slides 
    public boolean contains (T item){

        // First check the input to make sure its valid, if so assign integer key 
        Objects.requireNonNull(item);
        int key = item.hashCode();

        // Now acquire lock on first 2 nodes
        Node<T> pred = head;
        pred.lock();
        Node<T> curr = pred.next;
        curr.lock();

        // Iterate through the linked list and check if item is inside any element
        try{
            while (curr !=tail && curr.key <= key){
                // Check if the current node has the item
                if (Objects.equals(item, curr.item)){
                    return true;
                }

                // Otherwise move across list using hand over hand locking
                pred.unlock();
                pred = curr;
                curr = curr.next;
                curr.lock();
            }

            // no madches
            return false;
        }
        finally{
            curr.unlock();
            pred.unlock();
        }
    }


    // This method implements remove, following the style which was done in class as part of the 
    // slides, and the contains method above. This function removes an item from the list.
    public boolean remove(T item) {

        // Check for valid input, and assign the key
        Objects.requireNonNull(item);
        int key = item.hashCode();

        // Aquire lock according to hand over hand strategy
        Node<T> pred = head;
        pred.lock();
        Node<T> curr = pred.next;
        curr.lock();

        // Iterate through the list, and remove the item specified
        try {
            while (curr != tail && curr.key <= key) {
                // Check if item matches the current node
                if (Objects.equals(item, curr.item)) {
                    // remove current item in list!
                    pred.next = curr.next;
                    return true;
                }

                // move to nextr node, again with hand over hand
                pred.unlock();
                pred = curr;
                curr = curr.next;
                curr.lock();
            }
            return false;
        } 
        finally {
            curr.unlock();
            pred.unlock();
        }
    }


    // This method implements add, following a similar style to what was used for contains and for
    // remove method, which was seen in class. This function adds an item to the list
    public boolean add (T item) {

        // Check for valid input, and assign the key
        Objects.requireNonNull(item);
        int key = item.hashCode();

        // Aquire lock according to hand over hand strategy
        Node<T> pred = head;
        pred.lock();
        Node<T> curr = pred.next;
        curr.lock();

        // Iterate through the list, and add the item specified if not already added
        try {
            while (curr != tail && curr.key <= key) {
                // Check if item matches the current node
                if (Objects.equals(item, curr.item)) {
                    return false; // Item already in list, so no doubles
                }

                // move to nextr node, again with hand over hand
                pred.unlock();
                pred = curr;
                curr = curr.next;
                curr.lock();
            }

            // Since item not already in list, add to the end, since curr is the tail
            Node<T> node = new Node<>(item, key);
            node.next = curr;
            pred.next = node; // insert this node

            return true;
        } 
        finally {
            curr.unlock();
            pred.unlock();
        }
    }

}
