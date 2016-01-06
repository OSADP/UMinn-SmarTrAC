package com.smartracumn.smartrac.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smartracumn.smartrac.util.MotionFeatureBuffer;
import com.smartracumn.smartrac.util.SpeedFeatureBuffer;

public class ModeFeaturesDataSource {
	public String TAG = getClass().getSimpleName();

	private SmartracSQLiteHelper dbHelper;

	private SQLiteDatabase database;

	// Motion 30sec
	private static final String COLUMN_MOTION_MEAN_30 = "meanacc_30";
	private static final String COLUMN_MOTION_VAR_30 = "varacc_30";
	private static final String COLUMN_MOTION_MEDIAN_30 = "medianacc_30";
	private static final String COLUMN_MOTION_QTWN_30 = "qutweacc_30";
	private static final String COLUMN_MOTION_QEIG_30 = "queigacc_30";
	private static final String COLUMN_MOTION_ENT_30 = "entacc_30";
	private static final String COLUMN_MOTION_KURT_30 = "kurtacc_30";
	private static final String COLUMN_MOTION_SKEW_30 = "skewacc_30";
	private static final String COLUMN_MOTION_IQR_30 = "iqracc_30";
	private static final String COLUMN_MOTION_MIN_30 = "minacc_30";
	private static final String COLUMN_MOTION_MAX_30 = "maxacc_30";
	private static final String COLUMN_MOTION_AUTOCORR_30 = "auto_corr_30";

	// Motion 120 sec
	private static final String COLUMN_MOTION_MEAN_120 = "meanacc_120";
	private static final String COLUMN_MOTION_VAR_120 = "varacc_120";
	private static final String COLUMN_MOTION_MEDIAN_120 = "medianacc_120";
	private static final String COLUMN_MOTION_QTWN_120 = "qutweacc_120";
	private static final String COLUMN_MOTION_QEIG_120 = "queigacc_120";
	private static final String COLUMN_MOTION_ENT_120 = "entacc_120";
	private static final String COLUMN_MOTION_KURT_120 = "kurtacc_120";
	private static final String COLUMN_MOTION_SKEW_120 = "skewacc_120";
	private static final String COLUMN_MOTION_IQR_120 = "iqracc_120";
	private static final String COLUMN_MOTION_MIN_120 = "minacc_120";
	private static final String COLUMN_MOTION_MAX_120 = "maxacc_120";
	private static final String COLUMN_MOTION_AUTOCORR_120 = "auto_corr_120";

	// Seq Motion 30sec
	private static final String COLUMN_MOTION_MEAN_SEQ_30 = "meanseqacc_30";
	private static final String COLUMN_MOTION_VAR_SEQ_30 = "varseqacc_30";
	private static final String COLUMN_MOTION_MEDIAN_SEQ_30 = "medianseqacc_30";
	private static final String COLUMN_MOTION_QTWN_SEQ_30 = "qutweseqacc_30";
	private static final String COLUMN_MOTION_QEIG_SEQ_30 = "queigseqacc_30";
	private static final String COLUMN_MOTION_ENT_SEQ_30 = "entseqacc_30";
	private static final String COLUMN_MOTION_KURT_SEQ_30 = "kurtseqacc_30";
	private static final String COLUMN_MOTION_SKEW_SEQ_30 = "skewseqacc_30";
	private static final String COLUMN_MOTION_IQR_SEQ_30 = "iqrseqacc_30";
	private static final String COLUMN_MOTION_MIN_SEQ_30 = "minseqacc_30";
	private static final String COLUMN_MOTION_MAX_SEQ_30 = "maxseqacc_30";
	private static final String COLUMN_MOTION_AUTOCORR_SEQ_30 = "auto_corrseq_30";

	// Seq Motion 120 sec
	private static final String COLUMN_MOTION_MEAN_SEQ_120 = "meanseqacc_120";
	private static final String COLUMN_MOTION_VAR_SEQ_120 = "varseqacc_120";
	private static final String COLUMN_MOTION_MEDIAN_SEQ_120 = "medianseqacc_120";
	private static final String COLUMN_MOTION_QTWN_SEQ_120 = "qutweseqacc_120";
	private static final String COLUMN_MOTION_QEIG_SEQ_120 = "queigseqacc_120";
	private static final String COLUMN_MOTION_ENT_SEQ_120 = "entseqacc_120";
	private static final String COLUMN_MOTION_KURT_SEQ_120 = "kurtseqacc_120";
	private static final String COLUMN_MOTION_SKEW_SEQ_120 = "skewseqacc_120";
	private static final String COLUMN_MOTION_IQR_SEQ_120 = "iqrseqacc_120";
	private static final String COLUMN_MOTION_MIN_SEQ_120 = "minseqacc_120";
	private static final String COLUMN_MOTION_MAX_SEQ_120 = "maxseqacc_120";
	private static final String COLUMN_MOTION_AUTOCORR_SEQ_120 = "auto_corrseq_120";

