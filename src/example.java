
import NetworkElements.*;
import java.util.*;

public class example {
	// This object will be used to move time forward on all objects
	private ArrayList<IATMCellConsumer> allConsumers = new ArrayList<IATMCellConsumer>();
	private int time = 0;
	
	/**
	 * Create a network and creates connections
	 * @since 1.0
	 */
	public void go(){
		System.out.println("** SYSTEM SETUP **");
		
		// Create some new ATM Routers
		ATMRouter r1 = new ATMRouter(9);
		ATMRouter r2 = new ATMRouter(3);
		ATMRouter r3 = new ATMRouter(11);
		ATMRouter r4 = new ATMRouter(13);
		ATMRouter r5 = new ATMRouter(14);
		
		// give the routers interfaces
		ATMNIC r1n1 = new ATMNIC(r1);
		ATMNIC r2n1 = new ATMNIC(r2);
		ATMNIC r2n2 = new ATMNIC(r2);
		ATMNIC r2n3 = new ATMNIC(r2);
		ATMNIC r3n1 = new ATMNIC(r3);
		ATMNIC r4n1 = new ATMNIC(r4);
		ATMNIC r4n2 = new ATMNIC(r4);
		ATMNIC r5n1 = new ATMNIC(r5);
		
		// physically connect the router's nics
		OtoOLink l1 = new OtoOLink(r1n1, r2n1);
		OtoOLink l2 = new OtoOLink(r2n2, r3n1);
		OtoOLink l3 = new OtoOLink(r2n3, r4n1);
		OtoOLink l4 = new OtoOLink(r4n2, r5n1);
		
		// Create the forwarding tables so each Router knows what interface to use
		// to connect to another ATM router
		// router 1 - AS9
		r1.addNextHopInterface(3, r1n1);
		r1.addNextHopInterface(11, r1n1);
		r1.addNextHopInterface(13, r1n1);
		r1.addNextHopInterface(14, r1n1);
		
		// router 2 - AS3
		r2.addNextHopInterface(9, r2n1);
		r2.addNextHopInterface(11, r2n2);
		r2.addNextHopInterface(13, r2n3);
		r2.addNextHopInterface(14, r2n3);
		
		// router 3 - AS11
		r3.addNextHopInterface(3, r3n1);
		r3.addNextHopInterface(9, r3n1);
		r3.addNextHopInterface(13, r3n1);
		r3.addNextHopInterface(14, r3n1);
		
		// router 4 - AS13
		r4.addNextHopInterface(3, r4n1);
		r4.addNextHopInterface(9, r4n1);
		r4.addNextHopInterface(11, r4n1);
		r4.addNextHopInterface(14, r4n2);
		
		// router 5 - AS14
		r5.addNextHopInterface(3, r5n1);
		r5.addNextHopInterface(9, r5n1);
		r5.addNextHopInterface(11, r5n1);
		r5.addNextHopInterface(12, r5n1);
		
		// Connect a computer to r1
		Computer comp1 = new Computer("1");
		ATMNIC comp1n1 = new ATMNIC(comp1);
		ATMNIC r1n101 = new ATMNIC(r1);
		OtoOLink l101 = new OtoOLink(comp1n1, r1n101);
		
		// Connect a computer to r2
		Computer comp2 = new Computer("2");
		ATMNIC comp2n1 = new ATMNIC(comp2);
		ATMNIC r2n101 = new ATMNIC(r2);
		OtoOLink l201 = new OtoOLink(comp2n1, r2n101);
		
		// Add the objects that need to move in time to an array
		this.allConsumers.add(r1);
		this.allConsumers.add(r2);
		this.allConsumers.add(r3);
		this.allConsumers.add(r4);
		this.allConsumers.add(r5);
		this.allConsumers.add(comp1);
		this.allConsumers.add(comp2);
		
		// set the drop mechanism if we want to try them
		for(int i=0; i<this.allConsumers.size(); i++)
			this.allConsumers.get(i).useTailDrop();
		
		// Setup a connection from comp1 to router 13 and comp2 to 14
		tock();
		//comp1.sendPacket(500);
		comp1.setupConnection(13);
		comp2.setupConnection(14);
		for(int i=0; i<12; i++)
			this.tock();
		//comp1.sendPacket(500);
		//for(int i=0; i<8; i++)
			//this.tock();
	}
	
