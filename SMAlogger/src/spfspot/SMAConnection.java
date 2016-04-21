package spfspot;

import inverterdata.InverterData;
import inverterdata.InverterData.DEVICECLASS;
import inverterdata.InverterData.getInverterDataType;

import java.util.Date;
import java.util.Random;

import spfspot.misc.DEBUG;
import spfspot.misc.VERBOSE;
import eth.ethPacket;
import eth.ethPacketHeaderL1;
import eth.ethPacketHeaderL1L2;

public class SMAConnection 
{
	
	
	public enum LriDef
	{
	    OperationHealth                 ( 0x00214800),   // *08* Condition (aka INV_STATUS)
		CoolsysTmpNom					( 0x00237700),	// *40* Operating condition temperatures
	    DcMsWatt                        ( 0x00251E00),   // *40* DC power input (aka SPOT_PDC1 / SPOT_PDC2)
	    MeteringTotWhOut                ( 0x00260100),   // *00* Total yield (aka SPOT_ETOTAL)
	    MeteringDyWhOut                 ( 0x00262200),   // *00* Day yield (aka SPOT_ETODAY)
	    GridMsTotW                      ( 0x00263F00),   // *40* Power (aka SPOT_PACTOT)
	    BatChaStt                       ( 0x00295A00),   // *00* Current battery charge status
	    OperationHealthSttOk            ( 0x00411E00),   // *00* Nominal power in Ok Mode (aka INV_PACMAX1)
	    OperationHealthSttWrn           ( 0x00411F00),   // *00* Nominal power in Warning Mode (aka INV_PACMAX2)
	    OperationHealthSttAlm           ( 0x00412000),   // *00* Nominal power in Fault Mode (aka INV_PACMAX3)
	    OperationGriSwStt               ( 0x00416400),   // *08* Grid relay/contactor (aka INV_GRIDRELAY)
	    OperationRmgTms                 ( 0x00416600),   // *00* Waiting time until feed-in
	    DcMsVol                         ( 0x00451F00),   // *40* DC voltage input (aka SPOT_UDC1 / SPOT_UDC2)
	    DcMsAmp                         ( 0x00452100),   // *40* DC current input (aka SPOT_IDC1 / SPOT_IDC2)
	    MeteringPvMsTotWhOut            ( 0x00462300),   // *00* PV generation counter reading
	    MeteringGridMsTotWhOut          ( 0x00462400),   // *00* Grid feed-in counter reading
	    MeteringGridMsTotWhIn           ( 0x00462500),   // *00* Grid reference counter reading
	    MeteringCsmpTotWhIn             ( 0x00462600),   // *00* Meter reading consumption meter
	    MeteringGridMsDyWhOut	        ( 0x00462700),   // *00* ?
	    MeteringGridMsDyWhIn            ( 0x00462800),   // *00* ?
	    MeteringTotOpTms                ( 0x00462E00),   // *00* Operating time (aka SPOT_OPERTM)
	    MeteringTotFeedTms              ( 0x00462F00),   // *00* Feed-in time (aka SPOT_FEEDTM)
	    MeteringGriFailTms              ( 0x00463100),   // *00* Power outage
	    MeteringWhIn                    ( 0x00463A00),   // *00* Absorbed energy
	    MeteringWhOut                   ( 0x00463B00),   // *00* Released energy
	    MeteringPvMsTotWOut             ( 0x00463500),   // *40* PV power generated
	    MeteringGridMsTotWOut           ( 0x00463600),   // *40* Power grid feed-in
	    MeteringGridMsTotWIn            ( 0x00463700),   // *40* Power grid reference
	    MeteringCsmpTotWIn              ( 0x00463900),   // *40* Consumer power
	    GridMsWphsA                     ( 0x00464000),   // *40* Power L1 (aka SPOT_PAC1)
	    GridMsWphsB                     ( 0x00464100),   // *40* Power L2 (aka SPOT_PAC2)
	    GridMsWphsC                     ( 0x00464200),   // *40* Power L3 (aka SPOT_PAC3)
	    GridMsPhVphsA                   ( 0x00464800),   // *00* Grid voltage phase L1 (aka SPOT_UAC1)
	    GridMsPhVphsB                   ( 0x00464900),   // *00* Grid voltage phase L2 (aka SPOT_UAC2)
	    GridMsPhVphsC                   ( 0x00464A00),   // *00* Grid voltage phase L3 (aka SPOT_UAC3)
	    GridMsAphsA_1                   ( 0x00465000),   // *00* Grid current phase L1 (aka SPOT_IAC1)
	    GridMsAphsB_1                   ( 0x00465100),   // *00* Grid current phase L2 (aka SPOT_IAC2)
	    GridMsAphsC_1                   ( 0x00465200),   // *00* Grid current phase L3 (aka SPOT_IAC3)
	    GridMsAphsA                     ( 0x00465300),   // *00* Grid current phase L1 (aka SPOT_IAC1_2)
	    GridMsAphsB                     ( 0x00465400),   // *00* Grid current phase L2 (aka SPOT_IAC2_2)
	    GridMsAphsC                     ( 0x00465500),   // *00* Grid current phase L3 (aka SPOT_IAC3_2)
	    GridMsHz                        ( 0x00465700),   // *00* Grid frequency (aka SPOT_FREQ)
	    MeteringSelfCsmpSelfCsmpWh      ( 0x0046AA00),   // *00* Energy consumed internally
	    MeteringSelfCsmpActlSelfCsmp    ( 0x0046AB00),   // *00* Current self-consumption
	    MeteringSelfCsmpSelfCsmpInc     ( 0x0046AC00),   // *00* Current rise in self-consumption
	    MeteringSelfCsmpAbsSelfCsmpInc  ( 0x0046AD00),   // *00* Rise in self-consumption
	    MeteringSelfCsmpDySelfCsmpInc   ( 0x0046AE00),   // *00* Rise in self-consumption today
	    BatDiagCapacThrpCnt             ( 0x00491E00),   // *40* Number of battery charge throughputs
	    BatDiagTotAhIn                  ( 0x00492600),   // *00* Amp hours counter for battery charge
	    BatDiagTotAhOut                 ( 0x00492700),   // *00* Amp hours counter for battery discharge
	    BatTmpVal                       ( 0x00495B00),   // *40* Battery temperature
	    BatVol                          ( 0x00495C00),   // *40* Battery voltage
	    BatAmp                          ( 0x00495D00),   // *40* Battery current
	    NameplateLocation               ( 0x00821E00),   // *10* Device name (aka INV_NAME)
	    NameplateMainModel              ( 0x00821F00),   // *08* Device class (aka INV_CLASS)
	    NameplateModel                  ( 0x00822000),   // *08* Device type (aka INV_TYPE)
	    NameplateAvalGrpUsr             ( 0x00822100),   // *  * Unknown
	    NameplatePkgRev                 ( 0x00823400),   // *08* Software package (aka INV_SWVER)
	    InverterWLim                    ( 0x00832A00),   // *00* Maximum active power device (aka INV_PACMAX1_2) (Some inverters like SB3300/SB1200)
		GridMsPhVphsA2B6100             ( 0x00464B00),
		GridMsPhVphsB2C6100             ( 0x00464C00),
		GridMsPhVphsC2A6100             ( 0x00464D00);
	    
