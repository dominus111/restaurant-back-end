package comp1206.sushi;

/**
 * YOU ARE NOT ALLOWED TO MODIFY THIS CLASS IN ANY WAY
 * 
 * Any initialisation and changes you need to make can be done in the constructor for the Server or Client
 *
 */
public class Launcher {

	public static void main(String[] argv) {	
		if(argv.length > 0 && argv[0].equals("client")) {
			System.out.println("Running client");
			ClientApplication.main(argv);
		} else if(argv.length > 0 && argv[0].equals("server")) {
			System.out.println("Running server");
			ServerApplication.main(argv);
		} else {
			ServerApplication.main(argv);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ClientApplication.main(argv);

		}
	}
}
