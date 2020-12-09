package DataRetriever;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;


import NumericTools.IntegerTool;


public class GithubIO {
	/** Classe per ottenere la lista dei bug fixati da GitHub */
	
	private static Logger myLogger = Logger.getLogger("InfoLogging");
	final static String projNameMin = "vcl";
	final static String companyNameMin = "apache";

	public GithubIO() {

	}

	public ArrayList<String> getCommitData(ArrayList<String> ticketsIds) throws IOException {

		/*
		 * Login in Github is needed to avoid API limit call to get commit for the repo.
		 */
		GitHubClient client = new GitHubClient();
		BufferedReader variabile = new BufferedReader(new InputStreamReader(System.in));
		myLogger.log(Level.INFO,"Interire il token");
		String token = null;
		token = variabile.readLine();
		

		client.setOAuth2Token(token);


		
		RepositoryService service = new RepositoryService(client);
		// Searching for the repository
		Repository repo = null;
		CommitService commitService = null;
		List<RepositoryCommit> CommitList = null;
		
		repo = service.getRepository(companyNameMin, projNameMin);
		
		commitService = new CommitService(client);
		
		// Grep all commit of the repository
		myLogger.info("Creating " + companyNameMin + "/" + projNameMin + " repository commit list ...");
		CommitList = commitService.getCommits(repo);
		myLogger.info("#Commits from github = " + CommitList.size());

		// Clean list of commits
		HashMap<String, Date> fixedCommitList = new HashMap<>();
		for (String ticketId : ticketsIds) {
			for (RepositoryCommit commit : CommitList) {
				
				String message = commit.getCommit().getMessage();
				int index = message.indexOf(ticketId);
				if (index == -1) {
					continue;
				}
				

				int checkIndex = index + ticketId.length();
				IntegerTool it = new IntegerTool();
				if (checkIndex < message.length()) {
					if (it.isNumeric(message.substring(checkIndex, checkIndex + 1))) {
						continue;
					} 
				}

				if (fixedCommitList.containsKey(ticketId)) {
					// se avevo già trovato un commit che contiene quell'ID
					Date oldDate = fixedCommitList.get(ticketId);
					Date newDate = commit.getCommit().getCommitter().getDate();
					if (oldDate.compareTo(newDate) < 0) {
						fixedCommitList.put(ticketId, newDate);
					} 
				}else {
				// se è il primo commit contenente quell'ID
				Date newDate = commit.getCommit().getCommitter().getDate();
				fixedCommitList.put(ticketId, newDate);
				}
			
			}

		}
		
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
