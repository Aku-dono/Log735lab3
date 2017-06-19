package lab3;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class Banque extends UnicastRemoteObject implements BanqueInterface {
	private Map<Integer, SuccursaleInterface> _succursales;
	int _total;
	int _nextUid;

	public Banque() throws RemoteException
	{
		_succursales = new HashMap<>();
		_total = 0;
		_nextUid = 1;
	}

	public void connect(SuccursaleInterface succursale, int montant) throws RemoteException
	{
		_total += montant;
		System.out.println("Succursale #" + _nextUid + ": +" + montant + "$");
		System.out.println("Total: " + _total + "$");

		succursale.sendSuccursalesMap(_succursales);

		for(Map.Entry<Integer,SuccursaleInterface> entry :_succursales.entrySet())
			entry.getValue().newSuccursale(_nextUid, succursale);

		_succursales.put(_nextUid, succursale);
		succursale.newIdentity(_nextUid);
		_nextUid++;
	}
	
	public static void main(String[] args) throws RemoteException, MalformedURLException
	{
		LocateRegistry.createRegistry(2020);
		Naming.rebind("rmi://localhost:2020/Banque", new Banque());
	}

	@Override
	public int getTotal() {
		return _total;
	}
}