	    private static LriDef[] enumValues = values();
	    
	    public static LriDef intToEnum(int value)
	    {
            for(int i = 0; i < enumValues.length; i++)
            {
            	if(enumValues[i].Value == value)
            		return enumValues[i];
            }
            return null;
	    }
	    
	    private int Value;
	    
	    private LriDef(int value)
	    {
	    	this.Value = value;
	    }
	}
	
	public class E_SBFSPOT
	{
		public final static int E_OK			= 0;
		public final static int E_NODATA		= -1;	// Bluetooth buffer empty
		public final static int E_BADARG		= -2;	// Unknown command line argument
		public final static int E_CHKSUM		= -3;	// Invalid Checksum
		public final static int E_BUFOVRFLW		= -4;	// Buffer overflow
		public final static int E_ARCHNODATA	= -5;	// No archived data found for given timespan
		public final static int E_INIT			= -6;	// Unable to initialize
		public final static int E_INVPASSW		= -7;	// Invalid password
		public final static int E_RETRY			= -8;	// Retry the last action
		public final static int E_EOF			= -9;	// End of data
	}
	
	private final short anySUSyID = (short)0xFFFF;
	private final long anySerial = 0xFFFFFFFF;
	private final char[]  addr_unknown = {0xFF,0xFF,0xFF,0xFF,0xFF,0xFF};
	
	private final int COMMBUFSIZE = 1024;
	private byte[] CommBuf = new byte[COMMBUFSIZE];
	
	public Ethernet ethernet;
	private TagDefs tagdefs;
	
	public SMAConnection(TagDefs tagDefs)
	{
		this.tagdefs = tagDefs;
		this.ethernet = new Ethernet();
	}
	
	public int ethConnect(short port)
	{
		return ethernet.ethConnect(port);
	}
	
	public int ethClose()
	{
		return ethernet.ethClose();
	}
	
	int getInverterIndexBySerial(InverterData inverters[], short SUSyID, long Serial)
	{
	    for (int inv = 0; inverters[inv] != null && inv < SBFspot.MAX_INVERTERS; inv++)
	    {
	    	//System.out.printf("inv susyid = %d - inv serial = %d -- susyid = %d - serial = %d \n", inverters[inv].SUSyID, SUSyID, inverters[inv].Serial, Serial);
	        if ((inverters[inv].SUSyID == SUSyID) && inverters[inv].Serial == Serial)
	            return inv;
	    }

	    return -1;
	}
	
