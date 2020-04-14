package chat5;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	
	//생성자
	public MultiServer() {
		//실행부 없으
	}
	//서버 초기화하는 메소드 init()
	public void init() {
		try {
			serverSocket = new ServerSocket(9999);//9999번 포트로 접속
			System.out.println("서버가 시작되었습니다");
			
			/*
			 한명의 클라이언트가 접속할때마다 접속을 허용(accept())해주고
			 동시에 MultiServerT 쓰레드를 생성한다.
			 해당 쓰레드는 한명의 클라이언트가 전송하는 메세지를 읽어서
			 Echo해주는 역할을 담당한다
			 */
			while(true) {
				socket = serverSocket.accept();//접속허용
				
				Thread mst = new MultiServerT(socket);
				mst.start();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				serverSocket.close();//소켓하나닫기
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	//메인메소드: 서버 객체를 생성한 후 초기화 한다
	public static void main(String[] args) {
		MultiServer ms= new MultiServer();
		ms.init();
		}
	class MultiServerT extends Thread{
		
		Socket socket;
		PrintWriter out = null;//쓰고
		BufferedReader in = null;//읽고!
		
		public MultiServerT(Socket socket) {
			this.socket = socket;
			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader
										(this.socket.getInputStream()));
			}
			catch (Exception e) {
				System.out.println("예외:"+e);
			}
		}
		@Override
		public void run() {
			String name = "";
			String s = "";
			
			try {
				//클라이언트의 이름을 읽어온 후 콘솔에 출력하고 Echo한다
				if(in!=null) {
					name = in.readLine();
					System.out.println(name + "접속");
					out.println("> "+name+"님이 접속함");
				}
				//클라이언트의 메세지를 읽어서 콘솔에 출력하고 Echo해준다
				while(in!=null) {
					s = in.readLine();
					if(s==null) break;
					
					System.out.println(name  + " >>" +s);
					sendAllMsg(name, s);
				}
			}
			catch (Exception e) {
				System.out.println("예외:" + e);
			}
			finally {
				//종료되는 클라이언트의 쓰레드명을 출력한다
				System.out.println(Thread.currentThread().getName()+"종료");
				try {
					in.close();
					out.close();
					socket.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		//클라이언트에게 서버의 메세지를 Echo해줌
		public void sendAllMsg(String name, String msg) {
			try {
				out.println("> "+name + " ==>"+msg);
			}
			catch (Exception e) {
				System.out.println("예외"+e);
			}
		}
		
	}
}
