/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ar.edu.itba.pod.tp.player;

import ar.edu.itba.pod.tp.interfaces.Player;
import ar.edu.itba.pod.tp.interfaces.Referee;
import ar.edu.itba.pod.tp.interfaces.Registration;
import ar.edu.itba.pod.tp.interfaces.Request;
import ar.edu.itba.pod.tp.interfaces.Response;
import ar.edu.itba.pod.tp.interfaces.Utils;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mariano
 */
public class PlayerServer implements Player
{
	private String name;
	private String salt;
	private int id;
	private int clientSeq;
	private int serverSeq;
	private Referee referee;
	private List<Player> opponents;
	
	public PlayerServer(String name)
	{
		this.name = name;
	}

	public void init(Referee referee) throws RemoteException
	{
		Player playerStub = (Player) UnicastRemoteObject.exportObject(this, 0);
		Registration registration = referee.newPlayer(name, playerStub);
		this.id = registration.id;
		this.clientSeq = registration.clientSeq;
		this.serverSeq = registration.serverSeq;
		this.salt = registration.salt;
		this.referee = referee;
		this.opponents = registration.players;
	}
	
	@Override
	public Response operate(Request request) throws RemoteException
	{
		int myOpSeq = this.serverSeq++;
		Response response = new Response();
		response.reqPlayerId = request.playerId;
		response.reqClientSeq = request.clientSeq;
		response.reqMessage = request.message;
		response.reqHash = request.hash;

		response.rspPlayerId = this.id;
		response.rspServerSeq = myOpSeq;
		response.rspMessage = request.message + "aaaa";
		response.rspHash = hashMessage(myOpSeq, response.rspMessage);
		
		this.referee.registerResponse(this, response);
				
		return response;
	}

	public void play(String message, Player target) throws RemoteException, InterruptedException
	{
		int myOpSeq = this.clientSeq++;
		Request request = new Request(this.id, myOpSeq, message, hashMessage(myOpSeq, message));

		System.out.println("invoke " + request);
		this.referee.registerRequest(this, request);
//		Thread.sleep(100);
		Response response = target.operate(request);
		System.out.println("result " + response);
//		Thread.sleep(100);
	}
	
	public List<Player> getOpponents()
	{
		return this.opponents;
	}
	
	private String hashMessage(int opSeq, String message)
	{
		return Utils.hashMessage(this.id, opSeq, message, this.salt);
	}
}
