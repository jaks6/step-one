/*
 * Copyright 2010 Aalto University, ComNet / 2019 Jakob Mass University of Tartu
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.*;
import util.Tuple;

import java.util.*;

/**
 * Router that will deliver messages only to the final recipient.
 * Unlike DirectDeliveryRouter, multiple transfers on different connections are allowed.

 */
public class DirectMultiTransferRouter extends ActiveRouter {

	@Override
	public void init(DTNHost host, List<MessageListener> mListeners) {
		super.init(host, mListeners);
		this.sendingConnections = new HashSet<>();
		this.lastTtlCheck = 0;

	}


	Set<Connection> sendingConnections;

	private static final int MAX_SENDING_CONNECTIONS = 100;
	/** sim time when the last TTL check was done */
	private double lastTtlCheck;


	public DirectMultiTransferRouter(Settings s) { super(s); }

	protected DirectMultiTransferRouter(DirectMultiTransferRouter r) {
		super(r);
	}
	@Override
	public DirectMultiTransferRouter replicate() {
		return new DirectMultiTransferRouter(this);
	}


	/**
	 * Exchanges deliverable (to final recipient) messages between this host
	 * and all hosts this host is currently connected to. First all messages
	 * from this host are checked and then all other hosts are asked for
	 * messages to this host.
	 * @return A list of connections that started a transfer or null if no transfer
	 * was started
	 */
//
	protected Set<Connection> exchangeMultiDeliverableMessages() {
		List<Connection> connections = getConnections();

		if (connections.size() == 0) {
			return Collections.emptySet();
		}

		@SuppressWarnings(value = "unchecked")
		Set<Connection> ts =
				tryMultiMessagesForConnected(sortByQueueMode(getMessagesForConnected()));

		Set<Connection> transferStartedConnections = new HashSet<>(ts);

		//ask messages from connected
		// TODO: Should we re-add this?
//		for (Connection con : connections) {
//			if (con.getOtherNode(getHost()).requestDeliverableMessages(con)) {
//				ts.add(con);
//			}
//		}
		return transferStartedConnections;
	}


	/**
	 * Tries to send messages for the connections that are mentioned
	 * in the Tuples in the order they are in the list.
	 * @param tuples The tuples to try
	 * @return The list of connections who accepted the message.
	 */

	protected Set<Connection> tryMultiMessagesForConnected(List<Tuple<Message, Connection>> tuples) {

//		ArrayList<Tuple<Message, Connection>> result = new ArrayList<>();
		Set<Connection> result = new HashSet<>();
//		ArrayList<Connection> result = new ArrayList<>();
		if (tuples.size() == 0) {
			return result;
		}
		for (Tuple<Message, Connection> t : tuples) {
			Message m = t.getKey();
			Connection con = t.getValue();
			if (startTransfer(m, con) == RCV_OK) {
				result.add(con);
//				return t;
			}
		}
		return result;

	}

	@Override
	protected int checkReceiving(Message m, DTNHost from) {
//		if (isTransferring()) {
//			return TRY_LATER_BUSY; // only one connection at a time
//		}

		if ( hasMessage(m.getId()) || isDeliveredMessage(m) ||
				super.isBlacklistedMessage(m.getId())) {
			return DENIED_OLD; // already seen this message -> reject it
		}

		if (m.getTtl() <= 0 && m.getTo() != getHost()) {
			/* TTL has expired and this host is not the final recipient */
			return DENIED_TTL;
		}

		if (getEnergy() != null && getEnergy().getEnergy() <= 0) {
			return MessageRouter.DENIED_LOW_RESOURCES;
		}

		if (!getPolicy().acceptReceiving(from, getHost(), m)) {
			return MessageRouter.DENIED_POLICY;
		}

		/* remove oldest messages but not the ones being sent */
		if (!makeRoomForMessage(m.getSize())) {
			return DENIED_NO_SPACE; // couldn't fit into buffer -> reject
		}

		return RCV_OK;
	}


