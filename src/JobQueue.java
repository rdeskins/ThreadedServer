
/**
 * @author Robin, Created 2/8/20
 * JobQueue, only stores job objects. Server enqueues and Workers dequeue.
 * Its constructor takes an int capacity to specify the maximum number of jobs. This should be 50 by default. 
 */
public class JobQueue {	
	private class Node {
		private Job data;
		private Node next;
		public Node(Job elem) {
			this.data = elem;
			this.next = null;
		}
	}//End nested node class
	
	private int maxCapacity;
	private Node head, tail;
	private int size;
	
	public JobQueue(int capacity) {
		this.size = 0;
		this.head = null;
		this.tail = null;
		this.maxCapacity = capacity;
	}
	
	//Called by Server
	public synchronized void enqueue(Job job) {				 
		 Node node = new Node(job);
		 if (size == 0)
			 head = node;
		 else
			 tail.next = node;
		 tail = node;
		 size++;
		 notifyAll();
		 Server.log("Job enqueued. Current number of jobs is: " + this.size);
	}
	
	//Called by Worker
	public synchronized Job dequeue() throws InterruptedException {
		Job job;

	    while (isEmpty()) {
	    	wait();
	    }

	    job = head.data;
	    head = head.next;   
	    size--;
	    if (size == 0) {
	    	tail = null; 
	        head = null;
	    }

	    Server.log("Job dequeued. Current number of jobs is: " + this.size);
	    return job;
	}
	
	//Called by Server
	public synchronized void stopJobQueue() {
		this.head = null;
		this.tail = null;
		this.size = 0;
		notifyAll();
		Server.log("JobQueue has shut down");
	}
	
	//Called by JobQueue
	private synchronized boolean isEmpty() { 
		return ( ((head==null) && (tail==null)) || this.size == 0 );
	}
	
	//Called by Server
	public synchronized boolean isFull() {
		return (this.size == maxCapacity);
	}
	
	//Called by ThreadManager
	public synchronized int getSize() {
		return this.size;
	}
}
