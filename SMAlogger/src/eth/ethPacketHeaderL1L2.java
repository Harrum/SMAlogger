package eth;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ethPacketHeaderL1L2
{
	public ethPacketHeaderL1 pcktHdrL1;
    public ethPacketHeaderL2 pcktHdrL2;
    private ByteBuffer bb;
    
	public ethPacketHeaderL1L2(byte[] packet)
	{
		bb = ByteBuffer.wrap(packet);
        bb.order(ByteOrder.BIG_ENDIAN); // or LITTLE_ENDIAN
		pcktHdrL1 = new ethPacketHeaderL1(bb);
		pcktHdrL2 = new ethPacketHeaderL2(bb);
	}
	
	public ethPacketHeaderL1L2(byte[] packet, ByteOrder order)
	{
		bb = ByteBuffer.wrap(packet);
        bb.order(order); // or LITTLE_ENDIAN
		pcktHdrL1 = new ethPacketHeaderL1(bb);
		pcktHdrL2 = new ethPacketHeaderL2(bb);
	}
	
	public void setSize(int packetposition)
	{
		short dataLength = (short)(packetposition - getSize());
		System.out.println("=========== size is: " + dataLength);
		//System.out.println("datalenght is: " + dataLength);
        pcktHdrL1.hiPacketLen = (byte)((dataLength >> 8) & 0xFF);
        pcktHdrL1.loPacketLen = (byte)(dataLength & 0xFF);
        //bb.put(12, pcktHdrL1.hiPacketLen);
        //bb.put(pcktHdrL1.loPacketLen);
	}
	
	public static short getSize()
	{
		return (short)(ethPacketHeaderL1.getSize() + ethPacketHeaderL2.getSize());
		/*
		short size = 0;
		//ethPacketHeaderL1
    	size += Integer.SIZE / 8;
    	size += Integer.SIZE / 8;
    	size += Integer.SIZE / 8;
    	size += Byte.SIZE / 8;
    	size += Byte.SIZE / 8;
    	
    	//ethPacketHeaderL2
    	size += Integer.SIZE / 8;
    	size += Byte.SIZE / 8;
    	size += Byte.SIZE / 8;
    	return size;
    	*/
	}
}
