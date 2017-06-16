package lab3;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BanqueInterface extends Remote {
    void connect(SuccursaleInterface succursale, int montant) throws RemoteException;
}
