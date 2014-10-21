/**
 * @author andy
 * @since 1.0
 * @version 1.2
 * @date 24-10-2008
 */

package NetworkElements;

import java.util.*;

import DataTypes.*;

public class ATMRouter implements IATMCellConsumer{
	private int address; // The AS address of this router
	private ArrayList<ATMNIC> nics = new ArrayList<ATMNIC>(); // all of the nics in this router
	private TreeMap<Integer, ATMNIC> nextHop = new TreeMap<Integer, ATMNIC>(); // a map of which interface to use to get to a given router on the network
	private TreeMap<Integer, NICVCPair> VCtoVC = new TreeMap<Integer, NICVCPair>(); // a map of input VC to output nic and new VC number
	private boolean trace=true; // should we print out debug code?
	private int traceID = (int) (Math.random() * 100000); // create a random trace id for cells
	private ATMNIC currentConnAttemptNIC = null; // The nic that is currently trying to setup a connection
	private boolean displayCommands = true; // should we output the commands that are received?
	
	/**
	 * The default constructor for an ATM router
	 * @param address the address of the router
	 * @since 1.0
	 */
	public ATMRouter(int address){
		this.address = address;
	}
	
	/**
	 * Adds a nic to this router
	 * @param nic the nic to be added
	 * @since 1.0
	 */
	public void addNIC(ATMNIC nic){
		this.nics.add(nic);
	}
	
