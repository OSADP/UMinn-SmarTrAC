package com.smartracumn.smartrac.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Smartrac SQLite helper.
 * 
 * @author kangx385
 * 
 */
public class SmartracSQLiteHelper extends SQLiteOpenHelper {
	public static final String TABLE_LOCATIONS = "table_locations";
	public static final String TABLE_INTERMEDIATE_LOCATIONS = "table_intermediate_locations";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_SPEED = "speed";
	public static final String COLUMN_PROVIDER = "provider";
	public static final String COLUMN_ACCURACY = "accuracy";
	public static final String COLUMN_ALTITUDE = "altitude";
	public static final String COLUMN_BEARING = "bearing";

	public static final String TABLE_DWELLINGS = "table_dwellings";
	public static final String COLUMN_DWELLING_NONDWELLING = "dwelling";
	public static final String COLUMN_ADJUSTMENT = "adjustment";

	public static final String TABLE_MOTIONS = "table_motions";
	public static final String COLUMN_LINEAR_X = "linear_x";
	public static final String COLUMN_LINEAR_Y = "linear_y";
	public static final String COLUMN_LINEAR_Z = "linear_z";
	public static final String COLUMN_LINEAR_MAGNITUDE = "linear_magnitude";
	public static final String COLUMN_TRUE_X = "true_x";
	public static final String COLUMN_TRUE_Y = "true_y";
	public static final String COLUMN_TRUE_Z = "true_z";
	public static final String COLUMN_TRUE_MAGNITUDE = "true_magnitude";

	public static final String TABLE_INSTANT_MOVEMENTS = "table_instant_movements";
	public static final String COLUMN_GPS_SERVICE = "gps_service";

	public static final String TABLE_MODES = "table_modes";
	public static final String COLUMN_MODE = "mode";

	public static final String TABLE_CALENDAR_ITEM_TYPES = "table_calendar_item_types";
	public static final String COLUMN_CALENDAR_ITEM_TYPE = "calendar_item_type";

	public static final String TABLE_ACTIVITY_TYPES = "table_activity_types";
	public static final String COLUMN_ACTIVITY_TYPE = "activity_type";

	public static final String TABLE_TRAVEL_MODES = "table_travel_modes";
	public static final String COLUMN_TRAVEL_MODE = "travel_mode";

	public static final String TABLE_CALENDAR_ITEMS = "table_calendar_items";
	public static final String COLUMN_START_TIME = "start_time";
	public static final String COLUMN_END_TIME = "end_time";
	public static final String COLUMN_CALENDAR_ITEM_TYPE_ID = "calendar_item_type_id";

	public static final String TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS = "table_cal_item_trip_seg_relationships";
	public static final String COLUMN_CALENDAR_ITEM_ID = "calendar_item_id";
	public static final String COLUMN_TRIP_SEGMENT_ID = "trip_segment_id";

	public static final String TABLE_CAL_ITEM_DWELLING_REG_RELATIONSHIPS = "table_cal_item_dwelling_reg_relationships";
	public static final String COLUMN_DWELLING_REGION_ID = "dwelling_region_id";

	public static final String TABLE_TRIP_SEGMENTS = "table_trip_segments";
	public static final String COLUMN_PREDICTED_MODE = "predicted_mode";
	public static final String COLUMN_USER_CORRECTED_MODE = "user_corrected_mode";
	public static final String COLUMN_TRIP_SEGMENT = "trip_segment";
	public static final String COLUMN_TRIP_ID = "trip_id";

	public static final String TABLE_DWELLING_REGIONS = "table_dwelling_regions";
	public static final String COLUMN_PREDICTED_ACTIVITY = "predicted_activity";
	public static final String COLUMN_USER_CORRECTED_ACTIVITY = "user_corrected_activity";
	public static final String COLUMN_DWELLING_REGION = "dwelling_region";

	public static final String TABLE_TRANSFER_REGIONS = "table_transfer_regions";
	public static final String COLUMN_FROM_ITEM_ID = "from_item_id";
	public static final String COLUMN_TO_ITEM_ID = "to_item_id";
	public static final String COLUMN_TRANSFER_REGION = "transfer_region";

