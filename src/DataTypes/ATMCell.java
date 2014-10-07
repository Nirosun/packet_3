/**
 * @author andy
 * @version 1.0
 * @date 24-10-2008
 * @since 1.0
 */

package DataTypes;

public class ATMCell {
	private int vc = 0; // the vc the cell is on
	private boolean isOAM=false; // says if the cell is OAM
	private IPPacket packetData = null; // the IP packet header
	private String data = ""; // The IP packet data (if there is no header)
	private int traceID = 0; // The trace ID for the cell
	
	/**
	 * Constructor for an ATM cell when there is IP header data
	 * @param vc the vc to send this cell on
	 * @param data the IP header data for the packet
	 * @param traceID the trace ID of the cell
	 * @since 1.0
	 */
	public ATMCell(int vc, IPPacket data, int traceID){
		this.vc = vc;
		this.packetData = data;
		this.traceID = traceID;
	}
	
	/**
	 * Constructor for an ATM cell when there is data from the ip packet (or OAM data)
	 * @param vc the vc to send this cell on
	 * @param data the IP header data for the packet
	 * @param traceID the trace ID of the cell
	 * @since 1.0
	 */
	public ATMCell(int vc, String data, int traceID){
		this.vc = vc;
		this.data = data;
		this.traceID = traceID;
	}
	
	/**
	 * Returns the IP packet header data contained in this cell (or null if there isn't a header)
	 * @return the IP packet header data contained in this cell (or null if there isn't a header)
	 * @since 1.0
	 */
	public IPPacket getPacketData(){
		return this.packetData;
	}
	
	/**
	 * Returns the data within this cell, or blank if there isn't any
	 * @return the data within this cell, or blank if there isn't any
	 * @since 1.0
	 */
	public String getData(){
		return this.data;
	}
	
	/**
	 * Sets if this cell contains OAM information or data
	 * @param isOAM if true the cell contains OAM
	 * @since 1.0
	 */
	public void setIsOAM(boolean isOAM){
		this.isOAM = isOAM;
	}
	
	/**
	 * Gets if this cell contains OAM information or data
	 * @return true if this cell contains OAM
	 * @since 1.0
	 */
	public boolean getIsOAM(){
		return this.isOAM;
	}
	
	/**
	 * Returns the VC that this cell is sent on
	 * @return the VC that this cell is sent on
	 * @since 1.0
	 */
	public int getVC(){
		return this.vc;
	}
	
	/**
	 * Sets the VC that this cell is on
	 * @param vc the new VC that this cell is on
	 * @since 1.0
	 */
	public void setVC(int vc){
		this.vc = vc;
	}
	
	/**
	 * Returns the trace ID for this cell
	 * @return the trace ID for this cell
	 * @since 1.0
	 */
	public int getTraceID(){
		return this.traceID;
	}
}
