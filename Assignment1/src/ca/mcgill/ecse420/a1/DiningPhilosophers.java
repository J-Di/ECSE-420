package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiningPhilosophers {

	public static void main(String[] args) {

			int numberOfPhilosophers = args.length == 0 ? 5 : Integer.parseInt(args[0]);
			if (numberOfPhilosophers < 2) {
				throw new IllegalArgumentException("At least two philosophers are required.");
			}
                Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
                Object[] chopsticks = new Object[numberOfPhilosophers];


		//Initialize the chopsticks
		for (int i = 0; i < numberOfPhilosophers; i++) {
			chopsticks[i] = new Object();
		}

		//Initialize the philosophers with their respective chopsticks
		for (int i = 0; i < numberOfPhilosophers; i++) {
			Object leftChopstick = chopsticks[i];
			Object rightChopstick = chopsticks[(i + 1) % numberOfPhilosophers]; //Using modulo to ensure the last philosopher picks up the first chopstick as their right chopstick

			philosophers[i] = new Philosopher(i, leftChopstick, rightChopstick); //Creates the philosopher object with the id and the two chopsticks (Creates the tasks not the threads)
		}

		ExecutorService executor = Executors.newFixedThreadPool(numberOfPhilosophers); //Creates a thread pool containing a fixed number of threads equal to the number of philosophers

		for (int i = 0; i < numberOfPhilosophers; i++) { //Executes the tasks for each philosopher/thread in the thread pool

			executor.execute(philosophers[i]); //Hands a philosophor to the worker and runs each philosopher's run method

		}

			// Stop accepting new tasks; the philosopher tasks continue running.
			executor.shutdown();

	}

	public static class Philosopher implements Runnable {

		//Class Objects to represent the chopsticks and id
		private final int id;
		private final Object leftChopstick;
		private final Object rightChopstick;

		//Constructor to initialize the Philosopher object with an id and the two closest chopsticks
		public Philosopher(int id, Object leftChopstick, Object rightChopstick) {
			this.id = id;
			this.leftChopstick = leftChopstick;
			this.rightChopstick = rightChopstick;
		}

		@Override
		public void run() {

			try {

				//Deadlock solution:
				// Since each philosophere already has an ID between 0 and numberOfPhilosophers-1,
				// we can use the ID to determine which chopstick to pick up first.
				// Philosophers with even IDs pick up the left chopstick first, while philosophers with odd IDs pick up the right chopstick first.
				while (!Thread.currentThread().isInterrupted()) {

					//The philosopher thinks for a while, then becomes hungry and tries to pick up the chopsticks to eat
					System.out.println("Philosopher " + id + " is thinking.");
					Thread.sleep(200);
					System.out.println("Philosopher " + id + " is hungry.");

					// Each shared chopstick's monitor permits only one owner at a time.
					if (id % 2 == 0) {
						// Even-numbered philosophers pick up the left chopstick first.
						synchronized (leftChopstick) {

							System.out.println("Philosopher " + id + " picked up left chopstick.");

							// Sleep holds the left monitor to increase the chance of deadlock.
							Thread.sleep(200);
							System.out.println("Philosopher " + id + " is waiting for the right chopstick.");

							synchronized (rightChopstick) {

								System.out.println("Philosopher " + id + " picked up right chopstick.");
								System.out.println("Philosopher " + id + " is eating.");
								Thread.sleep(200);
							}

							System.out.println("Philosopher " + id + " put down right chopstick.");
						}
					} else {
						// Odd-numbered philosophers pick up the right chopstick first.
						synchronized (rightChopstick) {

							System.out.println("Philosopher " + id + " picked up right chopstick.");

							// Sleep holds the right monitor to increase the chance of deadlock.
							Thread.sleep(200);
							System.out.println("Philosopher " + id + " is waiting for the left chopstick.");

							synchronized (leftChopstick) {

								System.out.println("Philosopher " + id + " picked up left chopstick.");
								System.out.println("Philosopher " + id + " is eating.");
								Thread.sleep(200);
							}

							System.out.println("Philosopher " + id + " put down left chopstick.");
						}
					
						System.out.println("Philosopher " + id + " put down right chopstick.");
					}
				}

			} catch (InterruptedException e) {

				// Exiting synchronized blocks releases held monitors before reaching here.
				Thread.currentThread().interrupt();
			}
		}
	}
}
