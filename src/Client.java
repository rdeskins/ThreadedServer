import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

/**
 * Created 2/7/20
 * Swing-based client for connecting to server. Has a text area for entering commands
 * and a text area for seeing results.
 */
public class Client {
	private BufferedReader input;
	private PrintWriter output;
	private static Socket socket;
	private JFrame frame = new JFrame("Client");
	private JTextField dataField = new JTextField(40);
	private JTextArea messageArea = new JTextArea(8, 60);
	
	/**
	 * Sets up GUI for client
	 */
	public Client() {
		messageArea.setEditable(false);
		frame.getContentPane().add(dataField, "North");
		frame.getContentPane().add(new JScrollPane(messageArea), "Center");
		DefaultCaret caret = (DefaultCaret)messageArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		dataField.addActionListener(new ActionListener() {
			/**
			 * Responds to enter key in text field and sends content of text field to the server.
			 * Then, displays the answer from the server. 
			 */
			public void actionPerformed(ActionEvent e) {
				output.println(dataField.getText());
                String response = "";
                try {
                	String line;
                	while ((line = input.readLine()) != null) {
                		response += line + "\n";
                	}

                } catch (IOException ex) {
                           response = "Error: " + ex;
                    System.out.println(response + "\n");
                }
                messageArea.append(response + "\n");
                dataField.selectAll();
			}
			
		});
	}
	
	public void connectToServer() throws IOException {
		String serverAddress = JOptionPane.showInputDialog(
                frame,
                "Enter IP Address of the Server:",
                "Welcome!",
                JOptionPane.QUESTION_MESSAGE);
		
		socket = new Socket(serverAddress, 9898);
		input = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
		output = new PrintWriter(socket.getOutputStream(), true);
		
		//Consume initial messages from server
		String welcome = input.readLine();
		messageArea.append(welcome + "\n");
        if (welcome.contentEquals("The server is currently busy, please connect later!")) {
        	socket.close();
        	System.out.println("Server busy, try again later.");
        	System.exit(0);
        }else {
        	for (int i = 0; i < 1; i++) {
				messageArea.append(input.readLine() + "\n");
			}
        }

		
	}
	
	public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.pack();
        client.frame.setVisible(true);
        client.connectToServer();
        
        client.frame.addWindowListener(new WindowAdapter() {
					public void windowClosing( WindowEvent e){
						try {
							socket.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						System.exit(0);
					}
				});
  }
}
