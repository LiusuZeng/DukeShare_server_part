import java.util.*;
import java.net.*;
import java.io.*;

public class ServerProcess implements Runnable {
	private Socket my_sock;
	private Server my_sv;

	public ServerProcess(Socket src, Server sv_src)
	{
		this.my_sock = src;
		this.my_sv = sv_src;
	}

	public void run()
	{
		// try
		InputStream is;
		OutputStream os;
		StringBuilder content = new StringBuilder();
		try {
			is = this.my_sock.getInputStream();
			byte[] b = new byte[256];
			is.read(b);
			// read res
			for(int i = 0; i < b.length && (char)(b[i]) != '\0'; i++)
			{
				content.append((char)(b[i]));
				//System.out.println(b[i]);
			}
			String res = content.toString();
			System.out.println("[DEBUG]\n Received " + res + "req from: " + this.my_sock.getInetAddress().getHostAddress() + "\n[DEBUG]");
			//System.out.println(res);
			// split
			String[] res_parse = res.split(" ");
			// branch of cmd
			if(res_parse[0].toLowerCase().equals("login"))
			{
				assert(res_parse.length >= 3);
				String name = res_parse[1];
				String password = res_parse[2];
				String[] ipport = name.split("/");
				//
				System.out.println(Integer.parseInt(ipport[1]));
				// check user name
				if(!this.my_sv.UserList.containsKey(name))
				{
					String msg = name + ": does not exist!";
					os = this.my_sock.getOutputStream();
					os.write(msg.getBytes());
					os.flush();
					return;
				}
				synchronized(this.my_sv) // access server user table
				{
					// check user password
					if(!this.my_sv.UserList.get(name).getPW().equals(password))
					{
						String msg = name + ": incorrect password!";
						os = this.my_sock.getOutputStream();
						os.write(msg.getBytes());
						os.flush();
						return;
					}
					//
					UserInfo new_entry = new UserInfo(password, this.my_sock.getInetAddress().getHostAddress(), Integer.parseInt(ipport[1]));
					this.my_sv.UserList.put(name, new_entry);
					// update file info
					Iterator<Map.Entry<String, FileInfo>> itr = this.my_sv.FileList.entrySet().iterator();
					while(itr.hasNext())
					{
						Map.Entry<String, FileInfo> entry = (Map.Entry<String, FileInfo>)itr.next();
						if(entry.getValue().belongTo().equals(name)) entry.getValue().setVisible(true);
					}
				}
				//
				String msg = name + " logged in!";
				os = this.my_sock.getOutputStream();
				os.write(msg.getBytes());
				os.flush();
				return;
			}
			else if(res_parse[0].toLowerCase().equals("register"))
			{
				assert(res_parse.length >= 3);
				String name = res_parse[1];
				String password = res_parse[2];
				// check if this name can be used
				if(this.my_sv.UserList.containsKey(name))
				{
					String msg = "User name already exists!";
					byte[] data = msg.getBytes();
					os = this.my_sock.getOutputStream();
					os.write(data);
					os.flush();
					return;
				}
				// actual registration (memory)
				synchronized(this.my_sv)
				{
					UserInfo new_entry = new UserInfo(password, "-1", -1); // need to login again
					this.my_sv.UserList.put(name, new_entry);
				}
				// actual registration (disk)
				File user_inst = new File(this.my_sv.user_list);
				if(!user_inst.exists()) user_inst.createNewFile();
				String new_user = name + " " + password + "\n";
				myUtil.writeFile(user_inst, new_user, true);
				// feedback
				String msg = "Registration completed! Please login.";
				byte[] data = msg.getBytes();
				os = this.my_sock.getOutputStream();
				os.write(data);
				os.flush();
				return;
			}
			else if(res_parse[0].toLowerCase().equals("upload"))
			{
				assert(res_parse.length >= 3);
				String user_name = res_parse[1];
				String file_name = res_parse[2];
				// check if this file can be uploaded
				if(this.my_sv.FileList.containsKey(file_name))
				{
					String msg = "File already exists!";
					byte[] data = msg.getBytes();
					os = this.my_sock.getOutputStream();
					os.write(data);
					os.flush();
					return;
				}
				// actual upload (memory)
				synchronized(this.my_sv)
				{
					this.my_sv.FileList.put(file_name, new FileInfo(user_name, true));
				}
				// actual upload (disk)
				File file_inst = new File(this.my_sv.file_list);
				if(!file_inst.exists()) file_inst.createNewFile();
				String new_file = file_name + " " + user_name + "\n";
				myUtil.writeFile(file_inst, new_file, true);
				// feedback
				String msg = "Upload succeeded!";
				byte[] data = msg.getBytes();
				os = this.my_sock.getOutputStream();
				os.write(data);
				os.flush();
				return;
			}
			else if(res_parse[0].toLowerCase().equals("modify"))
			{
				String user_name = res_parse[1];
				String file_name = res_parse[2];
				String original_user = null;
				//
				if(!this.my_sv.FileList.containsKey(file_name))
				{
					String msg = "File does not exist! Cannot be modified!";
					byte[] data = msg.getBytes();
					os = this.my_sock.getOutputStream();
					os.write(data);
					os.flush();
					return;
				}
				// modify memory
				boolean ret = false;
				synchronized(this.my_sv)
				{
					if(!this.my_sv.FileList.get(file_name).isVisible())
					{
						String msg = "Owner disappeared. Modification failed.";
						os = this.my_sock.getOutputStream();
						os.write(msg.getBytes());
						os.flush();
						ret = true;
					}
					else
					{
						original_user = this.my_sv.FileList.get(file_name).belongTo();
						this.my_sv.FileList.get(file_name).setBelongTo(user_name);
					}
				}
				if(ret) return;
				// modify disk
				File file_inst = new File(this.my_sv.file_list);
				if(file_inst.exists())
				{
					file_inst.delete();
					file_inst.createNewFile();
					Iterator<Map.Entry<String, FileInfo>> itr = this.my_sv.FileList.entrySet().iterator();
					while(itr.hasNext())
					{
						Map.Entry<String, FileInfo> entry = (Map.Entry<String, FileInfo>)itr.next();
						String file = entry.getKey() + " " + entry.getValue() + "\n";
						myUtil.writeFile(file_inst, file, true);
					}
				}
				// notify original user
				String org_ip = this.my_sv.UserList.get(original_user).getIP();
				int org_port = this.my_sv.UserList.get(original_user).getport();
				Socket notification = new Socket();
				notification.setSoTimeout(3000);
				notification.connect(new InetSocketAddress(org_ip, org_port));
				String notice = "Your file: " + file_name + "has been modified by " + user_name;
				OutputStream n_os = notification.getOutputStream();
				n_os.write(notice.getBytes());
				n_os.flush();
				// feedback
				String msg = "Modification succeeded!";
				byte[] data = msg.getBytes();
				os = this.my_sock.getOutputStream();
				os.write(data);
				os.flush();
				return;
			}
			/***************************IMPORTANT****************************/
			else if(res_parse[0].toLowerCase().equals("download"))
			{
				String user_name = res_parse[1];
				String file_name = res_parse[2];
				String randomStr = res_parse[3];
				StringBuilder SecMsg = new StringBuilder();
				SecMsg.append(user_name + file_name + randomStr + "\n");
				// notify the owner
				String org_ip = this.my_sv.UserList.get(this.my_sv.FileList.get(file_name).belongTo()).getIP();
				int org_port = 	this.my_sv.UserList.get(this.my_sv.FileList.get(file_name).belongTo()).getport();
				Socket reqInfo = new Socket();
				reqInfo.setSoTimeout(3000);
				reqInfo.connect(new InetSocketAddress(org_ip, org_port));
				OutputStream ri_os = reqInfo.getOutputStream();
				ri_os.write(SecMsg.toString().getBytes());
				ri_os.flush();
				InputStream ri_is = reqInfo.getInputStream();
				byte[] flowcontrol = new byte[256];
				ri_is.read(flowcontrol);
				ri_is.close();
				// At this point, the owner knows the existence of this-time download request.
				// feedback with downloading point
				StringBuilder DnldMsg = new StringBuilder();
				DnldMsg.append(org_ip + org_port + "\n");
				os = this.my_sock.getOutputStream();
				os.write(DnldMsg.toString().getBytes());
				os.flush();
				return;
			}
			/***************************IMPORTANT****************************/
			else if(res_parse[0].toLowerCase().equals("cancel"))
			{
				String user_name = res_parse[1];
				String file_name = res_parse[2];
				//
				if(!this.my_sv.FileList.containsKey(file_name))
				{
					String msg = "File does not exist! Cannot be cancelled!";
					byte[] data = msg.getBytes();
					os = this.my_sock.getOutputStream();
					os.write(data);
					os.flush();
					return;
				}
				// clear memory
				synchronized(this.my_sv)
				{
					this.my_sv.FileList.remove(file_name);
				}
				// clear disk
				File file_inst = new File(this.my_sv.file_list);
				if(file_inst.exists())
				{
					file_inst.delete();
					file_inst.createNewFile();
					Iterator<Map.Entry<String, FileInfo>> itr = this.my_sv.FileList.entrySet().iterator();
					while(itr.hasNext())
					{
						Map.Entry<String, FileInfo> entry = (Map.Entry<String, FileInfo>)itr.next();
						String file = entry.getKey() + " " + entry.getValue() + "\n";
						myUtil.writeFile(file_inst, file, true);
					}
				}
				// feedback
				String msg = "Cancellation succeeded!";
				byte[] data = msg.getBytes();
				os = this.my_sock.getOutputStream();
				os.write(data);
				os.flush();
				return;
			}
			else if(res_parse[0].equals("ls")) // might be "ls"
			{
				StringBuilder list_res = new StringBuilder();
				Iterator<Map.Entry<String, FileInfo>> itr = this.my_sv.FileList.entrySet().iterator();
				while(itr.hasNext())
				{
					Map.Entry<String, FileInfo> entry = (Map.Entry<String, FileInfo>)itr.next();
					if(entry.getValue().isVisible()) list_res.append(entry.getKey() + " ");
				}
				if(list_res.length() != 0)
				{
					list_res.deleteCharAt(list_res.length()-1);
					list_res.append('\0');
				}
				byte[] data = list_res.toString().getBytes();
				os = this.my_sock.getOutputStream();
				os.write(data);
				os.flush();
				return;
			}
			else if(res_parse[0].toLowerCase().equals("quit"))
			{
				String name = res_parse[1];
				synchronized(this.my_sv)
				{
					Iterator<Map.Entry<String, FileInfo>> itr = this.my_sv.FileList.entrySet().iterator();
					while(itr.hasNext())
					{
						Map.Entry<String, FileInfo> entry = (Map.Entry<String, FileInfo>)itr.next();
						if(entry.getValue().belongTo().equals(name)) entry.getValue().setVisible(false);
					}
				}
				return;
			}
			else
			{
				System.out.println("Not supported!");
				return;
			}
			//
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
