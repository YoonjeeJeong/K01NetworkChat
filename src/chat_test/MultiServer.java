package chat_test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {

	public static void main(String[] args) {

		ServerSocket serverSocket = null;
		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String s = "";
		String name = "";

		try {
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");

			socket = serverSocket.accept();

			/*** ① ***/
			//아래: 클라이언트의 접속요청을 허가하는 코드
			System.out.println(socket.getInetAddress()+ ":"+
			socket.getPort());
			
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			//
			if(in != null) {
				name = in.readLine();
				System.out.println(name +" 접속");
				out.println("> "+ name +"님이 접속했습니다.");
			}

			while(in != null) {
				s = in.readLine();
				if(s==null) {
					break;
				}
				System.out.println(name +" ==> "+ s);
				out.println(">  "+ name +" ==> "+ s);
			}

			System.out.println("Bye...!!!");
		}
		catch (Exception e) {
			System.out.println("예외1:"+ e);
		}
		finally {
			try {
				in.close();
				out.close();
				socket.close();
				serverSocket.close();
			}
			catch (Exception e) {
				System.out.println("예외2:"+ e);
			}
		}
	}
}
