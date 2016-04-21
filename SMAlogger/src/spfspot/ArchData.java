package spfspot;

import inverterdata.DayData;
import inverterdata.EventData;
import inverterdata.InverterData;
import inverterdata.MonthData;
import inverterdata.SMA_EVENTDATA;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import spfspot.Config.CONNECTIONTYPE;
import spfspot.SMAConnection.E_SBFSPOT;
import spfspot.misc.DEBUG;
import spfspot.misc.VERBOSE;

public class ArchData 
{
	private SMAConnection smaConn;
	private Ethernet ethernet;
	
	public ArchData(SMAConnection smaConnection)
	{
		this.smaConn = smaConnection;
		this.ethernet = smaConnection.ethernet;
	}
	
	int ArchiveDayData(InverterData inverters[], long startTime)
	{
	    if (VERBOSE.NORMAL)
	    {
	        System.out.println("********************");
	        System.out.println("* ArchiveDayData() *");
	        System.out.println("********************");
	    }

	    //Not sure if this translates well into the java implementation of time
		startTime -= 86400000l;		// fix Issue CP23: to overcome problem with DST transition - RB@20140330

	    int rc = E_SBFSPOT.E_OK;
	    Calendar start_tm = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		start_tm.setTimeInMillis(startTime);
	    
		start_tm.set(Calendar.HOUR_OF_DAY, 0);
		start_tm.set(Calendar.MINUTE, 0);
	    start_tm.set(Calendar.SECOND, 0);
	    start_tm.set(Calendar.DAY_OF_MONTH, start_tm.get(Calendar.DAY_OF_MONTH) + 1); // fix Issue CP23: to overcome problem with DST transition - RB@20140330		
		startTime = start_tm.getTimeInMillis() / 1000l;

	    if (VERBOSE.NORMAL)
	    {
	    	System.out.println("Long time is: " + startTime);
	        System.out.printf("startTime = %08X -> %s\n", startTime, misc.printDate(new Date(startTime * 1000)));
	    }
	    
	    for (int inv = 0; inverters[inv] != null && inv < SBFspot.MAX_INVERTERS; inv++)
	    {
	    	//Pretty sure this line just loops trough the entire list of daydata...
	        //for(int i = 0; i < sizeof(inverters[inv].dayData) / sizeof(DayData); i++)
	    	for(int i = 0; i < inverters[inv].dayData.length; i++)
	    	{
	    		inverters[inv].dayData[i] = new DayData();
	    	} 
	    }

	    int packetcount = 0;
	    int validPcktID = 0;

	    int hasData = E_SBFSPOT.E_ARCHNODATA;

	    for (int inv = 0; inverters[inv] != null && inv < SBFspot.MAX_INVERTERS; inv++)
	    {
	    	/* Not neccesary for ethernet
	        do
	        {
            
	        }
	        while (!isCrcValid(pcktBuf[packetposition-3], pcktBuf[packetposition-2]));
	    	 */
	    	ethernet.pcktID++;
            ethernet.writePacketHeader();
            ethernet.writePacket((char)0x09, (char)0xE0, (short)0, inverters[inv].SUSyID, inverters[inv].Serial);
            ethernet.writeLong(0x70000200);
            ethernet.writeLong(startTime - 300l);	//Is this the right time?
            ethernet.writeLong(startTime + 86100l);
            ethernet.writePacketTrailer();
            ethernet.writePacketLength();
	    	
	        if (SBFspot.ConnType == CONNECTIONTYPE.CT_BLUETOOTH)
	        	System.err.println("Bluetooth not supported");
	            //bthSend(pcktBuf);
	        else
	            //TODO: Multiple inverters
	            ethernet.ethSend(ethernet.pcktBuf, inverters[0].IPAddress);

	        do
	        {
	            long totalWh = 0;
	            long totalWh_prev = 0;
	            long datetime;
	            final int recordsize = 12;

	            do
	            {
                    rc = smaConn.ethGetPacket();

	                if (rc != E_SBFSPOT.E_OK) 
	                	return rc;

	                packetcount = ethernet.pcktBuf[25];	            
                
	                //I think this is the right translation of this: unsigned short rcvpcktID = get_short(pcktBuf+27) & 0x7FFF;
					short rcvpcktID = (short) (misc.get_short(ethernet.pcktBuf, 27) & 0x7FFF);
                    if ((validPcktID == 1) || (ethernet.pcktID == rcvpcktID))
                    {	
                        validPcktID = 1;
                        for(int x = 41; x < (ethernet.packetposition - 3); x += recordsize)
                        { 	
                            datetime = misc.get_long(ethernet.pcktBuf, x);
                            totalWh = misc.get_longlong(ethernet.pcktBuf, x + 4);
                            if (totalWh == misc.NaN_U64) totalWh = 0;
                            if (totalWh > 0) hasData = E_SBFSPOT.E_OK;
                            if (totalWh_prev != 0)
                            {
                                Calendar timeinfo = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                timeinfo.setTimeInMillis(datetime * 1000l);	//Convert to ms since epoch for java

                                if (start_tm.get(Calendar.DAY_OF_MONTH) == timeinfo.get(Calendar.DAY_OF_MONTH))
                                {
                                    int idx = (timeinfo.get(Calendar.HOUR_OF_DAY) * 12) + (timeinfo.get(Calendar.MINUTE) / 5);
                                    if (idx < inverters[inv].dayData.length)
                                    {
                                        inverters[inv].dayData[idx].datetime = datetime;
                                        inverters[inv].dayData[idx].totalWh = totalWh;
                                        inverters[inv].dayData[idx].watt = (totalWh - totalWh_prev) * 12;	// 60:5
                                    }
                                }
                            }
                            totalWh_prev = totalWh;
                        } //for
                    }
                    else
                    {
                        if (DEBUG.HIGHEST) 
                        	System.out.printf("Packet ID mismatch. Expected %d, received %d\n", ethernet.pcktID, rcvpcktID);
                        validPcktID = 0;
                        packetcount = 0;
                    }                
	            }
	            while (packetcount > 0);
	        }
	        while (validPcktID == 0);
	    }

	    return hasData;
	}

