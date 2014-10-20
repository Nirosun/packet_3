/**
 * @author andy
 * @version 1.0
 * @date 24-10-2008
 * @since 1.0
 */

package NetworkElements;

import DataTypes.*;
import java.util.*;

public class ATMNIC {
	private IATMCellConsumer parent; // The router or computer that this nic is in
	private OtoOLink link; // The link connected to this nic
	private boolean trace = false; // should we print out debug statements?
	private ArrayList<ATMCell> inputBuffer = new ArrayList<ATMCell>(); // Where cells are put between the parent and nic
	private ArrayList<ATMCell> outputBuffer = new ArrayList<ATMCell>(); // Where cells are put to be outputted
	private boolean tail=true, red=false, ppd=false, epd=false; // set what type of drop mechanism
	private int maximumBufferCells = 20; // the maximum number of cells in the output buffer
	private int startDropAt = 10; // the minimum number of cells in the output buffer before we start dropping cells
	private boolean dropSamePacketCell = false;
	
	/**
	 * Default constructor for an ATM NIC
	 * @param parent
	 * @since 1.0
	 */
	public ATMNIC(IATMCellConsumer parent){
		this.parent = parent;
		this.parent.addNIC(this);
	}
	
	/**
	 * This method is called when a cell is passed to this nic to be sent. The cell is placed
	 * in an output buffer until a time unit passes
	 * @param cell the cell to be sent (placed in the buffer)
	 * @param parent the router the cell came from
	 * @since 1.0
	 */
	public void sendCell(ATMCell cell, IATMCellConsumer parent){
		if(this.trace){
			System.out.println("Trace (ATM NIC): Received cell");
			if(this.link==null)
				System.out.println("Error (ATM NIC): You are trying to send a cell through a nic not connected to anything");
			if(this.parent!=parent)
				System.out.println("Error (ATM NIC): You are sending data through a nic that this router is not connected to");
			if(cell==null)
				System.out.println("Warning (ATM NIC): You are sending a null cell");
		}
		
		
		if(this.tail) this.runTailDrop(cell);
		else if(this.red) this.runRED(cell);
		else if(this.ppd) this.runPPD(cell);
		else if(this.epd) this.runEPD(cell);
	}
	
	/**
	 * Runs tail drop on the cell
	 * @param cell the cell to be added/dropped
	 * @since 1.0
	 */
	private void runTailDrop(ATMCell cell){
		boolean cellDropped = false;
		
		if (outputBuffer.size() < this.maximumBufferCells) {
			outputBuffer.add(cell);
		}
		else {
			cellDropped = true;
		}
		
		// Output to the console what happened
		if(cellDropped)
			System.out.println("The cell " + cell.getTraceID() + " was tail dropped");
		else
			if(this.trace)
			System.out.println("The cell " + cell.getTraceID() + " was added to the output queue");
	}
	
	/**
	 * Runs Random early detection on the cell
	 * @param cell the cell to be added/dropped from the queue
	 * @since 1.0
	 */
	private void runRED(ATMCell cell){
		boolean cellDropped = false;
		double dropProbability = 0.0;
		
		if (outputBuffer.size() > this.startDropAt) {
			if (outputBuffer.size() < this.maximumBufferCells) {
				dropProbability = (outputBuffer.size() - this.startDropAt) 
						/ (double)(this.maximumBufferCells - this.startDropAt);
			}
			else {
				dropProbability = 1;
			}
		}
		
		if (dropProbability > 0) {
			double r = Math.random() * 1.0 / dropProbability;
			if (r <= 1.0) {
				cellDropped = true;
			}
		}
		
		// Output to the console what happened
		if(cellDropped)
			System.out.println("The cell " + cell.getTraceID() + " was dropped with probability " + dropProbability);
		else {
			outputBuffer.add(cell);
			if(this.trace)
				System.out.println("The cell " + cell.getTraceID() + " was added to the output queue");
		}
	}
	
	/**
	 * Runs Partial packet drop on the cell
	 * @param cell the cell to be added/dropped from the queue
	 * @since 1.0
	 */
	private void runPPD(ATMCell cell){
		boolean cellDropped = false;
		double dropProbability = 0.0;
		
		if (cell.getData().equals("")) {
			this.dropSamePacketCell = false;
		}
		
		if (this.dropSamePacketCell == false) {
			if (outputBuffer.size() > this.startDropAt) {
				if (outputBuffer.size() < this.maximumBufferCells) {
					dropProbability = (outputBuffer.size() - this.startDropAt) 
							/ (double)(this.maximumBufferCells - this.startDropAt);
				}
				else {
					dropProbability = 1;
				}
			}			
			if (dropProbability > 0) {
				double r = Math.random() * 1.0 / dropProbability;
				if (r <= 1.0) {
					cellDropped = true;
					this.dropSamePacketCell = true;
				}
			}
		}
		else {
			cellDropped = true;
		}		
		
		// Output to the console what happened
		if(cellDropped)
			System.out.println("The cell " + cell.getTraceID() + " was dropped");
		else {
			outputBuffer.add(cell);
			if(this.trace)
				System.out.println("The cell " + cell.getTraceID() + " was added to the output queue");
		}
	}
	
