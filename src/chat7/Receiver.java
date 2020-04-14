package chat7;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.security.Key;
import java.util.StringTokenizer;

public class Receiver extends Thread{
	
	Socket socket;
	BufferedReader in = null;
	
	public Receiver(Socket socket) {
		this.socket = socket;
		
		try {
			in = new BufferedReader(new InputStreamReader
					(this.socket.getInputStream(), "UTF-8"));
		}
		catch(Exception e) {
			System.out.println("예외1: "+e);
		}
	}
	@Override
	public void run() {
		///
		super.run();
		///

		while(in!=null) {
			try {
				System.out.println("Thread Receive: " + in.readLine());
			}
			catch (SocketException e) {
				System.out.println("SocketException 발생");
				break;
			}
			catch (Exception e) {
				System.out.println("예외2: "+e);
			}
		}
		try {
			in.close();
		}
		catch (Exception e) {
			System.out.println("예외발생: "+ e);
		}

	}
}
