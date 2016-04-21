package spfspot;

import java.util.Date;
import java.util.TimeZone;

public class Config 
{
	public enum CONNECTIONTYPE
	{
		CT_NONE ( 0 ),
		CT_BLUETOOTH ( 1 ),
		CT_ETHERNET ( 2 );
		
		private int Value;
		
		private CONNECTIONTYPE(int value)
		{
			this.Value = value;
		}
	}
	
	private final int MAX_PATH = 260;
	
	public String	ConfigFile;			//Fullpath to configuration file
	public String	AppPath;
	public String	BT_Address;			//Inverter bluetooth address 12:34:56:78:9A:BC
    public String	IP_Address;			//Inverter IP address 192.168.178.123 (for Speedwirecommunication )
    public int		BT_Timeout;
    public int		BT_ConnectRetries;
	public short   IP_Port;
	public CONNECTIONTYPE ConnectionType;     // CT_BLUETOOTH | CT_ETHERNET
	public char[]	SMA_Password = new char[13];
    public float	latitude;
    public float	longitude;
    public Date	archdata_from;
    public Date	archdata_to;
    public char	delimiter;			//CSV field delimiter
    public int		precision;			//CSV value precision
    public char	decimalpoint;		//CSV decimal point
    public String	outputPath;
    public String	outputPath_Events;
    public String	plantname;
    public String sqlDatabase;
    public String sqlHostname;
    public String sqlUsername;
    public String sqlUserPassword;
	public int		synchTime;				// 1=Synch inverter time with computer time (default=0)
	public float	sunrise;
	public float	sunset;
	public int		isLight;
	public int		calcMissingSpot;		// 0-1
	public String	DateTimeFormat;
	public String	DateFormat;
	public String	TimeFormat;
	public int		CSV_Export;
	public int		CSV_Header;
	public int		CSV_ExtendedHeader;
	public int		CSV_SaveZeroPower;
	public int		SunRSOffset;			// Offset to start before sunrise and end after sunset
	public long		userGroup;				// USER|INSTALLER
	public String	prgVersion;
//	VOLT_LOGGING VoltLogging;
	public int		SpotTimeSource;			// 0=Use inverter time; 1=Use PC time in Spot CSV
	public int		SpotWebboxHeader;		// 0=Use standard Spot CSV hdr; 1=Webbox style hdr
	public String	locale;		// default en-US
	//int		PVoutput;				// 0-1
	//int		PVoutput_SID;
	//char		PVoutput_Key[42];
	//int		PVoutput_InvTemp;		// Upload Inverter Temperature to PVoutput
	//int		PVoutput_InvTempMapTo;	// Upload Inverter Temperature to V5 or if in donation mode to v7..v12
	//int		PVoutput_CumulNRG;		// Cumulative Flag (0=Today's Energy or 1=Total Energy)
	public int		MIS_Enabled;			// Multi Inverter Support
	public String	timezone;
	public TimeZone tz;

	//Commandline settings
	public int		debug;				// -d			Debug level (0-5)
	public int		verbose;			// -v			Verbose output level (0-5)
	public int		archDays;			// -ad			Number of days back to get Archived DayData (0=disabled, 1=today, ...)
	public int		archMonths;			// -am			Number of months back to get Archived MonthData (0=disabled, 1=this month, ...)
	public int		archEventMonths;	// -ae			Number of months back to get Archived Events (0=disabled, 1=this month, ...)
	//int		upload;				// -u			Upload to online monitoring systems (PVOutput, ...)
	public int		forceInq;			// -finq		Inquire inverter also during the night
	public int		wsl;				// -wsl			WebSolarLog support (http://www.websolarlog.com/index.php/tag/sma-spot/)
	public int		quiet;				// -q			Silent operation (No output except for -wsl)
	public int		nocsv;				// -nocsv		Disables CSV export (Overrules CSV_Export in config)
	public int		nospot;				// -sp0			Disables Spot CSV export
	public int		nosql;				// -nosql		Disables SQL export
	public int		loadlive;			// -loadlive	Force settings to prepare for live loading to http://pvoutput.org/loadlive.jsp
}
