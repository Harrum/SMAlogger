package spfspot;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketTimeoutException;

import spfspot.misc.DEBUG;
import spfspot.misc.VERBOSE;

public class Ethernet extends SBFNet
{
	final String IP_Broadcast = "239.12.255.254";
	
	private short port;
	
	private int MAX_CommBuf = 0;
	private DatagramSocket sock;
	private byte[] ReadBuffer = null;
	
	public int ethConnect(short port)
	{
		if (VERBOSE.NORMAL) 
			System.out.println("Initialising Socket...\n");
		try
		{
			sock = new DatagramSocket();
		}
		catch(Exception e)
		{
			System.err.println("Socket error : " + e.getMessage());
		}
		
	    // set up parameters for UDP
		SocketAddress addr_out = new InetSocketAddress(port);
		this.port = port;
		/*
		try 
		{
			sock.bind(addr_out);
		} 
		catch (SocketException e1) 
		{
			System.err.println("Binding socket failed\n" + e1.getMessage());
		}
		*/
		try 
		{
			sock.setBroadcast(true);
		} 
		catch (SocketException e) 
		{
			System.err.println("Setting broadcast failed\n" + e.getMessage());
			return -1;
		}
	    // end of setting broadcast options

	    return 0; //OK
	}
	
	public int ethClose()
	{
		sock.disconnect();
		sock.close();
		return 0;
	}
	
	public int ethRead(byte[] buf, int bufsize)
	{
		boolean keepReading = true;
	    int bytes_read = 0;
	    short timeout = 5; //5 seconds
	    
	    while(keepReading)
	    {
	    	DatagramPacket recv = new DatagramPacket(buf, bufsize);
	    	try 
	    	{
				sock.setSoTimeout(timeout * 1000);
			} 
	    	catch (SocketException e) 
	    	{
	    		if (DEBUG.HIGHEST) System.out.println("Error setting timeout socket \n" + e.getMessage());
				return -1;
			}
	    	try 
	    	{
				sock.receive(recv);
				bytes_read = recv.getLength();
			} 
	    	catch (SocketTimeoutException e1)
	    	{
	    		if (DEBUG.HIGHEST) System.out.println("Timeout reading socket");
				return -1;
	    	}
	    	catch (IOException e) 
	    	{
	    		if (DEBUG.HIGHEST) System.out.println("Error reading socket \n" + e.getMessage());
				return -1;
			}	    	
			
			if ( bytes_read > 0)
			{
				if (bytes_read > MAX_CommBuf)
				{
					MAX_CommBuf = bytes_read;
					if (DEBUG.NORMAL)
						System.out.printf("MAX_CommBuf is now %d bytes\n", MAX_CommBuf);
				}
			   	if (DEBUG.NORMAL)
			   	{
					System.out.printf("Received %d bytes from IP [%s]\n", bytes_read, recv.getAddress().getHostAddress());
			   		if (bytes_read == 600 || bytes_read == 0)
			   			System.out.printf(" ==> packet ignored\n");
				}
			}
			else
				System.out.printf("recvfrom() returned an error: %d\n", bytes_read);

			if (bytes_read == 600) timeout--;	// decrease timeout if the packet received within the timeout is an energymeter packet
			else keepReading = false;
	    }
	    this.ReadBuffer = buf;
	    return bytes_read;
	}
	
	public byte[] getReadBuffer()
	{
		return this.ReadBuffer;
	}
	
	public int ethSend(byte[] buffer, String toIP)
	{
		if (DEBUG.NORMAL) 
			misc.HexDump(buffer, packetposition, 10);

		//addr_out.sin_addr.s_addr = inet_addr(toIP);
	    //size_t bytes_sent = sendto(sock, (const char*)buffer, packetposition, 0, (struct sockaddr *)&addr_out, sizeof(addr_out));
	    DatagramPacket p = new DatagramPacket(buffer, packetposition, new InetSocketAddress(toIP, port));
		int bytes_sent = p.getLength();
	    try 
	    {
			sock.send(p);
			if (DEBUG.NORMAL) 
		    	System.out.println(bytes_sent + " Bytes sent to IP [" + toIP + "]");
		} 
	    catch (IOException e) 
	    {
	    	if (DEBUG.NORMAL) 
	    		System.out.println("Failed to send data");
	    	return 0;
		}
	    return bytes_sent;
	}
}
