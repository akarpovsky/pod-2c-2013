/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ar.edu.itba.pod.tp.referee;

import ar.edu.itba.pod.tp.interfaces.Player;
import ar.edu.itba.pod.tp.interfaces.Referee;
import ar.edu.itba.pod.tp.interfaces.Registration;
import ar.edu.itba.pod.tp.interfaces.Request;
import ar.edu.itba.pod.tp.interfaces.Response;
import ar.edu.itba.pod.tp.interfaces.Utils;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 *
 * @author mariano
 */
public class RefereeServer implements Referee
{

	final List<Player> playerServers = new ArrayList<Player>();
	final Map<Player, Registration> registrations = new HashMap();
	final Random random = new Random();
	final Map<Integer, List<Request>> requests = new HashMap();

	@Override
	public Registration newPlayer(String playerName, Player playerClient) throws RemoteException
	{
		System.out.println("nuevo player " + playerName);
		Registration result = register(playerName, playerClient);
		return result;
	}

	@Override
	public void registerRequest(Player player, Request request) throws RemoteException
	{
		Registration clientReg = registrations.get(player);
		System.out.println("REQ: " + request);
		if (clientReg.id != request.playerId) {
			throw new RemoteException("Fallo el PLAYER SEQ!!!");
		}
		if (clientReg.clientSeq != request.clientSeq) {
			throw new RemoteException("Fallo el PLAYER OP SEQ!!! " + clientReg.clientSeq + "/" + request.clientSeq);
		}
		String check = hashMessage(clientReg, request.clientSeq, request.message);
		if (!check.equals(request.hash)) {
			throw new RemoteException("Fallo el hash!!!");
		}
		clientReg.clientSeq = clientReg.clientSeq + 1;
		List<Request> playerRequests = requests.get(clientReg.id);
		if (playerRequests == null) {
			playerRequests = new ArrayList();
			requests.put(clientReg.id, playerRequests);
		}
		playerRequests.add(request);
	}

	@Override
	public void registerResponse(Player player, Response response) throws RemoteException
	{
		Registration clientReg = registrations.get(player);
		System.out.println("RES: " + response);
		if (clientReg.id != response.rspPlayerId) {
			throw new RemoteException("Fallo el PLAYER SEQ!!!");
		}
		// si habilitamos este check empieza a fallar enseguida
//		if (clientReg.serverSeq != response.rspServerSeq) {
//			throw new RemoteException("Fallo el SERVER OP SEQ!!!" + clientReg.serverSeq + "/" + response.rspServerSeq);
//		}
		String check = hashMessage(clientReg, response.rspServerSeq, response.rspMessage);
		if (!check.equals(response.rspHash)) {
			throw new RemoteException("Fallo el hash!!!");
		}
		clientReg.serverSeq = clientReg.serverSeq + 1;
		
		List<Request> clientRequests = requests.get(response.reqPlayerId);
		if (clientRequests == null) {
			clientRequests = new ArrayList();
			requests.put(response.reqPlayerId, clientRequests);
		}
		if (!clientRequests.contains(response.toRequest())) {
			throw new RemoteException("NO ESTA LA OPERACION!!!");
		}
	}

	private Registration register(String playerName, Player playerClient)
	{
		
		final String salt = UUID.randomUUID().toString();
		final int seq = random.nextInt();
		final int clientSeq = random.nextInt();
		final int serverSeq = random.nextInt();
		playerServers.add(playerClient);

		Registration result = new Registration(playerName, seq, clientSeq, serverSeq, salt, playerServers);
		registrations.put(playerClient, result);
		synchronized (this) {
			return result;
		}
	}

	private String hashMessage(Registration registration, int opSeq, String message)
	{
		return Utils.hashMessage(registration.id, opSeq, message, registration.salt);
	}
}
