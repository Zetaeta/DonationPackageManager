package net.zetaeta.plugins.donationpackagemanager;

import java.io.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiLogger {
	
	private Logger logger;
	private PrintWriter writer;
	
	public MultiLogger(Logger logger, PrintWriter writer) {
		this.logger = logger;
		this.writer = writer;
	}
	public MultiLogger(Logger logger, File file) {
		this.logger = logger;
		try {
			writer = new PrintWriter(new FileOutputStream(file, true));
		} catch (FileNotFoundException e) {
			severe("Could not open log file.");
			e.printStackTrace();
		}
	}
	
	public void close() {
		writer.close();
	}
	public void finalize() {
		close();
	}
	
	public void finest(String input) {
		logger.finest(input);
		writer.println(addDate(input));
	}
	public void finer(String input) {
		logger.finer(input);
		writer.println(addDate(input));
	}
	public void fine(String input) {
		logger.fine(input);
		writer.println(addDate(input));
	}
	public void config(String input) {
		logger.config(input);
		writer.println(addDate(input));
	}
	public void info(String input) {
		logger.info(input);
		writer.println(addDate(input));
	}
	public void warning(String input) {
		logger.warning(input);
		writer.println(addDate(input));
	}
	public void severe(String input) {
		logger.severe(input);
		writer.println(addDate(input));
	}
	public void log(Level level, String input) {
		logger.log(level, input);
		writer.println(addDate(input));
	}
	
	private String addDate(String input) {
		Calendar calendar = new GregorianCalendar();
		return String.format("[%tY-%tm-%td  %tH:%tM:%tS]  %s", calendar, calendar, calendar, calendar, calendar, calendar, input);
	}
}
