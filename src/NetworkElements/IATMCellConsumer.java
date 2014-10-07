/**
 * @author andy
 * @version 1.0
 * @date 24-10-2008
 * @since 1.0
 */

package NetworkElements;

import DataTypes.*;

public interface IATMCellConsumer {
	public void addNIC(ATMNIC nic);
	public void receiveCell(ATMCell cell, ATMNIC nic);
	public void clearOutputBuffers();
	public void clearInputBuffers();
	public void useTailDrop();
	public void useRED();
	public void usePPD();
	public void useEPD();
}
