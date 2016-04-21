package spfspot;

public class Main 
{
	public static void main(String[] args) 
	{
		SBFspot sbfSpot = new SBFspot();
		int r = sbfSpot.Initialize(args);
		System.out.println("Program terminated: " + r);
	}
}
