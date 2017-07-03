package lab3;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import javax.swing.plaf.ActionMapUIResource;

public class Succursale extends UnicastRemoteObject implements SuccursaleInterface, Runnable {
	private int _montant;
	private int _uid;
	private Map<Integer, SuccursaleInterface> _succursales;
	private BanqueInterface _banque;
	private Random _random;
	
	//currently active snapshots and the succursales that started them.
	private Map<Integer, Integer> _observers; //<snapshot token, observerID> 

	protected Succursale(BanqueInterface banque, int montant) throws RemoteException
	{
		_random = new Random();
		_succursales = new HashMap<>();
		_banque = banque;
		_montant = montant;
		_banque.connect(this, _montant);
		_observers = new HashMap<>();
		_floatingMessages = new HashMap<>();
	}
	
	//Gets a list of currently active snapshots. 
	private Integer[] getSnapshotList()
	{
		if(_observers.isEmpty())
			return null;
		return _observers.keySet().toArray(new Integer[_observers.size()]); //avoid synchronization issues
	}

	public int getIdentity() throws RemoteException
	{
		return _uid;
	}

	public void newIdentity(int uid) throws RemoteException
	{
		_uid = uid;
		System.out.println("New identity #" + _uid);
	}

	public void newSuccursale(int uid, SuccursaleInterface succursale) throws RemoteException
	{
		_succursales.put(uid, succursale);
		printSuccursales();
	}

	public void sendSuccursalesMap(Map<Integer, SuccursaleInterface> map) throws RemoteException
	{
		for(Map.Entry<Integer,SuccursaleInterface> entry : map.entrySet())
			_succursales.put(entry.getKey(), entry.getValue());

		_succursales.remove(_uid); //remove succursale's reference to itself. 
		printSuccursales();
	}

	public void receiveMoney(Transfer transfer) throws RemoteException
	{
		_montant += transfer.getMoney();
		System.out.println("Reçu " + transfer.getMoney() + "$ de la succursale " + transfer.getSenderID());
		//System.out.println(this);
		
		//This message was sent before a snapshot request was received, 
		//add it to observer.
		if(!_observers.isEmpty())
		{
			Integer[] activeSnapshots = getSnapshotList();
			for(int i = activeSnapshots.length - 1; i >= 0; i--) //start by the newest tokens. 
			{
				//Transfer has no snapshot token, meaning it was sent when the sender had no
				//active snapshots, or the sender'S current snapshot isn't the latest one. 
				if(transfer.getSnapshotToken() == null || transfer.getSnapshotToken() != activeSnapshots[i])
					_succursales.get(
							_observers.get(activeSnapshots[i])  
							).appendMessage(transfer);
				//Transfer had a snapshot, and it's this one. We caught up in time with the sender, so end the loop.   
				else
				{
					break;
				}
			}
		}
	}

	private void printSuccursales()
	{
		if(_succursales.size() == 0)
			System.out.println("Aucune succursale connectée.");
		else
			System.out.println("Succursales connectées:");

		for(Map.Entry<Integer, SuccursaleInterface> entry : _succursales.entrySet())
			System.out.println("Succursale #"+entry.getKey());

		System.out.println();
	}

	public String toString()
	{
		return "Succursale #" + _uid + ": " + _montant + "$";
	}

