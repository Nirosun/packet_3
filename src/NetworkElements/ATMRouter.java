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
	private boolean trace=false; // should we print out debug code?
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
		if(trace)
			System.out.println("Trace (ATMRouter): Received a cell " + cell.getTraceID());
		
		if(cell.getIsOAM()){
			// What's OAM for?
		}
		else{
			// find the nic and new VC number to forward the cell on
			// otherwise the cell has nowhere to go. output to the console and drop the cell
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
