/**
 * @author andy
 * @version 1.2
 * @date 24-10-2008
 * @since 1.0
 */

package NetworkElements;

import DataTypes.*;

public class Computer implements IATMCellConsumer{
	private ATMNIC nic=null;
	private Boolean trace=true;
	private int traceID = (int) (Math.random() * 100000);
	private int vcNumber=-1;
	private String address="";
	
	/**
	 * The default constructor for a computer
	 * @since 1.0
	 * @deprecated in v1.2
	 */
	public Computer(){}
	
	/**
	 * The default constructor for a computer
	 * @since 1.2
	 */
	public Computer(String address){
		this.address = address;
	}
	
	/**
	 * This method processes cells that are received from the nic
	 * @param cell the cell that was received
	 * @param nic the nic the cell was received on
	 * @since 1.0
	 */
	public void receiveCell(ATMCell cell, ATMNIC nic){
		if(cell.getIsOAM()){
			
		}
		else{
			
		}
	}

	/**
	 * This method sends an ATM cell containing 'setup' and an address to the router
	 * @param toAddress the address we want to connect to
	 * @since 1.0
	 */
	public void setupConnection(int toAddress){
		// Create the ATM cell to send
		ATMCell conn = new ATMCell(0, "setup " + toAddress, this.getTraceID());
		conn.setIsOAM(true);	
		
		// Output to the console
		this.sentSetup(conn);
		
		// send the cell
		this.nic.sendCell(conn , this);
	}
	
	/**
	 * This method sends an ATM cell saying that we want to end the VC that we currently have open
	 * Currently each computer can only have one VC, but the routers can have many VC open
	 * @since 1.0
	 */
	public void endConnection(){
		// Create the ATM cell to send
		ATMCell end = new ATMCell(this.vcNumber, "end " + this.vcNumber, this.getTraceID());
		end.setIsOAM(true);
		
		// Output to the console
		this.sentEnd(end);
		
		// Send the cell
		this.nic.sendCell(end, this);
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
	 * Sends a packet from this computer to another ip address (not really since there aren't any place to send...)
	 * @param dest the destination address of the packet
	 * @param size the size of the packet
	 * @since 1.0
	 * @version 1.2
	 */
	public void sendPacket(int size){
		// Print error messages if needed
		if(this.nic==null)
			System.err.println("The computer you are sending from does not have a NIC!");
		if(trace)
			System.out.println("(Computer) Trace: sending packet from computer");	
		
		int bitsRemaining = size;
		boolean firstCell = true;
		
		while(bitsRemaining > 0){
			if(trace)
				System.out.println("(Computer) Trace: sending a cell for the part of the packet " + traceID);
			
			// If it's the first cell we'll send the IP header info
			if(firstCell){
				nic.sendCell(new ATMCell(this.vcNumber, new IPPacket("10.0.0.1", "10.0.0.2", size), this.getTraceID()), this);
				firstCell=false;
			}
			// otherwise we'll just send some fake data that would be in the packet
			else{
				// Make some dummy packet data
				String data = "";
				for(int i=0; (i <= 48*8) && (i <=bitsRemaining); i++)
					data += "d";
				
				nic.sendCell(new ATMCell(this.vcNumber, data, this.getTraceID()), this);
			}
			
			// 48*8 bits of data sent in an ATM cell payload
			bitsRemaining -= 48*8;
		}
	}
	
	/**
	 * adds a nic to this computer
	 * @param nic the nic to be added (only one nic per computer)
	 * @since 1.0
	 */
	public void addNIC(ATMNIC nic){
		this.nic = nic;
	}
	
	/**
	 * Moves the cells from the nics output buffer across the link to the routers nic
	 * @since 1.0
	 */
	public void clearOutputBuffers(){
			this.nic.clearOutputBuffers();
	}
	
	/**
	 * Moves the cells from the nics input buffer to the output buffer
	 * @since 1.0
	 */
	public void clearInputBuffers(){
			this.nic.clearInputBuffers();
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
	 * Sets the nic in the computer to use tail drop as its drop mechanism
	 * @since 1.0
	 */
	public void useTailDrop(){
		this.nic.setIsTailDrop();
	}
	
	/**
	 * Sets the nic in the computer to use RED as its drop mechanism
	 * @since 1.0
	 */
	public void useRED(){
		this.nic.setIsRED();
	}
	
	/**
	 * Sets the nic in the computer to use PPD as its drop mechanism
	 * @since 1.0
	 */
	public void usePPD(){
		this.nic.setIsPPD();
	}
	
	/**
	 * Sets the nic in the computer to use EPD as its drop mechanism
	 * @since 1.0
	 */
	public void useEPD(){
		this.nic.setIsEPD();
	}
	
	/**
	 * Outputs to the console that a connect message has been sent
	 * @since 1.0
	 * @version 1.2
	 */
	private void sentSetup(ATMCell cell){
		System.out.println("SND SETUP: Computer "+address+" sent a connect setup " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a call proceeding message has been received
	 * @since 1.0
	 * @version 1.2
	 */
	private void receivedCallProceeding(ATMCell cell){
		System.out.println("REC CALLPRO: Computer "+address+" received a call proceeding message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a connect message has been received
	 * @since 1.0
	 * @version 1.2
	 */
	private void receivedConnect(ATMCell cell){
		System.out.println("REC CONN: Computer "+address+" received a connect message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that an end message has been received
	 * @since 1.0
	 * @version 1.2
	 */
	private void sentEnd(ATMCell cell){
		System.out.println("SND END: Computer "+address+" sent an end message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that an end message has been received
	 * @since 1.0
	 * @version 1.2
	 */
	private void receivedEndAck(ATMCell cell){
		System.out.println("REC ENDACK: Computer "+address+" received an end ack message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a wait message has been received
	 * @since 1.0
	 * @version 1.2
	 */
	private void receivedWait(ATMCell cell){
		System.out.println("REC WAIT: Computer "+address+" received a wait message " + cell.getTraceID());
	}
	
	/**
	 * Outputs to the console that a connect ack message has been sent
	 * @since 1.2
	 */
	private void sentConnectAck(ATMCell cell){
		System.out.println("SND CALLACK: Computer "+address+" sent a connect ack message " + cell.getTraceID());
	}
}
