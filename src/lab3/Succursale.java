package lab3;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Succursale extends UnicastRemoteObject implements SuccursaleInterface, Runnable {
	private int _montant;
    private int _uid;
    private Map<Integer, SuccursaleInterface> _succursales;
    private BanqueInterface _banque;
    private Random _random;

    protected Succursale(BanqueInterface banque, int montant) throws RemoteException
    {
        _random = new Random();
        _succursales = new HashMap<>();
        _banque = banque;
        _montant = montant;
        _banque.connect(this, _montant);
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

        printSuccursales();
    }

    public void receiveMoney(int money) throws RemoteException
    {
        _montant += money;
        System.out.println("Reçu " + money + "$");
        System.out.println(this);
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
                    int succursaleIndex = _random.nextInt(_succursales.size());
                    SuccursaleInterface randomSuccursale = ((SuccursaleInterface)_succursales.values().toArray()[succursaleIndex]);

                    System.out.println("Transfère " + transferAmount + "$ à la succursale #" + randomSuccursale.getIdentity());
                    System.out.println(this);

                    //TRANSFERT-03: Sleep 5 seconds for latency
                    Thread.sleep(5000);
                    randomSuccursale.receiveMoney(transferAmount);
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
        new Thread(new Succursale(banque, montant)).start();
	}
}



