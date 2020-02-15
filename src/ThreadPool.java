import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Robin, Created 2/7/20
 * Contains a collection of threads. The  max number of threads is controlled by the variable 'maxCapacity'.
 * After being constructed, to use the threads, startThreadPool must be called.
 */
public class ThreadPool {
	private int initialThreads;
	private int maxCapacity;
	private int numThreadsRunning;
	private Worker[] workers;
	private static JobQueue queue;
	
	//Called by Server
	public ThreadPool(int initialThreads, int capacity, JobQueue jobQ) {
		this.numThreadsRunning = 0;
		this.initialThreads = initialThreads;
		this.maxCapacity = capacity;
		this.workers = new Worker[maxCapacity];
		queue = jobQ;
	}
	
	//Called by Server
	public void startThreadPool() {
		for (int i = 0; i < initialThreads; i++) {
			this.workers[i] = new Worker(i);
			this.workers[i].start();
		}
		this.numThreadsRunning = initialThreads;
	}
	
	//Called by Server
	public void stopThreadPool() {
		for (int i = 0; i < this.numThreadsRunning; i++) {
			try {
				workers[i].interrupt();
				workers[i].join();
			} catch (InterruptedException e) {
				Server.log("Error when shutting down ThreadPool: " + e);
			}
		}
		
		this.numThreadsRunning = 0;
		Server.log("ThreadPool has shut down");
	}
	
	//Called by ThreadManager
	public void increaseThreads() {
		int oldNumRunning = this.numThreadsRunning;
		this.numThreadsRunning *= 2;
		for (int i = oldNumRunning; i < this.numThreadsRunning && i < maxCapacity; i++) {
			workers[i] = new Worker(i);
			workers[i].start();
		}
		Server.log("Worker threads running increased from " + oldNumRunning + " to " + this.numThreadsRunning);
	}
	
	//Called by ThreadManager
	public void decreaseThreads() {
		int oldNumRunning = this.numThreadsRunning;
		this.numThreadsRunning = this.numThreadsRunning/2;
		for (int i = this.numThreadsRunning; i <  oldNumRunning; i ++) {
			try {
				workers[i].interrupt();
				workers[i].join();
			} catch (InterruptedException e) {
				Server.log("Error when decreasing threads: " + e);
			}
		}
		Server.log("Worker threads running decreased from " + oldNumRunning + " to " + this.numThreadsRunning);
	}
	
	//Called by ThreadManager
	public int getNumberThreadsRunning() {
		return this.numThreadsRunning;
	}
	
	//Called by ThreadManager
	public int getInitialThreads() {
		return this.initialThreads;
	}
	
	/**
	 * Created 2/7/20. Nested Worker class. Only ThreadPool makes workers. 
	 * Worker dequeues a Job from JobQueue and does the work, then sends result to client.
	 */
	private static class Worker extends Thread{
		private int workerID;
		private Socket socket;
		
		private Worker(int workNum) {
			this.workerID = workNum;
		}
		
		public void run() {
			try {
				while (!interrupted()) {					
					Job job = queue.dequeue();
					int clientID = job.getClientNumber();
					socket = job.getSocket();
					String input = job.getInput();
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					String command = getCommand(input);
					int[] args = getArgs(input);
					
					if (!isMathCommand(command)) {
						out.println("Cannot process your request. " + command + " is not an option.");
					}else if (args.length != 2) {
						out.println("Cannot process your request. Please use format \"[command],[int],[int]\"");
					}else if (command.contentEquals("DIV") && args[1] == 0) {
						out.println("Please do not divide by 0!");
					}else {//Then OK to do math
						int result = doMath(command,args);
						out.println("Result is: " + result);
					}
					
					Server.log("Worker id=" + workerID + " processed service request " + input + " for client id=" + clientID);
					out.println("Goodbye now!");
					out.close();
					socket.close();
					Server.log("Connection with client id=" + clientID + " closed");
				}
			}catch (IOException e) {
				Server.log("IOException with worker id=" + workerID + ": " + e);
				e.printStackTrace();
			} 
			catch (InterruptedException e) {
				Server.log("Worker id=" + workerID + " shutting down");
				return;
			}
			//Worker gets to this point if its being killed
			Server.log("Worker id=" + workerID + " shutting down");
		}
		
		private static String getCommand(String input) {
			Scanner scn = new Scanner(input);
			scn.useDelimiter(",");
			String res = null;
			if (scn.hasNext())
				res = scn.next();
			scn.close();
			return res;
		}
		
		private static int[] getArgs(String input) {
			Scanner scn = new Scanner(input);
			scn.useDelimiter(",");
			if (scn.hasNext())
				scn.next();
			int numOfArgs = 0;
			while (scn.hasNextInt()) {
				numOfArgs++;
				scn.nextInt();
			}
			scn.close();
			scn = new Scanner(input);
			scn.useDelimiter(",");
			if (scn.hasNext())
				scn.next();
			int[] args = new int[numOfArgs];
			for (int i = 0; i < args.length; i++) {
				args[i] = scn.nextInt();
			}
			scn.close();
			return args;
		}
				
		private static boolean isMathCommand(String comm) {
			if (comm != null) {
				return (comm.equals("ADD") ||
						comm.equals("SUB") ||
						comm.equals("MUL") ||
						comm.equals("DIV"));
			}
			return false;
					
		}		
		
		private static int doMath(String comm, int[] args) {
			int result;
			if (comm.equals("ADD")) {
				result = args[0] + args[1];
			}else if (comm.equals("SUB")) {
				result = args[0] - args[1];
			}else if (comm.equals("MUL")) {
				result = args[0] * args[1];
			}else { //must be div
				result = args[0] / args[1];
			}
			return result;
		}
	}//End nested Worker class
} //End ThreadPool class
