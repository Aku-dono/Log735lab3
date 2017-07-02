package lab3;

import java.io.Serializable;

public class Transfer implements Serializable {
	private int money;
	private int senderID;
	private int receiverID; 
	private int snapshotToken;
	
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

	public Transfer(int money, int senderID, int receiverID, int snapshotToken)
	{
		this.money = money;
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.snapshotToken = snapshotToken;
	}
}
