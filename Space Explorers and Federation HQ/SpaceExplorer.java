import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class for a space explorer.
 */
public class SpaceExplorer extends Thread {
	// number of hashes
	Integer hashCount;

	// descovered solar systems
	static Set<Integer> discovered;

	// communication
	CommunicationChannel channel;

	// bariera
	static ReentrantLock re;

	/**
	 * Creates a {@code SpaceExplorer} object.
	 * 
	 * @param hashCount
	 *            number of times that a space explorer repeats the hash operation
	 *            when decoding
	 * @param discovered
	 *            set containing the IDs of the discovered solar systems
	 * @param channel
	 *            communication channel between the space explorers and the
	 *            headquarters
	 */
	public SpaceExplorer(Integer hashCount, Set<Integer> discovered, CommunicationChannel channel) {
		this.hashCount = hashCount;
		this.discovered = discovered;
		this.channel = channel;
		re = new ReentrantLock();
	}

	@Override
	public void run() {
		while (true) {

			// extrage cate 2 mesaje(parintele si nodul curent)
			re.lock();
			Message receivedParent = channel.getMessageHeadQuarterChannel();
			Message receivedCurrent = channel.getMessageHeadQuarterChannel();
			if (re.isHeldByCurrentThread()) {
				re.unlock();
			}

			if (receivedParent != null && receivedCurrent != null) {
				// daca deja a descoperit sistemul solar trece mai departe
				if (discovered.contains(receivedCurrent.getCurrentSolarSystem())) {
					continue;
				}

				// decodificam frecventa
				String decoded = encryptMultipleTimes(receivedCurrent.getData(), hashCount);

				// cream noul mesaj
				Message messageToSend = new Message(receivedParent.getCurrentSolarSystem(),
						receivedCurrent.getCurrentSolarSystem(), decoded);

				// daca e root punem in loc de -1 alta valoare
				if (receivedParent.getCurrentSolarSystem() == -1) {
					messageToSend.setParentSolarSystem(receivedCurrent.getCurrentSolarSystem());
				}

				// trimite mesajul catre canal
				channel.putMessageSpaceExplorerChannel(messageToSend);

				// adauga sistemul solar ca fiind descoperit
				re.lock();

				discovered.add(receivedCurrent.getCurrentSolarSystem());

				if (re.isHeldByCurrentThread()) {
					re.unlock();
				}
			} else {
				continue; // daca n-a primit nimic, trece mai departe
			}
		}
	}
	
	/**
	 * Applies a hash function to a string for a given number of times (i.e.,
	 * decodes a frequency).
	 * 
	 * @param input
	 *            string to he hashed multiple times
	 * @param count
	 *            number of times that the string is hashed
	 * @return hashed string (i.e., decoded frequency)
	 */
	private String encryptMultipleTimes(String input, Integer count) {
		String hashed = input;
		for (int i = 0; i < count; ++i) {
			hashed = encryptThisString(hashed);
		}

		return hashed;
	}

	/**
	 * Applies a hash function to a string (to be used multiple times when decoding
	 * a frequency).
	 * 
	 * @param input
	 *            string to be hashed
	 * @return hashed string
	 */
	private static String encryptThisString(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			// convert to string
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xff & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
