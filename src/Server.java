import java.util.*;
import java.io.*;
import java.net.*;

public class Server implements Runnable {
	
	public HashMap<String, FileInfo> FileList = null;
	public HashMap<String, UserInfo> UserList = null;
	public ServerSocket mother = null;
	//
	public String file_list = "./file";
	public String user_list = "./user";
	
	public Server(int port) throws IOException
	{
		this.mother = new ServerSocket(port);
		this.FileList = new HashMap<String, FileInfo>();
		this.UserList = new HashMap<String, UserInfo>();
		// init FileList
		File file_inst = new File(this.file_list);
		if(file_inst.exists())
		{
			ArrayList<String> raw = myUtil.readByLine(file_inst);
			for(int i = 0; i < raw.size(); i++)
			{
				String[] material = myUtil.splitLine(raw.get(i));
				FileInfo new_info = new FileInfo(material[1], false);
				this.FileList.put(material[0], new_info);
			}
		}
		// init UserList
		File user_inst = new File(this.user_list);
		if(user_inst.exists())
		{
			ArrayList<String> raw = myUtil.readByLine(user_inst);
			for(int i = 0; i < raw.size(); i++)
			{
				String[] material = myUtil.splitLine(raw.get(i));
				UserInfo new_info = new UserInfo(material[1], "-1", -1);
				//System.out.println(material[1] + material[1].length());
				this.UserList.put(material[0], new_info);
			}
		}
		//
		Thread ServerThr = new Thread(this);
		ServerThr.start();
	}
	
	public synchronized void stopMe()
	{
		try {
			this.mother.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Stop Central Server failed!");
			return;
		}
	}
	
	// Listen Thr
	public void run()
	{
		try {
			while(true)
			{
				Socket son = mother.accept();
				ServerProcess dosth = new ServerProcess(son, this);
				Thread dosthThr = new Thread(dosth);
				dosthThr.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Central Server has been stopped gracefully. Resources retrieved.");
			return;
		}
	}

}
