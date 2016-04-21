package spfspot;

import inverterdata.EventData;
import inverterdata.InverterData;
import inverterdata.InverterData.DEVICECLASS;
import inverterdata.InverterData.getInverterDataType;
import inverterdata.SMA_EVENTDATA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import spfspot.Config.CONNECTIONTYPE;
import spfspot.SMAConnection.E_SBFSPOT;
import spfspot.misc.DEBUG;
import spfspot.misc.VERBOSE;

public class SBFspot
{
	int MAX_CommBuf = 0;
	int MAX_pcktBuf = 0;
	
	final String VERSION = "0.1";
	public final static int MAX_INVERTERS = 10;
	final int MAX_CFG_AD = 300; // Days
	final int MAX_CFG_AM = 300; // Months
	final int MAX_CFG_AE = 300; // Months
	public final static long UG_USER = 0x07L;
	public final static long UG_INSTALLER = 0x0AL;
	
	//public vars
	public static int quiet = 0;
	String DateTimeFormat;
	String DateFormat;
	public static CONNECTIONTYPE ConnType = CONNECTIONTYPE.CT_NONE;
	TagDefs tagdefs = new TagDefs();
	private SMAConnection smaConn;
	private ArchData archData;
	
	public SBFspot()
	{
		smaConn = new SMAConnection(tagdefs);
		archData = new ArchData(smaConn);
	}
	
