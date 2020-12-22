package data_retriever;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

import numeric_tools.IntegerTool;


public class GithubIO {
	/** Classe per ottenere la lista dei bug fixati da GitHub */
	
	private static Logger myLogger = Logger.getLogger("InfoLogging");
	private static final String PROJNAMEMIN = "vcl";
	private static final String COMPANYNAMEMIN = "apache";

	public GithubIO() {
		//empty constructor
	}
	
	/**
	 * Restituisce la lista dei commits sul branch master di una repository.
	 * */
	public List<RepositoryCommit> getCommitList() throws IOException{
		/*
		 * Login in Github is needed to avoid API limit call to get commit for the repo.
		 */
		GitHubClient client = new GitHubClient();
		BufferedReader variabile = new BufferedReader(new InputStreamReader(System.in));
		myLogger.log(Level.CONFIG,"Interire il token");
		String token = null;
		token = variabile.readLine();
		
		client.setOAuth2Token(token);
		
		RepositoryService service = new RepositoryService(client);
		// Searching for the repository
		Repository repo = null;
		CommitService commitService = null;
		List<RepositoryCommit> commitList = null;
		
		repo = service.getRepository(COMPANYNAMEMIN, PROJNAMEMIN);
		
		commitService = new CommitService(client);
		
		// Grep all commit of the repository
		myLogger.info("Creating " + COMPANYNAMEMIN + "/" + PROJNAMEMIN + " repository commit list ...");
		commitList = commitService.getCommits(repo);
		
		return commitList;
	}
	
	/**
	 * Inserisce nella mappa fixedCommitList una coppia ticketID-data con data la data del commit passato come parametro se:
	 * ticketID � contenuto nel commento del commit e non esiste gi� un elemento con la stessa chiave nella mappa, oppure se
	 * ticketID � contenuto nel commento del commit e esiste gi� un elemento con la stessa chiave nella mappa, ma la data associata � precedente
	 * alla data del commit passato come parametro.
	 * */
	private void updateCommitInfoForTicket(Map<String, Date> fixedCommitList, String ticketId, RepositoryCommit commit) {
		String message = commit.getCommit().getMessage();
		int index = message.indexOf(ticketId);
		if (index == -1) {
			return;
		}

		int checkIndex = index + ticketId.length();
		if (checkIndex < message.length()) {
			IntegerTool it = new IntegerTool();
			if (it.isNumeric(message.substring(checkIndex, checkIndex + 1))) {
				return;
			} 
		}

		if (fixedCommitList.containsKey(ticketId)) {
			// se avevo gi� trovato un commit che contiene quell'ID
			Date oldDate = fixedCommitList.get(ticketId);
			Date newDate = commit.getCommit().getCommitter().getDate();
			if (oldDate.compareTo(newDate) < 0) {
				fixedCommitList.put(ticketId, newDate);
			} 
		}else {
			// se � il primo commit contenente quell'ID
			Date newDate = commit.getCommit().getCommitter().getDate();
			fixedCommitList.put(ticketId, newDate);
		}
	}
	
	/**
	 * Per ogni commit invoca updateCommitInfoForTicket().
	 * */
	private void findLastCommitForTicket(Map<String, Date> fixedCommitList, String ticketId, List<RepositoryCommit> commitList) {
		for (RepositoryCommit commit : commitList) {
			updateCommitInfoForTicket(fixedCommitList, ticketId, commit);

		}
	}
	
	/**
	 * Per ogni ticket invoca findLastCommitForTicket()
	 * */
	public Map<String,Date> getMapCommit(List<RepositoryCommit> commitList, List<String> ticketsIds){
		// Clean list of commits
		HashMap<String, Date> fixedCommitList = new HashMap<>();
		for (String ticketId : ticketsIds) {
			findLastCommitForTicket(fixedCommitList, ticketId, commitList);

		}
		return fixedCommitList;
	}
	
	/**
	 * Data una mappa i cui valori sono oggetti Date, restituisce una lista di questi valori trasformati in oggetti String con formato "yyyy-MM"
	 * */
	public List<String> getCommitData(Map<String, Date> fixedCommitList){
		// Create a list of date
		ArrayList<String> dateList = new ArrayList<>();
		for (Date dateCommit : fixedCommitList.values()) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			String strDate = sdf.format(dateCommit);
			dateList.add(strDate);
		}
				

		return dateList;
	}
}
