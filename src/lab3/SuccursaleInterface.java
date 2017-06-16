package lab3;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface SuccursaleInterface extends Remote {
    void newIdentity(int uid) throws RemoteException;
    int  getIdentity() throws RemoteException;
    void newSuccursale(int uid, SuccursaleInterface succursale) throws RemoteException;
    void sendSuccursalesMap(Map<Integer,SuccursaleInterface> map) throws RemoteException;
    void receiveMoney(int money) throws RemoteException;
}
