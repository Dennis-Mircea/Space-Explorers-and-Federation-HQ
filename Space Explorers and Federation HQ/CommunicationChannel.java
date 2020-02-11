import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that implements the channel used by headquarters and space explorers to communicate.
 */
public class CommunicationChannel {
    // coada de mesaje de la HQs
	static ArrayBlockingQueue<Message> HqMsg;

	// coada de mesaje de la space explorers
	static ArrayBlockingQueue<Message> ExplorerMsg;

	// bariera
	static ReentrantLock re;

	// numele thread-ului curent care trimite un dublet de mesaje
	static String currentIdThread = "";
	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	public CommunicationChannel() {
		HqMsg = new ArrayBlockingQueue<Message>(500);
		ExplorerMsg = new ArrayBlockingQueue<Message>(500);
		re = new ReentrantLock();
	}

	/**
	 * Puts a message on the space explorer channel (i.e., where space explorers write to and 
	 * headquarters read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageSpaceExplorerChannel(Message message) {
		try {
			ExplorerMsg.put(message);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Gets a message from the space explorer channel (i.e., where space explorers write to and
	 * headquarters read from).
	 * 
	 * @return message from the space explorer channel
	 */
	public Message getMessageSpaceExplorerChannel() {
		try {
			return ExplorerMsg.take();
		} catch (InterruptedException e) {
		}
		return null;
	}

	/**
	 * Puts a message on the headquarters channel (i.e., where headquarters write to and 
	 * space explorers read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageHeadQuarterChannel(Message message) {
		re.lock();

		// trimite mesaje 2 cate 2
		if (currentIdThread.equals("")) {
		    // cum a primit primul mesaj din dublet, se blocheaza
			re.lock();
			currentIdThread = Thread.currentThread().getName(); // memoreaza numele thread-ului
			try {
			    // daca primeste END sau EXIT, asteapta iar dublete de mesaje
				if (message.getData().equals("END") || message.getData().equals("EXIT")) {
					currentIdThread = "";
					re.unlock();
				} else {
					HqMsg.put(message);
				}
			} catch (InterruptedException e) {
			}
		} else if (currentIdThread.equals(Thread.currentThread().getName())) { // daca primeste mesaj de la acelasi thread
			try {
				HqMsg.put(message);
			} catch (InterruptedException e) {
			}
			currentIdThread = "";
			re.unlock();
		}
		re.unlock();
	}

	/**
	 * Gets a message from the headquarters channel (i.e., where headquarters write to and
	 * space explorer read from).
	 * 
	 * @return message from the header quarter channel
	 */
	public Message getMessageHeadQuarterChannel() {
		try {
			return HqMsg.take();
		} catch (InterruptedException e) {
		}
		return null;
	}
}
