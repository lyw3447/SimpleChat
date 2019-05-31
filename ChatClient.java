//https://github.com/lyw3447/SimpleChat/blob/master/ChatServer.java

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class ChatClient {

	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("Usage : java ChatClient <username> <server-ip>");
			System.exit(1);
		}
		Socket sock = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		boolean endflag = false;
		try{
			sock = new Socket(args[1], 10001); //그냥 socket 생성 <ip주소 /socket 번호> server의 accept()로 간다.
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream())); //server로 보낼 output 
			br = new BufferedReader(new InputStreamReader(sock.getInputStream())); //server에서 읽어올 input
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); //입력하는 문자를 문자 스트림으로 변환하는 객체 생성
			// send username.
			pw.println(args[0]);
			pw.flush(); //server에 pw를 통해 id를 보내고 buffer 비우기 
			InputThread it = new InputThread(sock, br);
			it.start();
			String line = null;
			while((line = keyboard.readLine()) != null){ //키보드로부터 한줄씩 입력받아 line에 저장 
				pw.println(line);
				pw.flush(); //pw를 통해 server에 내용 보내기 
				if(line.equals("/quit")){ //그 뒤에 quit이면 멈추기 
					endflag = true;
					break;
				}
			}
			System.out.println("Connection closed.");
			
		}catch(Exception ex){
			if(!endflag)
				System.out.println(ex);
		}finally{
			try{
				if(pw != null)
					pw.close(); //print writter 끝내기 
			}catch(Exception ex){}
			try{
				if(br != null)
					br.close(); //buffered reader 끝 
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close(); //sock 끝 
			}catch(Exception ex){}
		} // finally
	} // main
} // class

class InputThread extends Thread{
	private Socket sock = null;
	private BufferedReader br = null; //server에서 보낸 message를 읽어들임 
	
	public InputThread(Socket sock, BufferedReader br){
		this.sock = sock;
		this.br = br;
	} //constructor
	
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				System.out.println(line);
			}
		}catch(Exception ex){
		}finally{
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // InputThread
	
}
