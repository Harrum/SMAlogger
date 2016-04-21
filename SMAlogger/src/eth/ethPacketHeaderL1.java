package eth;

import java.nio.ByteBuffer;

public class ethPacketHeaderL1 
{
    public int      MagicNumber;      // Packet signature 53 4d 41 00 (SMA\0)
    public int      unknown1;         // 00 04 02 a0
    public int      unknown2;         // 00 00 00 01
    public byte hiPacketLen;      // Packet length stored as big endian
    public byte loPacketLen ;     // Packet length Low Byte
    
    public ethPacketHeaderL1(ByteBuffer bb)
    {
    	MagicNumber = bb.getInt();
        unknown1 = bb.getInt();
        unknown2 = bb.getInt();
        hiPacketLen = bb.get();
        loPacketLen = bb.get();
    }
    
    public static short getSize()
    {
    	short size = 0;
    	size += Integer.SIZE / 8;
    	size += Integer.SIZE / 8;
    	size += Integer.SIZE / 8;
    	size += Byte.SIZE / 8;
    	size += Byte.SIZE / 8;
    	return size;
    }
}