	/**
	 * This method processes data and OAM cells that arrive from any nic in the router
	 * @param cell the cell that arrived at this router
	 * @param nic the nic that the cell arrived on
	 * @since 1.0
	 */
	public void receiveCell(ATMCell cell, ATMNIC nic){
		//if(trace)
			//System.out.println("Trace (ATMRouter): Received a cell " + cell.getTraceID());
		
		if(cell.getIsOAM()){
			// What's OAM for?
			int toAddress = this.getIntFromEndOfString(cell.getData());
			
			if (cell.getData().startsWith("setup ")) {		
				
				if (this.address == toAddress) {	// dest address match
					this.receivedSetup(cell);
					//System.out.println("Address matched");
					ATMCell call = new ATMCell(0, "callpro " + toAddress, this.getTraceID());
					call.setIsOAM(true);
					this.sentCallProceeding(call);
					nic.sendCell(call, this);	
					
					// send connect
					int thisVC;
					if (!this.VCtoVC.isEmpty()) {
						thisVC = this.VCtoVC.lastKey() + 1;
					}
					else {
						thisVC = 1;
					}
					//thisVC = (cell.getVC() > thisVC) ? cell.getVC() : thisVC;
					if (trace) {
						System.out.println("Trace (ATMRouter): First free VC = " + thisVC);
					}
					this.VCtoVC.put(thisVC, new NICVCPair(nic, thisVC));
					
					ATMCell conn = new ATMCell(0, "connect " + thisVC, this.getTraceID());
					conn.setIsOAM(true);
					this.sentConnect(conn);
					nic.sendCell(conn, this);					
				}
				
				else {	// not dest address
					if (this.nics.size() <= 1) {	// invalid end point
						System.out.println("Nowhere to forward");
						return;
					}
					else {
						if (currentConnAttemptNIC != null //&& 
								/*!this.currentConnAttemptNIC.equals(nic)*/) {	// is already trying to setup
							ATMCell wait = new ATMCell(0, "wait " + toAddress, this.getTraceID());
							wait.setIsOAM(true);
							this.sentWait(wait);
							nic.sendCell(wait, this);
							return;
						}
						
						currentConnAttemptNIC = nic;
						
						// send call proceeding
						this.receivedSetup(cell);
						ATMCell call = new ATMCell(0, "callpro " + toAddress, this.getTraceID());
						call.setIsOAM(true);
						this.sentCallProceeding(call);
						nic.sendCell(call, this);
						
						// forward setup
						/*if (!this.VCtoVC.isEmpty()) {
							cell.setVC(this.VCtoVC.lastKey() + 1);
						}*/
						
						if (this.nextHop.containsKey(toAddress)) {
							ATMNIC nicSent = this.nextHop.get(toAddress);
							this.sentSetup(cell);
							nicSent.sendCell(cell, this);
							//this.currentConnAttemptNIC = nicSent;
						}
						else {
							for (ATMNIC nicSent : this.nics) {
								if (!nicSent.equals(nic)) {
									nicSent.sendCell(cell, this);
									//this.currentConnAttemptNIC = nicSent;
								}
							}
						}
					}
				}
			}
			
			else if (cell.getData().startsWith("wait ")) {
				//System.out.println ("not setup");
				this.receivedWait(cell);
				ATMCell resent = new ATMCell(0, "setup " + toAddress, this.getTraceID());
				resent.setIsOAM(true);
				this.sentSetup(resent);
				nic.sendCell(resent, this);
			}
			else if (cell.getData().startsWith("callpro ")) {
				this.receivedCallProceeding(cell);
			}
			else if (cell.getData().startsWith("connect ")) {
				int inVC = this.getIntFromEndOfString(cell.getData());
				int outVC = 1;
				this.receivedConnect(cell);
				
				if (!this.VCtoVC.isEmpty()) {
					//outVC = (this.VCtoVC.lastKey() + 1 < inVC) ? 
							//(this.VCtoVC.lastKey() + 1) : inVC;
					//outVC = this.VCtoVC.lastKey() + 1;
					outVC = this.VCtoVC.lastKey() + 1;
					for (int i = 0; i < this.VCtoVC.lastKey(); i ++) {
						if (!this.VCtoVC.containsKey(i)) {
							outVC = i;
							break;
						}
					}
				}
				
				// forward connect
				if (this.currentConnAttemptNIC != null) {
					ATMCell conn = new ATMCell(0, "connect " + outVC, this.getTraceID());
					conn.setIsOAM(true);					
					this.sentConnect(conn);
					this.currentConnAttemptNIC.sendCell(conn, this);
					this.VCtoVC.put(outVC, new NICVCPair(nic, inVC));
					//this.VCtoVC.put(inVC, new NICVCPair(this.currentConnAttemptNIC, outVC));
					this.currentConnAttemptNIC = null;
				}
				else {
					System.out.println("Error: currentConnAttemptNIC is null.");
					return;
				}
				
				// send connect ack
				ATMCell ack = new ATMCell(0, "connack " + inVC, this.getTraceID());
				ack.setIsOAM(true);
				this.sentConnectAck(ack);
				nic.sendCell(ack, this);

			}
			else if (cell.getData().startsWith("connack ")) {
				this.receiveConnectAck(cell);
			}
			else if (cell.getData().startsWith("end ")) {
				int endVC = this.getIntFromEndOfString(cell.getData());
				ATMCell endack = new ATMCell(0, "endack " + endVC, this.getTraceID());
				endack.setIsOAM(true);
				this.sentEndAck(endack);
				nic.sendCell(endack, this);
							
				if (this.VCtoVC.get(endVC) == null) {
					if (trace)
						System.out.println("Trace (ATMRouter): Router " + this.address + " connection ended.");
						return;
				}
				
				int nextVC = this.VCtoVC.get(endVC).getVC();
				ATMCell end = new ATMCell(0, "end " + nextVC, cell.getTraceID());
				end.setIsOAM(true);
				this.VCtoVC.get(endVC).getNIC().sendCell(end, this);
				this.sentEnd(end);
				
				if (trace) {
					System.out.println("Trace (ATMRouter): Router " + this.address + " remove " + endVC + " " + nextVC);
				}
				
				this.VCtoVC.remove(nextVC);
				
			}
			else if (cell.getData().startsWith("endack ")) {
				this.receivedEndAck(cell);				
			}
			else {
				System.out.println("Error: Message not implemented.");
			}
			
		}
		
		else {
			// find the nic and new VC number to forward the cell on
			// otherwise the cell has nowhere to go. output to the console and drop the cell
			if (this.VCtoVC.isEmpty()) {
				System.out.println("Error: vc lookup table is empty.");
				return;
			}
			if (!this.VCtoVC.containsKey(cell.getVC())) {
				this.cellNoVC(cell);
				return;
			}
			int outVC = this.VCtoVC.get(cell.getVC()).getVC();
			ATMNIC outNIC = this.VCtoVC.get(cell.getVC()).getNIC();
			if (outNIC != nic) {
				cell.setVC(outVC);
				outNIC.sendCell(cell, this);
			}
			else {
				this.cellDeadEnd(cell);
			}
			
		}	
	}
	
	/**
	 * Gets the number from the end of a string
	 * @param string the sting to try and get a number from
	 * @return the number from the end of the string, or -1 if the end of the string is not a number
	 * @since 1.0
	 */
	private int getIntFromEndOfString(String string){
		// Try getting the number from the end of the string
		try{
			String num = string.split(" ")[string.split(" ").length-1];
			return Integer.parseInt(num);
		}
		// Couldn't do it, so return -1
		catch(Exception e){
			if(trace)
				System.out.println("Could not get int from end of string");
			return -1;
		}
	}
	
	/**
	 * This method returns a sequentially increasing random trace ID, so that we can
	 * differentiate cells in the network
	 * @return the trace id for the next cell
	 * @since 1.0
	 */
	public int getTraceID(){
		int ret = this.traceID;
		this.traceID++;
		return ret;
	}
	
