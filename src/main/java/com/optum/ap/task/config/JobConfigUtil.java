package com.optum.ap.task.config;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class JobConfigUtil {
	
	public static final String CACHE_NAMESPACE = "APTASKS";
	private static Logger logger = Logger.getLogger(JobConfigUtil.class);
	
	/**
	 * Returns a formatted string in minutes and seconds that represents
	 * the difference between two date/times.
	 */
	public static String getFormattedMinSecDiff(Temporal start, Temporal finish) {
		String minSecDiff = "";
		
		try {
			Duration duration = Duration.between(start, finish);
			long millis = duration.toMillis();
			minSecDiff = String.format("%d min %d sec", 
			        TimeUnit.MILLISECONDS.toMinutes(millis),
			        TimeUnit.MILLISECONDS.toSeconds(millis) - 
			        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		} catch (Exception e) {
			logger.error("Error getting minSecDiff. e.getMessage()=" + e.getMessage());
		}
        
        return minSecDiff;
	}
	
	/*public String buildListToCommaseparatedString(List list){
		return (String) list.stream().map(el->"'"+el+"'").collect(Collectors.joining(","));
	}*/
}
