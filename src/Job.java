import java.net.Socket;

/**
 * @author Robin, Created 2/7/20
 * Jobs are created by Server's main method. 
 */
public class Job {
	private int clientNum;
	private String input;
	private Socket socket;
	
	//Called by Server
	public Job(int clientNumber,String input,Socket socket) {
		this.clientNum = clientNumber;
		this.input = input;
		this.socket = socket;
	}
	
	//Called by Worker
	public int getClientNumber() {
		return this.clientNum;
	}
	
	//Called by Worker
	public String getInput() {
		return this.input;
	}
	
	//Called by Worker
	public Socket getSocket() {
		return this.socket;
	}
}