	/**
	 * Tells the router the nic to use to get towards a given router on the network
	 * @param destAddress the destination address of the ATM router
	 * @param outInterface the interface to use to connect to that router
	 * @since 1.0
	 */
	public void addNextHopInterface(int destAddress, ATMNIC outInterface){
		this.nextHop.put(destAddress, outInterface);
	}
	
	/**
	 * Makes each nic move its cells from the output buffer across the link to the next router's nic
	 * @since 1.0
	 */
	public void clearOutputBuffers(){
		for(int i=0; i<this.nics.size(); i++)
			this.nics.get(i).clearOutputBuffers();
	}
	
	/**
	 * Makes each nic move all of its cells from the input buffer to the output buffer
	 * @since 1.0
	 */
	public void clearInputBuffers(){
		for(int i=0; i<this.nics.size(); i++)
			this.nics.get(i).clearInputBuffers();
	}
	
	/**
	 * Sets the nics in the router to use tail drop as their drop mechanism
	 * @since 1.0
	 */
	public void useTailDrop(){
		for(int i=0; i<this.nics.size(); i++)
			nics.get(i).setIsTailDrop();
	}
	
	/**
	 * Sets the nics in the router to use RED as their drop mechanism
	 * @since 1.0
	 */
	public void useRED(){
		for(int i=0; i<this.nics.size(); i++)
			nics.get(i).setIsRED();
	}
	
	/**
	 * Sets the nics in the router to use PPD as their drop mechanism
	 * @since 1.0
	 */
	public void usePPD(){
		for(int i=0; i<this.nics.size(); i++)
			nics.get(i).setIsPPD();
	}
	
	/**
	 * Sets the nics in the router to use EPD as their drop mechanism
	 * @since 1.0
	 */
	public void useEPD(){
		for(int i=0; i<this.nics.size(); i++)
			nics.get(i).setIsEPD();
	}
	
	/**
	 * Sets if the commands should be displayed from the router in the console
	 * @param displayComments should the commands be displayed or not?
	 * @since 1.0
	 */
	public void displayCommands(boolean displayCommands){
		this.displayCommands = displayCommands;
	}
	
	/**
	 * Outputs to the console that a cell has been dropped because it reached its destination
	 * @since 1.0
	 */
	public void cellDeadEnd(ATMCell cell){
		if(this.displayCommands)
		System.out.println("The cell is destined for this router (" + this.address + "), taken off network " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a cell has been dropped as no such VC exists
	 * @since 1.0
	 */
	public void cellNoVC(ATMCell cell){
		if(this.displayCommands)
		System.out.println("The cell is trying to be sent on an incorrect VC " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a connect message has been sent
	 * @since 1.0
	 */
	private void sentSetup(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND SETUP: Router " +this.address+ " sent a setup " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a setup message has been sent
	 * @since 1.0
	 */
	private void receivedSetup(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC SETUP: Router " +this.address+ " received a setup message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a call proceeding message has been received
	 * @since 1.0
	 */
	private void receivedCallProceeding(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC CALLPRO: Router " +this.address+ " received a call proceeding message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a connect message has been sent
	 * @since 1.0
	 */
	private void sentConnect(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND CONN: Router " +this.address+ " sent a connect message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a connect message has been received
	 * @since 1.0
	 */
	private void receivedConnect(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC CONN: Router " +this.address+ " received a connect message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a connect ack message has been sent
	 * @since 1.0
	 * @version 1.2
	 */
	private void sentConnectAck(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND CALLACK: Router " +this.address+ " sent a connect ack message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a connect ack message has been received
	 * @since 1.0
	 */
	private void receiveConnectAck(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC CALLACK: Router " +this.address+ " received a connect ack message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that an call proceeding message has been received
	 * @since 1.0
	 */
	private void sentCallProceeding(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND CALLPRO: Router " +this.address+ " sent a call proceeding message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that an end message has been sent
	 * @since 1.0
	 */
	private void sentEnd(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND ENDACK: Router " +this.address+ " sent an end message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that an end message has been received
	 * @since 1.0
	 */
	private void recieveEnd(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC ENDACK: Router " +this.address+ " received an end message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that an end ack message has been received
	 * @since 1.0
	 */
	private void receivedEndAck(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC ENDACK: Router " +this.address+ " received an end ack message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that an end ack message has been sent
	 * @since 1.0
	 */
	private void sentEndAck(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND ENDACK: Router " +this.address+ " sent an end ack message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a wait message has been sent
	 * @since 1.0
	 */
	private void sentWait(ATMCell cell){
		if(this.displayCommands)
		System.out.println("SND WAIT: Router " +this.address+ " sent a wait message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a wait message has been received
	 * @since 1.0
	 */
	private void receivedWait(ATMCell cell){
		if(this.displayCommands)
		System.out.println("REC WAIT: Router " +this.address+ " received a wait message " + cell.getTraceID());
	}
}