	public void run() {
		//ETAT-02: Snapshot loop. Anonymous type because laziness. 
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true)
				{
					try {
						Thread.sleep(10000 + _random.nextInt(20000));
						getSnapshot(); //start snapshot process. 
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		
		while (true)
		{
			try
			{				
                //SUCCURSALE-05: Sleep 5 to 10 seconds between transfers
                Thread.sleep(5000 + _random.nextInt(5000));
				if(_montant > 0 && _succursales.size() > 0)
				{
					int transferAmount = 1 + _random.nextInt(_montant);
					_montant -= transferAmount;
					
					//get random element in map
					int index = _random.nextInt(_succursales.size());
					int receiverID = (Integer) _succursales.keySet().toArray()[index];
					
					SuccursaleInterface randomSuccursale = ((SuccursaleInterface)_succursales.get(receiverID));

					System.out.println("Transfère " + transferAmount + "$ à la succursale #" + randomSuccursale.getIdentity());
					System.out.println(this);

					//TRANSFERT-03: Sleep 5 seconds for latency
					Integer[] activeSnapshots = getSnapshotList();
					Integer snapshotToken = activeSnapshots != null ? activeSnapshots[activeSnapshots.length - 1] : null;
					
					Thread.sleep(5000);
					randomSuccursale.receiveMoney(
							new Transfer(transferAmount, _uid, receiverID, snapshotToken));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}


	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException
	{
		System.out.println("Veuillez entrer le montant de départ: ");
		Scanner input = new Scanner(System.in);
		int montant = input.nextInt();
		BanqueInterface banque = (BanqueInterface) Naming.lookup("rmi://localhost:2020/Banque");
		Succursale instance = new Succursale(banque, montant); 
		new Thread(instance).start();
		Boolean closing = false; 
		
		while(!closing)
		{
			String command = input.nextLine();
			switch(command)
			{
			case "exit":
				closing = true;
				break;
			case "get snapshot":
				instance.getSnapshot();
				break;
			case "transfer":
				System.out.println("Transferer des fonds vers quelle succursale?");
				int destinationID = input.nextInt();
				System.out.println("Transferer combien d'argent?");
				int money = input.nextInt();
				if(!instance.sendMoney(destinationID, money))
					System.out.println("Une erreur est survenue.");
				else
					System.out.println("Transfer complété");
				break;
			case "erreur":
				System.out.println("Retirer combien d'argent?");
				int moneyLost = input.nextInt();
				instance._montant -= moneyLost;
				System.out.println("Argent retiré.");
			default:
				System.out.println("invalid command");
				break;
			}	
		}
		input.close();
		//In theory, shut down the succursale here, but it's not part of the specs, so yeah. Program stalls. 
	}

	private boolean sendMoney(int destinationID, int money) {
		SuccursaleInterface destination = _succursales.get(destinationID);

		//Check if input is valid
		if (money <= 0 || money > _montant || destination == null)
			return false;
		
		//Send the funds
		try {
			_montant -= money;
			Integer[] activeSnapshots = getSnapshotList();
			int snapshotToken = activeSnapshots != null ? activeSnapshots[activeSnapshots.length - 1] : null;
			destination.receiveMoney(new Transfer(money, _uid, destinationID, snapshotToken));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	private Map<Integer, List<Transfer>> _floatingMessages;
	
	@Override
	public void appendMessage(Transfer t) throws RemoteException {
		_floatingMessages
			.get(t.getSnapshotToken())
			.add(t);
	}

	@Override
	public Transfer sendSnapshotRequest(int snapshotToken, int observerID) throws RemoteException {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(_observers.values().contains(observerID))
			return null; //Obsrver already has an active snapshot. Ignore this request.  
		_observers.put(snapshotToken, observerID);
		
		return new Transfer(_montant, _uid, -1, snapshotToken); 
	}
	
	private void getSnapshot()
	{
		int newsnapshotToken = -1;
		do{
			newsnapshotToken = _random.nextInt(); //Might roll an already active one.  
		}while(_observers.containsKey(newsnapshotToken));
		
		//Prepare list for canal messages
		_floatingMessages.put(newsnapshotToken, new ArrayList<Transfer>()); 
		
		

		try {
			System.out.println("Lancement de requête d'état: " + newsnapshotToken);
			List<Transfer> succursaleValues = new ArrayList<>();
			//Add this succursale's value to the result. 
			succursaleValues.add(sendSnapshotRequest(newsnapshotToken, _uid));
			for(Object key : _succursales.keySet())
			{
				//Send request to all known succursales.
				Transfer otherSuccursale = _succursales.get(key).sendSnapshotRequest(newsnapshotToken, _uid);
				if(otherSuccursale != null)
					succursaleValues.add(otherSuccursale);
			}
			System.out.println("Début de la fermeture de requête d'état: " + newsnapshotToken);
			
			List<Transfer> floatingMessages = new ArrayList<>();
			//Get floating messages at succursales
			for(SuccursaleInterface succ : _succursales.values())
			{
				floatingMessages.addAll(succ.endSnapshotRequest(newsnapshotToken));
			}
			//Get floating message at caller.
			floatingMessages.addAll(endSnapshotRequest(newsnapshotToken));
			
			System.out.println("Requête d'état terminée: " + newsnapshotToken);
			//List succursales
			int snapshotSum =0;
			for(Transfer t : succursaleValues)
			{
				System.out.println("Succursale #" + t.getSenderID() + " : " + t.getMoney() + "$");
				snapshotSum += t.getMoney();
			}
			
			//List floating messages
			for(Transfer t : floatingMessages)
			{
				System.out.println("Canal " + t.getSenderID() + "-" + t.getReceiverID() + ": " + t.getMoney() + "$");
				snapshotSum += t.getMoney();
			}
			
			
			int bankTotal = _banque.getTotal();
			System.out.println("Somme connue par la Banque : " + bankTotal + "$");
			System.out.println("Somme détectée par la Capture : " + snapshotSum + "$");
			
			if(bankTotal == snapshotSum)
				System.out.println("ÉTAT GLOBAL COHÉRENT");
			else
				System.out.println("ÉTAT GLOBAL INCOHÉRENT");
			
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public List<Transfer> endSnapshotRequest(int snapshotToken) throws RemoteException
	{
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!_observers.containsKey(snapshotToken))
			return null;  
		_observers.remove(snapshotToken);
		
		return _floatingMessages.remove(snapshotToken);
		
	}
	
	private List<Transfer> formatMessageList(List<Transfer> origin)
	{
		for(int i = 0; i < origin.size(); i++)
		{
			for(int k = i+1; k < origin.size(); k++)
			{
				if (origin.get(i).add(origin.get(k)))//if this is true, then the transfer should be removed from the list. 
				{
					origin.remove(k--);
					break; 
				}
			}
		}
		return origin;
	}
}



