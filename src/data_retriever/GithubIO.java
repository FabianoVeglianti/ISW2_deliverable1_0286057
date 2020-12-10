package data_retriever;

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

import numeric_tools.IntegerTool;


public class GithubIO {
	/** Classe per ottenere la lista dei bug fixati da GitHub */
	
	private static Logger myLogger = Logger.getLogger("InfoLogging");
	private static final String PROJNAMEMIN = "vcl";
	private static final String COMPANYNAMEMIN = "apache";

	public GithubIO() {
		//empty constructor
	}
	
	private List<RepositoryCommit> getCommitList() throws IOException{
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
		List<RepositoryCommit> commitList = null;
		
		repo = service.getRepository(COMPANYNAMEMIN, PROJNAMEMIN);
		
		commitService = new CommitService(client);
		
		// Grep all commit of the repository
		myLogger.info("Creating " + COMPANYNAMEMIN + "/" + PROJNAMEMIN + " repository commit list ...");
		commitList = commitService.getCommits(repo);
		
		return commitList;
	}
	

	public List<String> getCommitData(List<String> ticketsIds) throws IOException {

		List<RepositoryCommit> commitList = getCommitList();

		// Clean list of commits
		HashMap<String, Date> fixedCommitList = new HashMap<>();
		for (String ticketId : ticketsIds) {
			for (RepositoryCommit commit : commitList) {
				
				String message = commit.getCommit().getMessage();
				int index = message.indexOf(ticketId);
				if (index == -1) {
					// do nothing
				} else {
				
					int checkIndex = index + ticketId.length();
					if (checkIndex < message.length()) {
						IntegerTool it = new IntegerTool();
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
