package ar.edu.itba.pod.tp.referee;

import ar.edu.itba.pod.tp.interfaces.Referee;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
		int port = (args.length < 1) ? 7242 : Integer.valueOf(args[0]);
		
		try {
			System.out.println("Server starting on port: " + port);
			Registry registry = LocateRegistry.createRegistry(port);

			System.out.println("Registry started");
			RefereeServer server = new RefereeServer();
			Referee stub = (Referee) UnicastRemoteObject.exportObject(server, port);
			registry.bind("referee", stub);

			System.out.println("Press any key to start");
			Scanner scan = new Scanner(System.in);
			
			synchronized (server) {
				scan.nextLine();
			}
			System.out.println("EMPEZAMOS!");
		}
		catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}    
	}
}
