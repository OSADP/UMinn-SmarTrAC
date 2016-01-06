package com.smartracumn.smartrac;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import android.app.Application;
import android.os.Environment;

@ReportsCrashes(formKey = "", mailTo = "smartrac@umn.edu", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text, customReportContent = {
		ReportField.USER_CRASH_DATE, ReportField.APP_VERSION_CODE,
		ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION,
		ReportField.PHONE_MODEL, ReportField.BRAND, ReportField.CUSTOM_DATA,
		ReportField.STACK_TRACE })
public class CrashReport extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		YourOwnSender sender = new YourOwnSender();
		ACRA.init(this);
		ACRA.getErrorReporter().addReportSender(sender);
	}

	public class YourOwnSender implements ReportSender {

		public YourOwnSender() {
			// initialize your sender with needed parameters
		}

		@Override
		public void send(CrashReportData arg0) throws ReportSenderException {
			File file = createFile();
			writeToFile(file, arg0.toString());
		}

		private File createFile() {
			String baseDir = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			File directory = new File(baseDir + File.separator
					+ "Smartrac Crash Reports");
			directory.mkdirs();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar c = Calendar.getInstance();
			String date = sdf.format(c.getTime());
			String fileName = "crashReport-" + date + ".txt";

			File file = new File(directory, fileName);

			return file;
		}

		private void writeToFile(File file, String report) {

			FileOutputStream stream;
			try {
				stream = new FileOutputStream(file);
				stream.write(report.getBytes());
				stream.close();
			} catch (FileNotFoundException e) {
				System.out.println(e);
			} catch (IOException e) {
				System.out.println(e);
			}

		}
	}

}