	public static final String TABLE_DWELLING_LOCATIONS = "table_dwelling_locations";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_MOSTLIKELY_ACTIVITY_ID = "mostlikely_activity_id";
	public static final String COLUMN_VISIT_FREQ = "visit_frequency";

	public static final String TABLE_DWELLING_SUMMARY = "table_dwelling_summary";
	public static final String COLUMN_DWELLING_LOCATION_ID = "dwelling_location_Id";

	public static final String TABLE_TRIP_SUMMARY = "table_trip_summary";
	public static final String COLUMN_ROUTE_ID = "route_id";

	public static final String TABLE_ROUTES = "table_routes";
	public static final String COLUMN_ROUTE = "route";

	public static final String VIEW_DWELLING_SUMMARY = "view_dwelling_summary";

	public static final String TABLE_TRIP_USER_SUMMARY = "table_trip_user_summary";
	public static final String TABLE_ACTIVITY_USER_SUMMARY = "table_activity_user_summary";

	public static final String COLUMN_HAPPY = "happy";
	public static final String COLUMN_TIRED = "tired";
	public static final String COLUMN_STRESS = "stress";
	public static final String COLUMN_SAD = "sad";
	public static final String COLUMN_PAIN = "pain";
	public static final String COLUMN_MEANINGFUL = "meaningful";

	public static final String COLUMN_WITH_ALONE = "alone";
	public static final String COLUMN_WITH_SPOUSE = "spouse";
	public static final String COLUMN_WITH_CHILDREN = "own_children";
	public static final String COLUMN_WITH_OTHER_FAMILY = "other_family_members";
	public static final String COLUMN_WITH_FRIENDS = "friends_neighbors_acquaintances";
	public static final String COLUMN_WITH_COWORKERS = "coWorkers_customers_peopleFromWork";

	public static final String COLUMN_DISTANCE = "distance";

	public static final String COLUMN_CONFIRMED = "confirmed";
	public static final String COLUMN_CONFIRMED_DATE = "confirmed_date";
	public static final String COLUMN_DESC = "description";

	public static final String DATABASE_NAME = "smartrac.db";
	private static final int DATABASE_VERSION = 39;

	private static final String VIEW_DWELLING_SUMMARY_CREATE = "CREATE VIEW "
			+ VIEW_DWELLING_SUMMARY + " AS SELECT " + "lhs." + COLUMN_ID
			+ " AS " + COLUMN_ID + ", lhs." + COLUMN_LOCATION + " AS "
			+ COLUMN_LOCATION + ", lhs." + COLUMN_MOSTLIKELY_ACTIVITY_ID
			+ " AS " + COLUMN_MOSTLIKELY_ACTIVITY_ID + ", COUNT(*) AS "
			+ COLUMN_VISIT_FREQ + " FROM " + TABLE_DWELLING_LOCATIONS
			+ " AS lhs LEFT JOIN " + TABLE_DWELLING_SUMMARY + " AS rhs ON lhs."
			+ COLUMN_ID + " = rhs." + COLUMN_DWELLING_LOCATION_ID
			+ " GROUP BY lhs." + COLUMN_ID + ";";

	private static final String TRIP_SUMMARY_CREATE = "create table "
			+ TABLE_TRIP_SUMMARY + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TRIP_ID
			+ " integer, " + COLUMN_ROUTE_ID + " integer, foreign key("
			+ COLUMN_TRIP_ID + ") references " + TABLE_TRIP_SEGMENTS + "("
			+ COLUMN_TRIP_ID + "), foreign key(" + COLUMN_ROUTE_ID
			+ ") references " + TABLE_ROUTES + "(" + COLUMN_ID + "));";

