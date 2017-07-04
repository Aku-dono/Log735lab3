/******************************************************
Cours : LOG735-E17 Groupe 01
Projet : Laboratoire #3
Étudiants : David Chavigny CHAD01108504
Jonathan St-Cyr STCJ08029302
******************************************************/
package lab3;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BanqueInterface extends Remote {
    void connect(SuccursaleInterface succursale, int montant) throws RemoteException;
    int getTotal() throws RemoteException;
}
