
public class FileInfo {
	private String belongTo;
	private boolean visible;
	
	public FileInfo(String user, boolean op)
	{
		this.belongTo = user;
		this.visible = op;
	}
	
	// get series
	public String belongTo()
	{
		return this.belongTo;
	}
	
	public boolean isVisible()
	{
		return this.visible;
	}
	// set series
	public void setBelongTo(String user)
	{
		this.belongTo = user;
	}
	
	public void setVisible(boolean src)
	{
		this.visible = src;
	}
}
