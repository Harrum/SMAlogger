package spfspot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TagDefs 
{
	static boolean quiet;
	static int verbose;
	
	final static int READ_OK = 0;
	final static int READ_ERROR = -1;
	
	private class TD
	{
		private String m_tag; 	//label
		private int m_lri; 		//Logical Record Index
		private String m_desc; 	//Description
		
		public TD(String tag, int lri, String desc)
		{
			this.m_tag = tag;
			this.m_lri = lri;
			this.m_desc = desc;
		}
		
		public String getTag() 
		{ 
			return m_tag; 
		}
		
		public int getLRI()
		{ 
			return m_lri; 
		}
		
		public String getDesc()
		{ 
			return m_desc; 
		}
	}
	
	private Map<Integer, TD> m_tagdefmap = new HashMap<Integer, TD>();
	
	private boolean isverbose(int level)
	{
		if(!quiet && (verbose >= level)) return true;
		else return false;
	}
	
	private void print_error(String msg)
	{
		System.err.println("Error: " + msg + "\n");
	}
	
	private void print_error(String msg, int line, String fpath)
	{
		System.err.println("Error: " + msg + " on line " + line + " [" + fpath + "]\n");
	}
	
	private void add(int tagID, String tag, int lri, String desc)
	{
		m_tagdefmap.put(tagID, new TD(tag, lri, desc));
	}
	
	public int readall(String path, String locale)
	{
		locale = locale.toUpperCase();

		//Build fullpath to taglist<locale>.txt
		//Default to EN-US if localized file not found
		String fn_taglist = path + "\\support_files\\TagList" + locale + ".txt";
		File f = new File(fn_taglist);
		
		FileReader fr = null;
		try
		{
			 fr = new FileReader(fn_taglist);
		}
		catch(Exception e)
		{
			print_error("Could not open file " + fn_taglist);
			e.printStackTrace();
			
			if (!locale.equals("EN-US"))
			{
				if (isverbose(0)) System.out.println("Using default locale en-US\n");
				fn_taglist = path + "\\support_files\\TagListEN-US.txt";
				
				try
				{
					fr = new FileReader((fn_taglist));
				}
				catch(Exception ex)
				{
					print_error("Could not open file " + fn_taglist + e.getMessage());
					return READ_ERROR;
				}
			}
			
			else return READ_ERROR;
		}
		BufferedReader br = new BufferedReader(fr);
		String line;
		int lineCnt = 0;
		try 
		{
			while((line = br.readLine()) != null)
			{
				lineCnt++;
				
				//Get rid of comments and empty lines
				int hashpos = -1;
				if(line.startsWith("#") || line.startsWith("\r"))
				{
					hashpos = line.indexOf('#');
				}		
				if(hashpos == -1) hashpos = line.indexOf('\r');
				
				if (hashpos != -1) line = line.substring(0, hashpos);
				
				if(line.length() > 0)
				{
					//Split line TagID=Tag\Lri\Descr
					String[] lineparts;
					lineparts = line.split("[=\\\\]");
					if (lineparts.length != 4)
					{
						print_error("Wrong number of items", lineCnt, fn_taglist);
					}
					else
					{
						int entryOK = 1;
						int tagID = 0;
						try
						{
							tagID = Integer.parseInt(lineparts[0]);
						}
						catch(NumberFormatException e)
						{
							print_error("Invalid tagID", lineCnt, fn_taglist);
							entryOK = 0;
						}

						int lri = 0;
						try
						{
							lri = Integer.parseInt(lineparts[2]);
						}
						catch(NumberFormatException e)
						{
							print_error("Invalid LRI", lineCnt, fn_taglist);
							entryOK = 0;
						}

						if (entryOK == 1)
						{
							String tag = lineparts[1];
							tag = tag.trim();

							String descr = lineparts[3];
							descr = descr.trim();

							add(tagID, tag, lri, descr);
						}
					}
				}
			}
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try 
		{
			br.close();
			fr.close();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return READ_OK;
	}
	
	public String getTag(int tagID) 
	{ 
		return m_tagdefmap.get(tagID).getTag();
	}
	
	public int getTagIDForLRI(int LRI)
	{
		LRI &= 0x00FFFF00;
		for (Map.Entry<Integer, TD> entry : m_tagdefmap.entrySet())
		{
		    if (LRI == entry.getValue().getLRI())
		    	return entry.getKey();
		}
		return 0;
	}

	public String getTagForLRI(int LRI)
	{
		LRI &= 0x00FFFF00;
		for (Map.Entry<Integer, TD> entry : m_tagdefmap.entrySet())
		{
			if (LRI == entry.getValue().getLRI())
				return entry.getValue().getTag();
		}
		return "";
	}

	public String getDescForLRI(int LRI)
	{
		LRI &= 0x00FFFF00;
		for (Map.Entry<Integer, TD> entry : m_tagdefmap.entrySet())
		{
			if (LRI == entry.getValue().getLRI())
				return entry.getValue().getDesc();
		}
		return "";
	}
	
	
	public int getLRI(int tagID) 
	{ 
		return m_tagdefmap.get(tagID).getLRI();
	}
	
	public String getDesc( int tagID) 
	{ 
		return m_tagdefmap.get(tagID).getDesc();
	}
	
	public String getDesc(int tagID, String _default) 
	{
		return m_tagdefmap.get(tagID).getDesc() == null ? _default : m_tagdefmap.get(tagID).getDesc(); 
	}
	
	public int size()
	{
		return m_tagdefmap.size();
	}
}
