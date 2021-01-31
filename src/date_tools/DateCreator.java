package date_tools;

import java.util.Calendar;
import java.util.Date;

public class DateCreator {
	
	private DateCreator() {
		//private constructor
	}
	
	public static Date getDateFromEpoch(long millisecondsFromEpoch) {
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTimeInMillis(millisecondsFromEpoch);
		
		return calendar.getTime();
	}
}