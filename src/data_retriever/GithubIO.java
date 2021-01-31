package data_retriever;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import numeric_tools.IntegerTool;


public class GithubIO {
	/** Classe per ottenere la lista dei bug fixati da GitHub */
	
	private static final String PROJNAMEMIN = "vcl";
	private final String url = "https://github.com/apache/";
	private Git git;
	private static final String REPOLOCALPATH = "./vclRepo";
	
	
	public GithubIO() {
		//empty constructor
	}
	
	private boolean isEmpty(Path path) {
	    if (Files.isDirectory(path)) {
	        try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
	            return !directory.iterator().hasNext();
	        } catch(IOException e) {
	        	e.printStackTrace();
	        }
	    }
	    return false;
	}
	
	/**
	 * Inizializza l'oggetto Git effettuando il clone o il checkout della repository 
	 * */
	public void init() {
		String uri = url + PROJNAMEMIN + ".git";
		try {
			
			if (!Files.exists(Paths.get(REPOLOCALPATH)) || this.isEmpty(Paths.get(REPOLOCALPATH))) {
				git = Git.cloneRepository().setURI(uri).setDirectory(new File(REPOLOCALPATH)).call();
			} else {
				git = Git.open(new File(REPOLOCALPATH));
				git.checkout().setName(this.getDefaultBranchName()).call();
				git.pull().call();
			}
		
		} catch (GitAPIException | IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Ottiene il nome del branch di default
	 * */
	private String getDefaultBranchName() {
		try {
		    List<Ref> branches = git.branchList().setListMode(ListMode.ALL).call();
			for (Ref branch: branches) {
				String branchName = branch.getName();
				if (branchName.startsWith("refs/heads/")) {
					int startIndex = "refs/heads/".length();
					return branchName.substring(startIndex);
				}
			}
			
	    } catch (GitAPIException e) {
	    	e.printStackTrace();
	    	System.exit(0);
	    }
		return "";
	}
	
	
	public List<RevCommit> getCommitList(){
		List<RevCommit> commits = new ArrayList<>();  
	    Iterable<RevCommit> iterableCommits = null;
	 
	    try {
			git.checkout().setName(this.getDefaultBranchName()).call();
			iterableCommits = git.log().call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		} 

	    for(RevCommit commit: iterableCommits) {
	    	commits.add(commit);
	    }
	    
	    return commits;
	}
	
	/**
	 * Per ogni ticket invoca findLastCommitForTicket()
	 * */
	public Map<String,Date> getMapCommit(List<RevCommit> commitList, List<String> ticketsIds){
		// Clean list of commits
		HashMap<String, Date> fixedCommitList = new HashMap<>();
		for (String ticketId : ticketsIds) {
			findLastCommitForTicket(fixedCommitList, ticketId, commitList);

		}
		return fixedCommitList;
	}
	
	/**
	 * Inserisce nella mappa fixedCommitList una coppia ticketID-data con data la data del commit passato come parametro se:
	 * ticketID è contenuto nel commento del commit e non esiste già un elemento con la stessa chiave nella mappa, oppure se
	 * ticketID è contenuto nel commento del commit e esiste già un elemento con la stessa chiave nella mappa, ma la data associata è precedente
	 * alla data del commit passato come parametro.
	 * */
	private void updateCommitInfoForTicket(Map<String, Date> fixedCommitList, String ticketId, RevCommit commit) {
		String message = commit.getFullMessage();
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
			// se avevo già trovato un commit che contiene quell'ID
			Date oldDate = fixedCommitList.get(ticketId);
			Date newDate = new Date(commit.getCommitTime() *1000L);
			if (oldDate.compareTo(newDate) < 0) {
				fixedCommitList.put(ticketId, newDate);
			} 
		}else {
			// se è il primo commit contenente quell'ID
			Date newDate = new Date(commit.getCommitTime() *1000L);
			fixedCommitList.put(ticketId, newDate);
		}
	}
	
	/**
	 * Per ogni commit invoca updateCommitInfoForTicket().
	 * */
	private void findLastCommitForTicket(Map<String, Date> fixedCommitList, String ticketId, List<RevCommit> commitList) {
		for (RevCommit commit : commitList) {
			updateCommitInfoForTicket(fixedCommitList, ticketId, commit);

		}
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