	private static final String ROUTE_CREATE = "create table " + TABLE_ROUTES
			+ "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_ROUTE + " text not null);";

	private static final String TRANSFER_REGION_CREATE = "create table "
			+ TABLE_TRANSFER_REGIONS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_FROM_ITEM_ID
			+ " integer, " + COLUMN_TO_ITEM_ID + " integer, "
			+ COLUMN_TRANSFER_REGION + " text not null, foreign key("
			+ COLUMN_FROM_ITEM_ID + ") references "
			+ TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS + "("
			+ COLUMN_CALENDAR_ITEM_ID + "), foreign key (" + COLUMN_TO_ITEM_ID
			+ ") references " + TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS + "("
			+ COLUMN_CALENDAR_ITEM_ID + "));";

	private static final String DWELLING_LOCATION_CREATE = "create table "
			+ TABLE_DWELLING_LOCATIONS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_LOCATION
			+ " text not null, " + COLUMN_MOSTLIKELY_ACTIVITY_ID
			+ " integer, foreign key(" + COLUMN_MOSTLIKELY_ACTIVITY_ID
			+ ") references " + TABLE_ACTIVITY_TYPES + "(" + COLUMN_ID + "));";

	private static final String DWELLING_SUMMARY_CREATE = "create table "
			+ TABLE_DWELLING_SUMMARY + "(" + COLUMN_ID
			+ " integer primary key autoincrement, "
			+ COLUMN_DWELLING_REGION_ID + " integer, "
			+ COLUMN_DWELLING_LOCATION_ID + " integer, foreign key("
			+ COLUMN_DWELLING_REGION_ID + ") references "
			+ TABLE_DWELLING_REGIONS + "(" + COLUMN_ID
			+ ")  ON DELETE CASCADE, foreign key("
			+ COLUMN_DWELLING_LOCATION_ID + ") references "
			+ TABLE_DWELLING_LOCATIONS + "(" + COLUMN_ID
			+ ") ON DELETE CASCADE);";

	private static final String CALENDAR_ITEM_CREATE = "create table "
			+ TABLE_CALENDAR_ITEMS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_START_TIME
			+ " text not null, " + COLUMN_END_TIME + " text not null, "
			+ COLUMN_CALENDAR_ITEM_TYPE_ID + " integer, foreign key("
			+ COLUMN_CALENDAR_ITEM_TYPE_ID + ") references "
			+ TABLE_CALENDAR_ITEM_TYPES + "(" + COLUMN_ID + "));";

	private static final String CAL_ITEM_TRIP_SEG_RELATIONSHIP_CREATE = "create table "
			+ TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS
			+ "("
			+ COLUMN_ID
			+ " integer primary key autoincrement, "
			+ COLUMN_CALENDAR_ITEM_ID
			+ " integer, "
			+ COLUMN_TRIP_SEGMENT_ID
			+ " integer, foreign key("
			+ COLUMN_CALENDAR_ITEM_ID
			+ ") references "
			+ TABLE_CALENDAR_ITEMS
			+ "("
			+ COLUMN_ID
			+ "), foreign key("
			+ COLUMN_TRIP_SEGMENT_ID
			+ ") references " + TABLE_TRIP_SEGMENTS + "(" + COLUMN_ID + "));";

	private static final String CAL_ITEM_DWELLING_REG_RELATIONSHIP_CREATE = "create table "
			+ TABLE_CAL_ITEM_DWELLING_REG_RELATIONSHIPS
			+ "("
			+ COLUMN_ID
			+ " integer primary key autoincrement, "
			+ COLUMN_CALENDAR_ITEM_ID
			+ " integer, "
			+ COLUMN_DWELLING_REGION_ID
			+ " integer, foreign key("
			+ COLUMN_CALENDAR_ITEM_ID
			+ ") references "
			+ TABLE_CALENDAR_ITEMS
			+ "("
			+ COLUMN_ID
			+ "), foreign key("
			+ COLUMN_DWELLING_REGION_ID
			+ ") references "
			+ TABLE_DWELLING_REGIONS + "(" + COLUMN_ID + "));";

	private static final String TRIP_SEGMENT_CREATE = "create table "
			+ TABLE_TRIP_SEGMENTS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TRIP_ID
			+ " integer, " + COLUMN_PREDICTED_MODE + " integer, "
			+ COLUMN_USER_CORRECTED_MODE + " integer, " + COLUMN_TRIP_SEGMENT
			+ " text not null, foreign key(" + COLUMN_PREDICTED_MODE
			+ ") references " + TABLE_TRAVEL_MODES + "(" + COLUMN_ID
			+ "), foreign key(" + COLUMN_USER_CORRECTED_MODE + ") references "
			+ TABLE_TRAVEL_MODES + "(" + COLUMN_ID + "));";

	private static final String DWELLING_REGION_CREATE = "create table "
			+ TABLE_DWELLING_REGIONS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, "
			+ COLUMN_PREDICTED_ACTIVITY + " integer, "
			+ COLUMN_USER_CORRECTED_ACTIVITY + " integer, "
			+ COLUMN_DWELLING_REGION + " text not null, foreign key("
			+ COLUMN_PREDICTED_ACTIVITY + ") references "
			+ TABLE_ACTIVITY_TYPES + "(" + COLUMN_ID + "), foreign key("
			+ COLUMN_USER_CORRECTED_ACTIVITY + ") references "
			+ TABLE_ACTIVITY_TYPES + "(" + COLUMN_ID + "));";

	private static final String CALENDAR_ITEM_TYPE_CREATE = "create table "
			+ TABLE_CALENDAR_ITEM_TYPES + "(" + COLUMN_ID
			+ " integer primary key, " + COLUMN_CALENDAR_ITEM_TYPE
			+ " text not null);";

	private static final String TABLE_TRIP_USER_SUMMARY_CREATE = "create table "
			+ TABLE_TRIP_USER_SUMMARY
			+ "("
			+ COLUMN_ID
			+ " integer primary key autoincrement, "
			+ COLUMN_CALENDAR_ITEM_ID
			+ " integer,"
			+ COLUMN_HAPPY
			+ " integer,"
			+ COLUMN_TIRED
			+ " integer,"
			+ COLUMN_STRESS
			+ " integer,"
			+ COLUMN_SAD
			+ " integer,"
			+ COLUMN_PAIN
			+ " integer,"
			+ COLUMN_MEANINGFUL
			+ " integer,"
			+ COLUMN_DISTANCE
			+ " real,"
			+ COLUMN_CONFIRMED
			+ " integer,"
			+ COLUMN_CONFIRMED_DATE
			+ " text,"
			+ COLUMN_DESC
			+ " text,"
			+ COLUMN_WITH_ALONE
			+ " integer,"
			+ COLUMN_WITH_SPOUSE
			+ " integer,"
			+ COLUMN_WITH_CHILDREN
			+ " integer,"
			+ COLUMN_WITH_OTHER_FAMILY
			+ " integer,"
			+ COLUMN_WITH_FRIENDS
			+ " integer,"
			+ COLUMN_WITH_COWORKERS
			+ " integer, foreign key("
			+ COLUMN_CALENDAR_ITEM_ID
			+ ") references "
			+ TABLE_CALENDAR_ITEMS
			+ "(" + COLUMN_ID + "));";

	private static final String TABLE_ACTIVITY_USER_SUMMARY_CREATE = "create table "
			+ TABLE_ACTIVITY_USER_SUMMARY
			+ "("
			+ COLUMN_ID
			+ " integer primary key autoincrement, "
			+ COLUMN_CALENDAR_ITEM_ID
			+ " integer,"
			+ COLUMN_HAPPY
			+ " integer,"
			+ COLUMN_TIRED
			+ " integer,"
			+ COLUMN_STRESS
			+ " integer,"
			+ COLUMN_SAD
			+ " integer,"
			+ COLUMN_PAIN
			+ " integer,"
			+ COLUMN_MEANINGFUL
			+ " integer,"
			+ COLUMN_CONFIRMED
			+ " integer,"
			+ COLUMN_CONFIRMED_DATE
			+ " text,"
			+ COLUMN_DESC
			+ " text,"
			+ COLUMN_WITH_ALONE
			+ " integer,"
			+ COLUMN_WITH_SPOUSE
			+ " integer,"
			+ COLUMN_WITH_CHILDREN
			+ " integer,"
			+ COLUMN_WITH_OTHER_FAMILY
			+ " integer,"
			+ COLUMN_WITH_FRIENDS
			+ " integer,"
			+ COLUMN_WITH_COWORKERS
			+ " integer,foreign key("
			+ COLUMN_CALENDAR_ITEM_ID
			+ ") references "
			+ TABLE_CALENDAR_ITEMS
			+ "(" + COLUMN_ID + "));";

	private static final String CALENDAR_ITEM_TYPE_DEFAULT = "insert into "
			+ TABLE_CALENDAR_ITEM_TYPES
			+ " values ( 0, \"UNKNOWN_TYPE\"), (1, \"TRIP\"), (2, \"ACTIVITY\"), (3, \"SERVICE_OFF\");";

	private static final String TRAVEL_MODE_CREATE = "create table "
			+ TABLE_TRAVEL_MODES + "(" + COLUMN_ID + " integer primary key, "
			+ COLUMN_TRAVEL_MODE + " text not null);";

	private static final String TRAVEL_MODE_DEFAULT = "insert into "
			+ TABLE_TRAVEL_MODES
			+ " values (0, \"UNKNOWN_TRAVEL_MODE\"), (1, \"CAR\"), (2, \"BUS\"), (3, \"WALKING\"), (4, \"BIKE\"), (5, \"RAIL\"), (6, \"WAIT\");";

	private static final String ACTIVITY_TYPE_CREATE = "create table "
			+ TABLE_ACTIVITY_TYPES + "(" + COLUMN_ID + " integer primary key, "
			+ COLUMN_ACTIVITY_TYPE + " text not null);";

	private static final String ACTIVITY_TYPE_DEFAULT = "insert into "
			+ TABLE_ACTIVITY_TYPES
			+ " values (0, \"UNKNOWN_ACTIVITY\"), (1, \"HOME\"), (2, \"WORK\"),"
			+ " (3, \"EDUCATION\"), (4, \"SHOPPING\"), (5, \"EAT_OUT\"), (6, \"OTHER_PERSONAL_BUSINESS\"), (7, \"SOCIAL_RECREATION_COMMUNITY\");";

	private static final String LOCATION_CREATE = "create table "
			+ TABLE_LOCATIONS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TIME
			+ " text not null, " + COLUMN_LATITUDE + " real, "
			+ COLUMN_LONGITUDE + " real, " + COLUMN_SPEED + " real, "
			+ COLUMN_PROVIDER + " text, " + COLUMN_ACCURACY + " real, "
			+ COLUMN_ALTITUDE + " real, " + COLUMN_BEARING + " real);";

	private static final String INTERMEDIATE_LOCATION_CREATE = "create table "
			+ TABLE_INTERMEDIATE_LOCATIONS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TIME
			+ " text not null, " + COLUMN_LATITUDE + " real, "
			+ COLUMN_LONGITUDE + " real, " + COLUMN_SPEED + " real, "
			+ COLUMN_PROVIDER + " text, " + COLUMN_ACCURACY + " real, "
			+ COLUMN_ALTITUDE + " real, " + COLUMN_BEARING + " real);";

	private static final String DWELLING_CREATE = "CREATE TABLE "
			+ TABLE_DWELLINGS + "(" + COLUMN_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TIME
			+ " TEXT NOT NULL, " + COLUMN_DWELLING_NONDWELLING
			+ " INTEGER NOT NULL, " + COLUMN_ADJUSTMENT + " text );";

	private static final String MOTION_CREATE = "create table " + TABLE_MOTIONS
			+ "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_TIME + " text not null, " + COLUMN_LINEAR_X + " real, "
			+ COLUMN_LINEAR_Y + " real, " + COLUMN_LINEAR_Z + " real, "
			+ COLUMN_LINEAR_MAGNITUDE + " real, " + COLUMN_TRUE_X + " real, "
			+ COLUMN_TRUE_Y + " real, " + COLUMN_TRUE_Z + " real, "
			+ COLUMN_TRUE_MAGNITUDE + " real);";

	private static final String INSTANT_MOVEMENTS_CREATE = "create table "
			+ TABLE_INSTANT_MOVEMENTS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TIME
			+ " text not null, " + COLUMN_GPS_SERVICE + " integer not null);";

	private static final String MODE_CREATE = "CREATE TABLE " + TABLE_MODES
			+ "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_TIME + " TEXT NOT NULL, " + COLUMN_MODE
			+ " INTEGER NOT NULL);";

	private static SmartracSQLiteHelper instance = null;

	/**
	 * Get a instance of the SmartracSQLiteHelper.
	 * 
	 * @param context
	 *            the application context.
	 * @return A singleton instance of smartracSQLiteHelper.
	 */
	public static synchronized SmartracSQLiteHelper getInstance(Context context) {
		if (instance == null) {
			instance = new SmartracSQLiteHelper(context);
		}

		return instance;
	}

	private SmartracSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create location, dwelling, motion tables.
		db.execSQL(INTERMEDIATE_LOCATION_CREATE);
		db.execSQL(LOCATION_CREATE);
		db.execSQL(DWELLING_CREATE);
		db.execSQL(MOTION_CREATE);
		db.execSQL(INSTANT_MOVEMENTS_CREATE);
		db.execSQL(MODE_CREATE);
		db.execSQL(ACTIVITY_TYPE_CREATE);
		db.execSQL(ACTIVITY_TYPE_DEFAULT);
		db.execSQL(TRAVEL_MODE_CREATE);
		db.execSQL(TRAVEL_MODE_DEFAULT);
		db.execSQL(CALENDAR_ITEM_TYPE_CREATE);
		db.execSQL(CALENDAR_ITEM_TYPE_DEFAULT);
		db.execSQL(CALENDAR_ITEM_CREATE);
		db.execSQL(CAL_ITEM_TRIP_SEG_RELATIONSHIP_CREATE);
		db.execSQL(CAL_ITEM_DWELLING_REG_RELATIONSHIP_CREATE);
		db.execSQL(TRIP_SEGMENT_CREATE);
		db.execSQL(DWELLING_REGION_CREATE);
		db.execSQL(TRANSFER_REGION_CREATE);
		db.execSQL(DWELLING_LOCATION_CREATE);
		db.execSQL(DWELLING_SUMMARY_CREATE);
		db.execSQL(ROUTE_CREATE);
		db.execSQL(TRIP_SUMMARY_CREATE);
		db.execSQL(VIEW_DWELLING_SUMMARY_CREATE);
		db.execSQL(TABLE_TRIP_USER_SUMMARY_CREATE);
		db.execSQL(TABLE_ACTIVITY_USER_SUMMARY_CREATE);
		db.execSQL(ModeFeaturesDataSource.TABLE_SPEED_FEATURES_CREATE);
		db.execSQL(ModeFeaturesDataSource.TABLE_MOTION_FEATURES_CREATE);
		db.execSQL(ActivityFeaturesDataSource.TABLE_ACTIVITY_PREDICTION_FEATURES_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Delete all old schema and data, then create new one.
		Log.w(getClass().getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALENDAR_ITEM_TYPES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY_TYPES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRAVEL_MODES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DWELLING_LOCATIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DWELLING_SUMMARY);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP_SUMMARY);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSFER_REGIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_INTERMEDIATE_LOCATIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DWELLINGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_INSTANT_MOVEMENTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOTIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALENDAR_ITEMS);
		db.execSQL("DROP TABLE IF EXISTS "
				+ TABLE_CAL_ITEM_DWELLING_REG_RELATIONSHIPS);
		db.execSQL("DROP TABLE IF EXISTS "
				+ TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY_USER_SUMMARY);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP_USER_SUMMARY);

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP_SEGMENTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DWELLING_REGIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTES);
		db.execSQL("DROP VIEW IF EXISTS " + VIEW_DWELLING_SUMMARY);
		db.execSQL("DROP TABLE IF EXISTS "
				+ ModeFeaturesDataSource.TABLE_SPEED_FEATURES);
		db.execSQL("DROP TABLE IF EXISTS "
				+ ModeFeaturesDataSource.TABLE_MOTION_FEATURES);
		db.execSQL("DROP TABLE IF EXISTS "
				+ ActivityFeaturesDataSource.TABLE_ACTIVITY_PREDICTION_FEATURES);
		onCreate(db);
	}
}
