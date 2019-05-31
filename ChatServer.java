//https://github.com/lyw3447/SimpleChat/blob/master/ChatServer.java

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class ChatServer {

	public static void main(String[] args) {
		try{
			//서버소켓 생성 
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			//hash map 생성 
			HashMap hm = new HashMap();
			while(true){ 
				Socket sock = server.accept(); //client의 요청 대기하는 .accept() 10001로 누가 들어오면 받아들여 socket 생성  
				ChatThread chatthread = new ChatThread(sock, hm); //chatthread instance 생성 
				chatthread.start();
			} // while
		}catch(Exception e){
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
	private static ArrayList<String> spam = new ArrayList<String>();
	
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream())); //client에 정보를 보냄 
			br = new BufferedReader(new InputStreamReader(sock.getInputStream())); //client에서 오는 정보를 읽어들임 
			id = br.readLine(); //한줄씩 읽어들이는 readLine(). client에서 제일 처음 id를 보낸다.  -->\n을 만날 때 까지 읽음
			String tempDate = CurrTime();
			broadcast(tempDate + id + " entered.");
			System.out.println(tempDate+"[Server] User (" + id + ") entered.");
			
			synchronized(hm){ //hm을 사용할 땐 꼭 해야하는 것 같다 
				hm.put(this.id, pw); //id와 print writer의 주솟값을 hashMap에 저장한다 
			}
			initFlag = true;
		}catch(Exception ex){
			String tempDate = CurrTime();
			System.out.println(tempDate+ex);
		}
	} // construcor
	
	public void run(){
		try{
			String line = null;
			String str = null;
			String tempDate = CurrTime();
			
			while((line = br.readLine()) != null){ //client에서 오는 정보가 null이 아닐 까지 line을 저장 
				if(line.equals("/quit"))
					break;
				if((str = checkword(line))!= null){ //금지어 check 
					warning(tempDate + str);
				}
				else if(line.equals("/userlist")){
					senduserlist();
				}
				else if(line.indexOf("/to ") == 0){ //'/to'의 index가 0일 때 
					sendmsg(line);
				}
				else if (line.equals("/spamlist")) {
					spamlist(line);
				}
				else if(line.indexOf("/addspam") == 0) {
					addspam(line);
				}else
					broadcast(tempDate + id + " : " + line);
			}
		}catch(Exception ex){
			String tempDate = CurrTime();
			System.out.println(tempDate+ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	private void senduserlist(){
		int j = 1;
		PrintWriter pw = null;
		Object obj = null;
		Iterator<String> iter = null;
		String tempDate = CurrTime();
		synchronized(hm){
			iter = hm.keySet().iterator(); //iterator를 이용해 id list를 저장 
			obj = hm.get(id); //해당 id를 가져와 obj에 저장 
		}
		if(obj != null){
				pw = (PrintWriter)obj; //id를 찾으면 해당 client에게 보낼 output stream -> pw 설정  
		}
		pw.println("<User list>");
		while(iter.hasNext()){
				String list = (String)iter.next(); //user id 하나 하나 list에 저장 
				pw.println(CurrTime()+j+". "+list);
				j++; //갯수 check 
		}
		j--;
		pw.println(CurrTime()+"Total : " + j+".");
		pw.flush(); //현재 버퍼에 저장되어 있는 내용을 client로 전송하고 버퍼를 비움 
	} //userList()

	public String checkword(String msg){
		
		synchronized(spam) {
			for(String w:spam){
				if (msg.contains(w))	// 단어가 있는지 확인
					return w;
			}
		}
		return null;
		
	}//check word()

	public void addspam(String msg) {
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		String str = null;
		String word = msg.substring(end+1);	//금지어 
		synchronized (hm)
		{
			PrintWriter pw = (PrintWriter)hm.get(id);
			if ((str = checkword(msg))!= null) {	// 금지어가 이미 존재하면 
				pw.println(CurrTime()+word+" 이미 존재합니다!");
				pw.flush();
			}
			synchronized(spam)					// 다른 스레드 접근을 막기 
			{	
				spam.add(word);
			}
			pw.println(CurrTime()+word+" 저장 완료!");
			pw.flush();
		}
	}
	
	public void spamlist(String msg) {

		synchronized(hm) {
			PrintWriter pw = (PrintWriter)hm.get(id);
			String tempDate = CurrTime();
			pw.println(tempDate+"<Spam List>");
			pw.flush();
			synchronized(spam)
			{
				for (String word : spam){
					pw.println(CurrTime()+word);
					pw.flush();
				}
			}
		}
	}
	
	public void warning(String msg){
		Object obj = hm.get(id); //id list가 저장되어 있는 hash map에서 id에 해당하는 object를 찾아 저장 

		if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println("Don't use "+ msg);
				pw.flush();
		} // if
	}//해당 word를 알려주는 function
	
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		String tempDate = CurrTime();
		if(end != -1){
			String to = msg.substring(start, end); //상대방 
			String msg2 = msg.substring(end+1); //내용 
			Object obj = hm.get(to);

			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(tempDate + id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values(); //각 client에 대한 pw들의 모음
			Iterator iter = collection.iterator(); //pw들의 모음에 순서대로 접근할 수 있는 iterator
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next(); //다음 pw 
				PrintWriter pw2 = (PrintWriter)hm.get(id); //자기 id에 대한 object
				if(pw==pw2) continue; //반복문의 끝으로 감  (자기 차례는 건너뛰기)
				pw.println(msg);
				pw.flush();
			}
		}
	} // broadcast
	
	public String CurrTime()
	{
		SimpleDateFormat Format = new SimpleDateFormat("[HH:mm:ss]");
		Date nowDate = new Date();
		String tempDate = Format.format(nowDate);
		tempDate += " ";
		return tempDate;
	} //print the current time. 모든 class안에서 사용할 수 있도록 
}