package chat6;

import java.net.Socket;
import java.util.Scanner;

public class MultiClient {

	public static void main(String[] args) {

		System.out.print("이름을 입력하세요");
		Scanner scanner = new Scanner(System.in);
		String s_name = scanner.nextLine();
		
		//sender가 기능을 가져가므로 여기서는 필요없다
		//PrintWriter out =null;
		//리시버가 기능을 가져가므로 여기서 필요없다카이
		//BufferReader in = null;
		
		try {
			String ServerIP = "localhost";
			if(args.length > 0) {
				ServerIP = args[0];
			}
			Socket socket = new Socket(ServerIP, 9999);
			System.out.println("서버와 연결되었습니다");
			
			//서버에서 보내는 Echo메세지를 클라이언트에 출력하기 위한 쓰레드 생성
			Thread receiver = new Receiver(socket);
			receiver.start();
			
			//클라이언트의 메세지를 서버로 전송해주는 쓰레드 형성
			Thread sender = new Sender(socket, s_name);
			sender.start();
		}
		catch (Exception e) {
			System.out.println("예외발생[MultiClient]"+e);
		}
	}

}
