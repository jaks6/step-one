/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package core;

import routing.MessageRouter;

import java.util.HashMap;

/**
 * A connection between two DTN nodes.  The transmission speed
 * is updated every round from the end point transmission speeds
 */
public class SharedBandwidthConnection extends VBRConnection {


	HashMap<Integer, FlyMessage> msgsOnFly;

	private int msgsize;
	private int msgsent;
	private int currentspeed = 0;
	private double lastUpdate = 0;


	/**
	 * Creates a new connection between nodes and sets the connection
	 * state to "up".
	 * @param fromNode The node that initiated the connection
	 * @param fromInterface The interface that initiated the connection
	 * @param toNode The node in the other side of the connection
	 * @param toInterface The interface in the other side of the connection
	 */
   public SharedBandwidthConnection(DTNHost fromNode, NetworkInterface fromInterface,
                                    DTNHost toNode, NetworkInterface toInterface) {
	    super(fromNode, fromInterface, toNode, toInterface);
	    msgsOnFly = new HashMap<>();
		this.msgsent = 0;
	}


	public int startTransfer(DTNHost from, Message m) {

		this.msgFromNode = from;
		Message newMessage = m.replicate();
		int retVal = getOtherNode(from).receiveMessage(newMessage, from);

		if (retVal == MessageRouter.RCV_OK) {
			FlyMessage flyMessage = new FlyMessage(newMessage, from);

			this.msgsOnFly.put(newMessage.getUniqueId(), flyMessage );
//			this.msgsize = m.getSize();
//			this.msgsent = 0;
		}

		return retVal;
	}

	@Override
	public boolean isMessageTransferred() {
		return super.isMessageTransferred();
	}

	@Override
	public boolean isTransferring() {
		return !this.msgsOnFly.isEmpty();
	}

	public HashMap<Integer, FlyMessage> getMessagesOnFly(){
   		return msgsOnFly;
	}

	@Override
	public void finalizeTransfer() {
		assert !this.msgsOnFly.isEmpty()  : "Nothing to finalize in " + this;
		assert msgFromNode != null : "msgFromNode is not set";

		this.bytesTransferred += msgOnFly.getSize();

		getOtherNode(msgFromNode).messageTransferred(this.msgOnFly.getId(),
				msgFromNode);
		clearMsgOnFly();
	}


	@Override
	public void abortTransfer() {
//		super.abortTransfer();
	}
	public void abortTransfer(FlyMessage flyMessage) {
		int bytesLeft = flyMessage.msgsize - flyMessage.msgsent;
		bytesLeft = (bytesLeft > 0 ? bytesLeft : 0);
//		int bytesRemaining = getRemainingByteCount();

		bytesTransferred += flyMessage.msgsize - bytesLeft;

		getOtherNode(msgFromNode).messageAborted(this.msgOnFly.getId(),
				msgFromNode, bytesLeft);
		clearMsgOnFly();
	}


	@Override
	public int getTotalBytesTransferred() {
		// TODO
		return -1;
	}

	@Override
	public int getRemainingByteCount() {
		return super.getRemainingByteCount();
	}





	public void finalizeTransfer(FlyMessage flyMsg) {
		assert !this.msgsOnFly.isEmpty()  : "Nothing to finalize in " + this;
		assert flyMsg.msgFromNode != null : "msgFromNode is not set";

		this.bytesTransferred += flyMsg.msgsize;

		getOtherNode(flyMsg.msgFromNode).messageTransferred(flyMsg.message.getId(),
				flyMsg.msgFromNode);
		clearMsgOnFly(flyMsg);


	}

	/**
	 * Calculate the current transmission speed from the information
	 * given by the interfaces, and calculate the missing data amount.
	 *
	 */
	public void update() {

		//TODO: find out how much bandwidth is left for this connecion.. or use round robin scheme?
		currentspeed =  this.fromInterface.getTransmitSpeed(toInterface);
		int othspeed =  this.toInterface.getTransmitSpeed(fromInterface);
		double now = core.SimClock.getTime();

		if (othspeed < currentspeed) {
			currentspeed = othspeed;
		}

		for (FlyMessage flyMessage : msgsOnFly.values()) {
			flyMessage.msgsent += currentspeed * (now - this.lastUpdate);
		}


		this.lastUpdate = now;
	}

	public boolean isMessageTransferred(FlyMessage message) {
		if (message.msgsent >= message.msgsize) {
			return true;
		} else {
			return false;
		}
	}



	@Override
	protected void clearMsgOnFly() {//
	}

	private void clearMsgOnFly(FlyMessage flyMsg) {
		msgsOnFly.remove(flyMsg.message.getUniqueId());
	}


	public class FlyMessage {

		final DTNHost msgFromNode;
		Message message;

		public FlyMessage(Message newMessage, DTNHost msgFromNode) {
			this.msgFromNode = msgFromNode;
			this.message = newMessage;
			this.msgsize = newMessage.getSize();
			this.msgsent = 0;
		}

		public Message getMessage() {
			return message;
		}

		public void setMessage(Message message) {
			this.message = message;
		}


		public int getMsgsent() {
			return msgsent;
		}

		public void setMsgsent(int msgsent) {
			this.msgsent = msgsent;
		}

		public int getCurrentspeed() {
			return currentspeed;
		}

		public void setCurrentspeed(int currentspeed) {
			this.currentspeed = currentspeed;
		}

		public double getLastUpdate() {
			return lastUpdate;
		}

		public void setLastUpdate(double lastUpdate) {
			this.lastUpdate = lastUpdate;
		}

		private int msgsize;
		private int msgsent;
		private int bytestransferred;
		private int currentspeed = 0;
		private double lastUpdate = 0;

	}


}