	/**
	 * Runs Early packet drop on the cell
	 * @param cell the cell to be added/dropped from the queue
	 * @since 1.0
	 */
	private void runEPD(ATMCell cell){
		boolean cellDropped = false;
		
		if (cell.getData().equals("")) {
			this.dropSamePacketCell = false;
			
			double totalDropProbability = 1.0;
			int size = cell.getPacketData().getSize() / (48*8) + 1;
			
			for (int i = 0; i < size; i ++) {
				double dropProbability = 0.0;
				if (outputBuffer.size() + i > this.startDropAt) {
					if (outputBuffer.size() + i < this.maximumBufferCells) {
						dropProbability = (outputBuffer.size() + i - this.startDropAt) 
								/ (double)(this.maximumBufferCells - this.startDropAt);
					}
					else {
						dropProbability = 1;
					}
				}							
				totalDropProbability *= 1.0 - dropProbability;
			}
			totalDropProbability = 1.0 - totalDropProbability;
			
			if (totalDropProbability > 0) {
				double r = Math.random() * 1.0 / totalDropProbability;
				if (r <= 1.0) {
					cellDropped = true;
					this.dropSamePacketCell = true;
				}
			}
		}
		else {
			if (this.dropSamePacketCell) {
				cellDropped = true;
			}
		}		
		
		//outputBuffer.add(cell);
		
		// Output to the console what happened
		if(cellDropped)
			System.out.println("The cell " + cell.getTraceID() + " was dropped");
		else {
			outputBuffer.add(cell);
			if(this.trace)
				System.out.println("The cell " + cell.getTraceID() + " was added to the output queue");
		}
	}
	
	/**
	 * Sets that the nic should use Tail drop when deciding weather or not to add cells to the queue
	 * @since 1.0
	 */
	public void setIsTailDrop(){
		this.red=false;
		this.tail=true;
		this.ppd=false;
		this.epd=false;
	}
	
	/**
	 * Sets that the nic should use RED when deciding weather or not to add cells to the queue
	 * @since 1.0
	 */
	public void setIsRED(){
		this.red=true;
		this.tail=false;
		this.ppd=false;
		this.epd=false;
	}
	
	/**
	 * Sets that the nic should use PPD when deciding weather or not to add cells to the queue
	 * @since 1.0
	 */
	public void setIsPPD(){
		this.red=false;
		this.tail=false;
		this.ppd=true;
		this.epd=false;
	}
	
	/**
	 * Sets that the nic should use EPD when deciding weather or not to add cells to the queue
	 * @since 1.0
	 */
	public void setIsEPD(){
		this.red=false;
		this.tail=false;
		this.ppd=false;
		this.epd=true;
	}
	
	/**
	 * This method connects a link to this nic
	 * @param link the link to connect to this nic
	 * @since 1.0
	 */
	public void connectOtoOLink(OtoOLink link){
		this.link = link;
	}
	
	/**
	 * This method is called when a cell is received over the link that this nic is connected to
	 * @param cell the cell that was received
	 * @since 1.0
	 */
	public void receiveCell(ATMCell cell){
		this.inputBuffer.add(cell);

	}
	
	/**
	 * Moves the cells from the output buffer to the line (then they get moved to the next nic's input buffer)
	 * @since 1.0
	 */
	public void clearOutputBuffers(){
        int line_rate = 10;
        for(int i=0; i<Math.min(line_rate,this.outputBuffer.size()); i++)
                this.link.sendCell(this.outputBuffer.get(i), this);
        ArrayList<ATMCell> temp = new ArrayList<ATMCell>();
        for(int i=Math.min(line_rate,this.outputBuffer.size()); i<this.outputBuffer.size(); i++)
                temp.add(this.outputBuffer.get(i));
        this.outputBuffer.clear();
        this.outputBuffer.addAll(temp);
}
	
	/**
	 * Moves cells from this nics input buffer to its output buffer
	 * @since 1.0
	 */
	public void clearInputBuffers(){
		for(int i=0; i<this.inputBuffer.size(); i++)
			this.parent.receiveCell(this.inputBuffer.get(i), this);
		this.inputBuffer.clear();
	}
}
