package ar.edu.itba.pod.tp.player;

import ar.edu.itba.pod.tp.interfaces.Player;
import ar.edu.itba.pod.tp.interfaces.Referee;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
		String name = (args.length < 1) ? "yo" : args[0];
		int loop = (args.length < 2) ? 200 : Integer.valueOf(args[1]);
		String host = (args.length < 3) ? "localhost" : args[2];
		int port = (args.length < 4) ? 7242 : Integer.valueOf(args[3]);

		try {
			System.out.println("Registering player " + name + " on referee: " + host + ":" + port);
			Registry registry = LocateRegistry.getRegistry(host, port);
			Referee referee = (Referee) registry.lookup("referee");

			// create the player server
			PlayerServer server = new PlayerServer(name);
			System.out.println("Player ready to play");

			server.init(referee);
			
			List<Player> players = server.getOpponents();
			int plays = 0;
			System.out.println("EMPEZAMOS!!");
			do {
				int opt = (int) (java.lang.Math.random() * players.size());
				Player other = players.get(opt);
				server.play("hola! estamos jugando " + plays, other);
			} while (++plays < loop);
			
				
			System.out.println("salio!");
			System.exit(0);
		}
		catch (Exception e) {
			System.err.println("Client exception: " + e.toString());			
			System.err.println("PERDI!");
			System.exit(1);
		}    
	}
	
}