	int ArchiveMonthData(InverterData inverters[], Calendar start_tm)
	{
	    if (VERBOSE.NORMAL)
	    {
	        System.out.println("**********************");
	        System.out.println("* ArchiveMonthData() *");
	        System.out.println("**********************");
	    }

	    int rc = E_SBFSPOT.E_OK;

	    // Set time to 1st of the month at 12:00:00
	    start_tm.set(Calendar.HOUR_OF_DAY, 12);
	    start_tm.set(Calendar.MINUTE, 0);
	    start_tm.set(Calendar.SECOND, 0);
	    start_tm.set(Calendar.DAY_OF_MONTH, 1);
	    long startTime = start_tm.getTimeInMillis() / 1000l;

	    if (VERBOSE.NORMAL)
	    {
	        System.out.printf("startTime = %08X -> %s\n", startTime, misc.printDate(new Date(startTime * 1000l)));
	    }
	        
	    for (int inv = 0; inverters[inv] != null && inv < SBFspot.MAX_INVERTERS; inv++)
	    {
	    	for(int i = 0; i < inverters[inv].monthData.length; i++)
			{
	    		inverters[inv].monthData[i] = new MonthData();
			}
	    }

	    int packetcount = 0;
	    int validPcktID = 0;

	    for (int inv = 0; inverters[inv] != null && inv < SBFspot.MAX_INVERTERS; inv++)
	    {
	    	/* Not neccessary for ethernet
	        do
	        {
	            
	        }
	        while (!isCrcValid(pcktBuf[packetposition-3], pcktBuf[packetposition-2]));
			*/
	    	ethernet.pcktID++;
            ethernet.writePacketHeader();
            ethernet.writePacket((char)0x09, (char)0xE0, (short)0, inverters[inv].SUSyID, inverters[inv].Serial);
            ethernet.writeLong(0x70200200);
            ethernet.writeLong(startTime - 86400 - 86400);	//Java time ?
            ethernet.writeLong(startTime + 86400 * (inverters[inv].monthData.length + 1));
            ethernet.writePacketTrailer();
            ethernet.writePacketLength();
	    	
	        if (SBFspot.ConnType == CONNECTIONTYPE.CT_BLUETOOTH)
	            System.err.println("Bluetooth not supported");
	        else
	            ethernet.ethSend(ethernet.pcktBuf, inverters[0].IPAddress);

	        do
	        {
	            long totalWh = 0;
	            long totalWh_prev = 0;
	            final int recordsize = 12;
	            long datetime;

	            int idx = 0;
	            do
	            {
                    rc = smaConn.ethGetPacket();

	                if (rc != E_SBFSPOT.E_OK) 
	                	return rc;

                    packetcount = ethernet.pcktBuf[25];
					short rcvpcktID = (short) (misc.get_short(ethernet.pcktBuf, 27) & 0x7FFF);
                    if ((validPcktID == 1) || (ethernet.pcktID == rcvpcktID))
                    {
                        validPcktID = 1;

                        for(int x = 41; x < (ethernet.packetposition - 3); x += recordsize)
                        {
                            datetime = misc.get_long(ethernet.pcktBuf, x);
							//datetime -= (datetime % 86400) + 43200; // 3.0 - Round to UTC 12:00 - Removed 3.0.1 see issue C54
                            totalWh = misc.get_longlong(ethernet.pcktBuf, x + 4);
                            if (totalWh != Long.MAX_VALUE)
                            {
                                if (totalWh_prev != 0)
                                {
                                    Calendar utc_tm = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                    utc_tm.setTimeInMillis(datetime * 1000);	// Convert to ms since epoch for java

                                    if (utc_tm.get(Calendar.MONTH) == start_tm.get(Calendar.MONTH))
                                    {
                                        if (idx < inverters[inv].monthData.length)
                                        {
                                            inverters[inv].monthData[idx].datetime = datetime;
                                            inverters[inv].monthData[idx].totalWh = totalWh;
                                            inverters[inv].monthData[idx].dayWh = totalWh - totalWh_prev;
                                            idx++;
                                        }
                                    }
                                }
                                totalWh_prev = totalWh;
                            }
                        } //for
					}
                    else
                    {
                        if (DEBUG.HIGHEST) 
                        	System.out.printf("Packet ID mismatch. Expected %d, received %d\n", ethernet.pcktID, rcvpcktID);
                        validPcktID = 0;
                        packetcount = 0;
                    }
	            }
	            while (packetcount > 0);
	        }
	        while (validPcktID == 0);
	    }
	    return E_SBFSPOT.E_OK;
	}

