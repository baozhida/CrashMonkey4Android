package com.android.cts.tradefed.result;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

public class CrashAnalyzer {
	public File getmCrashFile() {
		return mCrashFile;
	}

	public void setmCrashFile(File mCrashFile) {
		this.mCrashFile = mCrashFile;
	}

	private static final String ANR = "ActivityManager: ANR in.*";
	private static final String ANR1 = "ANRManager: ANR in.*";
	
	private static final String CRASH_FILE = "crash.txt";
	private static final String ERROR_REGEX = "^[0-1][0-9]-[0-3][0-9]\\s[0-2][0-9]:[0-6][0-9]:[0-5][0-9].[\\d]+\\s+[\\d]+\\s+[\\d]+\\s+E\\s";
	private static final String ANR_REGEX = ERROR_REGEX+ANR;
	private static final String ANR_REGEX1 = ERROR_REGEX+ANR1;

	private static final String CRASH_REGEX = ERROR_REGEX + "AndroidRuntime:.*";
	private static final String WARN_REGEX = "^[0-1][0-9]-[0-3][0-9]\\s[0-2][0-9]:[0-6][0-9]:[0-5][0-9].[\\d]+\\s+[\\d]+\\s+[\\d]+\\s+W\\s.*";
	private File mLogFile = null;
	private File mCrashFile = null;
	private int CrashCount = 1;
	private int ANRCount = 1;
	private int CommonERRORCount = 1;
	private int WARNCount = 1;



	public CrashAnalyzer(File file, File crashFile) {
		mLogFile = file;
		mCrashFile = crashFile;
	}

	public CrashAnalyzer(File logFile) {
		mLogFile = logFile;
		if (mCrashFile == null)
			mCrashFile = new File(mLogFile.getParent(), CRASH_FILE);

	}
	
	public void parserLogcat() {
		FileReader fr = null;
		BufferedReader br = null;
		FileWriter wr = null;

		try {
			fr = new FileReader(mLogFile);
			br = new BufferedReader(fr);
			wr = new FileWriter(mCrashFile);
			String line = null;
			while ((line = br.readLine()) != null) {

				// 先判断有没有crash
				if (Pattern.matches(CRASH_REGEX, line)) {
					String str = getHead("Crash num " + CrashCount + " Message");
					CrashCount ++;
					wr.write(str);
					wr.write(line + System.getProperty("line.separator"));
					continue;
				}

				// 判断有没有ANR
				if (Pattern.matches(ANR_REGEX, line)) {
					String str = getHead("ANR num " + ANRCount + " Message");
					ANRCount ++;
					wr.write(str);
					wr.write(line + System.getProperty("line.separator"));
					continue;
				}
				// 判断有没有ANR1
				if (Pattern.matches(ANR_REGEX1, line)) {
					String str = getHead("ANR num " + ANRCount + " Message");
					ANRCount ++;
					wr.write(str);
					wr.write(line + System.getProperty("line.separator"));
					continue;
				}

				// 输出有E标识的信息
				if (Pattern.matches(ERROR_REGEX + ".*", line)) {
					String str = getHead("CommonERROR num " + CommonERRORCount + " Message");
					CommonERRORCount ++;
					wr.write(str);
					wr.write(line + System.getProperty("line.separator"));
					continue;
				}
				
				// 输出有WARN标识的信息
				/*
				if (Pattern.matches(WARN_REGEX + ".*", line)) {
					String str = getHead("WARN num " + WARNCount + " Message");
					WARNCount ++;
					wr.write(str);
					wr.write(line + System.getProperty("line.separator"));
					continue;
				}
				*/
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (wr != null)
					wr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if (fr != null)
					fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private String getHead(String str) {
		String output = "==="+ str + "===";

		return output;
	}

	public boolean hasCrash() {
		return ANRCount > 1 || CrashCount > 1;
	}
	
	public int getCrashCount(){
		return ANRCount + CrashCount -2;
	}

	public static void main(String[] args) {
		File file = new File("d:/monkey_8660870776125556895.txt");
		CrashAnalyzer analyzer = new CrashAnalyzer(file);
		analyzer.parserLogcat();

	}

}
