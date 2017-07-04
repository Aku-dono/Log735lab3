/******************************************************
Cours : LOG735-E17 Groupe 01
Projet : Laboratoire #3
Étudiants : David Chavigny CHAD01108504
Jonathan St-Cyr STCJ08029302
******************************************************/

package lab3;

import java.io.Serializable;

public class Transfer implements Serializable {
	private int money;
	private int senderID;
	private int receiverID; 
	private Integer snapshotToken;
	
	public int getMoney() {
		return money;
	}

	public int getSenderID() {
		return senderID;
	}

	public int getReceiverID() {
		return receiverID;
	}

	public Integer getSnapshotToken() {
		return snapshotToken;
	}

	public Transfer(int money, int senderID, int receiverID, Integer snapshotToken)
	{
		this.money = money;
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.snapshotToken = snapshotToken;
	}
	
	///Adds transfers. returns true if t should be removed from the list, meaning their senders and receivers are the same or reversed. 
	///This is because we want "Canal #1 to #3" to be kept, but "Canal #3 to #1" to be removed, and we don't want duplicates
	///this is strictly used to formatting. 
	public boolean add(Transfer t)
	{
		if((t.receiverID == this.receiverID || t.receiverID == this.senderID) && //Accept 1->2, but also 2->1. 
				(t.senderID == this.receiverID || t.senderID == this.senderID))
		{
			//case 3 -> 1 
			if(this.senderID > this.receiverID)
			{
				int backup = senderID;
				senderID = receiverID;
				receiverID = backup;
			}
			this.money += t.money;
			return true;
		}
		return false; //Do not have the same targets. 
	}
}
