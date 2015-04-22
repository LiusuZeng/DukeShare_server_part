import java.io.*;

public class ServerUI {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		boolean flag = true;
		Server my_server = null;
		BufferedReader br = null;
		while(flag)
		{
			System.out.println("Please input Central Server listening port numer:");
			br = new BufferedReader(new InputStreamReader(System.in));
			String port_str;
			try {
				port_str = br.readLine();
				int port = Integer.parseInt(port_str);
				my_server = new Server(port);
				flag = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("Invalid port number!");
				flag = true;
			}
		}
		//
		System.out.println("Server is running... Enter 'quit or q' to stop it.");
		// prepare for quit
		String cmd = "default";
		while(!cmd.equals("quit") && !cmd.equals("q"))
		{
			try {
				cmd = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		//
		my_server.stopMe();
	}

}
