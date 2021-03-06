package chat6;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultiServer {
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	//클라이언트 정보 저장을 위한 맵 컬렉션 생성!
	Map<String, PrintWriter> clientMap;
	
	//생성자
	public MultiServer() {
		//클라이언트의 이름과 출력 스트림을 저장할 hashmap생성
		clientMap = new HashMap<String, PrintWriter>();
		//hashmap동기화 설정. 쓰레드가 사용자 정보에 동시에 접근하는 것을 차단한다.
		Collections.synchronizedMap(clientMap);
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
				//System.out.println(socket.getInetAddress()+ ":"+socket.getPort());
				
				/*
				 클라이언트의 메세지를 모든 클라이언트에게 전달하기 위한 쓰레드 
				 생성및 start. */
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
	//접속된 모든 클라이언트에게 메세지를 전달하는 역할의 메소드
		public void sendAllMsg(String name, String msg) {
			
			//Map에 저장된 객체의 키값(이름)을 먼저 얻어온다
			Iterator<String> it = clientMap.keySet().iterator();
			
			//저장된 객체(클라이언트)의 갯수만큼 반복한다
			while(it.hasNext()) {
				
			try {
				//각 클라이언트의 PrintWriter객체를 얻어온다
				PrintWriter it_out = 
						(PrintWriter) clientMap.get(it.next());
				
				//클라이언트에게 메세지를 전달한다
				/*매개변수 name이 있는 경우에는 이름+메세지
				 없는 경우에는 메세지만 클라이언트로 전송함*/
				if(name.equals("")) {
					it_out.println(msg);
				}
				else {
					it_out.println("["+name+"]"+ msg);
				}
			}
			catch (Exception e) {
				System.out.println("예외"+e);
			}
		}

	}	
	class MultiServerT extends Thread{
		
		//멤버변수
		Socket socket;
		PrintWriter out = null;//쓰고
		BufferedReader in = null;//읽고!
		
		//생성자: 소켓을 기반으로 입출력 스트림을 생성한다
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
			//클라이언트로부터 전송된 "대화명"을 저장할 변수
			String name = "";
			//메세지 저장용 변수
			String s = "";
			
			try {
				//클라이언트의 이름을 읽어와서 저장
				if(in!=null) {
					name = in.readLine();
					//접속한 클라이언트에게 새로운 사용자의 입장을 알림
					//접속자를 제외한 나머지 클라이언트에게 입장 메세지를 받는다
					sendAllMsg("", name+"님이 입장");
					
					//현재 접속한 클라이언트를 해쉬맵에 저장한다
					clientMap.put(name, out);
					
					//해쉬맵에 저장된 객체의 수로 접속자 수를 파악할 수 있다.
					System.out.println(name + "접속");
					System.out.println("현재 접속자 수는"+clientMap.size()+"입니다");
				}
				//클라이언트의 메세지를 읽어서 콘솔에 출력하고 Echo해준다
				//입력한 메세지는 모든 클라이언트에게 echo 됨
				while(in!=null) {
					s = in.readLine();
					if(s==null) break;
					
					System.out.println(name  + " >> " +s);
					sendAllMsg(name, s);
				}
			}
			catch (Exception e) {
				System.out.println("예외:" + e);
			}
			finally {
				/*클라이언트가 접속을 종료하면 예외가 발생하게 되어 finally로
				 넘어오게된다. 이때 "대화명"을 통해 remove() 시켜준다*/
				clientMap.remove(name);
				sendAllMsg("", name + "님이 퇴장함");
				
				//퇴장하는 클라이언트의 쓰레드명을 보여준다
				System.out.println(name+"["+
						Thread.currentThread().getName()+"]퇴장");
				System.out.println("현재접속자 수는"+clientMap.size()+"명입니다");
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
	}
}
