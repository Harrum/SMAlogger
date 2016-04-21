package spfspot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class misc 
{
	public static final short NaN_S16 =	(short) 0x8000;		// "Not a Number" representation for SHORT (converted to 0 by SBFspot)
	public static final short NaN_U16 = (short) 0xFFFF;		// "Not a Number" representation for USHORT (converted to 0 by SBFspot)
	public static final int NaN_S32	= (int) 0x80000000L;	// "Not a Number" representation for LONG (converted to 0 by SBFspot)
	public static final int NaN_U32 = (int) 0xFFFFFFFFL;	// "Not a Number" representation for ULONG (converted to 0 by SBFspot)
	public static final long NaN_S64 = 0x8000000000000000l;	// "Not a Number" representation for LONGLONG (converted to 0 by SBFspot)
	public static final long NaN_U64 = 0xFFFFFFFFFFFFFFFFl;	// "Not a Number" representation for ULONGLONG (converted to 0 by SBFspot)
	public static final String SYM_DEGREE = "\u00b0"; //"\302\260" for linux ?
	
	public static class VERBOSE
	{
		private static int verbose;
		
		public static boolean LOW;
		public static boolean NORMAL;
		public static boolean HIGH;
		public static boolean VERYHIGH;
		public static boolean HIGHEST;
		
		public static void SetVerbose(int verbose)
		{
			VERBOSE.verbose = verbose;
			LOW = verbose >= 1;
			NORMAL = verbose >= 2;
			HIGH = verbose >= 3;
			VERYHIGH = verbose >= 4;
			HIGHEST = verbose >= 5;
		}
		
		public static void PrintVerbose()
		{
			System.out.printf("Verbose is: %d\n", verbose);
			if(VERBOSE.LOW)
				System.out.println("Verbose is LOW");
			if(NORMAL)
				System.out.println("Verbose is NORMAL");
			if(HIGH)
				System.out.println("Verbose is HIGH");
			if(VERYHIGH)
				System.out.println("Verbose is VERYHIGH");
			if(HIGHEST)
				System.out.println("Verbose is HIGHEST");
		}
	}
	
	public final static class DEBUG
	{
		private static int debug;
		
		public static boolean LOW;
		public static boolean NORMAL;
		public static boolean HIGH;
		public static boolean VERYHIGH;
		public static boolean HIGHEST;
		
		public static void SetDebug(int debug)
		{
			DEBUG.debug = debug;
			LOW = debug >= 1;
			NORMAL = debug >= 2;
			HIGH = debug >= 3;
			VERYHIGH = debug >= 4;
			HIGHEST = debug >= 5;
		}
	}
	
	public static double tokWh(long value)
	{
		return value / 1000d;
	}
	
	public static float tokW(long value)
	{
		return value / 1000f;
	}
	
	public static double toHour(long value)
	{
		return value / 3600d;	//Make sure to divide by a double value
	}
	
	public static float toCelc(long value)
	{
		return value / 100f;
	}
	
	public static float toAmp(long value)
	{
		return value / 1000f;
	}
	
	public static float toVolt(long value)
	{
		return value / 100f;
	}
	
	public static float toHz(long value)
	{
		return value / 100f;
	}
	
	public static int intSwap(int i) {
		//return i;
	    return (i&0xff)<<24 | (i&0xff00)<<8 | (i&0xff0000)>>8 | (i>>24)&0xff;
	}
	
	public static short shortSwap(short s) {
		//return s;
		int b1 = s & 0xff;
	    int b2 = (s >> 8) & 0xff;

	    return (short) (b1 << 8 | b2 << 0);
	}
	
	public static String printDate(Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		return sdf.format(date);
	}
	
	public static int get_longbe(byte[] buf, int pos)
	{/*
		ByteBuffer bb;
		bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.position(pos);
		*/
		int lng = 0;
		//lng = bb.getInt();
		
		lng += buf[pos + 3];
		lng <<= 8;
		lng += buf[pos + 2];
		lng <<= 8;
		lng += buf[pos + 1];
		lng <<= 8;
		lng += buf[pos];
		
		return lng;
	}
	
	public static long get_longlongbe(byte[] buf, int pos)
	{
		/*
		ByteBuffer bb;
		bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.position(pos);
		*/
		long lnglng = 0;
		//lng = bb.getLong();
		lnglng += buf[pos + 7];
		lnglng <<= 8;
		lnglng += buf[pos + 6];
		lnglng <<= 8;
		lnglng += buf[pos + 5];
		lnglng <<= 8;
		lnglng += buf[pos + 4];
		lnglng <<= 8;
		lnglng += buf[pos + 3];
		lnglng <<= 8;
		lnglng += buf[pos + 2];
		lnglng <<= 8;
		lnglng += buf[pos + 1];
		lnglng <<= 8;
		lnglng += buf[pos];
		return lnglng;
	}
	
	public static long get_longlong(byte[] buf, int pos)
	{
		ByteBuffer bb;
		bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.position(pos);

		long lnglng = 0;
		lnglng = bb.getLong();
		return lnglng;
	}
	
	public static int get_long(byte[] buf, int pos)
	{
		ByteBuffer bb;
		bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.position(pos);
		
		int lng = 0;
		lng = bb.getInt();
		/*
		lng += buf[pos + 3];
		lng <<= 8;
		lng += buf[pos + 2];
		lng <<= 8;
		lng += buf[pos + 1];
		lng <<= 8;
		lng += buf[pos];
		*/
		return lng;
	}
	
	public static short get_short(byte[] buf, int pos)
	{
		ByteBuffer bb;
		bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.position(pos);
		
		short shrt = 0;
		shrt = bb.getShort();
		
		return shrt;
	}
	
	public static short get_shortbe(byte[] buf, int pos)
	{
		short shrt = 0;
		
		shrt += buf[pos];
		shrt <<= 8;
		shrt += buf[pos + 1];
		
		return shrt;
	}
	
	public static void HexDump(byte[] buf, int count, int radix)
	{
	    int i, j;
	    System.out.printf("--------:");
	    for (i=0; i < radix; i++)
	    {
	    	System.out.printf(" %02X", i);
	    }
	    for (i = 0, j = 0; i < count; i++)
	    {
	        if (j % radix == 0)
	        {
				/*
				if (i > 0)
				{
					for (int ii = radix; ii>0; ii--)
						System.out.print(((buf[i-ii] >= ' ') && (buf[i-ii] <= '~')) ? buf[i-ii] : '_');
				}*/
				
	            if (radix == 16)
	            	System.out.printf("\n%08X: ", j);
	            else
	            	System.out.printf("\n%08d: ", j);
	        }
	        System.out.printf("%02X ", buf[i]);
	        j++;
	    }
	    System.out.printf("\n");
	}
}
