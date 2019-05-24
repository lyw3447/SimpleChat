//https://github.com/lyw3447/SimpleChat

import java.net.*;
import java.io.*;

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
			sock = new Socket(args[1], 10001);
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//define bufferedReader
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
			// send username
			pw.println(args[0]);
			pw.flush();
			InputThread it = new InputThread(sock, br);
			it.start();
			String line = null;
			//set forbidden words
			String[] list = {"aaaaa", "bbbbb", "ccccc", "ddddd", "eeeee"};
			
			while((line = keyboard.readLine()) != null){
				boolean flag = true;
				//forbidden words가 있는지 체크하기
				for (String word : list) {
					if(word != null && line.equals(word)) {
						System.out.println("Opps... It's forbidden words. Please try again!\n");
						flag = false;
						break;
					}
				}
				if (flag == true) {
					pw.println(line);
					pw.flush();
					if(line.equals("/quit")){
						endflag = true;
						break;
					}		
				}

			}
			System.out.println("Connection closed.");
		}catch(Exception ex){
			if(!endflag)
				System.out.println(ex);
		}finally{
			try{
				if(pw != null)
					pw.close();
			}catch(Exception ex){}
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		} // finally
	} // main
} // class

class InputThread extends Thread{
	private Socket sock = null;
	private BufferedReader br = null;
	public InputThread(Socket sock, BufferedReader br){
		this.sock = sock;
		this.br = br;
	}
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
