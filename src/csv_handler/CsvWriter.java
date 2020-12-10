package csv_handler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.egit.github.core.RepositoryCommit;
import org.json.JSONException;

import data_retriever.GithubIO;
import data_retriever.JiraIn;
import date_tools.DateTool;
import exception.IncompatibleSizesException;

public class CsvWriter {

	private static final String FILENAME = "processControlData.csv";
	private static final String PROJNAME = "VCL";
	
	public void writeFile(List<String> dates, List<Integer> frequencies) throws IOException, IncompatibleSizesException {
		
		int datesSize = dates.size();
		int frequenciesSize = frequencies.size();
		if (datesSize != frequenciesSize) {
			throw new IncompatibleSizesException("Inputs have to have the same size!");
		}
		
			FileWriter fw = new FileWriter(FILENAME, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			
			pw.println("Mese"+","+"Numero di bugfix");
			
			for (int i = 0; i < datesSize; i++) {
				pw.println(dates.get(i)+","+frequencies.get(i));
			}
			pw.flush();
			pw.close();

	}
	
	public static void main(String[] args) {
		
		Logger logger = null;
		logger = Logger.getLogger(CsvWriter.class.getName());
		
		
		logger.log(Level.INFO,"Retrieving bug fixed list");
		JiraIn ji = new JiraIn();
		ArrayList<String> fixedBugs = null;
		try {
			fixedBugs = (ArrayList<String>) ji.getfixedBugs(PROJNAME);
		} catch (JSONException | IOException e) {
			logger.log(Level.WARNING, e.getMessage());
			System.exit(1);
		}

		logger.log(Level.INFO,"Retrieving the list of bugfix dates");
		GithubIO githubHandler = new GithubIO();
		ArrayList<String> dateList = null;
		try {
			
			List<RepositoryCommit> repositoryCommitList = githubHandler.getCommitList();
			
			
			dateList = (ArrayList<String>) githubHandler.getCommitData(repositoryCommitList, fixedBugs);
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage());
			System.exit(1);
		}
		

		//Date managing
		DateTool dt = new DateTool();
		//Create a set of dates
		ArrayList<String> dateSet= (ArrayList<String>) dt.getOrderedSet(dateList);
		//adding missing dates
		dt.addMissingDates(dateSet);
		/*compute the frequency with which each date in the set appears in the list -
		 *  it is equal to number of bugfix in that date */
		ArrayList<Integer> dateFrequency = (ArrayList<Integer>) dt.computeFrequency(dateSet, dateList);
		
		logger.log(Level.INFO,"Writing csv file");
		CsvWriter csvw = new CsvWriter();
		try {
			csvw.writeFile(dateSet, dateFrequency);
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage());
			System.exit(1);
		} catch (IncompatibleSizesException e) {
			logger.log(Level.SEVERE, e.getMessage());
			System.exit(1);
		}
		
		logger.log(Level.INFO,"Csv file written successfully!");
		
	}

	
}