	/**
	 * moves time forward in all of the networks objects, so that cells take some amount of time to
	 * travel from once place to another
	 * @since 1.0
	 */
	public void tock(){
		System.out.println("** TIME = " + time + " **");
		time++;
		
		// Move the cells in the output buffers
		for(int i=0; i<this.allConsumers.size(); i++)
			allConsumers.get(i).clearOutputBuffers();
		
		// Move the cells from the input buffers to the output buffers
		for(int i=0; i<this.allConsumers.size(); i++)
			allConsumers.get(i).clearInputBuffers();
	}
	
	public static void main(String args[]){
		example go = new example();
		go.go();
	}
}








/*
 * @since 1.0
 * @version 1.2
** SYSTEM SETUP **
** TIME = 0 **
SND SETUP: Computer 1 sent a connect setup 62496
SND SETUP: Computer 2 sent a connect setup 83256
** TIME = 1 **
REC SETUP: Router 9 received a setup message 62496
SND CALLPRO: Router 9 sent a call proceeding message 40587
SND SETUP: Router 9 sent a setup 62496
REC SETUP: Router 3 received a setup message 83256
SND CALLPRO: Router 3 sent a call proceeding message 74698
SND SETUP: Router 3 sent a setup 83256
** TIME = 2 **
SND WAIT: Router 3 sent a wait message 74699
REC SETUP: Router 13 received a setup message 83256
SND CALLPRO: Router 13 sent a call proceeding message 64919
SND SETUP: Router 13 sent a setup 83256
REC CALLPRO: Computer 1 received a call proceeding message 40587
REC CALLPRO: Computer 2 received a call proceeding message 74698
** TIME = 3 **
REC WAIT: Router 9 received a wait message 74699
SND SETUP: Router 9 sent a setup 40588
REC CALLPRO: Router 3 received a call proceeding message 64919
REC SETUP: Router 14 received a setup message 83256
SND CALLPRO: Router 14 sent a call proceeding message 70064
Trace (ATMRouter): First free VC = 1
SND CONN: Router 14 sent a connect message 70065
** TIME = 4 **
SND WAIT: Router 3 sent a wait message 74700
REC CALLPRO: Router 13 received a call proceeding message 70064
REC CONN: Router 13 received a connect message 70065
SND CONN: Router 13 sent a connect message 64920
SND CALLACK: Router 13 sent a connect ack message 70065
** TIME = 5 **
REC WAIT: Router 9 received a wait message 74700
SND SETUP: Router 9 sent a setup 40589
REC CONN: Router 3 received a connect message 64920
SND CONN: Router 3 sent a connect message 74701
SND CALLACK: Router 3 sent a connect ack message 64920
REC CALLACK: Router 14 received a connect ack message 64921
** TIME = 6 **
REC SETUP: Router 3 received a setup message 40589
SND CALLPRO: Router 3 sent a call proceeding message 74703
SND SETUP: Router 3 sent a setup 40589
REC CALLACK: Router 13 received a connect ack message 74702
REC CONN: Computer 2 received a connect message 74701
The connection is setup on VC 1
SND CALLACK: Computer 2 sent a connect ack message 83257
** TIME = 7 **
REC CALLPRO: Router 9 received a call proceeding message 74703
REC CALLACK: Router 3 received a connect ack message 83257
REC SETUP: Router 13 received a setup message 40589
SND CALLPRO: Router 13 sent a call proceeding message 64922
Trace (ATMRouter): First free VC = 2
SND CONN: Router 13 sent a connect message 64923
** TIME = 8 **
REC CALLPRO: Router 3 received a call proceeding message 64922
REC CONN: Router 3 received a connect message 64923
SND CONN: Router 3 sent a connect message 74704
SND CALLACK: Router 3 sent a connect ack message 64923
** TIME = 9 **
REC CONN: Router 9 received a connect message 74704
SND CONN: Router 9 sent a connect message 40590
SND CALLACK: Router 9 sent a connect ack message 74704
REC CALLACK: Router 13 received a connect ack message 74705
** TIME = 10 **
REC CALLACK: Router 3 received a connect ack message 40591
REC CONN: Computer 1 received a connect message 40590
The connection is setup on VC 1
SND CALLACK: Computer 1 sent a connect ack message 62497
** TIME = 11 **
REC CALLACK: Router 9 received a connect ack message 62497
** TIME = 12 **

*/