	public static final String TABLE_SPEED_FEATURES = "table_speed_features";

	public static final String TABLE_MOTION_FEATURES = "table_motion_features";

	private final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private static final String COLUMN_TIME = "time";
	// Speed Non zero 30 sec
	private static final String COLUMN_SPEED_MEANNON_30 = "meannon_30";
	private static final String COLUMN_SPEED_VARNON_30 = "varnon_30";
	private static final String COLUMN_SPEED_MEDIANNON_30 = "mediannon_30";
	private static final String COLUMN_SPEED_TWNNON_30 = "qutwenon_30";
	private static final String COLUMN_SPEED_EIGNON_30 = "queignon_30";
	private static final String COLUMN_SPEED_ENTNON_30 = "entnon_30";
	private static final String COLUMN_SPEED_KURTNON_30 = "kurtnon_30";
	private static final String COLUMN_SPEED_SKEWNON_30 = "skewnon_30";
	private static final String COLUMN_SPEED_IQRNON_30 = "iqrnon_30";
	private static final String COLUMN_SPEED_MINNON_30 = "minnon_30";
	private static final String COLUMN_SPEED_MAXNON_30 = "maxnon_30";
	private static final String COLUMN_SPEED_AUTO_CORRNON_30 = "auto_corrnon_30";

	// Speed 30 sec
	private static final String COLUMN_SPEED_MEAN_30 = "meansp_30";
	private static final String COLUMN_SPEED_VAR_30 = "varsp_30";
	private static final String COLUMN_SPEED_MEDIAN_30 = "mediansp_30";
	private static final String COLUMN_SPEED_TWN_30 = "qutwesp_30";
	private static final String COLUMN_SPEED_EIG_30 = "queigsp_30";
	private static final String COLUMN_SPEED_ENT_30 = "entsp_30";
	// private static final String COLUMN_SPEED_KURT_30="kurtnon.30";
	private static final String COLUMN_SPEED_SKEW_30 = "skewsp_30";
	private static final String COLUMN_SPEED_IQR_30 = "iqrsp_30";
	private static final String COLUMN_SPEED_MIN_30 = "minsp_30";
	private static final String COLUMN_SPEED_MAX_30 = "maxsp_30";
	// private static final String COLUMN_SPEED_AUTO_CORR_30="auto_corrnon_30";

	// Speed Non zero 120 sec
	private static final String COLUMN_SPEED_MEANNON_120 = "meannon_120";
	private static final String COLUMN_SPEED_VARNON_120 = "varnon_120";
	private static final String COLUMN_SPEED_MEDIANNON_120 = "mediannon_120";
	private static final String COLUMN_SPEED_TWNNON_120 = "qutwenon_120";
	private static final String COLUMN_SPEED_EIGNON_120 = "queignon_120";
	private static final String COLUMN_SPEED_ENTNON_120 = "entnon_120";
	private static final String COLUMN_SPEED_KURTNON_120 = "kurtnon_120";
	private static final String COLUMN_SPEED_SKEWNON_120 = "skewnon_120";
	private static final String COLUMN_SPEED_IQRNON_120 = "iqrnon_120";
	private static final String COLUMN_SPEED_MINNON_120 = "minnon_120";
	private static final String COLUMN_SPEED_MAXNON_120 = "maxnon_120";
	private static final String COLUMN_SPEED_AUTO_CORRNON_120 = "auto_corrnon_120";

	// Speed 120 sec
	private static final String COLUMN_SPEED_MEAN_120 = "meansp_120";
	private static final String COLUMN_SPEED_VAR_120 = "varsp_120";
	private static final String COLUMN_SPEED_MEDIAN_120 = "mediansp_120";
	private static final String COLUMN_SPEED_TWN_120 = "qutwesp_120";
	private static final String COLUMN_SPEED_EIG_120 = "queigsp_120";
	private static final String COLUMN_SPEED_ENT_120 = "entsp_120";
	// private static final String COLUMN_SPEED_KURT_120="kurtnonsp_120";
	private static final String COLUMN_SPEED_SKEW_120 = "skewsp_120";
	private static final String COLUMN_SPEED_IQR_120 = "iqrsp_120";
	private static final String COLUMN_SPEED_MIN_120 = "minsp_120";
	private static final String COLUMN_SPEED_MAX_120 = "maxsp_120";
	// private static final String
	// COLUMN_SPEED_AUTO_CORR_120="auto_corrnon_120";