	/**
	 * Adds a connections to sending connections which are monitored in
	 * the update.
	 * @see #update()
	 * @param con The connection to add
	 */
	protected void addToSendingConnections(Connection con) {
		this.sendingConnections.add(con);
	}

	@Override
	public void update() {

		for (Application application : this.getApplications(null)) {
			application.update(this.getHost());
		}

		Iterator<Connection> iter = this.sendingConnections.iterator();
		while ( iter.hasNext() ){
			Connection connection = iter.next();
//		}
//		for (int i = 0; i < this.sendingConnections.size(); ) {
//			final Connection connection = sendingConnections.

			boolean removeCurrent;
			if (connection instanceof DuplexConnection) {
				removeCurrent = updateDuplexConnection((DuplexConnection) connection);
			} else {
				removeCurrent = updateConnection(connection);
			}

			if (removeCurrent) {
				iter.remove();
			}
		}

		/* time to do a TTL check and drop old messages? */
		if (SimClock.getTime() - lastTtlCheck >= TTL_CHECK_INTERVAL ) {
			dropExpiredMessages();
			lastTtlCheck = SimClock.getTime();
		}

		if (getEnergy() != null) {
			/* TODO: add support for other interfaces */
			NetworkInterface iface = getHost().getInterface(1);
			getEnergy().update(iface, getHost().getComBus());
		}


		if (!canStartTransfer()) {
			return; // can't start a new transfer
		}

		// Try only the messages that can be delivered to final recipient
		Set<Connection> newTransferCons = exchangeMultiDeliverableMessages();

	}

	/**
	 * Drops messages whose TTL is less than zero.
	 */
	protected void dropExpiredMessages() {
		Message[] messages = getMessageCollection().toArray(new Message[0]);
		for (int i=0; i<messages.length; i++) {
			int ttl = messages[i].getTtl();
			if (ttl <= 0) {
				// !TODO: check if message not being transferred (?)
				deleteMessage(messages[i].getId(), true);
			}
		}
	}

	/**
	 * Returns true if this router is currently sending a message with
	 * <CODE>msgId</CODE>.
	 *
	 * @param msgId The ID of the message
	 * @return True if the message is being sent false if not
	 */
	@Override
	public boolean isSending(String msgId) {

		for (Connection con : this.sendingConnections) {

			if (con instanceof DuplexConnection) {
				for (DuplexVirtualConnection subConnection : ((DuplexConnection) con).getSubConnections()) {
					if (subConnection.getMessage() == null) {
						continue; // transmission is finalized
					}
					if (subConnection.getMessage().getId().equals(msgId)) {
						return true;
					}
				}
			} else {
				if (con.getMessage() == null) {
					continue; // transmission is finalized
				}
				if (con.getMessage().getId().equals(msgId)) {
					return true;
				}
			}


		}
		return false;
	}

	private boolean updateConnection(Connection con) {
		boolean removeCurrent = false;
		/* finalize ready transfers */
		if (con.isMessageTransferred()) {
			if (con.getMessage() != null) {
				transferDone(con);
				con.finalizeTransfer();
			} /* else: some other entity aborted transfer */
			removeCurrent = true;
		}
		/* remove connections that have gone down */
		else if (!con.isUp()) {
			if (con.getMessage() != null) {
				transferAborted(con);
				con.abortTransfer();
			}
			removeCurrent = true;
		}

		if (removeCurrent) {
			// if the message being sent was holding excess buffer, free it
			if (this.getFreeBufferSize() < 0) {
				this.makeRoomForMessage(0);
			}
//			sendingConnections.remove(i);
		}
		return  removeCurrent;
	}

	private boolean updateDuplexConnection(DuplexConnection connection) {
		boolean allRemoved = true;
		for (Iterator<DuplexVirtualConnection> it = connection.getTransfers(); it.hasNext(); ) {
			DuplexVirtualConnection con = it.next();
			boolean removeSubConnection = updateConnection(con);

			allRemoved = allRemoved && removeSubConnection;
		}
		return allRemoved;

	}


}
