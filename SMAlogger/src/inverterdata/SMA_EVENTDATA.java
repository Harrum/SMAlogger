package inverterdata;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SMA_EVENTDATA
{
	public int DateTime;   // fix issue CP30
	public int EntryID;
	public int SUSyID;
	public int SerNo;
	public int EventCode;
	public int EventFlags;
	public int Group;
	public int ulong1;
	public int Tag;
	public int Counter;
	public int DT_Change;
	public int Parameter;
	public int NewVal;
	public int OldVal;
	
	public SMA_EVENTDATA(ByteBuffer bb)
	{
		bb.order(ByteOrder.LITTLE_ENDIAN); // I think ?
		DateTime = bb.getInt();
		EntryID = bb.getShort();
		SUSyID = bb.getShort();
		SerNo = bb.getInt();
		EventCode = bb.getShort();
		EventFlags = bb.getShort();
		Group = bb.getInt();
		ulong1 = bb.getInt();
		Tag = bb.getInt();
		Counter = bb.getInt();
		DT_Change = bb.getInt();
		Parameter = bb.getInt();
		NewVal = bb.getInt();
		OldVal = bb.getInt();
	}
	
	public static int GetSize()
	{
		//48u
		int size = 0;
		size += Integer.BYTES;
		size += Short.BYTES;
		size += Short.BYTES;
		size += Integer.BYTES;
		size += Short.BYTES;
		size += Short.BYTES;
		size += Integer.BYTES;
		size += Integer.BYTES;
		size += Integer.BYTES;
		size += Integer.BYTES;
		size += Integer.BYTES;
		size += Integer.BYTES;
		size += Integer.BYTES;
		size += Integer.BYTES;
		return size;
	}
}
