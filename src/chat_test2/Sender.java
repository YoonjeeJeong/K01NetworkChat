package chat_test2;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Scanner;

public class Sender extends Thread{
	
	Socket socket;
	PrintWriter out = null;
	String name;
	
	public Sender(Socket socket, String name) {
		this.socket = socket;
		try {
			out = new PrintWriter(this.socket.getOutputStream(), true);
			this.name = name;
		}
		catch (Exception e) {
			System.out.println("예외>sender>생성자: "+ e);
		}
	}
	@Override
	public void run() {
		Scanner s = new Scanner(System.in);	
				
		try {
			out.println(URLEncoder.encode(name, "UTF-8"));
			
			while(out!=null) {
			try {
				String s2 = s.nextLine();
				
			////added
//				if(s2.startsWith("/to")) {
//					String[] sArray = s2.split(" ");
//					
//					if(sArray.length==1) {
//						out.println();
//					}
//				}
//				
			////added
				if(s2.equalsIgnoreCase("Q")) {
					break;
					
				}
				else {
					out.println(URLEncoder.encode(s2, "UTF-8"));
				}
			}
			catch (Exception e) {
				System.out.println("예외>sender>run1: "+ e);
				}
			}
			out.close();
			socket.close();
			}
			catch(UnsupportedEncodingException e1) {		
		}
		catch(Exception e) {
		System.out.println("예외>sender>run2: "+ e);
		}
	}
}
