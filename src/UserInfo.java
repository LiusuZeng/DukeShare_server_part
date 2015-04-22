
public class UserInfo {
	private String password;
	private String IP;
	private int port;
	
	public UserInfo(String pw_src, String IP_src, int port_src)
	{
		this.password = pw_src;
		this.IP = IP_src;
		this.port = port_src;
	}
	
	// get series
	public String getPW()
	{
		return this.password;
	}
	
	public String getIP()
	{
		return this.IP;
	}
	
	public int getport()
	{
		return this.port;
	}
	// set series
	public void setPW(String src)
	{
		this.password = new String(src);
	}
	
	public void setIP(String src)
	{
		this.IP = new String(src);
	}
	
	public void setPort(int src)
	{
		this.port = src;
	}
}