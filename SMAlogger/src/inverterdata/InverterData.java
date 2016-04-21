package inverterdata;

import java.util.ArrayList;
import java.util.Date;

public class InverterData 
{
	public enum DEVICECLASS
	{
	    AllDevices ( 8000) ,          // DevClss0
	    SolarInverter ( 8001 ),       // DevClss1
	    WindTurbineInverter ( 8002 ), // DevClss2
	    BatteryInverter ( 8007 ),     // DevClss7
	    Consumer ( 8033 ),            // DevClss33
	    SensorSystem ( 8064 ),        // DevClss64
	    ElectricityMeter ( 8065 ),    // DevClss65
	    CommunicationProduct ( 8128 ); // DevClss128
	    
	    private int Value;
	    
	    private static DEVICECLASS[] enumValues = values();
	    
	    public static DEVICECLASS intToEnum(int value)
	    {
            for(int i = 0; i < enumValues.length; i++)
            {
            	if(enumValues[i].Value == value)
            		return enumValues[i];
            }
            return null;
	    }
	    
	    private DEVICECLASS(int value)
	    {
	    	this.Value = value;
	    }
	}
	
	public enum getInverterDataType
	{
		EnergyProduction	( 1 << 0 ),
		SpotDCPower			( 1 << 1 ),
		SpotDCVoltage		( 1 << 2 ),
		SpotACPower			( 1 << 3 ),
		SpotACVoltage		( 1 << 4 ),
		SpotGridFrequency	( 1 << 5 ),
		MaxACPower			( 1 << 6 ),
		MaxACPower2			( 1 << 7 ),
		SpotACTotalPower	( 1 << 8 ),
		TypeLabel			( 1 << 9 ),
		OperationTime		( 1 << 10 ),
		SoftwareVersion		( 1 << 11 ),
		DeviceStatus		( 1 << 12 ),
		GridRelayStatus		( 1 << 13 ),
		BatteryChargeStatus ( 1 << 14 ),
		BatteryInfo         ( 1 << 15 ),
		InverterTemperature	( 1 << 16 ),

		sbftest             ( 1 << 31 );
		
		private int Value;
		
		private getInverterDataType(int value)
		{
			this.Value = value;
		}

		public int getValue() {
			return this.Value;
		}
	};
	
	public char[] DeviceName = new char[33];    //32 bytes + terminating zero
	public String BTAddress;
	public String IPAddress;
	public short SUSyID;
	public long Serial;
	public char NetID;
	public float BT_Signal;
	public Date InverterDatetime;
	public Date WakeupTime;
	public Date SleepTime;
	public long Pdc1;
	public long Pdc2;
	public long Udc1;
	public long Udc2;
	public long Idc1;
	public long Idc2;
	public long Pmax1;
	public long Pmax2;
	public long Pmax3;
	public long TotalPac;
	public long Pac1;
	public long Pac2;
	public long Pac3;
	public long Uac1;
	public long Uac2;
	public long Uac3;
	public long Iac1;
    public long Iac2;
    public long Iac3;
    public long GridFreq;
    public long OperationTime;
    public long FeedInTime;
    public long EToday;
    public long ETotal;
    public short modelID;
    public char[] DeviceType = new char[64];
    public char[] DeviceClass = new char[64];
    public DEVICECLASS DevClass;
    public String SWVersion;	//"03.01.05.R"
    public int DeviceStatus;
    public int GridRelayStatus;
    public int flags;
    public DayData[] dayData = new DayData[288];
    public MonthData[] monthData = new MonthData[31];
    public ArrayList<EventData> eventData;
    public long calPdcTot;
    public long calPacTot;
    public float calEfficiency;
    public long BatChaStt;			// Current battery charge status
    public long BatDiagCapacThrpCnt;	// Number of battery charge throughputs
    public long BatDiagTotAhIn;		// Amp hours counter for battery charge
    public long BatDiagTotAhOut;		// Amp hours counter for battery discharge
    public long BatTmpVal;			// Battery temperature
    public long BatVol;				// Battery voltage
    public long BatAmp;						// Battery current
    public long Temperature;					// Inverter Temperature
}
