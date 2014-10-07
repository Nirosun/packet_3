/**
 * @author andy
 * @version 1.0
 * @date 24-10-2008
 * @since 1.0
 */

package DataTypes;

import java.net.*;

public class IPPacket {
	private int size=0; // The size of the packet
	private Inet4Address source=null, dest=null; // The source and destination addresses
	private int delay=0; // The delay the packet has experienced
	private double finishTime=0; // The expected finish time for the packet in WFQ
	
	/**
	 * The default constructor for a packet
	 * @param source the source ip address of this packet
	 * @param dest the destination ip address of this packet
	 * @param size the size of this packet (in bits)
	 * @since 1.0
	 */
	public IPPacket(String source, String dest, int size){
		try{
			this.source = (Inet4Address) InetAddress.getByName(source);
			this.dest = (Inet4Address) InetAddress.getByName(dest);
			this.size = size;
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the size of this packet
	 * @return the size of this packet (in bits)
	 * @since 1.0
	 */
	public int getSize(){
		return this.size;
	}
	
	/**
	 * Returns the source ip address of this packet
	 * @return the source ip address of this packet
	 * @since 1.0
	 */
	public Inet4Address getSource(){
		return this.source;
	}
	
	/**
	 * Returns the destination ip address of this packet
	 * @return the destination ip address of this packet
	 * @since 1.0
	 */
	public Inet4Address getDest(){
		return this.dest;
	}
	
	/**
	 * Adds delay to this packet
	 * @param delay the amount of delay this packet has experienced
	 * @since 1.0
	 */
	public void addDelay(int delay){
		this.delay +=delay;
	}
	
	/**
	 * Returns the delay this packet has experienced
	 * @return the total delay of this packet
	 * @since 1.0
	 */
	public int getDelay(){
		return this.delay;
	}
	
	/**
	 * Returns this packet as a String
	 * @return the string version of this packet
	 * @since 1.0
	 */
	public String toString(){
		return this.source.getHostAddress() + " > " + this.dest.getHostAddress() + " took " + this.getDelay() + " time"; 
	}
	
	/**
	 * Sets the expected finishTime of this packet
	 * @param finishTime the expected finish time of this packet
	 * @since 1.0
	 */
	public void setFinishTime(double finishTime){
		this.finishTime = finishTime;
	}
	
	/**
	 * returns the expected finish time
	 * @return the expected finish time
	 * @since 1.0
	 */
	public double getFinishTime(){
		return this.finishTime;
	}
}
