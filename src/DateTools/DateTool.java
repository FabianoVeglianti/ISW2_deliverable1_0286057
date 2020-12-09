package DateTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DateTool {

	public void addMissingDates(ArrayList<String> date) {
	//This function adds all the missing dates in date from the first present date to the last present date
		String firstDate = date.get(0);
		String lastDate = date.get(date.size()-1);
		String currentDate = firstDate;
		while (!currentDate.equalsIgnoreCase(lastDate)) {
			String nextDate = next(currentDate);
			if (!date.contains(nextDate)) {
				date.add(nextDate);
				Collections.sort(date);
			}
			currentDate = nextDate;
		}
	}
	
	private String next(String date) {
	//Given a date as string "yyyy-mm" it returns the next date as string "yyyy-mm"
		String nextDate = null;
		String[] temp = date.split("-");
		String year = temp[0];
		String month = temp[1];
		if(!month.equals("12")) {
			Integer monthInt = Integer.valueOf(month);
			monthInt ++;
			if (monthInt < 10) {
				String nextMonth = String.valueOf(monthInt);
				nextDate = year + "-0" + nextMonth;
			} else {
				String nextMonth = String.valueOf(monthInt);
				nextDate = year + "-" + nextMonth;
			}
		} else {
			Integer yearInt = Integer.valueOf(year);
			yearInt ++;
			String nextYear = String.valueOf(yearInt);
			nextDate = nextYear + "-01";
		}

		return nextDate;
	}
	 
	public ArrayList<String> getOrderedSet(ArrayList<String> dateList){ 
		// Create a set of date taken from the list, there are not replicated dates, dates are sorted
		Set<String> uniqueDateSet = new HashSet<String>(dateList);
		ArrayList<String> uniqueDateList = new ArrayList<>(uniqueDateSet);
		Collections.sort(uniqueDateList);

		return uniqueDateList;
	}
	
	public ArrayList<Integer> computeFrequency(ArrayList<String> dateSet, ArrayList<String> dateList){
		// Count the frequency of each date in the list of dates
		ArrayList<Integer> dateFrequency = new ArrayList<>();
		for (String date : dateSet) {
			dateFrequency.add(Collections.frequency(dateList, date));
		}
		return dateFrequency;
	}

}