	int ArchiveEventData(InverterData inverters[], Calendar startDate, long UserGroup)
	{
	    int rc = E_SBFSPOT.E_OK; 

	    short pcktcount = 0;
	    int validPcktID = 0;

		long startTime = startDate.getTimeInMillis() / 1000l;
		long endTime = startTime + 86400 * startDate.getActualMaximum(Calendar.DAY_OF_MONTH);

	    for (int inv = 0; inverters[inv] != null && inv < SBFspot.MAX_INVERTERS; inv++)
	    {
	    	//Create the evenData arrayList
		    inverters[inv].eventData = new ArrayList<EventData>();
		    
	    	/* Not neccesary for ethernet
	        do
	        {
	            
	        }
	        while (!isCrcValid(pcktBuf[packetposition-3], pcktBuf[packetposition-2]));
	        */
	    	ethernet.pcktID++;
	    	ethernet.writePacketHeader();
	    	ethernet.writePacket((char)0x09, (char)0xE0, (short)0, inverters[inv].SUSyID, inverters[inv].Serial);
	    	ethernet.writeLong(UserGroup == SBFspot.UG_USER ? 0x70100200 : 0x70120200);
	    	ethernet.writeLong(startTime);
	    	ethernet.writeLong(endTime);
	    	ethernet.writePacketTrailer();
	    	ethernet.writePacketLength();

	        if (SBFspot.ConnType == CONNECTIONTYPE.CT_BLUETOOTH)
	        	System.err.println("Bluetooth not supported");
	            //bthSend(pcktBuf);
	        else
	            ethernet.ethSend(ethernet.pcktBuf, inverters[0].IPAddress);

			boolean FIRST_EVENT_FOUND = false;
	        do
	        {
	            do
	            {
                    rc = smaConn.ethGetPacket();

	                if (rc != E_SBFSPOT.E_OK) 
	                	return rc;

                    pcktcount = ethernet.pcktBuf[25];
					short rcvpcktID = (short) (ethernet.pcktBuf[27] & 0x7FFF);
                    if ((validPcktID == 1) || (ethernet.pcktID == rcvpcktID))
                    {
                        validPcktID = 1;
                        for (int x = 41; x < (ethernet.packetposition - 3); x += SMA_EVENTDATA.GetSize())
                        {
                        	//probably the right translation for this cast (SMA_EVENTDATA *)(pcktBuf + x);
							SMA_EVENTDATA pEventData = new SMA_EVENTDATA(ByteBuffer.wrap(ethernet.pcktBuf, x, SMA_EVENTDATA.GetSize()));
							inverters[inv].eventData.add(new EventData(UserGroup, pEventData));
							if (pEventData.EntryID == 1)
							{
								FIRST_EVENT_FOUND = true;
								rc = E_SBFSPOT.E_EOF;
							}
						}

                    }
                    else
                    {
                        if (DEBUG.HIGHEST) 
                        	System.out.printf("Packet ID mismatch. Expected %d, received %d\n", ethernet.pcktID, rcvpcktID);
                        validPcktID = 0;
                        pcktcount = 0;
                    }
	            }
	            while (pcktcount > 0);
	        }
	        while ((validPcktID == 0) && (!FIRST_EVENT_FOUND));
	    }

	    return rc;
	}
}
