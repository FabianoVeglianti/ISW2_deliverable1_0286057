package CSVHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;

import DataRetriever.GithubIO;
import DataRetriever.JiraIn;
import DateTools.DateTool;
import Exception.IncompatibleSizesException;

public class CsvWriter {

	private final String filename = "processControlData.csv";
	private final static String projName = "VCL";
	
	public void writeFile(ArrayList<String> dates, ArrayList<Integer> frequencies) throws IOException, IncompatibleSizesException {
		
		int datesSize = dates.size();
		int frequenciesSize = frequencies.size();
		if (datesSize != frequenciesSize) {
			throw new IncompatibleSizesException("Inputs have to have the same size!");
		}
		
			FileWriter fw = new FileWriter(filename, true);
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
			fixedBugs = ji.getfixedBugs(projName);
		} catch (JSONException | IOException e) {
			logger.log(Level.WARNING, e.getMessage());
			System.exit(1);
		}
		logger.log(Level.INFO,"#Tickets = " + fixedBugs.size());
		logger.log(Level.INFO,"Retrieving the list of bugfix dates");
		GithubIO githubHandler = new GithubIO();
		ArrayList<String> dateList = null;
		try {
			dateList = githubHandler.getCommitData(fixedBugs);
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage());
			System.exit(1);
		}
		logger.info("#Commits for bug fix = "+ dateList.size());

		//Date managing
		DateTool dt = new DateTool();
		//Create a set of dates
		ArrayList<String> dateSet= dt.getOrderedSet(dateList);
		//adding missing dates
		dt.addMissingDates(dateSet);
		/*compute the frequency with which each date in the set appears in the list -
		 *  it is equal to number of bugfix in that date */
		ArrayList<Integer> dateFrequency = dt.computeFrequency(dateSet, dateList);
		
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