	// Speed 30 sec
	private static final String COLUMN_SPEED_MEAN_SEQ_30 = "meanseqsp_30";
	private static final String COLUMN_SPEED_VAR_SEQ_30 = "varseqsp_30";
	private static final String COLUMN_SPEED_MEDIAN_SEQ_30 = "medianseqsp_30";
	private static final String COLUMN_SPEED_TWN_SEQ_30 = "qutweseqsp_30";
	private static final String COLUMN_SPEED_EIG_SEQ_30 = "queigseqsp_30";
	private static final String COLUMN_SPEED_ENT_SEQ_30 = "entseqsp_30";
	// private static final String COLUMN_SPEED_KURT_SEQ_30="kurtnon_30";
	private static final String COLUMN_SPEED_SKEW_SEQ_30 = "skewseqsp_30";
	private static final String COLUMN_SPEED_IQR_SEQ_30 = "iqrseqsp_30";
	private static final String COLUMN_SPEED_MIN_SEQ_30 = "minseqsp_30";
	private static final String COLUMN_SPEED_MAX_SEQ_30 = "maxseqsp_30";
	// private static final String
	// COLUMN_SPEED_AUTO_CORR_SEQ_30="auto_corrnon_30";

	// Speed 120 sec
	private static final String COLUMN_SPEED_MEAN_SEQ_120 = "meanseqsp_120";
	private static final String COLUMN_SPEED_VAR_SEQ_120 = "varseqsp_120";
	private static final String COLUMN_SPEED_MEDIAN_SEQ_120 = "medianseqsp_120";
	private static final String COLUMN_SPEED_TWN_SEQ_120 = "qutweseqsp_120";
	private static final String COLUMN_SPEED_EIG_SEQ_120 = "queigseqsp_120";
	private static final String COLUMN_SPEED_ENT_SEQ_120 = "entseqsp_120";
	// private static final String COLUMN_SPEED_KURT_SEQ_120="kurtnon_120";
	private static final String COLUMN_SPEED_SKEW_SEQ_120 = "skewseqsp_120";
	private static final String COLUMN_SPEED_IQR_SEQ_120 = "iqrseqsp_120";
	private static final String COLUMN_SPEED_MIN_SEQ_120 = "minseqsp_120";
	private static final String COLUMN_SPEED_MAX_SEQ_120 = "maxseqsp_120";
	// private static final String
	// COLUMN_SPEED_AUTO_CORR_120="auto_corrnon_120";

	public final static String TABLE_MOTION_FEATURES_CREATE = "create table "
			+ TABLE_MOTION_FEATURES + "(" + SmartracSQLiteHelper.COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TIME + " text, "
			+ COLUMN_MOTION_VAR_30 + " real," + COLUMN_MOTION_MEAN_30
			+ " real," + COLUMN_MOTION_MEDIAN_30 + " real,"
			+ COLUMN_MOTION_QTWN_30 + " real," + COLUMN_MOTION_QEIG_30
			+ " real," + COLUMN_MOTION_ENT_30 + " real,"
			+ COLUMN_MOTION_KURT_30 + " real," + COLUMN_MOTION_SKEW_30
			+ " real," + COLUMN_MOTION_IQR_30 + " real," + COLUMN_MOTION_MIN_30
			+ " real," + COLUMN_MOTION_MAX_30 + " real,"
			+ COLUMN_MOTION_AUTOCORR_30 + " real," + COLUMN_MOTION_VAR_120
			+ " real," + COLUMN_MOTION_MEAN_120 + " real,"
			+ COLUMN_MOTION_MEDIAN_120 + " real," + COLUMN_MOTION_QTWN_120
			+ " real," + COLUMN_MOTION_QEIG_120 + " real,"
			+ COLUMN_MOTION_ENT_120 + " real," + COLUMN_MOTION_KURT_120
			+ " real," + COLUMN_MOTION_SKEW_120 + " real,"
			+ COLUMN_MOTION_IQR_120 + " real," + COLUMN_MOTION_MIN_120
			+ " real," + COLUMN_MOTION_MAX_120 + " real,"
			+ COLUMN_MOTION_AUTOCORR_120 + " real," + COLUMN_MOTION_VAR_SEQ_30
			+ " real," + COLUMN_MOTION_MEAN_SEQ_30 + " real,"
			+ COLUMN_MOTION_MEDIAN_SEQ_30 + " real,"
			+ COLUMN_MOTION_QTWN_SEQ_30 + " real," + COLUMN_MOTION_QEIG_SEQ_30
			+ " real," + COLUMN_MOTION_ENT_SEQ_30 + " real,"
			+ COLUMN_MOTION_KURT_SEQ_30 + " real," + COLUMN_MOTION_SKEW_SEQ_30
			+ " real," + COLUMN_MOTION_IQR_SEQ_30 + " real,"
			+ COLUMN_MOTION_MIN_SEQ_30 + " real," + COLUMN_MOTION_MAX_SEQ_30
			+ " real," + COLUMN_MOTION_AUTOCORR_SEQ_30 + " real,"
			+ COLUMN_MOTION_VAR_SEQ_120 + " real," + COLUMN_MOTION_MEAN_SEQ_120
			+ " real," + COLUMN_MOTION_MEDIAN_SEQ_120 + " real,"
			+ COLUMN_MOTION_QTWN_SEQ_120 + " real,"
			+ COLUMN_MOTION_QEIG_SEQ_120 + " real," + COLUMN_MOTION_ENT_SEQ_120
			+ " real," + COLUMN_MOTION_KURT_SEQ_120 + " real,"
			+ COLUMN_MOTION_SKEW_SEQ_120 + " real," + COLUMN_MOTION_IQR_SEQ_120
			+ " real," + COLUMN_MOTION_MIN_SEQ_120 + " real,"
			+ COLUMN_MOTION_MAX_SEQ_120 + " real,"
			+ COLUMN_MOTION_AUTOCORR_SEQ_120 + " real);";

