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
	
	///Adds transfers. returns true if t should be removed from the list (its sender is less than this one's sender, or if they have the same sender.
	///This is because we want "Canal #1 to #3" to be kept, but "Canal #3 to #1" to be removed, and we don't want duplicates
	public boolean add(Transfer t)
	{
		if((t.receiverID == this.receiverID || t.receiverID == this.senderID) && //Accept 1->2, but also 2->1. 
				(t.senderID == this.receiverID || t.senderID == this.senderID))
		{
			this.money += t.money;
			return t.senderID > t.receiverID || this.senderID == t.senderID;
		}
		return false; //Do not have the same targets. 
	}
}
