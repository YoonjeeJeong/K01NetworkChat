package chat8;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

public class MultiServer extends ConnectDB{	

	static ServerSocket serverSocket = null;
	static Socket socket=null;
	//클라이언트 정보 저장을 위한 맵 컬렉션 생성!
	Map<String, PrintWriter> clientMap; 
	
	//생성자
	public MultiServer() {	
		super();
		//클라이언트의 이름과 출력 스트림을 저장할 hashmap생성
		clientMap = new HashMap<String, PrintWriter>();
		//hashmap동기화 설정. 쓰레드가 사용자 정보에 동시에 접근하는 것을 차단한다.
		Collections.synchronizedMap(clientMap);
	}
	//서버 초기화하는 메소드 init()
	public void init() {
		try {
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다");
			
			while(true) {
				socket = serverSocket.accept();
				
				/* 클라이언트의 메세지를 모든 클라이언트에게 전달하기 위한 쓰레드 
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
				serverSocket.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}	
	public static void main(String[] args) {
		
		MultiServer ms = new MultiServer();
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
							(PrintWriter)clientMap.get(it.next());
					/*매개변수 name이 있는 경우에는 이름+메세지
					 없는 경우에는 메세지만 클라이언트로 전송함*/
					if(name.equals("")) {
						it_out.println(URLEncoder.encode(msg, "UTF-8"));
					}
					else {
						it_out.println("["+name+"]:"+msg);
					}
				}
				catch (Exception e) {
					System.out.println("예외: "+e);
				}
			}
		}
		//귓속말 메소드
		public void sendPrivMsg(String from_who, String to_who, String toMsg) {
			//Map에 저장된 객체의 키값(이름)을 먼저 얻어온다
			Iterator<String> it = clientMap.keySet().iterator();		
			
			//저장된 객체(클라이언트)의 갯수만큼 반복한다
			while(it.hasNext()) {
				try {
									
					if(to_who.equals(it.next())) {
						PrintWriter it_out = (PrintWriter)clientMap.get(to_who);
						it_out.println(from_who +"님이 보낸 귓속말:"+toMsg);
					}
				}
				catch (Exception e) {
					System.out.println("예외" + e);
				}
			}
		}//귓속말 끝
		
		class MultiServerT extends Thread {
			
			//멤버변수
			Socket socket;
			PrintWriter out = null;//쓰고
			BufferedReader in = null;//읽는다
			Scanner scan = new Scanner(System.in);
			
			//생성자: 소켓을 기반으로 입출력 스트림을 생성한다
			public MultiServerT(Socket socket) {
				this.socket = socket;
				
				try {
					out = new PrintWriter(this.socket.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader
							(this.socket.getInputStream(), "UTF-8"));
				}
				catch(Exception e) {
					System.out.println("예외발생: "+e);
				}
			}
			@Override
			public void run() {
				String name ="";//대화명 저장
				String s="";//메세지 저장용 변수
				
				try {
					//클라이언트의 이름을 읽어와서 저장
					name = in.readLine();
					name = URLDecoder.decode(name, "UTF-8");
					
					//접속한 클라이언트에게 새로운 사용자의 입장을 알림
					//접속자를 제외한 나머지 클라이언트에게 입장 메세지를 받는다
					sendAllMsg("", name +"님이 입장하셨습니다"  );
					
					//현재 접속한 클라이언트를 해쉬맵에 저장한다
					clientMap.put(name, out);
					
					//해쉬맵에 저장된 객체의 수로 접속자 수를 파악할 수 있다.
					System.out.println(name+"접속함");
					System.out.println("현재 접속자 수는 "+clientMap.size()+"명입니다");
				
					//입력한 메세지는 모든 클라이언트에게 echo 됨
					while(in!=null) {
						
						s=in.readLine();
						s=URLDecoder.decode(s, "UTF-8");
						System.out.println(name + ">> "+s);
						System.out.println(s);
					
						if(s==null) break;
						////added - 리스트 출력
						Iterator<String> mapMsg = clientMap.keySet().iterator();
						if(s.startsWith("/")) {
							
							if(s.substring(0).startsWith("/list")) {
								out.println("현재 접속자 리스트: ");
								while(mapMsg.hasNext()) {
									String keys = mapMsg.next();
									out.println("["+ keys + "]");
								}
							}
							//귓속말
							else if(s.substring(0).startsWith("/to")) {
								String[] sArray = s.split(" ");//공백으로 배열을 나눈다.
								/*
								 split()으로 문장 "/to Lala Hello"를 공백으로 나눈다.
								 0번 배열은 /to, 1번 배열은 Lala, 2번은 Hello가 된다
								 */
								String to_who= sArray[1];//to_who(받는이)는 split을 썼으므로 1번이 된다
									sendPrivMsg(name, to_who, s);
								}
								out.println();
							}
						else {
							sendAllMsg(name, s);
						}////added
					}
						
						String query = "INSERT into chating_tb values(seq_chating.nextval, ?, ?, sysdate)";
				
						psmt = con.prepareStatement(query);
						
						psmt.setString(1, name);
						psmt.setString(2, s);
						
						psmt.executeUpdate();
						//System.out.println(name + ">> "+s);
						
					}catch (Exception e) {
					System.out.println("예외: "+ e);
				}
				finally {
				/*클라이언트가 접속을 종료하면 예외가 발생하게 되어 finally로
				 넘어오게된다. 이때 "대화명"을 통해 remove() 시켜준다*/		
					clientMap.remove(name);
					sendAllMsg("", name +"님이 퇴장하셨습니다");
					
					//퇴장하는 클라이언트의 쓰레드명을 보여준다
					System.out.println(name + "[" + 
							Thread.currentThread().getName()+"]"+"퇴장");
					System.out.println("현재 접속자 수는 "+clientMap.size()+"명입니다");
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
		void execute(){
	}

}