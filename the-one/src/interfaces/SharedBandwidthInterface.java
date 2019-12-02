/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package interfaces;

import core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A simple Network Interface that provides a variable bit-rate service, where
 * the bit-rate depends on the number of other transmitting connections on this interface.
 * he current transmit speed is updated only if there are ongoing
 * transmissions. The configured transmit speed is the maximum obtainable speed.
 *
 * // TODO: Should optimizer be allowed for this interface?
 *
 * //TODO: We shouldn't extend InterferenceLimitInterface here..?
 */
public class SharedBandwidthInterface extends InterferenceLimitedInterface {

	private static final String UP_TRANSMIT_SPEED_S = "uploadSpeed";
	private static final String DOWN_TRANSMIT_SPEED_S = "downloadSpeed";
	Logger log = LoggerFactory.getLogger(this.getClass());
	int lastSpeedUpdate = 0;

	int uploadSpeed;
	int downloadSpeed;
	int currentDownloadSpeed = 0;
	int currentUploadSpeed = 0;

	// From NetworkInterface.java: (private there)
	private static final int CON_UP = 1;
	private static final int CON_DOWN = 2;
	private List<ConnectionListener> cListeners = null; // list of listeners
	private boolean disableOptimizer = true; //TODO! make it into setting or refactor


	@Override
	public void connect(NetworkInterface anotherInterface) {
		if (isScanning()
				&& !isConnected(anotherInterface)
				&& anotherInterface.getHost().isRadioActive()
				&& isWithinRange(anotherInterface)
				&& (this != anotherInterface)) {
			// new contact within range

			Connection con = new DuplexConnection(this.host, this,
					anotherInterface.getHost(), anotherInterface);
			connect(con, anotherInterface);
		}
	}

	@Override
	public void createConnection(NetworkInterface anotherInterface) {
		if (!isConnected(anotherInterface) && (this != anotherInterface)) {
			// new contact within range

			Connection con = new DuplexConnection(this.host, this,
					anotherInterface.getHost(), anotherInterface);
			connect(con,anotherInterface);
		}
	}

	public SharedBandwidthInterface(Settings s) {
		super(s);

		this.uploadSpeed = s.getInt(UP_TRANSMIT_SPEED_S);
		this.downloadSpeed = s.getInt(DOWN_TRANSMIT_SPEED_S);
		ensurePositiveValue(uploadSpeed, UP_TRANSMIT_SPEED_S);
		ensurePositiveValue(downloadSpeed, DOWN_TRANSMIT_SPEED_S);

		this.transmitRange = Double.MAX_VALUE;
		this.currentTransmitSpeed = 0;
		this.numberOfTransmissions = 0;
	}

	/**
	 * Copy constructor
	 * @param ni the copied network interface object
	 */
	public SharedBandwidthInterface(SharedBandwidthInterface ni) {
		super(ni);
		this.transmitRange = Double.MAX_VALUE;
		this.transmitSpeed = ni.transmitSpeed;

		this.downloadSpeed = ni.downloadSpeed;
		this.uploadSpeed = ni.uploadSpeed;

		this.currentTransmitSpeed = 0;
		this.numberOfTransmissions = 0;

	}


	public NetworkInterface replicate() { return new SharedBandwidthInterface(this); }

	/**
	 * Updates the state of current connections (i.e., tears down connections
	 * that are out of range).
	 */
	public void update() {
		if (optimizer == null) {
			return; /* nothing to do */
		}

		// First break the old ones
		optimizer.updateLocation(this);
		for (int i=0; i<this.connections.size(); ) {
			Connection con = this.connections.get(i);
			NetworkInterface anotherInterface = con.getOtherInterface(this);

			// all connections should be up at this stage
			assert con.isUp() : "Connection " + con + " was down!";

			if (!isWithinRange(anotherInterface)) {
				disconnect(con,anotherInterface);
				connections.remove(i);
			} else {
				i++;
			}
		}

		// Then find new possible connections


		if (disableOptimizer){
			// "Special case where optimizer is not used, makes sense for wired connections "
			for (NetworkInterface otherInterface : optimizer.getAllInterfaces()) {
				if (otherInterface.getInterfaceType().equals(this.interfacetype)){
					connect(otherInterface);
				}
			}
		} else {

			for (NetworkInterface otherInterface : optimizer.getNearInterfaces(this)) {
				if (otherInterface.getInterfaceType().equals(this.interfacetype)){
					connect(otherInterface);
				}
			}
		}

//		int numberOfActive = 1;
		updateTransmitSpeed();


		for (Connection con : getConnections()) {
			con.update();
		}
	}
	/**
	 * Connects this host to another host. The derived class should check
	 * that all pre-requisites for making a connection are satisfied before
	 * actually connecting.
	 * SharedBandwidthInterface does NOT notify connection listeners of subconnection changes!!
	 * @param con The new connection object
	 * @param anotherInterface The interface to connect to
	 */
	@Override
	protected void connect(Connection con, NetworkInterface anotherInterface) {
		this.connections.add(con);

		if (! (con instanceof DuplexVirtualConnection)){
			notifyConnectionListeners(CON_UP, anotherInterface.getHost());
		}

		// set up bidirectional connection
		anotherInterface.getConnections().add(con);

		// inform routers about the connection
		this.host.connectionUp(con);
		anotherInterface.getHost().connectionUp(con);
	}

