/******************************************************
Cours : LOG735-E17 Groupe 01
Projet : Laboratoire #3
Étudiants : David Chavigny CHAD01108504
Jonathan St-Cyr STCJ08029302
******************************************************/

package lab3;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface SuccursaleInterface extends Remote {
    void newIdentity(int uid) throws RemoteException;
    int  getIdentity() throws RemoteException;
    void newSuccursale(int uid, SuccursaleInterface succursale) throws RemoteException;
    void sendSuccursalesMap(Map<Integer,SuccursaleInterface> map) throws RemoteException;
    List<Transfer> endSnapshotRequest(int snapshotToken) throws RemoteException;
    void receiveMoney(Transfer trans) throws RemoteException;
    //void appendMessage(Transfer trans) throws RemoteException;
    Transfer sendSnapshotRequest(int snapshotToken, int observerID) throws RemoteException;
}
