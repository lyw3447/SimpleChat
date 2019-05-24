//https://github.com/lyw3447/SimpleCha

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e) {
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		
		try{
			String line = null;
			
			while((line = br.readLine()) != null){
				boolean flag = true;
				if(line.equals("/quit"))
					break;
				if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}
				if(line.equals("/userlist")) //calling 'send_userlist()'
					send_userlist();
				else {
					broadcast(id + " : " + line);
				}
					
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				;
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to); //해당하는 pw의 주솟값을 리턴
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			Object obj = hm.get(id);
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				
				//최근 날라온 pw를 제외하고 msg를 보낸다 
				if (pw != (PrintWriter)obj) {
					pw.println(msg); //모든 pw한테 보낸다
					pw.flush();
				}
			}
		}
	} // broadcast
	
	// userlist 출력 method
	// 내아이디에 해당되는 pw가 뭔지 찾아서 그곳에만 내용을 보낸다
	// collection에 key들을 모아 저장한 후 iterator을 이용해 하나씩 보낸다.
	public void send_userlist() { 
		Collection collection = hm.keySet();
		Iterator iter = collection.iterator();
		int num = 0;
		Object obj = hm.get(id); //내 아이디에 맞는 pw에 넣어주어야 한다.
		
		if (obj != null){
			PrintWriter pw = (PrintWriter)obj;
			while(iter.hasNext()) {
				pw.println(iter.next()); 
				pw.flush();
				num += 1;
			}
			pw.println("Total number : " + num);
			pw.flush();
		}
	} 
}
