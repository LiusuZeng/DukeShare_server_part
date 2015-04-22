import java.util.*;
import java.io.*;
import java.net.*;

public class myUtil {

	public static ArrayList<String> readByLine(File src) throws IOException
	{
		ArrayList<String> ret = new ArrayList<String>(0);
		FileReader reader = new FileReader(src);
		BufferedReader br = new BufferedReader(reader);
		String new_entry = null;
		//
		while((new_entry = br.readLine()) != null)
		{
			ret.add(new_entry);
		}
		//
		br.close();
		reader.close();
		//
		return ret;
	}
	
	public static void writeFile(File src, String content, boolean append) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(src, append));
		bw.write(content);
		bw.flush();
		bw.close();
	}
	
	public static String[] splitLine(String src)
	{
		return src.split(" ");
	}	
}
