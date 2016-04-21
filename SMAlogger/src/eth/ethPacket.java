package eth;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.xml.bind.DatatypeConverter;

import org.omg.CORBA.FREE_MEM;

public class ethPacket 
{
	public byte dummy0;
	public ethPacketHeaderL2 pcktHdrL2;
	public ethEndpoint Destination;
	public ethEndpoint Source;
    public short ErrorCode;
    public short FragmentID;  //Count Down
    public short PacketID;    //Count Up
    
    private ByteBuffer bb;
    
    public ethPacket(byte[] packet)
    {
    	bb = ByteBuffer.wrap(packet);
        bb.order(ByteOrder.LITTLE_ENDIAN); // or LITTLE_ENDIAN
        
        dummy0 = bb.get();
        pcktHdrL2 = new ethPacketHeaderL2(bb);
        Destination = new ethEndpoint(bb);
        Source = new ethEndpoint(bb);
        ErrorCode = bb.getShort();
        FragmentID = bb.getShort();
        PacketID = bb.getShort();
    }
}