	int ethGetPacket()
	{
		CommBuf = new byte[COMMBUFSIZE];
		boolean retry = false;
	    if (DEBUG.NORMAL) 
	    	System.out.printf("ethGetPacket()\n");
	    int rc = E_SBFSPOT.E_OK;
	    //int rc = E_SBFSPOT.E_RETRY;
	    do 
	    {
	    	retry = false;
	    	int bib = ethernet.ethRead(CommBuf, CommBuf.length);
	    	//System.out.printf("bib is: %d\n", bib);
	    	CommBuf = ethernet.getReadBuffer();

	    	if (bib <= 0)
	        {
	            if (DEBUG.NORMAL) 
	            	System.out.printf("No data!\n");
	            rc = E_SBFSPOT.E_NODATA;
	        }
	        else
	        {
	        	ethPacketHeaderL1L2 pkHdr = new ethPacketHeaderL1L2(CommBuf);
	        	int pkLen = ((pkHdr.pcktHdrL1.hiPacketLen << 8) + pkHdr.pcktHdrL1.loPacketLen) & 0xff;	//0xff to convert it to unsigned?
	        	//System.out.println("pkLen is: " + pkLen);
	        	//System.out.println("hiPacketLen = " + (pkHdr.pcktHdrL1.hiPacketLen << 8));
	        	//System.out.println("loPacketLen = " + (pkHdr.pcktHdrL1.loPacketLen & 0xff));
	            //More data after header?
	            if (pkLen > 0)
	            {
	            	if (DEBUG.HIGH) 
		        		misc.HexDump(CommBuf, bib, 10);

	                if (misc.intSwap(pkHdr.pcktHdrL2.MagicNumber) == ethernet.ETH_L2SIGNATURE)
	                {
	                    // Copy CommBuf to packetbuffer
	                    // Dummy byte to align with BTH (7E)
	                    ethernet.pcktBuf[0]= 0;
	                    // We need last 6 bytes of ethPacketHeader too
	                    System.arraycopy(CommBuf, ethPacketHeaderL1.getSize(), ethernet.pcktBuf, 1, bib - ethPacketHeaderL1.getSize());
	                    
	                    //memcpy(pcktBuf+1, CommBuf + sizeof(ethPacketHeaderL1), bib - sizeof(ethPacketHeaderL1));
	                    // Point packetposition at last byte in our buffer
						// This is different from BTH
	                    ethernet.packetposition = bib - ethPacketHeaderL1.getSize();

	                    if (DEBUG.HIGH)
	                    {
	                        System.out.printf("<<<====== Content of pcktBuf =======>>>\n");
	                        misc.HexDump(ethernet.pcktBuf, ethernet.packetposition, 10);
	                        System.out.printf("<<<=================================>>>\n");
	                    }
	                }
	                else
	                {
	                	if (DEBUG.NORMAL)  
	                		System.out.printf("L2 header not found.\n");
	                    retry = true;
	                }
	            }
	            else
	                rc = E_SBFSPOT.E_NODATA;
	                
	    	}
		} while (retry == true);
	    return rc;
	}
	
	public int ethInitConnection(InverterData inverters[], String IP_Address)
	{
	    if (VERBOSE.NORMAL) System.out.println("Initializing...");

	    //Generate a Serial Number for application
	    ethernet.AppSUSyID = 125;
	    Random r = new Random(System.currentTimeMillis());
	    //ethernet.AppSerial = 900000000 + r.nextInt(100000000);
	    ethernet.AppSerial = 900000000 + ((r.nextInt() << 16) + r.nextInt()) % 100000000;
	    //AppSerial = 900000000 + ((rand() << 16) + rand()) % 100000000;
	    if (VERBOSE.NORMAL)
	    	System.out.printf("SUSyID: %d - SN: %d (0x%08X)\n", ethernet.AppSUSyID, ethernet.AppSerial, ethernet.AppSerial);

	    int rc = E_SBFSPOT.E_OK;

	    if (IP_Address.length() < 8) // len less than 0.0.0.0 or len of no string ==> use broadcast to detect inverterg
	    {
		    // Start with UDP broadcast to check for SMA devices on the LAN
	    	ethernet.writeLong(0x00414D53);  //Start of SMA header
	    	ethernet.writeLong(0xA0020400);  //Unknown
	    	ethernet.writeLong(0xFFFFFFFF);  //Unknown
	    	ethernet.writeLong(0x20000000);  //Unknown
	    	ethernet.writeLong(0x00000000);  //Unknown

	    	ethernet.ethSend(ethernet.pcktBuf, ethernet.IP_Broadcast);

	    	//SMA inverter announces it´s presence in response to the discovery request packet
	    	int bytesRead = ethernet.ethRead(CommBuf, CommBuf.length);

			// if bytesRead < 0, a timeout has occurred
			// if bytesRead == 0, no data was received
			if (bytesRead <= 0)
			{
				System.err.println("ERROR: No inverter responded to identification broadcast.\n");
				System.err.println("Try to set IP_Address in SBFspot.cfg!\n");
				return E_SBFSPOT.E_INIT;
			}

	    	if (DEBUG.NORMAL) 
	    		misc.HexDump(CommBuf, bytesRead, 10);
	    }

	    int devcount = 0;
	    //for_each inverter found
	    //{
	        inverters[devcount] = new InverterData();
			resetInverterData(inverters[devcount]);
	        // Store received IP address as readable text to InverterData struct
	        // IP address is found at pos 38 in the buffer
	        // Don't know yet for multiple inverter plants
	        if (IP_Address.length() > 8) // len bigger than 0.0.0.0 or len of no string ==> use IP_Adress from config
	        {
	        	inverters[devcount].IPAddress = IP_Address;
	            if (SBFspot.quiet == 0) 
	            	System.out.printf("Inverter IP address: %s from SBFspot.cfg\n", inverters[devcount].IPAddress);
	        }
	        else                             // use IP from broadcast-detection of inverter
	        {
	        	String ip = String.format("%d.%d.%d.%d", (CommBuf[38] & 0xFF), (CommBuf[39] & 0xFF), (CommBuf[40] & 0xFF), (CommBuf[41] & 0xFF));
	        	inverters[devcount].IPAddress = ip;
	            if (SBFspot.quiet == 0) 
	            	System.out.printf("Inverter IP address: %s found via broadcastidentification\n", inverters[devcount].IPAddress);
	        }
	        devcount++;
	    //}


	    ethernet.writePacketHeader();
	    ethernet.writePacket((char)0x09, (char)0xA0, (short)0, anySUSyID, anySerial);
	    ethernet.writeLong(0x00000200);
	    ethernet.writeLong(0);
	    ethernet.writeLong(0);
	    ethernet.writeLong(0);
	    ethernet.writePacketLength();

	    //Send packet to first inverter
	    ethernet.ethSend(ethernet.pcktBuf, inverters[0].IPAddress);

	    if ((rc = ethGetPacket()) == E_SBFSPOT.E_OK)
	    {
	    	ethPacket pckt = new ethPacket(ethernet.pcktBuf);
	    	inverters[0].SUSyID = pckt.Source.SUSyID;
	    	inverters[0].Serial = pckt.Source.Serial;
	    }
		else
		{
			System.err.println("ERROR: Connection to inverter failed!\n");
			System.err.println("Is " + inverters[0].IPAddress + " the correct IP?\n");
			System.err.println("Please check IP_Address in SBFspot.cfg!\n");
			return E_SBFSPOT.E_INIT;
		}

		logoffSMAInverter(inverters[0]);

	    return rc;
	}
	
