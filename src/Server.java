import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Robin, Created 2/7/20
 * A server program that accepts requests from clients. Each client sends one request, then, the Server moves on to accept another input.
 * Server enqueues a job into the job queue.
 * Variables as specified in the homework document, maxJobs, T1, T2, V, initialThreads, maxThreads are all listed in the beginning of Server
 * and can be edited for testing purposes. 
 */
public class Server {	
	private static int maxJobs = 50;
	private static int T1 = 10;
	private static int T2 = 20;
	private static int V = 2;
	private static int initialThreads = 5;
	private static int maxThreads = 40;
	
	/** 
	 * Run server in a loop listening in on port 9898. Main does not sleep or wait 
	 * so it can respond to requests to connect. 
	 */
	public static void main(String[] args) {
		log("Server started running");
		try {
			int clientID = 0;
			ServerSocket listener = new ServerSocket(9898);
			JobQueue jobQueue = new JobQueue(maxJobs);
			ThreadPool tPool = new ThreadPool(initialThreads,maxThreads,jobQueue);
			ThreadManager threadMngr = new ThreadManager(T1,T2,V,tPool,jobQueue);
			threadMngr.start();
			tPool.startThreadPool();
			
			String input = "";
			while (!input.contentEquals("KILL")) {
				Socket socket = listener.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				
				if (!jobQueue.isFull()) {
					Server.log("Connection with client id=" + clientID + " began at " + socket);
					out.println("Hello, you are client #" + clientID);
		            out.println("Please enter your request or enter \"KILL\" to kill server.");
		            input = in.readLine();
		            
					if (!input.contentEquals("KILL")) {
						Job job = new Job(clientID, input, socket);
			            jobQueue.enqueue(job);
		            }else {
						out.println("KILL received! Goodbye!");
						in.close();
						out.close();
						socket.close();
		            }
				}else {
					out.println("The server is currently busy, please connect later!");
	        		log("Client id=" + clientID + " rejected because the server is busy");
				}
				
				clientID++;
			}
			
			//Kill code here
        	log("KILL received in Server main");
			listener.close();
			tPool.stopThreadPool();
			threadMngr.interrupt();
			threadMngr.join();
			jobQueue.stopJobQueue();
			
		}catch (IOException e) {
			log("IOException in Server: " + e);
			e.printStackTrace();
		}catch (InterruptedException e1) {
			log("InterruptedException in Server: " + e1);
		}
		
		log("Server has shut down");		
	}//End Main
	
	//Called by Server, ThreadManager, ThreadPool, JobQueue, and Worker to print log of actions done on the server.
	public static void log(String message) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS MM/dd/yyyy"); 
		LocalDateTime now = LocalDateTime.now();
		System.out.println(dtf.format(now) + " " + message);
	}
}
