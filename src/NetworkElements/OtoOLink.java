/**
 * @author andy
 * @version 1.0
 * @date 24-10-2008
 * @since 1.0
 */

package NetworkElements;

import DataTypes.*;

public class OtoOLink {
	private ATMNIC r1NIC=null, r2NIC=null;
	private Boolean trace=false;
	
	/**
	 * The default constructor for a OtoOLink
	 * @param computerNIC
	 * @param routerNIC
	 * @since 1.0
	 */
	public OtoOLink(ATMNIC r1NIC, ATMNIC r2NIC){	
		this.r1NIC = r1NIC;
		this.r1NIC.connectOtoOLink(this);
		this.r2NIC = r2NIC;
		this.r2NIC.connectOtoOLink(this);
		
		if(this.trace){
			if(r1NIC==null)
				System.err.println("Error (OtoOLink): R1 nic is null");
			if(r1NIC==null)
				System.err.println("Error (OtoOLink): R2 nic is null");
		}
	}
	
	/**
	 * Sends a cell from one end of the link to the other
	 * @param cell the cell to be sent
	 * @param nic the nic the cell is being sent from
	 * @since 1.0
	 */
	public void sendCell(ATMCell cell, ATMNIC nic){
		if(this.r1NIC.equals(nic)){
			if(this.trace)
				System.out.println("(OtoOLink) Trace: sending cell from router A to router B");
			
			this.r2NIC.receiveCell(cell);
		}
		else if(this.r2NIC.equals(nic)){
			if(this.trace)
				System.out.println("(OtoOLink) Trace: sending cell from router B to router A");
			
			this.r1NIC.receiveCell(cell);
		}
		else
			System.err.println("(OtoOLink) Error: You are trying to send a cell down a link that you are not connected to");
	}
}
