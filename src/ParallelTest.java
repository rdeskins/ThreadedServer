import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * @author Robin, Created 2/9/20
 * A test simulates many clients communicating with the server.
 * This test does not have a GUI. It does not test the KILL command. 
 * Each client sends one message and is then disconnected. There are helpful stats at the end that show how many clients
 * were rejected due to server being busy and if any clients were dropped (received no response despite connecting)
 */
public class ParallelTest {
	private static int accepted, rejected;
	private final static int numClients = 500;
	
	public static void main(String[] args) throws InterruptedException {
		accepted = 0;
		rejected = 0;
		
		TestClient[] clients = new TestClient[numClients];
		for (int i = 0; i < clients.length; i++) {
			clients[i]= new TestClient();
			clients[i].start();
		}
		
		for (int i = 0; i < clients.length; i++) {
			clients[i].join();
		}
		
		System.out.println("Tests are done!");
		//Some helpful stats, optional!
		printResults();
		
	}
	
	private static void printResults() {
		System.out.println("Number of clients accepted = " + accepted + ", Number of clients rejected = " + rejected);
		int processed = accepted + rejected;
		System.out.println("Total processed: " + (processed));
		DecimalFormat df = new DecimalFormat("#.##");
		double acceptanceRate = ((double)accepted/(double)processed)*100;
		double droppedRate = (((double)numClients -(double)processed)/(double)numClients)*100;
		System.out.println(df.format(acceptanceRate) + "% of clients were accepted");
		System.out.println(df.format(droppedRate) + "% of clients were dropped.");
	}
	
	private synchronized static void incrementAccepted() {
		accepted++;
	}
	
	private synchronized static void incrementRejected() {
		rejected++;
	}
	
	public static class TestClient extends Thread{
		private BufferedReader in;
		private PrintWriter out;
		Socket socket;
		String[] commands = {"ADD,2,3","SUB,4,5","MUL,3,5","DIV,6,3",
				"ADD,2,4,3","NOTCOMMAND,2,3",null,"","Random words",", , ,",
				"ADD,460,340","SUB,1040,40","MUL,500,4","DIV,1000,5"};
		
		public void run() {
			Random random = new Random();
			String response ="";
			
			try {
				connectToServer();
				String welcome = in.readLine();
				System.out.println("\n" + welcome);
				if (!welcome.contentEquals("The server is currently busy, please connect later!")) {
					System.out.println(in.readLine());
					out.println(commands[random.nextInt(commands.length)]);
					
					boolean gotResponse = false;
					while ((response = in.readLine()) != null) {
						System.out.println(response);
						gotResponse = true;
					}
					
					if (gotResponse)
						incrementAccepted();
					
				}else {
					incrementRejected();
				}
				
				socket.close();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		

		private void connectToServer() throws IOException {
			String serverAddress = "localhost";
			socket = new Socket(serverAddress, 9898);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(),true);
		}
	}//End nested TestClient	
}