	public int Initialize(String[] args)
	{
		char[] msg = new char[80];
		
		int rc = 0;
		
		Config cfg = new Config();
		
		//Read the command line and store settings in config struct
	    rc = parseCmdline(args.length, args, cfg);
	    if (rc == -1) return 1;	//Invalid commandline - Quit, error
	    if (rc == 1) return 0;	//Nothing to do - Quit, no error

	    //Read config file and store settings in config struct
	    try 
	    {
			rc = GetConfig(cfg);
		} 
	    catch (IOException e) 
	    {
			e.printStackTrace();
			return rc;
		}	
	    //Config struct contains fullpath to config file
	    if (rc != 0) return rc;

	    //Copy some config settings to public variables
	    DEBUG.SetDebug(cfg.debug);
	    VERBOSE.SetVerbose(cfg.verbose);
	    quiet = cfg.quiet;
	    ConnType = cfg.ConnectionType;
	    DateTimeFormat = cfg.DateTimeFormat;
	    DateFormat = cfg.DateFormat;
	    
	    if(VERBOSE.NORMAL)
	    	System.out.println("Starting...\n");
		
	    // If co-ordinates provided, calculate sunrise & sunset times
	    // for this location
	    /*
	    if ((cfg.latitude != 0) || (cfg.longitude != 0))
	    {
	        cfg.isLight = sunrise_sunset(cfg.latitude, cfg.longitude, &cfg.sunrise, &cfg.sunset, (float)cfg.SunRSOffset / 3600);

	        if (VERBOSE_NORMAL)
	        {
	            printf("sunrise: %02d:%02d\n", (int)cfg.sunrise, (int)((cfg.sunrise - (int)cfg.sunrise) * 60));
	            printf("sunset : %02d:%02d\n", (int)cfg.sunset, (int)((cfg.sunset - (int)cfg.sunset) * 60));
	        }

	        if ((cfg.forceInq == 0) && (cfg.isLight == 0))
	        {
	            if (quiet == 0) puts("Nothing to do... it's dark. Use -finq to force inquiry.");
	            return 0;
	        }
	    }*/

		int status = tagdefs.readall(cfg.AppPath, new String(cfg.locale));
		if (status != TagDefs.READ_OK)
		{
			System.err.print("Error reading tags\n");
			return(2);
		}
		
		//Allocate array to hold InverterData structs
		InverterData[] Inverters = new InverterData[MAX_INVERTERS];
	    for (int i=0; i<MAX_INVERTERS; i++)
	    {
	    	Inverters[i] = null;
	    }
	    
	    if (ConnType == CONNECTIONTYPE.CT_BLUETOOTH)
	    {
    		System.err.print("Bluetooth is not supported in this version.");
	    }
	    else // CT_ETHERNET
	    {
			if (VERBOSE.NORMAL) 
				System.out.println("Connecting to Local Network...\n");
			rc = smaConn.ethConnect(cfg.IP_Port);
			if (rc != 0)
			{
				System.err.println("Failed to set up socket connection.");
				return rc;
			}

			rc = smaConn.ethInitConnection(Inverters, cfg.IP_Address);
			if (rc != E_SBFSPOT.E_OK)
			{
				System.err.println("Failed to initialize Speedwire connection.");
				smaConn.ethClose();
				return rc;
			}
	    }
	    
	    if (smaConn.logonSMAInverter(Inverters, cfg.userGroup, cfg.SMA_Password) != E_SBFSPOT.E_OK)
	    {
	        System.err.printf("Logon failed. Check '%s' Password\n", cfg.userGroup == UG_USER? "USER":"INSTALLER");
	        freemem(Inverters);
	        smaConn.ethClose();
	        return 1;
	    }
	    
	    /*************************************************
	     * At this point we are logged on to the inverter
	     *************************************************/
	    
	    if (VERBOSE.NORMAL) 
	    	System.out.println("Logon OK");
	    
	    if (ConnType == CONNECTIONTYPE.CT_BLUETOOTH)
	    {
	        System.err.println("Bluetooth not supported in this version");
	    }
	    else
	        if (quiet == 0) System.out.println("SetInverterTime() not executed! We're still testing..."); //2.4.5a	    
	    
	    //TODO: Typelabel not working
	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.SoftwareVersion)) != 0)
	        System.out.printf("getSoftwareVersion returned an error: %d\n", rc);
	    
	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.TypeLabel)) != 0)
	        System.out.printf("getTypeLabel returned an error: %d\n", rc);    
	    else
	    {
	        for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
	        {
	            if (VERBOSE.NORMAL)
	            {
	                System.out.printf("SUSyID: %d - SN: %d\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
	                System.out.printf("Device Name:      %s\n", new String(Inverters[inv].DeviceName));
	                System.out.printf("Device Class:     %s\n", new String(Inverters[inv].DeviceClass));
	                System.out.printf("Device Type:      %s\n", new String(Inverters[inv].DeviceType));
	                System.out.printf("Software Version: %s\n", Inverters[inv].SWVersion);
	                System.out.printf("Serial number:    %d\n", Inverters[inv].Serial);
	            }
	        }
	    }
	    
	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.BatteryChargeStatus)) != 0)
			System.out.printf("getBatteryChargeStatus returned an error: %d\n", rc);
		else
		{
			for (int inv = 0; Inverters[inv]!= null && inv < MAX_INVERTERS; inv++)
			{
			    if (Inverters[inv].DevClass == DEVICECLASS.BatteryInverter)
			    {
	                if (VERBOSE.NORMAL)
	                {
	                    System.out.printf("SUSyID: %d - SN: %lu\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
	                    System.out.printf("Batt. Charging Status: %lu%%\n", Inverters[inv].BatChaStt);
	                }
			    }
			}
		}

		if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.BatteryInfo)) != 0)
			System.out.printf("getBatteryInfo returned an error: %d\n", rc);
		else
		{
			for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
			{
			    if (Inverters[inv].DevClass == DEVICECLASS.BatteryInverter)
			    {
	                if (VERBOSE.NORMAL)
	                {
	                    System.out.printf("SUSyID: %d - SN: %lu\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
	                    System.out.printf("Batt. Temperature: %3.1f%sC\n", (float)(Inverters[inv].BatTmpVal / 10), misc.SYM_DEGREE); // degree symbol is different on windows/linux
	                    System.out.printf("Batt. Voltage    : %3.2fV\n", misc.toVolt(Inverters[inv].BatVol));
	                    System.out.printf("Batt. Current    : %2.3fA\n", misc.toAmp(Inverters[inv].BatAmp));
	                }
			    }
			}
		}

	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.DeviceStatus)) != 0)
	        System.out.printf("getDeviceStatus returned an error: %d\n", rc);
	    else
	    {
	        for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
	        {
	            if (VERBOSE.NORMAL)
	            {
	                System.out.printf("SUSyID: %d - SN: %d\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
					System.out.printf("Device Status:      %s\n", tagdefs.getDesc(Inverters[inv].DeviceStatus, "?"));
	            }
	        }
	    }

		if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.InverterTemperature)) != 0)
	        System.out.printf("getInverterTemperature returned an error: %d\n", rc);
	    else
	    {
	        for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
	        {
	            if (VERBOSE.NORMAL)
	            {
	                System.out.printf("SUSyID: %d - SN: %d\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
					//System.out.printf("Device Temperature: %3.1f%sC\n", (float)(Inverters[inv].Temperature / 100f), misc.SYM_DEGREE); // degree symbol is different on windows/linux
					System.out.printf("Device Temperature: %3.1f%sC\n", misc.toCelc(Inverters[inv].Temperature), misc.SYM_DEGREE); // degree symbol is different on windows/linux
	            }
	        }
	    }

		if (Inverters[0].DevClass == DEVICECLASS.SolarInverter)
	    {
	        if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.GridRelayStatus)) != 0)
	            System.out.printf("getGridRelayStatus returned an error: %d\n", rc);
	        else
	        {
	            for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
	            {
	                if (Inverters[inv].DevClass == DEVICECLASS.SolarInverter)
	                {
	                    if (VERBOSE.NORMAL)
	                    {
	                        System.out.printf("SUSyID: %d - SN: %d\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
							System.out.printf("GridRelay Status:      %s\n", tagdefs.getDesc(Inverters[inv].GridRelayStatus, "?"));
	                    }
	                }
	            }
	        }
	    }

	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.MaxACPower)) != 0)
	        System.out.printf("getMaxACPower returned an error: %d\n", rc);
	    else
	    {
	        //TODO: REVIEW THIS PART (getMaxACPower & getMaxACPower2 should be 1 function)
	        if ((Inverters[0].Pmax1 == 0) && (rc = smaConn.getInverterData(Inverters, getInverterDataType.MaxACPower2)) != 0)
	            System.out.printf("getMaxACPower2 returned an error: %d\n", rc);
	        else
	        {
	            for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
	            {
	                if (VERBOSE.NORMAL)
	                {
	                    System.out.printf("SUSyID: %d - SN: %d\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
	                    System.out.printf("Pac max phase 1: %dW\n", Inverters[inv].Pmax1);
	                    System.out.printf("Pac max phase 2: %dW\n", Inverters[inv].Pmax2);
	                    System.out.printf("Pac max phase 3: %dW\n", Inverters[inv].Pmax3);
	                }
	            }
	        }
	    }

	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.EnergyProduction)) != 0)
	        System.out.printf("getEnergyProduction returned an error: %d\n", rc);

	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.OperationTime)) != 0)
	        System.out.printf("getOperationTime returned an error: %d\n", rc);
	    else
	    {
	        for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
	        {
	            if (VERBOSE.NORMAL)
	            {
	                System.out.printf("SUSyID: %d - SN: %d\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
	                System.out.println("Energy Production:");
	                System.out.printf("\tEToday: %.3fkWh\n", misc.tokWh(Inverters[inv].EToday));
	                System.out.printf("\tETotal: %.3fkWh\n", misc.tokWh(Inverters[inv].ETotal));
	                System.out.printf("\tOperation Time: %.2fh\n", misc.toHour(Inverters[inv].OperationTime));
	                System.out.printf("\tFeed-In Time  : %.2fh\n", misc.toHour(Inverters[inv].FeedInTime));
	            }
	        }
	    }

	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.SpotDCPower)) != 0)
	        System.out.printf("getSpotDCPower returned an error: %d\n", rc);

	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.SpotDCVoltage)) != 0)
	        System.out.printf("getSpotDCVoltage returned an error: %d\n", rc);

	    //Calculate missing DC Spot Values
	    if (cfg.calcMissingSpot == 1)
	        CalcMissingSpot(Inverters[0]);

	    for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
	    {
			Inverters[inv].calPdcTot = Inverters[inv].Pdc1 + Inverters[inv].Pdc2;
	        if (VERBOSE.NORMAL)
	        {
	            System.out.printf("SUSyID: %d - SN: %d\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
	            System.out.println("DC Spot Data:");
	            System.out.printf("\tString 1 Pdc: %7.3fkW - Udc: %6.2fV - Idc: %6.3fA\n", misc.tokW(Inverters[inv].Pdc1), misc.toVolt(Inverters[inv].Udc1), misc.toAmp(Inverters[inv].Idc1));
	            System.out.printf("\tString 2 Pdc: %7.3fkW - Udc: %6.2fV - Idc: %6.3fA\n", misc.tokW(Inverters[inv].Pdc2), misc.toVolt(Inverters[inv].Udc2), misc.toAmp(Inverters[inv].Idc2));
	        }
	    }

	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.SpotACPower)) != 0)
	        System.out.printf("getSpotACPower returned an error: %d\n", rc);

	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.SpotACVoltage)) != 0)
	        System.out.printf("getSpotACVoltage returned an error: %d\n", rc);

	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.SpotACTotalPower)) != 0)
	        System.out.printf("getSpotACTotalPower returned an error: %d\n", rc);

	    //Calculate missing AC Spot Values
	    if (cfg.calcMissingSpot == 1)
	        CalcMissingSpot(Inverters[0]);

	    for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
	    {
	        if (VERBOSE.NORMAL)
	        {
	            System.out.printf("SUSyID: %d - SN: %d\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
	            System.out.println("AC Spot Data:");
	            System.out.printf("\tPhase 1 Pac : %7.3fkW - Uac: %6.2fV - Iac: %6.3fA\n", misc.tokW(Inverters[inv].Pac1), misc.toVolt(Inverters[inv].Uac1), misc.toAmp(Inverters[inv].Iac1));
	            System.out.printf("\tPhase 2 Pac : %7.3fkW - Uac: %6.2fV - Iac: %6.3fA\n", misc.tokW(Inverters[inv].Pac2), misc.toVolt(Inverters[inv].Uac2), misc.toAmp(Inverters[inv].Iac2));
	            System.out.printf("\tPhase 3 Pac : %7.3fkW - Uac: %6.2fV - Iac: %6.3fA\n", misc.tokW(Inverters[inv].Pac3), misc.toVolt(Inverters[inv].Uac3), misc.toAmp(Inverters[inv].Iac3));
	            System.out.printf("\tTotal Pac   : %7.3fkW\n", misc.tokW(Inverters[inv].TotalPac));
	        }
	    }
	    
	    if ((rc = smaConn.getInverterData(Inverters, getInverterDataType.SpotGridFrequency)) != 0)
	        System.out.printf("getSpotGridFrequency returned an error: %d\n", rc);
	    else
	    {
	        for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
	        {
	            if (VERBOSE.NORMAL)
	            {
	                System.out.printf("SUSyID: %d - SN: %d\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
	                System.out.printf("Grid Freq. : %.2fHz\n", misc.toHz(Inverters[inv].GridFreq));
	            }
	        }
	    }

	    if (Inverters[0].DevClass == DEVICECLASS.SolarInverter)
		{
			for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
			{
				if (VERBOSE.NORMAL)
				{
					System.out.printf("SUSyID: %d - SN: %d\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
					if (Inverters[inv].InverterDatetime.getTime() > 0)
						System.out.printf("Current Inverter Time: %s\n", misc.printDate(Inverters[inv].InverterDatetime));

					if (Inverters[inv].WakeupTime.getTime() > 0)
						System.out.printf("Inverter Wake-Up Time: %s\n", misc.printDate(Inverters[inv].WakeupTime));

					if (Inverters[inv].SleepTime.getTime() > 0)
						System.out.printf("Inverter Sleep Time  : %s\n", misc.printDate(Inverters[inv].SleepTime));
				}
			}
		}
	    
	    /* Logging stuff, not needed maybe later.
		if (Inverters[0].DevClass == DEVICECLASS.SolarInverter)
		{
			if ((cfg.CSV_Export == 1) && (cfg.nospot == 0))
				ExportSpotDataToCSV(&cfg, Inverters);

			if (cfg.wsl == 1)
				ExportSpotDataToWSL(&cfg, Inverters);

			if (cfg.s123 == S123_DATA)
				ExportSpotDataTo123s(&cfg, Inverters);
			if (cfg.s123 == S123_INFO)
				ExportInformationDataTo123s(&cfg, Inverters);
			if (cfg.s123 == S123_STATE)
				ExportStateDataTo123s(&cfg, Inverters);
		}
		*/

		if (Inverters[0].DevClass == DEVICECLASS.BatteryInverter)
		{
			//Note removed the logging part, also removed a second if statement which was the same as the first, why ?
			smaConn.logoffSMAInverter(Inverters[0]);
			freemem(Inverters);
			smaConn.ethClose();
			System.out.printf("Terminating here... Dealing with Battery Inverter.\n");
			System.exit(0);
		}
		
		//SolarInveretr -> Continue to get archive data
		int idx;

	    //Get current time
	    cfg.archdata_from = new Date();

	    
	    /***************
	    * Get Day Data *
	    ****************/
	    
	    long arch_time = System.currentTimeMillis();
	    for (int count = 0; count < cfg.archDays; count++)
	    {
	        if ((rc = archData.ArchiveDayData(Inverters, arch_time)) != E_SBFSPOT.E_OK)
	        {
	            if (rc != E_SBFSPOT.E_ARCHNODATA) 
	            	System.out.printf("ArchiveDayData returned an error: %d\n", rc);
	        }
	        else
	        {
	            if (VERBOSE.HIGH)
	            {
	                for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
	                {
	                    System.out.printf("SUSyID: %d - SN: %d\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
	                    for (idx = 0; idx < Inverters[inv].dayData.length; idx++)
	                    {
	                        if (Inverters[inv].dayData[idx].datetime > 0)
	                        {
	                        	//SimpleDateFormat sdf = new SimpleDateFormat(cfg.DateTimeFormat);
	                            System.out.printf("%s : %.3fkWh - %3.3fW\n", misc.printDate(new Date(Inverters[inv].dayData[idx].datetime * 1000)), (double)Inverters[inv].dayData[idx].totalWh/1000, (double)Inverters[inv].dayData[idx].watt);
	                        }
	                    }
                        System.out.println("======");
	                }
	            }
	            
	            /* No csv or sql export
	            if (cfg.CSV_Export == 1)
	                ExportDayDataToCSV(&cfg, Inverters);
				#if defined(USE_SQLITE) || defined(USE_MYSQL)
				if ((!cfg.nosql) && db.isopen())
					db.day_data(Inverters);
				#endif
				*/
	        }
	        //Goto previous day
	        arch_time -= 86400;
	    }
	    
	    /*****************
	     * Get Month Data *
	     ******************/
    	arch_time = System.currentTimeMillis();
    	Calendar arch_tm = Calendar.getInstance();
    	arch_tm.setTimeInMillis(arch_time);
//	 	memcpy(&arch_tm, localtime(&arch_time), sizeof(arch_tm));

    	for (int count = 0; count < cfg.archMonths; count++)
    	{
    		archData.ArchiveMonthData(Inverters, arch_tm);

    		if (VERBOSE.HIGH)
    		{
    			for (int inv = 0; Inverters[inv] != null && inv < MAX_INVERTERS; inv++)
    			{
    				System.out.printf("SUSyID: %d - SN: %d\n", Inverters[inv].SUSyID, Inverters[inv].Serial);
    				for (idx = 0; idx < Inverters[inv].monthData.length; idx++)
    				{
    					if(Inverters[inv].monthData[idx] != null)
    					{
	    					if (Inverters[inv].monthData[idx].datetime > 0)
	    					{
	    						//SimpleDateFormat sdf = new SimpleDateFormat(cfg.DateTimeFormat);
	    						System.out.printf("%s : %.3fkWh - %3.3fkWh\n", misc.printDate(new Date(Inverters[inv].monthData[idx].datetime * 1000)), (double)Inverters[inv].monthData[idx].totalWh/1000, (double)Inverters[inv].monthData[idx].dayWh/1000);
	    					}
    					}
    				}
    				System.out.println("======");
    			}
    		}
	         
	         /*
	 		if ((cfg.CSV_Export == 1) && (cfg.archMonths > 0))
	             ExportMonthDataToCSV(&cfg, Inverters);

	 		#if defined(USE_SQLITE) || defined(USE_MYSQL)
	 		if ((!cfg.nosql) && db.isopen())
	 			db.month_data(Inverters);
	 		#endif
	          */
	         //Go to previous month
    		if(arch_tm.get(Calendar.MONTH) > 0)
			{
    			arch_tm.set(Calendar.MONDAY, arch_tm.get(Calendar.MONTH) - 1);
			}
    		else
    		{
    			arch_tm.set(Calendar.MONTH, 11);
    			arch_tm.set(Calendar.YEAR, arch_tm.get(Calendar.YEAR) - 1);
    		}
	     }

	     /*****************
	     * Get Event Data *
	     ******************/
	 	
    	Calendar dt_utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    	dt_utc.set(Calendar.DAY_OF_MONTH, 1);
	 	
	 	for (int m = 0; m < cfg.archEventMonths; m++)
	 	{
	 		if (VERBOSE.LOW) 
	 		{
	 			System.out.println("Reading events: " + misc.printDate(dt_utc.getTime()));
	 		}
	 		
 			//Get user level events
	 		rc = archData.ArchiveEventData(Inverters, dt_utc, UG_USER);
	 		if (rc == E_SBFSPOT.E_EOF) 
	 			break; // No more data (first event reached)
	 		else if (rc != E_SBFSPOT.E_OK) 
	 			System.err.println("ArchiveEventData(user) returned an error: " + rc);

	 		//When logged in as installer, get installer level events
	 		if (cfg.userGroup == UG_INSTALLER)
	 		{
	 			rc = archData.ArchiveEventData(Inverters, dt_utc, UG_INSTALLER);
	 			if (rc == E_SBFSPOT.E_EOF) 
	 				break; // No more data (first event reached)
	 			else if (rc != E_SBFSPOT.E_OK) 
	 				System.err.println("ArchiveEventData(installer) returned an error: " + rc);
	 		}
	 		
	 		//Move to previous month
	 		if(dt_utc.get(Calendar.MONTH) > 0)
			{
	 			dt_utc.set(Calendar.MONDAY, dt_utc.get(Calendar.MONTH) - 1);
			}
    		else
    		{
    			dt_utc.set(Calendar.MONTH, 11);
    			dt_utc.set(Calendar.YEAR, dt_utc.get(Calendar.YEAR) - 1);
    		}

	 	}

	 	if (rc == E_SBFSPOT.E_OK)
	 	{
	 		//Adjust start of range with 1 months
	 		if (dt_utc.get(Calendar.MONTH) == 11)
	 			dt_utc.set(dt_utc.get(Calendar.YEAR) + 1, Calendar.JANUARY, 1);
	 		else
	 			dt_utc.set(dt_utc.get(Calendar.YEAR), dt_utc.get(Calendar.MONTH) + 1, 1);
	 	}
	 	
	 	if ((rc == E_SBFSPOT.E_OK) || (rc == E_SBFSPOT.E_EOF))
		{
	 		if(VERBOSE.HIGH)
	 			PrintEventData(Inverters);
		}
	 	/* No csv or sql export
	 	if ((rc == E_SBFSPOT.E_OK) || (rc == E_SBFSPOT.E_EOF))
	 	{
	 		dt_range_csv = str(format("%d%02d-%s") % dt_utc.year() % static_cast<short>(dt_utc.month()) % dt_range_csv);

	 		if ((cfg.CSV_Export == 1) && (cfg.archEventMonths > 0))
	 			ExportEventsToCSV(&cfg, Inverters, dt_range_csv);

	 	#if defined(USE_SQLITE) || defined(USE_MYSQL)
	 	if ((!cfg.nosql) && db.isopen())
	 		db.event_data(Inverters, tagdefs);
	 	#endif
	 	}
	 	*/

	    smaConn.logoffSMAInverter(Inverters[0]);
	    freemem(Inverters);
	    smaConn.ethClose();

	    System.out.println("Done.");
	    
		return 0;
	}
	
	public void PrintEventData(InverterData[] inverters)
	{
		if (VERBOSE.NORMAL) 
			System.out.println("PrintEventData()");

		for (int inv = 0; inverters[inv] != null && inv < MAX_INVERTERS; inv++)
		{
			System.out.println("======");
			// Sort events on ascending Entry_ID
			Collections.sort(inverters[inv].eventData, Comparator.comparing(EventData::EntryID));
			//std::sort(inverters[inv]->eventData.begin(), inverters[inv]->eventData.end(), SortEntryID_Asc);

			for(EventData ed : inverters[inv].eventData)
			{
				System.out.printf("Eventnumber: %d", ed.Counter());
				System.out.printf("\tDeviceType \t%s\n", new String(inverters[inv].DeviceType));
				System.out.printf("\tDeviceLocation \t%s\n", new String(inverters[inv].DeviceName));
				System.out.printf("\tSusyId \t%d\n", ed.SUSyID());
				System.out.printf("\tSerNo \t%d\n", ed.SerNo());
				System.out.printf("\tTimeStamp \t%s\n", misc.printDate(ed.DateTime()));
				System.out.printf("\tEntryId \t%d\n", ed.EntryID());
				System.out.printf("\tEventCode \t%d\n", ed.EventCode());
				System.out.printf("\tEventType \t%s\n", ed.EventType());
				System.out.printf("\tCategory \t%s\n", ed.EventCategory());
				System.out.printf("\tGroup \t%s\n", tagdefs.getDesc(ed.Group()));
				System.out.print("\tTag \t");
				String EventDescription = tagdefs.getDesc(ed.Tag());

				// If description contains "%s", replace it with localized parameter
				if (EventDescription.contains("%s"))
					System.out.printf(EventDescription, tagdefs.getDescForLRI(ed.Parameter()));
				else
					System.out.print(EventDescription);
				System.out.print("\n");
				
				// As an extra: export old and new values
				// This is "forgotten" in Sunny Explorer
				switch (ed.DataType())
				{
				case 0x08: // Status
					System.out.printf("\tOldValue \t%s\n", tagdefs.getDesc(ed.OldVal() & 0xFFFF));
					System.out.printf("\tNewValue \t%s\n", tagdefs.getDesc(ed.NewVal() & 0xFFFF));
					break;

				case 0x00: // Unsigned int
					System.out.printf("\tOldValue \t%s\n", Integer.toUnsignedString(ed.OldVal()));
					System.out.printf("\tNewValue \t%s\n", Integer.toUnsignedString(ed.NewVal()));
					break;

				case 0x40: // Signed int
					System.out.printf("\tOldValue \t%d\n", ed.OldVal());
					System.out.printf("\tNewValue \t%d\n", ed.NewVal());
					break;

				case 0x10: // String
					System.out.printf("\tOldValue \t%08X\n", ed.OldVal());
					System.out.printf("\tNewValue \t%08X\n", ed.NewVal());
					break;

				default:
					System.out.printf("\tOldValue \t%s\n", " - ");
					System.out.printf("\tNewValue \t%s\n", " - ");
				}

				// As an extra: User or Installer Event
				System.out.printf("\tUserGroup \t%s\n", tagdefs.getDesc(ed.UserGroupTagID()));
			}
			System.out.println("======");
		}
	}
	
	//Free memory allocated by initialiseSMAConnection()
	public void freemem(InverterData[] inverters)
	{
	    for (int i=0; i<inverters.length; i++)
	        if (inverters[i] != null)
	        {
	        	inverters[i] = null;
	        }
	}
	
	private int parseCmdline(int argc, String[] argv, Config cfg)
	{
		cfg.debug = 0;				// debug level - 0=none, 5=highest
		cfg.verbose = 0;			// verbose level - 0=none, 5=highest
	    cfg.archDays = 1;			// today only
	    cfg.archMonths = 1;			// this month only
		cfg.archEventMonths = 1;	// this month only
//	    cfg.upload = 0;				// upload to PVoutput and others (See config file)
	    cfg.forceInq = 0;			// Inquire inverter also during the night
	    cfg.userGroup = UG_USER;
	    // WebSolarLog support (http://www.websolarlog.com/index.php/tag/sma-spot/)
	    // This is an undocumented feature and should only be used for WebSolarLog
	    cfg.wsl = 0;
	    cfg.quiet = 0;
	    cfg.nocsv = 0;
	    cfg.nospot = 0;
		cfg.nosql = 0;
	    // 123Solar Web Solar logger support(http://www.123solar.org/)
	    // This is an undocumented feature and should only be used for 123solar
	    //cfg.s123 = S123_NOP;
		cfg.loadlive = 0;	//force settings to prepare for live loading to http://pvoutput.org/loadlive.jsp

	    //Set quiet mode
	    for (int i = 1; i < argc; i++)
		{
	        if (argv[i].equals("-q"))
	        {
	            cfg.quiet = 1;
	            break;
	        }
		}

		cfg.AppPath = new File(".").getAbsolutePath();
		int pos = cfg.AppPath.lastIndexOf('\\');
		if (pos != -1)
			cfg.AppPath = cfg.AppPath.substring(++pos);
		else
			cfg.AppPath = "";

		//Build fullpath to config file (SBFspot.cfg should be in same folder as SBFspot.exe)
		cfg.ConfigFile = cfg.AppPath + "\\support_files\\SBFspot.cfg";

	    char pEnd = 0;
	    int lValue = 0;

	    if (cfg.quiet == 0)
	    {
	        SayHello(0);
	        System.out.println("Commandline Args:\n");
	        for (int i = 0; i < argc; i++)
	        	System.out.print(argv[i]);

	        System.out.println("\n");
	    }

	    for (int i = 0; i < argc; i++)
	    {
	        if (argv[i] ==  "/")
	            argv[i] = "-";

	        //Set #days (archived daydata)
	        if (argv[i].startsWith("-ad"))
	        {
				if (argv[i].length() > 6)
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
				lValue = Integer.parseInt(argv[i].substring(3, argv[i].length()));
	            if ((lValue < 0) || (lValue > MAX_CFG_AD) || (pEnd != 0))
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
	                cfg.archDays = lValue;

	        }

	        //Set #months (archived monthdata)
	        else if (argv[i].startsWith("-am"))
	        {
				if (argv[i].length() > 6)
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            lValue = Integer.parseInt(argv[i].substring(3, argv[i].length()));
	            if ((lValue < 0) || (lValue > MAX_CFG_AM) || (pEnd != 0))
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
	                cfg.archMonths = lValue;
	        }

			//Set #days (archived events)
			else if (argv[i].startsWith("-ae"))
			{
				if (argv[i].length() > 6)
				{
					InvalidArg(argv[i]);
					return -1;
				}
				lValue = Integer.parseInt(argv[i].substring(3, argv[i].length()));
				if ((lValue < 0) || (lValue > MAX_CFG_AE) || (pEnd != 0))
				{
					InvalidArg(argv[i]);
					return -1;
				}
				else
					cfg.archEventMonths = lValue;

			}

	        //Set debug level
	        else if(argv[i].startsWith("-d"))
	        {
	            lValue = Integer.parseInt(argv[i].substring(2, argv[i].length()));
	            if (argv[i].length() == 2) lValue = 2;	// only -d sets medium debug level
	            if ((lValue < 0) || (lValue > 5) || (pEnd != 0))
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
	                cfg.debug = lValue;
	        }

	        //Set verbose level
	        else if (argv[i].startsWith("-v"))
	        {
	        	if (argv[i].length() == 2) 
	        		lValue = 2;	// only -v sets medium verbose level
	        	else
	        		lValue = Integer.parseInt(argv[i].substring(2, argv[i].length()));
	            if (lValue < 0 || lValue > 5 || pEnd != 0)
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
	                cfg.verbose = lValue;
	        }

			//force settings to prepare for live loading to http://pvoutput.org/loadlive.jsp
			else if (argv[i].equals("-liveload") || argv[i].equals("-loadlive"))
				cfg.loadlive = 1;

			//Set upload flag
	        //else if (stricmp(argv[i], "-u") == 0)
	        //    cfg->upload = 1;

	        //Set inquiryDark flag
	        else if (argv[i].equals("-finq"))
	            cfg.forceInq = 1;

	        //Set WebSolarLog flag (Undocumented - For WSL usage only)
	        else if (argv[i].equals("-wsl"))
	            cfg.wsl = 1;

	        //Set 123Solar command value (Undocumented - For WSL usage only)
	        /*disabled
	        else if (strnicmp(argv[i], "-123s", 5) == 0)
	        {
	            if (strlen(argv[i]) == 5)
	                cfg->s123 = S123_DATA;
	            else if (strnicmp(argv[i]+5, "=DATA", 5) == 0)
	                cfg->s123 = S123_DATA;
	            else if (strnicmp(argv[i]+5, "=INFO", 5) == 0)
	                cfg->s123 = S123_INFO;
	            else if (strnicmp(argv[i]+5, "=SYNC", 5) == 0)
	                cfg->s123 = S123_SYNC;
	            else if (strnicmp(argv[i]+5, "=STATE", 6) == 0)
	                cfg->s123 = S123_STATE;
	            else
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	        }
			*/
	        //Set NoCSV flag (Disable CSV export - Overrules Config setting)
	        else if (argv[i].equals("-nocsv"))
	            cfg.nocsv = 1;

	        //Set NoSQL flag (Disable SQL export)
	        else if (argv[i].equals("-nosql"))
	            cfg.nosql = 1;

			//Set NoSpot flag (Disable Spot CSV export)
	        else if (argv[i].equals("-sp0"))
	            cfg.nospot = 1;

	        else if (argv[i].equals("-installer"))
	            cfg.userGroup = UG_INSTALLER;

	        else if (argv[i].startsWith("-password:"))
	            if (argv[i].length() == 10)
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
	            {
	            	//cfg.SMA_Password = argv[i]+10;
	            	String argPass = argv[i].substring(10, argv[i].length());
	            	for(int ch = 0; ch < argPass.length(); ch++)
	            	{
	            		cfg.SMA_Password[ch] = argPass.charAt(ch);
	            	}
	            	//cfg.SMA_Password = argv[i].substring(10, argv[i].length());
	                //strncpy(cfg->SMA_Password, argv[i]+10, sizeof(cfg->SMA_Password));
	            }

	        //look for alternative config file
	        else if (argv[i].startsWith("-cfg"))
	        {
	            if (argv[i].length() == 4)
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
				{
					//Fix Issue G90 (code.google.com)
					//If -cfg arg has no '\' it's only a filename and should be in the same folder as SBFspot executable
					cfg.ConfigFile = argv[i].substring(4, argv[i].length());
					if (cfg.ConfigFile.indexOf("\\") == -1)
						cfg.ConfigFile = cfg.AppPath + argv[i].substring(3, argv[i].length());
				}
	        }

	        //Scan for bluetooth devices
	        else if (argv[i].equals("-scan"))
	        {
	        	InvalidArg(argv[i] + " bluetooth not supported.");
                return -1;
	        }

	        //Show Help
	        else if (argv[i].equals("-?"))
	        {
	            SayHello(1);
	            return 1;	// Caller should terminate, no error
	        }

	        else if (cfg.quiet == 0)
	        {
	            InvalidArg(argv[i]);
	            return -1;
	        }

	    }

	    //Disable verbose/debug modes when silent
	    if (cfg.quiet == 1)
	    {
	        cfg.verbose = 0;
	        cfg.debug = 0;
	    }

	    return 0;
	}
	
	private void InvalidArg(String arg)
	{
	    System.out.printf("Invalid argument: %s\nUse -? for help\n", arg);
	}
	
	private void SayHello(int ShowHelp)
	{
		System.out.println("SmaLogger Java v" + VERSION + "\n");
		System.out.println("Based on yet another tool to read power production of SMA solar inverters by SBFspot (https://sbfspot.codeplex.com)\n");
		System.out.println("(c) 2015, Hoogterp\n");
	    System.out.println("Compiled for " + System.getProperty("os.name") + " in Java (" + System.getProperty("os.arch") + ")");
	    if (ShowHelp != 0)
	    {
	    	System.out.println("SBFspot [-options]\n");
	    	//bluetooth disabled
	    	//System.out.println(" -scan          Scan for bluetooth enabled SMA inverters.\n");
	    	System.out.println(" -d#            Set debug level: 0-5 (0=none, default=2)\n");
	    	System.out.println(" -v#            Set verbose output level: 0-5 (0=none, default=2)\n");
	    	System.out.println(" -ad#           Set #days for archived daydata: 0-" + MAX_CFG_AD + "\n");
	    	System.out.println("                0=disabled, 1=today (default), ...\n");
	    	System.out.println(" -am#           Set #months for archived monthdata: 0-" + MAX_CFG_AM + "\n");
	    	System.out.println("                0=disabled, 1=current month (default), ...\n");
	    	System.out.println(" -ae#           Set #months for archived events: 0-" + MAX_CFG_AE + "\n");
	    	System.out.println("                0=disabled, 1=current month (default), ...\n");
	    	System.out.println(" -cfgX.Y        Set alternative config file to X.Y (multiple inverters)\n");
	    	System.out.println(" -u             Upload to online monitoring system (see config file)\n");
	    	System.out.println(" -finq          Force Inquiry (Inquire inverter also during the night)\n");
	    	System.out.println(" -q             Quiet (No output)\n");
	    	System.out.println(" -nocsv         Disables CSV export (Overrules CSV_Export in config)\n");
	    	System.out.println(" -nosql         Disables SQL export\n");
	    	System.out.println(" -sp0           Disables Spot.csv export\n");
	    	System.out.println(" -installer     Login as installer\n");
	    	System.out.println(" -password:xxxx Installer password\n");
	    	System.out.println(" -loadlive      Use predefined settings for manual upload to pvoutput.org\n");
	    }
	}
	
	/* read Config from file */
	private int GetConfig(Config cfg) throws IOException
	{
	    //Initialise config structure and set default values
		cfg.prgVersion = VERSION;
	    //memset(cfg.BT_Address, 0, sizeof(cfg.BT_Address));
	    //memset(cfg.IP_Address, 0, sizeof(cfg.IP_Address));
		cfg.BT_Address = "";
		cfg.IP_Address = "";
	    cfg.outputPath = "";
		cfg.outputPath_Events = "";
	    if (cfg.userGroup == UG_USER) cfg.SMA_Password[0] = 0;
	    cfg.plantname = "";
	    cfg.latitude = 0.0f;
	    cfg.longitude = 0.0f;
	    cfg.archdata_from = null;
	    cfg.archdata_to = null;
	    cfg.delimiter = ';';
	    cfg.precision = 3;
	    cfg.decimalpoint = ',';
	    cfg.BT_Timeout = 5;
	    cfg.BT_ConnectRetries = 10;

	    cfg.calcMissingSpot = 0;
	    cfg.DateTimeFormat = "d/m/Y H:M:S";
	    cfg.DateFormat = "d/m/Y";
	    cfg.TimeFormat = "H:M:S";
	    cfg.synchTime = 1;
	    cfg.CSV_Export = 1;
	    cfg.CSV_ExtendedHeader = 1;
	    cfg.CSV_Header = 1;
	    cfg.CSV_SaveZeroPower = 1;
	    cfg.SunRSOffset = 900;
//	    cfg.PVoutput = 0;
//	    cfg.VoltLogging = VL_AC_MAX;
	    cfg.SpotTimeSource = 0;
	    cfg.SpotWebboxHeader = 0;
	    cfg.MIS_Enabled = 0;
	    cfg.locale = "en-US";
//		cfg.PVoutput_InvTemp=0;
//		cfg.PVoutput_InvTempMapTo=5;
//		cfg.PVoutput_CumulNRG = 0;

	    final String CFG_Boolean = "(0-1)";
	    final String CFG_InvalidValue = "Invalid value for '%s' %s\n";

	    FileReader fr;
	    //BufferedReader br = new BufferedReader(fr);

	    try
	    {
	    	fr = new FileReader(cfg.ConfigFile);
	    }
	    catch(Exception e)
	    {
	    	System.err.println("Error! Could not open file " + cfg.ConfigFile + "\n" + e.getMessage());
	    	return -1;
	    }

		if (cfg.verbose >= 2)
			System.out.println("Reading config '" + cfg.ConfigFile + "'");

	    char pEnd = 0;
	    int lValue = 0;
	    String line = "";
	    int rc = 0;
	    
	    BufferedReader br = new BufferedReader(fr);
	    
	    while((line = br.readLine()) != null)
	    {
	        if (line.length() > 0 && line.charAt(0) != '#' && line.charAt(0) != 0 && line.charAt(0) != 10)
	        {
	        	int index = line.indexOf("=");
	        	String variable = line.substring(0, index);
	        	String value = line.substring(index + 1, line.length());
        		
	            if ((value != null) && (value.trim() != ""))
	            {
					//if(stricmp(variable, "BTaddress") == 0) strncpy(cfg.BT_Address, value, sizeof(cfg.BT_Address));
	                if(variable.equals("IP_Address")) 
	                	cfg.IP_Address = value;
					else if(variable.equals("Password"))
					{
	                    if(cfg.userGroup == UG_USER) 
	                    {
	                    	for(int ch = 0; ch < value.length(); ch++)
	                    	{
	                    		cfg.SMA_Password[ch] = value.charAt(ch);
	                    	}
	                    	//cfg.SMA_Password = value;
	                    }
					}
					else if(variable.equals("OutputPath")) 
						cfg.outputPath = value;
					else if(variable.equals("OutputPathEvents")) 
						cfg.outputPath_Events = value;
					else if(variable.equals("Latitude")) 
						cfg.latitude = Float.valueOf(value);
					else if(variable.equals("Longitude")) 
						cfg.longitude = Float.valueOf(value);
					else if(variable.equals("Plantname")) 
						cfg.plantname = value;
					else if(variable.equals("CalculateMissingSpotValues"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        cfg.calcMissingSpot = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("DateTimeFormat")) 
						cfg.DateTimeFormat = value;
					else if(variable.equals("DateFormat")) 
						cfg.DateFormat = value;
					else if(variable.equals("TimeFormat")) 
						cfg.TimeFormat = value;
					else if(variable.equals("DecimalPoint"))
	                {
						if (value.equals("comma")) 
							cfg.decimalpoint = ',';
						else if ((value.equals("dot")) || (value.equals("point"))) 
							cfg.decimalpoint = '.'; // Fix Issue 84 - 'Point' is accepted for backward compatibility
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, "(comma|dot)");
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_Delimiter"))
	                {
						if (value.equals("comma")) 
							cfg.delimiter = ',';
						else if (value.equals("semicolon")) 
							cfg.delimiter = ';';
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, "(comma|semicolon)");
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("SynchTime"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        cfg.synchTime = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_Export"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        cfg.CSV_Export = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_ExtendedHeader"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        cfg.CSV_ExtendedHeader = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_Header"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        cfg.CSV_Header = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_SaveZeroPower"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        cfg.CSV_SaveZeroPower = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("SunRSOffset"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if ((lValue >= 0) && (lValue <= 3600) && (pEnd == 0))
	                        cfg.SunRSOffset = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, "(0-3600)");
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_Spot_TimeSource"))
	                {
						if (value.equals("Inverter")) 
							cfg.SpotTimeSource = 0;
						else if (value.equals("Computer")) 
							cfg.SpotTimeSource = 1;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, "Inverter|Computer");
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_Spot_WebboxHeader"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        cfg.SpotWebboxHeader = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
	                else if(variable.equals("MIS_Enabled"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        cfg.MIS_Enabled = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("Locale"))
					{
						if ((value.equals("de-DE")) ||
							(value.equals("en-US")) ||
							(value.equals("fr-FR")) ||
							(value.equals("nl-NL")) ||
							(value.equals("it-IT")) ||
							(value.equals("es-ES"))
							)
							cfg.locale = value;
						else
						{
							System.err.printf(CFG_InvalidValue, variable, "de-DE|en-US|fr-FR|nl-NL|it-IT|es-ES");
	                        rc = -2;
						}
					}
					else if(variable.equals("BTConnectRetries"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if ((lValue >= 0) && (lValue <= 15) && (pEnd == 0))
							cfg.BT_ConnectRetries = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, "(1-15)");
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("Timezone"))
					{
						cfg.timezone = value;
						try
						{
							//get the timezone
							cfg.tz = TimeZone.getTimeZone(value);
						}
						catch(Exception e)
						{
							System.err.printf("Invalid timezone specified: " + value);
							rc = -2;
						}
					}

					else if(variable.equals("SQL_Database"))
						cfg.sqlDatabase = value;
	                /*
	#if defined(USE_MYSQL)
					else if(stricmp(variable, "SQL_Hostname") == 0)
						cfg.sqlHostname = value;
					else if(stricmp(variable, "SQL_Username") == 0)
						cfg.sqlUsername = value;
					else if(stricmp(variable, "SQL_Password") == 0)
						cfg.sqlUserPassword = value;
	#endif*/
	                else
	                {
	                	System.err.printf("Warning: Ignoring keyword '%s'\n", variable);
                    	rc = -2;
	                }
	            }
	        }
	    }
	    //fr.close();
	    br.close();

	    if (cfg.BT_Address.length() > 0)
	    {
	        cfg.ConnectionType = CONNECTIONTYPE.CT_BLUETOOTH;
	    }
	    else
	    {
	        cfg.ConnectionType = CONNECTIONTYPE.CT_ETHERNET;
	        cfg.IP_Port = 9522;
	    }

	    if (cfg.SMA_Password.length == 0)
	    {
	    	System.err.printf("Missing USER Password.\n");
        	rc = -2;
	    }

	    if (cfg.decimalpoint == cfg.delimiter)
	    {
	    	System.err.printf("'CSV_Delimiter' and 'DecimalPoint' must be different character.\n");
        	rc = -2;
	    }

	    //Overrule CSV_Export from config with Commandline setting -nocsv
	    if (cfg.nocsv == 1)
	        cfg.CSV_Export = 0;

	    //Silently enable CSV_Header when CSV_ExtendedHeader is enabled
	    if (cfg.CSV_ExtendedHeader == 1)
	        cfg .CSV_Header = 1;

	    if (cfg.outputPath.length() == 0)
	    {
	    	System.err.printf("Missing OutputPath.\n");
        	rc = -2;
	    }

		//If OutputPathEvents is omitted, use OutputPath
		if (cfg.outputPath_Events.length() == 0)
			cfg.outputPath_Events = cfg.outputPath;

	    if (cfg.plantname.length() == 0)
	        cfg.plantname = "MyPlant";

		if (cfg.timezone == null)
		{
			System.err.printf("Missing timezone.\n");
        	rc = -2;
		}
		/*disabled
		//force settings to prepare for live loading to http://pvoutput.org/loadlive.jsp
		if (cfg.loadlive == 1)
		{
			strncat(cfg.outputPath, "/LoadLive", sizeof(cfg.outputPath));
			strcpy(cfg.DateTimeFormat, "%H:%M");
			cfg.CSV_Export = 1;
			cfg.decimalpoint = '.';
			cfg.CSV_Header = 0;
			cfg.CSV_ExtendedHeader = 0;
			cfg.CSV_SaveZeroPower = 0;
			cfg.delimiter = ';';
//			cfg.PVoutput = 0;
			cfg.archEventMonths = 0;
			cfg.archMonths = 0;
			cfg.nospot = 1;
		}*/
		
		// If 1st day of the month and -am1 specified, force to -am2 to get last day of prev month
		if (cfg.archMonths == 1)
		{
			Calendar now;;
			now = Calendar.getInstance();
			if(now.get(Calendar.DAY_OF_MONTH) == 1)
				cfg.archMonths++;
		}

		if (cfg.verbose > 2) ShowConfig(cfg);
	    return rc;
	}
	
	private void ShowConfig(Config cfg)
	{
		System.out.println("Configuration settings:");
		if (cfg.IP_Address.length() == 0)	// No IP address -> Show BT address
			System.out.println("\nBTAddress=" + cfg.BT_Address.toString());
		if (cfg.BT_Address.length() == 0)	// No BT address -> Show IP address
			System.out.println("\nIP_Address=" + cfg.IP_Address.toString());
		System.out.print("\nPassword=<undisclosed>");
		System.out.print("\nPassword=<undisclosed>" + 
			"\nMIS_Enabled=" + cfg.MIS_Enabled +
			"\nPlantname=" + new String(cfg.plantname) + 
			"\nOutputPath=" + new String(cfg.outputPath) + 
			"\nOutputPathEvents=" + new String(cfg.outputPath_Events) + 
			"\nLatitude=" + cfg.latitude + 
			"\nLongitude=" + cfg.longitude + 
			"\nTimezone=" + cfg.timezone + 
			"\nCalculateMissingSpotValues=" + cfg.calcMissingSpot + 
			"\nDateTimeFormat=" + new String(cfg.DateTimeFormat) + 
			"\nDateFormat=" + new String(cfg.DateFormat) + 
			"\nTimeFormat=" + new String(cfg.TimeFormat) + 
			"\nSynchTime=" + cfg.synchTime + 
			"\nSunRSOffset=" + cfg.SunRSOffset + 
			"\nDecimalPoint=" + cfg.decimalpoint + 
			"\nCSV_Delimiter=" + cfg.delimiter + 
			"\nPrecision=" + cfg.precision + 
			"\nCSV_Export=" + cfg.CSV_Export + 
			"\nCSV_ExtendedHeader=" + cfg.CSV_ExtendedHeader + 
			"\nCSV_Header=" + cfg.CSV_Header + 
			"\nCSV_SaveZeroPower=" + cfg.CSV_SaveZeroPower + 
			"\nCSV_Spot_TimeSource=" + cfg.SpotTimeSource + 
			"\nCSV_Spot_WebboxHeader=" + cfg.SpotWebboxHeader + 
			"\nLocale=" + new String(cfg.locale) + 
			"\nBTConnectRetries=" + cfg.BT_ConnectRetries);

		/*
	#if defined(USE_MYSQL) || defined(USE_SQLITE)
		std::cout << "SQL_Database=" << cfg->sqlDatabase << std::endl;
	#endif

	#if defined(USE_MYSQL)
		std::cout << "SQL_Hostname=" << cfg->sqlHostname << \
			"\nSQL_Username=" << cfg->sqlUsername << \
			"\nSQL_Password=<undisclosed>" << std::endl;
	#endif*/
		System.out.println("\n### End of Config ###");
	}
	
	private void CalcMissingSpot(InverterData invData)
	{
		if (invData.Pdc1 == 0) invData.Pdc1 = (invData.Idc1 * invData.Udc1) / 100000;
		if (invData.Pdc2 == 0) invData.Pdc2 = (invData.Idc2 * invData.Udc2) / 100000;

		if (invData.Pac1 == 0) invData.Pac1 = (invData.Iac1 * invData.Uac1) / 100000;
		if (invData.Pac2 == 0) invData.Pac2 = (invData.Iac2 * invData.Uac2) / 100000;
		if (invData.Pac3 == 0) invData.Pac3 = (invData.Iac3 * invData.Uac3) / 100000;

	    if (invData.TotalPac == 0) invData.TotalPac = invData.Pac1 + invData.Pac2 + invData.Pac3;
	}
}