	public int logonSMAInverter(InverterData inverters[], long userGroup, char[] password)
	{
		final int MAX_PWLENGTH = 12;
	    char pw[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	    if (DEBUG.NORMAL) 
	    	System.out.println("logonSMAInverter()");

	    char encChar = (char) ((userGroup == SBFspot.UG_USER)? 0x88:0xBB);
	    //Encode password
	    int idx;
	    for (idx = 0; (password[idx] != 0) && (idx <= pw.length); idx++)
	    {
	        pw[idx] = (char) (password[idx] + encChar);
	    }
	    for (; idx < MAX_PWLENGTH; idx++)
	        pw[idx] = encChar;

	    int rc = E_SBFSPOT.E_OK;
	    int validPcktID = 0;

	    Date now;

        ethernet.pcktID++;
        now = new Date();
        ethernet.writePacketHeader();
        ethernet.writePacket((char)0x0E, (char)0xA0, (short)0x0100, anySUSyID, anySerial);
        ethernet.writeLong(0xFFFD040C);
        ethernet.writeLong(userGroup);
        ethernet.writeLong(0x00000384);
        ethernet.writeLong(now.getTime());
        ethernet.writeLong(0);
        ethernet.writeArray(pw, pw.length);
        ethernet.writePacketTrailer();
        ethernet.writePacketLength();
	    //not necessary for ethernet
	    //while (!isCrcValid(pcktBuf[packetposition-3], pcktBuf[packetposition-2]));

        //TODO: make this work for multiple inverters
        ethernet.ethSend(ethernet.pcktBuf, inverters[0].IPAddress);

        validPcktID = 0;
        do
        {
            if ((rc = ethGetPacket()) == E_SBFSPOT.E_OK)
            {
                ethPacket pckt = new ethPacket(ethernet.pcktBuf);
                //if (pcktID == (btohs(pckt->PacketID) & 0x7FFF))   // Valid Packet ID
                //TEST TEST TEST TEST not sure if this is the right conversion.
                if (ethernet.pcktID == ((pckt.PacketID) & 0x7FFF))   // Valid Packet ID
                {
                    validPcktID = 1;
                    //rc = (pckt->ErrorCode == 0) ? E_OK : E_INVPASSW;
					// Fix Issue CP5 - Logon problem Sunny Island
					//rc = (btohs(pckt->ErrorCode) == 0x0100) ? E_INVPASSW : E_OK;
                    rc = (misc.shortSwap(pckt.ErrorCode) == 0x0100) ? E_SBFSPOT.E_INVPASSW : E_SBFSPOT.E_OK;
				}
                else
                    if (DEBUG.HIGHEST) 
                    	System.out.printf("Packet ID mismatch. Expected %d, received %d\n", ethernet.pcktID, (misc.shortSwap(pckt.PacketID) & 0x7FFF));
            }
            else
            	return rc;
        } while (validPcktID == 0);

	    return rc;
	}
	
	public int logoffSMAInverter(InverterData inverter)
	{
	    if (DEBUG.NORMAL) 
	    	System.out.println("logoffSMAInverter()");
        
	    ethernet.pcktID++;
        ethernet.writePacketHeader();
        ethernet.writePacket((char)0x08, (char)0xA0, (short)0x0300, anySUSyID, anySerial);
        ethernet.writeLong(0xFFFD010E);
        ethernet.writeLong(0xFFFFFFFF);
        ethernet.writePacketTrailer();
        ethernet.writePacketLength();

        //TODO: make this work for multiple inverters
        ethernet.ethSend(ethernet.pcktBuf, new String(inverter.IPAddress));

	    return E_SBFSPOT.E_OK;
	}
	
	public int getInverterData(InverterData devList[], getInverterDataType type)
	{
	    if (DEBUG.NORMAL) 
	    	System.out.printf("getInverterData(%s)\n", type.toString());
	    String strWatt = "%-12s: %d (W) %s\n";
	    String strVolt = "%-12s: %.2f (V) %s\n";
	    String strAmp = "%-12s: %.3f (A) %s\n";
	    String strkWh = "%-12s: %.3f (kWh) %s\n";
	    String strHour = "%-12s: %.3f (h) %s\n";

	    int rc = E_SBFSPOT.E_OK;

	    int recordsize = 0;
	    int validPcktID = 0;

	    long command;
	    long first;
	    long last;

	    switch(type)
	    {
		    case EnergyProduction:
		        // SPOT_ETODAY, SPOT_ETOTAL
		        command = 0x54000200;
		        first = 0x00260100;
		        last = 0x002622FF;
		        break;
	
		    case SpotDCPower:
		        // SPOT_PDC1, SPOT_PDC2
		        command = 0x53800200;
		        first = 0x00251E00;
		        last = 0x00251EFF;
		        break;
	
		    case SpotDCVoltage:
		        // SPOT_UDC1, SPOT_UDC2, SPOT_IDC1, SPOT_IDC2
		        command = 0x53800200;
		        first = 0x00451F00;
		        last = 0x004521FF;
		        break;
	
		    case SpotACPower:
		        // SPOT_PAC1, SPOT_PAC2, SPOT_PAC3
		        command = 0x51000200;
		        first = 0x00464000;
		        last = 0x004642FF;
		        break;
	
		    case SpotACVoltage:
		        // SPOT_UAC1, SPOT_UAC2, SPOT_UAC3, SPOT_IAC1, SPOT_IAC2, SPOT_IAC3
		        command = 0x51000200;
		        first = 0x00464800;
		        last = 0x004655FF;
		        break;
	
		    case SpotGridFrequency:
		        // SPOT_FREQ
		        command = 0x51000200;
		        first = 0x00465700;
		        last = 0x004657FF;
		        break;
	
		    case MaxACPower:
		        // INV_PACMAX1, INV_PACMAX2, INV_PACMAX3
		        command = 0x51000200;
		        first = 0x00411E00;
		        last = 0x004120FF;
		        break;
	
		    case MaxACPower2:
		        // INV_PACMAX1_2
		        command = 0x51000200;
		        first = 0x00832A00;
		        last = 0x00832AFF;
		        break;
	
		    case SpotACTotalPower:
		        // SPOT_PACTOT
		        command = 0x51000200;
		        first = 0x00263F00;
		        last = 0x00263FFF;
		        break;
	
		    case TypeLabel:
		        // INV_NAME, INV_TYPE, INV_CLASS
		        command = 0x58000200;
		        first = 0x00821E00;
		        last = 0x008220FF;
		        break;
	
		    case SoftwareVersion:
		        // INV_SWVERSION
		        command = 0x58000200;
		        first = 0x00823400;
		        last = 0x008234FF;
		        break;
	
		    case DeviceStatus:
		        // INV_STATUS
		        command = 0x51800200;
		        first = 0x00214800;
		        last = 0x002148FF;
		        break;
	
		    case GridRelayStatus:
		        // INV_GRIDRELAY
		        command = 0x51800200;
		        first = 0x00416400;
		        last = 0x004164FF;
		        break;
	
		    case OperationTime:
		        // SPOT_OPERTM, SPOT_FEEDTM
		        command = 0x54000200;
		        first = 0x00462E00;
		        last = 0x00462FFF;
		        break;
	
		    case BatteryChargeStatus:
		        command = 0x51000200;
		        first = 0x00295A00;
		        last = 0x00295AFF;
		        break;
	
		    case BatteryInfo:
		        command = 0x51000200;
		        first = 0x00491E00;
		        last = 0x00495DFF;
		        break;
	
			case InverterTemperature:
				command = 0x52000200;
				first = 0x00237700;
				last = 0x002377FF;
				break;
	
		    default:
		        return E_SBFSPOT.E_BADARG;
	    };

        ethernet.pcktID++;
        ethernet.writePacketHeader();
        ethernet.writePacket((char)0x09, (char)0xA0, (short)0, anySUSyID, anySerial);
        ethernet.writeLong(command);
        ethernet.writeLong(first);
        ethernet.writeLong(last);
        ethernet.writePacketTrailer();
        ethernet.writePacketLength();

        // no bluetooth
        /*
	    if (ConnType == CT_BLUETOOTH)
	        bthSend(pcktBuf);
	    else
	        ethSend(pcktBuf, devList[0]->IPAddress);
	    */
        ethernet.ethSend(ethernet.pcktBuf, devList[0].IPAddress);

	    for (int i = 0; devList[i] != null && i < SBFspot.MAX_INVERTERS; i++)
	    {
	        validPcktID = 0;
	        do
	        {
                rc = ethGetPacket();

	            if (rc != E_SBFSPOT.E_OK) 
	            	return rc;
	            //System.out.println("ethGetPacket status: " + rc);

                short rcvpcktID = (short) (ethernet.pcktBuf[27] & 0x7FFF);
                if (ethernet.pcktID == rcvpcktID)
                {
                    //int inv = getInverterIndexBySerial(devList, misc.get_shortli(ethernet.pcktBuf, 15), misc.get_longli(ethernet.pcktBuf, 17));
                	int inv = getInverterIndexBySerial(devList, misc.get_short(ethernet.pcktBuf, 15), misc.get_long(ethernet.pcktBuf, 17));
                    if (inv >= 0)
                    {
                        validPcktID = 1;
                        int value = 0;
                        long value64 = 0;
                        char Vtype = 0;
                        char Vbuild = 0;
                        char Vminor = 0;
                        char Vmajor = 0;
                        for (int ix = 41; ix < ethernet.packetposition - 3; ix += recordsize)
                        {
                        	//uint32_t code = ((uint32_t)get_long(pcktBuf + i));
                            //int code = misc.get_longli(ethernet.pcktBuf, ix);
                        	int code = misc.get_long(ethernet.pcktBuf, ix);
                        	//code = (int) Integer.toUnsignedLong(code);
                            //Check this if something doesn't work, int to enum conversion. Should be good now
                            LriDef lri = LriDef.intToEnum((code & 0x00FFFF00));
                            long cls = code & 0xFF;
                            char dataType = (char) (code >> 24);
                            //Not sure if java uses same long date, well it doesn't
                            //Multiply by 1000 cause java uses milliseconds and the inverter uses seconds since epoch.
                            Date datetime = new Date(misc.get_long(ethernet.pcktBuf, ix + 4) * 1000l);                        
                            
                            // fix: We can't rely on dataType because it can be both 0x00 or 0x40 for DWORDs
                            if ((lri == LriDef.MeteringDyWhOut) || (lri == LriDef.MeteringTotWhOut) || (lri == LriDef.MeteringTotFeedTms) || (lri == LriDef.MeteringTotOpTms))	//QWORD
                            //if ((code == SPOT_ETODAY) || (code == SPOT_ETOTAL) || (code == SPOT_FEEDTM) || (code == SPOT_OPERTM))	//QWORD
                            {
                            	value64 = misc.get_longlong(ethernet.pcktBuf, ix + 8);
                                if ((value64 == misc.NaN_S64) || (value64 == misc.NaN_U64)) value64 = 0;
                            }
                            else if ((dataType != 0x10) && (dataType != 0x08))	//Not TEXT or STATUS, so it should be DWORD
                            {
                                value = misc.get_long(ethernet.pcktBuf, ix + 8);
                                if ((value == misc.NaN_S32) || (value == misc.NaN_U32)) value = 0;
                            }

                            switch (lri)
                            {
                            case GridMsTotW: //SPOT_PACTOT
                                if (recordsize == 0) recordsize = 28;
                                //This function gives us the time when the inverter was switched off
                                devList[inv].SleepTime = datetime;
                                devList[inv].TotalPac = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL) 
                                	System.out.printf(strWatt, "SPOT_PACTOT", value, datetime.toString());
                                break;

                            case OperationHealthSttOk: //INV_PACMAX1
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].Pmax1 = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL) 
                                	System.out.printf(strWatt, "INV_PACMAX1", value, datetime.toString());
                                break;

                            case OperationHealthSttWrn: //INV_PACMAX2
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].Pmax2 = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL) 
                                	System.out.printf(strWatt, "INV_PACMAX2", value, datetime.toString());
                                break;

                            case OperationHealthSttAlm: //INV_PACMAX3
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].Pmax3 = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strWatt, "INV_PACMAX3", value, datetime.toString());
                                break;

                            case GridMsWphsA: //SPOT_PAC1
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].Pac1 = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strWatt, "SPOT_PAC1", value, datetime.toString());
                                break;

                            case GridMsWphsB: //SPOT_PAC2
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].Pac2 = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strWatt, "SPOT_PAC2", value, datetime.toString());
                                break;

                            case GridMsWphsC: //SPOT_PAC3
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].Pac3 = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strWatt, "SPOT_PAC3", value, datetime.toString());
                                break;

                            case GridMsPhVphsA: //SPOT_UAC1
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].Uac1 = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strVolt, "SPOT_UAC1", misc.toVolt(value), datetime.toString());
                                break;

                            case GridMsPhVphsB: //SPOT_UAC2
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].Uac2 = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strVolt, "SPOT_UAC2", misc.toVolt(value), datetime.toString());
                                break;

                            case GridMsPhVphsC: //SPOT_UAC3
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].Uac3 = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strVolt, "SPOT_UAC3", misc.toVolt(value), datetime.toString());
                                break;

                            case GridMsAphsA_1: //SPOT_IAC1
							case GridMsAphsA:
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].Iac1 = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strAmp, "SPOT_IAC1", misc.toAmp(value), datetime.toString());
                                break;

                            case GridMsAphsB_1: //SPOT_IAC2
							case GridMsAphsB:
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].Iac2 = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strAmp, "SPOT_IAC2", misc.toAmp(value), datetime.toString());
                                break;

                            case GridMsAphsC_1: //SPOT_IAC3
							case GridMsAphsC:
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].Iac3 = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strAmp, "SPOT_IAC3", misc.toAmp(value), datetime.toString());
                                break;

                            case GridMsHz: //SPOT_FREQ
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].GridFreq = value;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf("%-12s: %.2f (Hz) %s", "SPOT_FREQ", misc.toHz(value), datetime.toString());
                                break;

                            case DcMsWatt: //SPOT_PDC1 / SPOT_PDC2
                                if (recordsize == 0) recordsize = 28;
                                if (cls == 1)   // MPP1
                                {
                                    devList[inv].Pdc1 = value;
                                    if (DEBUG.NORMAL)  
                                    	System.out.printf(strWatt, "SPOT_PDC1", value, datetime.toString());
                                }
                                if (cls == 2)   // MPP2
                                {
                                    devList[inv].Pdc2 = value;
                                    if (DEBUG.NORMAL)  
                                    	System.out.printf(strWatt, "SPOT_PDC2", value, datetime.toString());
                                }
                                devList[inv].flags |= type.getValue();
                                break;

                            case DcMsVol: //SPOT_UDC1 / SPOT_UDC2
                                if (recordsize == 0) recordsize = 28;
                                if (cls == 1)
                                {
                                    devList[inv].Udc1 = value;
                                    if (DEBUG.NORMAL)  
                                    	System.out.printf(strVolt, "SPOT_UDC1", misc.toVolt(value), datetime.toString());
                                }
                                if (cls == 2)
                                {
                                    devList[inv].Udc2 = value;
                                    if (DEBUG.NORMAL)  
                                    	System.out.printf(strVolt, "SPOT_UDC2", misc.toVolt(value), datetime.toString());
                                }
                                devList[inv].flags |= type.getValue();
                                break;

                            case DcMsAmp: //SPOT_IDC1 / SPOT_IDC2
                                if (recordsize == 0) recordsize = 28;
                                if (cls == 1)
                                {
                                    devList[inv].Idc1 = value;
                                    if (DEBUG.NORMAL)  
                                    	System.out.printf(strAmp, "SPOT_IDC1", misc.toAmp(value), datetime.toString());
                                }
                                if (cls == 2)
                                {
                                    devList[inv].Idc2 = value;
                                    if (DEBUG.NORMAL)  
                                    	System.out.printf(strAmp, "SPOT_IDC2", misc.toAmp(value), datetime.toString());
                                }
                                devList[inv].flags |= type.getValue();
                                break;

                            case MeteringTotWhOut: //SPOT_ETOTAL
                                if (recordsize == 0) recordsize = 16;
                                devList[inv].ETotal = value64;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strkWh, "SPOT_ETOTAL", misc.tokWh(value64), datetime.toString());
                                break;

                            case MeteringDyWhOut: //SPOT_ETODAY
                                if (recordsize == 0) recordsize = 16;
                                //This function gives us the current inverter time
                                devList[inv].InverterDatetime = datetime;
                                devList[inv].EToday = value64;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strkWh, "SPOT_ETODAY", misc.tokWh(value64), datetime.toString());
                                break;

                            case MeteringTotOpTms: //SPOT_OPERTM
                                if (recordsize == 0) recordsize = 16;
                                devList[inv].OperationTime = value64;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strHour, "SPOT_OPERTM", misc.toHour(value64), datetime.toString());
                                break;

                            case MeteringTotFeedTms: //SPOT_FEEDTM
                                if (recordsize == 0) recordsize = 16;
                                devList[inv].FeedInTime = value64;
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf(strHour, "SPOT_FEEDTM", misc.toHour(value64), datetime.toString());
                                break;

                            case NameplateLocation: //INV_NAME
                                if (recordsize == 0) recordsize = 40;
                                //This function gives us the time when the inverter was switched on
                                devList[inv].WakeupTime = datetime;
                                //String dvcName = new String(ethernet.pcktBuf, ix + 8, devList[inv].DeviceName.length() - 1);
                                //devList[inv].DeviceName = dvcName;
                                for(int ch = 0; ch < devList[inv].DeviceName.length - 1; ch++)
                                {
                                	devList[inv].DeviceName[ch] = (char)ethernet.pcktBuf[ix + 8 + ch];
                                }
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf("%-12s: '%s' %s", "INV_NAME", new String(devList[inv].DeviceName), datetime.toString());
                                break;

                            case NameplatePkgRev: //INV_SWVER
                                if (recordsize == 0) recordsize = 40;
                                Vtype = (char) ethernet.pcktBuf[ix + 24];
                                String ReleaseType;
                                if (Vtype > 5)
                                	ReleaseType = String.format("%c", Vtype);
                                else
                                	ReleaseType = String.format("%c", "NEABRS".charAt(Vtype));//NOREV-EXPERIMENTAL-ALPHA-BETA-RELEASE-SPECIAL
                                Vbuild = (char) ethernet.pcktBuf[ix + 25];
                                Vminor = (char) ethernet.pcktBuf[ix + 26];
                                Vmajor = (char) ethernet.pcktBuf[ix + 27];
                                //Vmajor and Vminor = 0x12 should be printed as '12' and not '18' (BCD)
                                devList[inv].SWVersion = String.format("%c%c.%c%c.%02d.%s", '0'+(Vmajor >> 4), '0'+(Vmajor & 0x0F), '0'+(Vminor >> 4), '0'+(Vminor & 0x0F), (int)Vbuild, ReleaseType);   
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL)  
                                	System.out.printf("%-12s: '%s' %s", "INV_SWVER", devList[inv].SWVersion, datetime.toString());
                                break;

                            case NameplateModel: //INV_TYPE
                                if (recordsize == 0) 
                                	recordsize = 40;
                                for (int idx = 8; idx < recordsize; idx += 4)
                                {
                                    int attribute = misc.get_long(ethernet.pcktBuf, ix + idx) & 0x00FFFFFF;
                                    char status = (char) ethernet.pcktBuf[ix + idx + 3];
                                    if (attribute == 0xFFFFFE) 
                                    	break;	//End of attributes
                                    if (status == 1)
                                    {
										String devtype = tagdefs.getDesc(attribute);
										if (!devtype.isEmpty())
											devList[inv].DeviceType = devtype.toCharArray();
										else
										{
											devList[inv].DeviceType = "UNKNOWN TYPE".toCharArray();
                                            System.out.printf("Unknown Inverter Type. Report this issue at https://sbfspot.codeplex.com/workitem/list/basic with following info:\n");
                                            System.out.printf("0x%08lX and Inverter Type=<Fill in the exact type> (e.g. SB1300TL-10)\n", attribute);
										}
                                    }
                                }
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL) 
                                	System.out.printf("%-12s: '%s' %s", "INV_TYPE", new String(devList[inv].DeviceType), datetime.toString());
                                break;

                            case NameplateMainModel: //INV_CLASS
                                if (recordsize == 0) 
                                	recordsize = 40;
                                for (int idx = 8; idx < recordsize; idx += 4)
                                {
                                    int attribute = misc.get_long(ethernet.pcktBuf, ix + idx) & 0x00FFFFFF;
                                    char attValue = (char) ethernet.pcktBuf[ix + idx + 3];
                                    if (attribute == 0xFFFFFE) break;	//End of attributes
                                    if (attValue == 1)
                                    {
                                        devList[inv].DevClass = DEVICECLASS.intToEnum(attribute);
										String devclass = tagdefs.getDesc(attribute);
										if (!devclass.isEmpty())
											devList[inv].DeviceClass = devclass.toCharArray();
										else
										{
											devList[inv].DeviceClass = "UNKNOWN CLASS".toCharArray();
                                            System.out.printf("Unknown Device Class. Report this issue at https://sbfspot.codeplex.com/workitem/list/basic with following info:\n");
                                            System.out.printf("0x%08lX and Device Class=...\n", attribute);
                                        }
                                    }
                                }
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL) 
                                	System.out.printf("%-12s: '%s' %s", "INV_CLASS", new String(devList[inv].DeviceClass), datetime.toString());
                                break;

                            case OperationHealth: //INV_STATUS:
                                if (recordsize == 0) recordsize = 40;
                                for (int idx = 8; idx < recordsize; idx += 4)
                                {
                                    int attribute = misc.get_long(ethernet.pcktBuf, ix + idx) & 0x00FFFFFF;
                                    char attValue = (char) ethernet.pcktBuf[ix + idx + 3];
                                    if (attribute == 0xFFFFFE) break;	//End of attributes
                                    if (attValue == 1)
                                        devList[inv].DeviceStatus = attribute;
                                }
                                devList[inv].flags |= type.getValue();
								if (DEBUG.NORMAL) 
									System.out.printf("%-12s: '%s' %s", "INV_STATUS", tagdefs.getDesc(devList[inv].DeviceStatus, "?"), datetime.toString());
                                break;

                            case OperationGriSwStt: //INV_GRIDRELAY
                                if (recordsize == 0) recordsize = 40;
                                for (int idx = 8; idx < recordsize; idx += 4)
                                {
                                    int attribute = misc.get_long(ethernet.pcktBuf, ix + idx) & 0x00FFFFFF;
                                    char attValue = (char) ethernet.pcktBuf[ix + idx + 3];
                                    if (attribute == 0xFFFFFE) break;	//End of attributes
                                    if (attValue == 1)
                                        devList[inv].GridRelayStatus = attribute;
                                }
                                devList[inv].flags |= type.getValue();
                                if (DEBUG.NORMAL) 
                                	System.out.printf("%-12s: '%s' %s", "INV_GRIDRELAY", tagdefs.getDesc(devList[inv].GridRelayStatus, "?"), datetime.toString());
                                break;

                            case BatChaStt:
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].BatChaStt = value;
                                devList[inv].flags |= type.getValue();
                                break;

                            case BatDiagCapacThrpCnt:
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].BatDiagCapacThrpCnt = value;
                                devList[inv].flags |= type.getValue();
                                break;

                            case BatDiagTotAhIn:
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].BatDiagTotAhIn = value;
                                devList[inv].flags |= type.getValue();
                                break;

                            case BatDiagTotAhOut:
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].BatDiagTotAhOut = value;
                                devList[inv].flags |= type.getValue();
                                break;

                            case BatTmpVal:
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].BatTmpVal = value;
                                devList[inv].flags |= type.getValue();
                                break;

                            case BatVol:
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].BatVol = value;
                                devList[inv].flags |= type.getValue();
                                break;

                            case BatAmp:
                                if (recordsize == 0) recordsize = 28;
                                devList[inv].BatAmp = value;
                                devList[inv].flags |= type.getValue();
                                break;

							case CoolsysTmpNom:
                                if (recordsize == 0) recordsize = 28;
								devList[inv].Temperature = value;
								devList[inv].flags |= type.getValue();
								break;

                            default:
                                if (recordsize == 0) recordsize = 12;
                            }
                        }
                    }
                }
                else
                {
                    if (DEBUG.HIGHEST) 
                    	System.out.printf("Packet ID mismatch. Expected %d, received %d\n", ethernet.pcktID, rcvpcktID);
                }
	        }
	        while (validPcktID == 0);
	    }
	    return E_SBFSPOT.E_OK;
	}
	
	private void resetInverterData(InverterData inv)
	{
		inv.BatAmp = 0;
		inv.BatChaStt = 0;
		inv.BatDiagCapacThrpCnt = 0;
		inv.BatDiagTotAhIn = 0;
		inv.BatDiagTotAhOut = 0;
		inv.BatTmpVal = 0;
		inv.BatVol = 0;
		inv.BT_Signal = 0;
		inv.calEfficiency = 0;
		inv.calPacTot = 0;
		inv.calPdcTot = 0;
		inv.DevClass = DEVICECLASS.AllDevices;
		inv.DeviceClass = new char[inv.DeviceClass.length];
		inv.DeviceName = new char[inv.DeviceName.length];
		inv.DeviceStatus = 0;
		inv.DeviceType = new char[inv.DeviceType.length];
		inv.EToday = 0;
		inv.ETotal = 0;
		inv.FeedInTime = 0;
		inv.flags = 0;
		inv.GridFreq = 0;
		inv.GridRelayStatus = 0;
		inv.Iac1 = 0;
		inv.Iac2 = 0;
		inv.Iac3 = 0;
		inv.Idc1 = 0;
		inv.Idc2 = 0;
		inv.InverterDatetime = new Date(0);
		inv.IPAddress = "";
		inv.modelID = 0;
		inv.NetID = 0;
		inv.OperationTime = 0;
		inv.Pac1 = 0;
		inv.Pac2 = 0;
		inv.Pac3 = 0;
		inv.Pdc1 = 0;
		inv.Pdc2 = 0;
		inv.Pmax1 = 0;
		inv.Pmax2 = 0;
		inv.Pmax3 = 0;
		inv.Serial = 0;
		inv.SleepTime = new Date(0);
		inv.SUSyID = 0;
		inv.SWVersion = "";
		inv.Temperature = 0;
		inv.TotalPac = 0;
		inv.Uac1 = 0;
		inv.Uac2 = 0;
		inv.Uac3 = 0;
		inv.Udc1 = 0;
		inv.Udc2 = 0;
		inv.WakeupTime = new Date(0);
	}
}
