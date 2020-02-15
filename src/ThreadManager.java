
/**
 * @author Robin, Created 2/11/20
 * Manages the ThreadPool. If number of jobs running passes threshold T1, the number of threads running is doubled and maintained at that number.
 * If it passes a second threshold T2, the number is doubled again and maintained. numTreads is decreased according to the same logic and
 * thresholds.
 * ThreadManager is created by Server's main and interrupted from Server's main.
 */
public class ThreadManager extends Thread{
	private JobQueue jobQueue;
	private ThreadPool pool;
	private int T1, T2, V; 
	
	public ThreadManager(int t1, int t2, int v, ThreadPool tPool, JobQueue jobQ) {
		this.T1 = t1;
		this.T2 = t2;
		this.V = v;
		this.pool = tPool;
		this.jobQueue = jobQ;
	}
	
	public void run() {
		int numThreads;
		int numJobs;
		
		int initialThreads = this.pool.getInitialThreads();
		while (!interrupted()) {
			numThreads = pool.getNumberThreadsRunning();
			numJobs = jobQueue.getSize();
						
			if (numJobs <= T1 && numThreads > initialThreads) { 
				pool.decreaseThreads();
			}
			else if (numJobs > T2 && numThreads < initialThreads*4) {
				pool.increaseThreads();
			}
			else if (numJobs > T1 && numJobs <= T2 && numThreads < initialThreads*2) {
				pool.increaseThreads();
			}
			else if (numJobs > T1 && numJobs <= T2 && numThreads > initialThreads*2) {
				pool.decreaseThreads();
			}
			
			try {
				sleep(V);
			} catch (InterruptedException e) {
				break;
			}
		}
		
		Server.log("ThreadManager has shut down");
	}
}