	public final static String TABLE_SPEED_FEATURES_CREATE = "create table "
			+ TABLE_SPEED_FEATURES + "(" + SmartracSQLiteHelper.COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TIME + " text, "
			+ COLUMN_SPEED_MEANNON_30 + " real," + COLUMN_SPEED_VARNON_30
			+ " real," + COLUMN_SPEED_MEDIANNON_30 + " real,"
			+ COLUMN_SPEED_TWNNON_30 + " real," + COLUMN_SPEED_EIGNON_30
			+ " real," + COLUMN_SPEED_ENTNON_30 + " real,"
			+ COLUMN_SPEED_KURTNON_30 + " real," + COLUMN_SPEED_SKEWNON_30
			+ " real," + COLUMN_SPEED_IQRNON_30 + " real,"
			+ COLUMN_SPEED_MINNON_30 + " real," + COLUMN_SPEED_MAXNON_30
			+ " real," + COLUMN_SPEED_AUTO_CORRNON_30 + " real,"
			+ COLUMN_SPEED_MEAN_30 + " real," + COLUMN_SPEED_VAR_30 + " real,"
			+ COLUMN_SPEED_MEDIAN_30 + " real," + COLUMN_SPEED_TWN_30
			+ " real," + COLUMN_SPEED_EIG_30 + " real," + COLUMN_SPEED_ENT_30
			+ " real," + COLUMN_SPEED_SKEW_30 + " real," + COLUMN_SPEED_IQR_30
			+ " real," + COLUMN_SPEED_MIN_30 + " real," + COLUMN_SPEED_MAX_30
			+ " real," + COLUMN_SPEED_MEANNON_120 + " real,"
			+ COLUMN_SPEED_VARNON_120 + " real," + COLUMN_SPEED_MEDIANNON_120
			+ " real," + COLUMN_SPEED_TWNNON_120 + " real,"
			+ COLUMN_SPEED_EIGNON_120 + " real," + COLUMN_SPEED_ENTNON_120
			+ " real," + COLUMN_SPEED_KURTNON_120 + " real,"
			+ COLUMN_SPEED_SKEWNON_120 + " real," + COLUMN_SPEED_IQRNON_120
			+ " real," + COLUMN_SPEED_MINNON_120 + " real,"
			+ COLUMN_SPEED_MAXNON_120 + " real,"
			+ COLUMN_SPEED_AUTO_CORRNON_120 + " real," + COLUMN_SPEED_MEAN_120
			+ " real," + COLUMN_SPEED_VAR_120 + " real,"
			+ COLUMN_SPEED_MEDIAN_120 + " real," + COLUMN_SPEED_TWN_120
			+ " real," + COLUMN_SPEED_EIG_120 + " real," + COLUMN_SPEED_ENT_120
			+ " real," + COLUMN_SPEED_SKEW_120 + " real,"
			+ COLUMN_SPEED_IQR_120 + " real," + COLUMN_SPEED_MIN_120 + " real,"
			+ COLUMN_SPEED_MAX_120 + " real," + COLUMN_SPEED_MEAN_SEQ_30
			+ " real," + COLUMN_SPEED_VAR_SEQ_30 + " real,"
			+ COLUMN_SPEED_MEDIAN_SEQ_30 + " real," + COLUMN_SPEED_TWN_SEQ_30
			+ " real," + COLUMN_SPEED_EIG_SEQ_30 + " real,"
			+ COLUMN_SPEED_ENT_SEQ_30 + " real," + COLUMN_SPEED_SKEW_SEQ_30
			+ " real," + COLUMN_SPEED_IQR_SEQ_30 + " real,"
			+ COLUMN_SPEED_MIN_SEQ_30 + " real," + COLUMN_SPEED_MAX_SEQ_30
			+ " real," + COLUMN_SPEED_MEAN_SEQ_120 + " real,"
			+ COLUMN_SPEED_VAR_SEQ_120 + " real," + COLUMN_SPEED_MEDIAN_SEQ_120
			+ " real," + COLUMN_SPEED_TWN_SEQ_120 + " real,"
			+ COLUMN_SPEED_EIG_SEQ_120 + " real," + COLUMN_SPEED_ENT_SEQ_120
			+ " real," + COLUMN_SPEED_SKEW_SEQ_120 + " real,"
			+ COLUMN_SPEED_IQR_SEQ_120 + " real," + COLUMN_SPEED_MIN_SEQ_120
			+ " real," + COLUMN_SPEED_MAX_SEQ_120 + " real);";