	/**
	 * Disconnects this host from another host.  The derived class should
	 * make the decision whether to disconnect or not
	 * @param con The connection to tear down
	 */
	protected void disconnect(Connection con,
							  NetworkInterface anotherInterface) {
		con.setUpState(false);

		if (! (con instanceof DuplexVirtualConnection)){
			notifyConnectionListeners(CON_DOWN, anotherInterface.getHost());
		}

		// tear down bidirectional connection
		if (!anotherInterface.getConnections().remove(con)) {
			throw new SimError("No connection " + con + " found in " +
					anotherInterface);
		}

		this.host.connectionDown(con);
		anotherInterface.getHost().connectionDown(con);
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	public String toString() {
		return "SharedBandwidthInterface " + super.toString();
	}

	public void updateTransmitSpeed(){
		updateTransmitSpeed(true);
	}

	/**
	 *
	 * @param propagateUpdate - whether the update is also broadcast to interfaces at the other end of connections
	 */
	private void updateTransmitSpeed(boolean propagateUpdate) {
		int now = SimClock.getIntTime();

		if (lastSpeedUpdate >= now){
			log.debug("\t\t* Skipping transmitSpeed Update of " + this.getHost().getName() + " *");
			return; } // maybe another Interface has already updated for us (see below)
		else { this.lastSpeedUpdate = now;}
		log.debug(" **** Updating Interface of " + this.getHost().getName() + " ****");

		// Find the current number of transmissions (to calculate the current transmission speed
//		numberOfTransmissions = 0;
//		for (Connection con : this.connections) {
//			numberOfTransmissions += ((DuplexConnection)con).getSubConnections().size();
//		}

		int noOfSending = 0;
		int noOfRecving = 0;
		for (Connection connection : host.getConnections()) {
			if (connection instanceof DuplexVirtualConnection){
				if ( connection.isInitiator(host)){
					noOfSending++;
				} else {
					noOfRecving++;
				}
			}
		}
		// TODO 05.09 failing to get right valuesshould maybe hold sendingConnections and recvingConnections in separate collections
		if (noOfRecving < 1) noOfRecving = 1;
		if (noOfSending < 1) noOfSending = 1;

		int ntrans = numberOfTransmissions;
		if ( numberOfTransmissions < 1) ntrans = 1;
//		if ( numberOfActive <2 ) numberOfActive = 2;

		// transmission speed is divided equally to all the ongoing transmissions
		currentTransmitSpeed = (int) Math.floor((double)transmitSpeed / ntrans);
		log.debug("\t\t %{}  [{}] {}  transmissions, speed: {} -------", SimClock.getTime(), host.getName(), ntrans, currentTransmitSpeed);

		currentDownloadSpeed = (int) Math.floor(downloadSpeed / noOfRecving);
		currentUploadSpeed = (int) Math.floor(uploadSpeed / noOfSending);

		// also update speed for interface at other end of the connection
		if (propagateUpdate){
			for (Connection con : this.connections){

				NetworkInterface otherInterface = con.getOtherInterface(this);
				if (otherInterface.getInterfaceType().equals(this.interfacetype)){
					log.debug("\t\t\t\t\t propagating speed update to interface of " + otherInterface.getHost().getName());

					// could also use some flag for this
					((SharedBandwidthInterface) otherInterface).updateTransmitSpeed(false);

				}
			}
		}

	}


	/**
	 * Notifies all the connection listeners about a change in connections.
	 * @param type Type of the change (e.g. {@link #CON_DOWN} )
	 * @param otherHost The other host on the other end of the connection.
	 */
	private void notifyConnectionListeners(int type, DTNHost otherHost) {
		if (this.cListeners == null) {
			return;
		}
		for (ConnectionListener cl : this.cListeners) {
			switch (type) {
				case CON_UP:
					cl.hostsConnected(this.host, otherHost);
					break;
				case CON_DOWN:
					cl.hostsDisconnected(this.host, otherHost);
					break;
				default:
					assert false : type;	// invalid type code
			}
		}
	}


	public int getUpSpeed() {
		return currentUploadSpeed;
	}
	public int getDownSpeed() {
		return currentDownloadSpeed;
	}



}
