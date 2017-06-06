package lab3;

public class Banque {
	//private Map<Int32, RMIsocket> _succursales; //use correct RMI socket class.
	private int PORT_NUMBER = 11111;
	private boolean closing = false; 
	
	public static void Main()
	{		
		Banque b = new Banque();
		
		Thread incomingConnectionThread = new Thread(new Runnable(){
			@Override
			public void run() {
				b.TakeIncomingConnections();
			}
		});
		
		incomingConnectionThread.start();
		
		
		
	}
	
	private void TakeIncomingConnections()
	{
		while(!closing)
		{
			//Start watching incoming connections. 
			
			//Take incoming cnnection, add connection to _succursales. ID is _succrsales.Count().

			//send ID to succursale
			
		}
	}
	
	private void TakeCommands()
	{
		while(!closing)
		{
			String input; //read input from console. 
		}
	}
	
	
	
}
