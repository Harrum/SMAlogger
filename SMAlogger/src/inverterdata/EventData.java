package inverterdata;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Comparator;
import java.util.Date;

public class EventData 
{
	private Date m_DateTime;
	private int m_EntryID;
	private int m_SUSyID;
	private int m_SerNo;
	private int m_EventCode;
	private int m_EventFlags;
	private int m_Group;
	private int m_Tag;
	private int m_Counter;
	private int m_DT_Change;
	private int m_Parameter;
	private int m_NewVal;
	private int m_OldVal;
	private int m_UserGroup;

	public EventData(long UserGroup, SMA_EVENTDATA ev)
	{
		m_DateTime = new Date(ev.DateTime);
		m_EntryID = ev.EntryID;
		m_SUSyID = ev.SUSyID;
		m_SerNo = ev.SerNo;
		m_EventCode = ev.EventCode;
		m_EventFlags = ev.EventFlags;
		m_Group = ev.Group;
		m_Tag = ev.Tag;
		m_Counter = ev.Counter;
		m_DT_Change = ev.DT_Change;
		m_Parameter = ev.Parameter;
		m_NewVal = ev.NewVal;
		m_OldVal = ev.OldVal;
		m_UserGroup = (int) UserGroup;
	}

	public Date DateTime() 
	{ 
		return m_DateTime; 
	}

	public int EntryID() 
	{ 
		return m_EntryID; 
	}

	public int SUSyID() 
	{ 
		return m_SUSyID; 
	}

	public int SerNo() 
	{ 
		return m_SerNo; 
	}
	public int EventCode() 
	{ 
		return m_EventCode; 
	}
	
	public int EventFlags() 
	{ 
		return m_EventFlags; 
	}
	
	public int Group() 
	{ 
		return GroupTagID(); 
	}
	
	public int Tag()  
	{ 
		return m_Tag; 
	}
	
	public int Counter()  
	{ 
		return m_Counter; 
	}
	
	public int DT_Change()  
	{ 
		return m_DT_Change; 
	}
	
	public int Parameter()  
	{ 
		return m_Parameter; 
	}
	
	public int NewVal()  
	{ 
		return m_NewVal; 
	}
	
	public int OldVal()  
	{ 
		return m_OldVal; 
	}
	
	public int UserGroup()  
	{ 
		return m_UserGroup; 
	}
	
	public int GroupTagID()
	{
		int GroupDefOffset = 829;
		int GroupDef = m_Group & 0x1F;
		if ((GroupDef > 0) && (GroupDef <= 17))
			return GroupDef + GroupDefOffset;	 // 830 = LriGrpStt (Status)
		else
			return 0;
	}

	public int UserGroupTagID()
	{
		if (m_UserGroup == 0x07) // UG_USER
			return 861;	// Usr
		else if (m_UserGroup == 0x0A) //UG_INSTALLER
			return 862;	// Istl
		else
			return 0;	// Should never happen
	}

	public String EventType()
	{
	    switch (m_EventFlags & 3)
	    {
			case 0: return "Incoming";
			case 1: return "Outgoing";
			case 2: return "Event";
			case 3: return "Acknowledge";
			case 4: return "Reminder";
			case 7: return "Invalid";
			default: return "N/A";
	    }
	}

	public String EventCategory()
	{
		switch ((m_EventFlags >> 14) & 3)
	    {
			case 0: return "Info";
			case 1: return "Warning";
			case 2: return "Error";
			default: return "None";
	    }
	}
	
	public int DataType() 
	{ 
		return m_Parameter >> 24; 
	}
	protected Boolean SortEntryID_Asc(EventData ed1, EventData ed2) 
	{ 
		return ed1.m_EntryID < ed2.m_EntryID; 
	}
	
	protected Boolean SortEntryID_Desc(EventData ed1, EventData ed2) 
	{ 
		return ed1.m_EntryID > ed2.m_EntryID; 
	}
}