	public ModeFeaturesDataSource(Context context) {
		dbHelper = SmartracSQLiteHelper.getInstance(context);
		database = dbHelper.getReadableDatabase();
	}

	private ContentValues getSpeedFeaturesContentValues(Date t,
			SpeedFeatureBuffer speedBuffer30, SpeedFeatureBuffer speedBuffer120) {
		ContentValues values = new ContentValues();

		String time = ISO8601FORMAT.format(t);
		values.put(COLUMN_TIME, time);

		Map<String, Double> speedFeatures30s = speedBuffer30.getFeaturesMap();
		Map<String, Double> speedFeatures120s = speedBuffer120.getFeaturesMap();
		for (String key : speedFeatures30s.keySet()) {
			double value = speedFeatures30s.get(key);
			key = key.replace("\"", "");
			key = key.replace(".", "_");
			// Log.i(TAG, key + ": " + value);
			values.put(key, value);
		}

		for (String key : speedFeatures120s.keySet()) {
			double value = speedFeatures120s.get(key);
			key = key.replace("\"", "");
			key = key.replace(".", "_");
			// Log.i(TAG, key + ": " + value);
			values.put(key, value);
		}

		return values;
	}

	private ContentValues getMotionFeaturesContentValues(Date t,
			MotionFeatureBuffer motionBuffer30,
			MotionFeatureBuffer motionBuffer120) {
		ContentValues values = new ContentValues();

		String time = ISO8601FORMAT.format(t);
		values.put(COLUMN_TIME, time);

		Map<String, Double> motionBuffer30s = motionBuffer30.getFeaturesMap();
		Map<String, Double> motionBuffer120s = motionBuffer120.getFeaturesMap();
		for (String key : motionBuffer30s.keySet()) {
			double value = motionBuffer30s.get(key);
			key = key.replace("\"", "");
			key = key.replace(".", "_");
			// Log.i(TAG, key + ": " + value);
			values.put(key, value);
		}

		for (String key : motionBuffer120s.keySet()) {
			double value = motionBuffer120s.get(key);
			key = key.replace("\"", "");
			key = key.replace(".", "_");
			// Log.i(TAG, key + ": " + value);
			values.put(key, value);
		}

		return values;
	}

	public void writeSpeedBuffer(Date t, SpeedFeatureBuffer speedBuffer30,
			SpeedFeatureBuffer speedBuffer120) {
		Log.i(TAG, "insert speed features");
		database.insert(TABLE_SPEED_FEATURES, null,
				getSpeedFeaturesContentValues(t, speedBuffer30, speedBuffer120));
	}

	public void writeMotionBuffer(Date t, MotionFeatureBuffer motionBuffer30,
			MotionFeatureBuffer motionBuffer120) {
		Log.i(TAG, "insert motion features");
		database.insert(
				TABLE_MOTION_FEATURES,
				null,
				getMotionFeaturesContentValues(t, motionBuffer30,
						motionBuffer120));

	}
}
