package com.smartracumn.smartrac.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;
import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.util.MotionFeatureBuffer;
import com.smartracumn.smartrac.util.SpeedFeatureBuffer;

public class ActivityFeaturesDataSource {

	public static final String TABLE_ACTIVITY_PREDICTION_FEATURES="table_activity_prediction_features";
	
	private static final String COLUMN_TIME="time";

	private SmartracSQLiteHelper dbHelper;

	private SQLiteDatabase database;
	
	private final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private static final String COLUMN_LONGITUDE="longitude";
	private static final String COLUMN_LATITUDE="latitude";
	private static final String COLUMN_CALENDAR_ID="calendar_id";

	private static final String COLUMN_ACCOUNTING ="accounting";
	private static final String COLUMN_AIRPORT ="airport";
	private static final String COLUMN_AMUSEMENT ="amusement_park";
	private static final String COLUMN_AQUARIUM ="aquarium";
	private static final String COLUMN_ART ="art_gallery";
	private static final String COLUMN_ATM ="atm";
	private static final String COLUMN_BAKERY ="bakery";
	private static final String COLUMN_BANK ="bank";
	private static final String COLUMN_BAR ="bar";
	private static final String COLUMN_BEAUTY ="beauty_salon";

	private static final String COLUMN_BICYCLE ="bicycle_store";
	private static final String COLUMN_BOOK ="book_store";
	private static final String COLUMN_BOWLING ="bowling_alley";
	private static final String COLUMN_BUS ="bus_station";
	private static final String COLUMN_CAFE ="cafe";
	private static final String COLUMN_CAMPGROUND ="campground";
	private static final String COLUMN_CAR_DEALER ="car_dealer";
	private static final String COLUMN_CAR_RENTAL ="car_rental";
	private static final String COLUMN_CAR_WASH ="car_wash";
	private static final String COLUMN_CAR_REPAIR ="car_repair";

	private static final String COLUMN_CASINO ="casino";
	private static final String COLUMN_CEMETERY ="cemetery";
	private static final String COLUMN_CHURCH ="church";
	private static final String COLUMN_CITY_HALL ="city_hall";
	private static final String COLUMN_CLOTHING_STORE ="clothing_store";
	private static final String COLUMN_CONVENIENCE__STORE ="convenience_store";
	private static final String COLUMN_COURTHOUSE ="courthouse";
	private static final String COLUMN_DENTIST ="dentist";
	private static final String COLUMN_DEPARTMENT_STORE ="department_store";
	private static final String COLUMN_DOCTOR ="doctor";

	private static final String COLUMN_ELECTRICIAN ="electrician";
	private static final String COLUMN_ELECTRONICS_STORE ="electronics_store";

	private static final String COLUMN_EMBASSY ="embassy";
	private static final String COLUMN_ESTABLISHMENT ="establishment";
	private static final String COLUMN_FINANCE ="finance";
	private static final String COLUMN_FIRE_STATION ="fire_station";
	private static final String COLUMN_FLORIST ="florist";
	private static final String COLUMN_FOOD ="food";
	private static final String COLUMN_FUNERAL_HOME ="funeral_home";
	private static final String COLUMN_FURNITURE_STORE ="furniture_store";
	private static final String COLUMN_GAS_STATION ="gas_station";
	private static final String COLUMN_GENERAL_CONTRACTOR ="general_contractor";

	private static final String COLUMN_GROCERY_SUPERMARKET ="grocery_or_supermarket";
	private static final String COLUMN_GYM ="gym";
	private static final String COLUMN_HAIR_CARE ="hair_care";
	private static final String COLUMN_HARDWARE_STORE ="hardware_store";
	private static final String COLUMN_HEALTH ="health";
	private static final String COLUMN_HINDU_TEMPLE ="hindu_temple";
	private static final String COLUMN_HOME_GOODS_STORE ="home_goods_store";
	private static final String COLUMN_HOSPITAL ="hospital";
	private static final String COLUMN_INSURANCE_AGENCY ="insurance_agency";
	private static final String COLUMN_JEWELERY_STORE ="jewelry_store";
	
	private static final String COLUMN_LAUNDRY ="laundry";
	private static final String COLUMN_LAWYER ="lawyer";
	private static final String COLUMN_LIBRARY ="library";
	private static final String COLUMN_LIQUOR_STORE ="liquor_store";
	private static final String COLUMN_LOCAL_GOVERNMENT_OFFICE ="local_government_office";
	private static final String COLUMN_LOCKSMITH ="locksmith";
	private static final String COLUMN_LODGING ="lodging";
	private static final String COLUMN_MEAL_DELIVERY ="meal_delivery";
	private static final String COLUMN_MEAL_TAKEAWAY ="meal_takeaway";
	private static final String COLUMN_MOSQUE ="mosque";

	private static final String COLUMN_MOVIE_RENTAL ="movie_rental";
	private static final String COLUMN_MOVIE_THEATER ="movie_theater";
	
	private static final String COLUMN_MOVING_COMPANY ="moving_company";
	private static final String COLUMN_MUSEUM ="museum";
	private static final String COLUMN_NIGHT_CLUB ="night_club";
	private static final String COLUMN_PAINTER ="painter";
	private static final String COLUMN_PARK ="park";
	private static final String COLUMN_PARKING ="parking";
	private static final String COLUMN_PET_STORE ="pet_store";
	private static final String COLUMN_PHARMACY ="pharmacy";
	private static final String COLUMN_PHYSIOTHERAPIST ="physiotherapist";
	private static final String COLUMN_PLACE_OF_WORSHIP ="place_of_worship";

	private static final String COLUMN_PLUMBER ="plumber";
	private static final String COLUMN_POLICE ="police";
	private static final String COLUMN_POST_OFFICE ="post_office";
	private static final String COLUMN_REAL_ESTATE_AGENCY ="real_estate_agency";
	private static final String COLUMN_RESTAURANT ="restaurant";
	private static final String COLUMN_ROOFING_CONTRACTOR ="roofing_contractor";
	private static final String COLUMN_RV_PARK ="rv_park";
	private static final String COLUMN_SCHOOL ="school";
	private static final String COLUMN_SHOE_STORE ="shoe_store";
	private static final String COLUMN_SHOPPING_MALL ="shopping_mall";

	private static final String COLUMN_SPA ="spa";
	private static final String COLUMN_STADIUM ="stadium";
	private static final String COLUMN_STORAGE ="storage";
	private static final String COLUMN_STORE ="store";
	private static final String COLUMN_SUBWAY_STATION ="subway_station";
	private static final String COLUMN_SYNAGOGUE ="synagogue";
	private static final String COLUMN_TAXI_STAND ="taxi_stand";
	private static final String COLUMN_TRAIN_STATION ="train_station";
	private static final String COLUMN_TRAVEL_AGENCY ="travel_agency";
	private static final String COLUMN_UNIVERSITY ="university";

	private static final String COLUMN_VETERINARY_CARE ="veterinary_care";
	private static final String COLUMN_ZOO ="zoo";

	private static final String COLUMN_NEAREST_ACCOUNTING ="nearesttag_accounting";
	private static final String COLUMN_NEAREST_AIRPORT ="nearesttag_airport";
	private static final String COLUMN_NEAREST_AMUSEMENT ="nearesttag_amusement_park";
	private static final String COLUMN_NEAREST_AQUARIUM ="nearesttag_aquarium";
	private static final String COLUMN_NEAREST_ART ="nearesttag_art_gallery";
	private static final String COLUMN_NEAREST_ATM ="nearesttag_atm";
	private static final String COLUMN_NEAREST_BAKERY ="nearesttag_bakery";
	private static final String COLUMN_NEAREST_BANK ="nearesttag_bank";
	private static final String COLUMN_NEAREST_BAR ="nearesttag_bar";
	private static final String COLUMN_NEAREST_BEAUTY ="nearesttag_beauty_salon";

	private static final String COLUMN_NEAREST_BICYCLE ="nearesttag_bicycle_store";
	private static final String COLUMN_NEAREST_BOOK ="nearesttag_book_store";
	private static final String COLUMN_NEAREST_BOWLING ="nearesttag_bowling_alley";
	private static final String COLUMN_NEAREST_BUS ="nearesttag_bus_station";
	private static final String COLUMN_NEAREST_CAFE ="nearesttag_cafe";
	private static final String COLUMN_NEAREST_CAMPGROUND ="nearesttag_campground";
	private static final String COLUMN_NEAREST_CAR_DEALER ="nearesttag_car_dealer";
	private static final String COLUMN_NEAREST_CAR_RENTAL ="nearesttag_car_rental";
	private static final String COLUMN_NEAREST_CAR_WASH ="nearesttag_car_wash";
	private static final String COLUMN_NEAREST_CAR_REPAIR ="nearesttag_car_repair";

	private static final String COLUMN_NEAREST_CASINO ="nearesttag_casino";
	private static final String COLUMN_NEAREST_CEMETERY ="nearesttag_cemetery";
	private static final String COLUMN_NEAREST_CHURCH ="nearesttag_church";
	private static final String COLUMN_NEAREST_CITY_HALL ="nearesttag_city_hall";
	private static final String COLUMN_NEAREST_CLOTHING_STORE ="nearesttag_clothing_store";
	private static final String COLUMN_NEAREST_CONVENIENCE__STORE ="nearesttag_convenience_store";
	private static final String COLUMN_NEAREST_COURTHOUSE ="nearesttag_courthouse";
	private static final String COLUMN_NEAREST_DENTIST ="nearesttag_dentist";
	private static final String COLUMN_NEAREST_DEPARTMENT_STORE ="nearesttag_department_store";
	private static final String COLUMN_NEAREST_DOCTOR ="nearesttag_doctor";

	private static final String COLUMN_NEAREST_ELECTRICIAN ="nearesttag_electrician";
	private static final String COLUMN_NEAREST_ELECTRONICS_STORE ="nearesttag_electronics_store";

	private static final String COLUMN_NEAREST_EMBASSY ="nearesttag_embassy";
	private static final String COLUMN_NEAREST_ESTABLISHMENT ="nearesttag_establishment";
	private static final String COLUMN_NEAREST_FINANCE ="nearesttag_finance";
	private static final String COLUMN_NEAREST_FIRE_STATION ="nearesttag_fire_station";
	private static final String COLUMN_NEAREST_FLORIST ="nearesttag_florist";
	private static final String COLUMN_NEAREST_FOOD ="nearesttag_food";
	private static final String COLUMN_NEAREST_FUNERAL_HOME ="nearesttag_funeral_home";
	private static final String COLUMN_NEAREST_FURNITURE_STORE ="nearesttag_furniture_store";
	private static final String COLUMN_NEAREST_GAS_STATION ="nearesttag_gas_station";
	private static final String COLUMN_NEAREST_GENERAL_CONTRACTOR ="nearesttag_general_contractor";

	private static final String COLUMN_NEAREST_GROCERY_SUPERMARKET ="nearesttag_grocery_or_supermarket";
	private static final String COLUMN_NEAREST_GYM ="nearesttag_gym";
	private static final String COLUMN_NEAREST_HAIR_CARE ="nearesttag_hair_care";
	private static final String COLUMN_NEAREST_HARDWARE_STORE ="nearesttag_hardware_store";
	private static final String COLUMN_NEAREST_HEALTH ="nearesttag_health";
	private static final String COLUMN_NEAREST_HINDU_TEMPLE ="nearesttag_hindu_temple";
	private static final String COLUMN_NEAREST_HOME_GOODS_STORE ="nearesttag_home_goods_store";
	private static final String COLUMN_NEAREST_HOSPITAL ="nearesttag_hospital";
	private static final String COLUMN_NEAREST_INSURANCE_AGENCY ="nearesttag_insurance_agency";
	private static final String COLUMN_NEAREST_JEWELERY_STORE ="nearesttag_jewelry_store";
	
	private static final String COLUMN_NEAREST_LAUNDRY ="nearesttag_laundry";
	private static final String COLUMN_NEAREST_LAWYER ="nearesttag_lawyer";
	private static final String COLUMN_NEAREST_LIBRARY ="nearesttag_library";
	private static final String COLUMN_NEAREST_LIQUOR_STORE ="nearesttag_liquor_store";
	private static final String COLUMN_NEAREST_LOCAL_GOVERNMENT_OFFICE ="nearesttag_local_government_office";
	private static final String COLUMN_NEAREST_LOCKSMITH ="nearesttag_locksmith";
	private static final String COLUMN_NEAREST_LODGING ="nearesttag_lodging";
	private static final String COLUMN_NEAREST_MEAL_DELIVERY ="nearesttag_meal_delivery";
	private static final String COLUMN_NEAREST_MEAL_TAKEAWAY ="nearesttag_meal_takeaway";
	private static final String COLUMN_NEAREST_MOSQUE ="nearesttag_mosque";

	private static final String COLUMN_NEAREST_MOVIE_RENTAL ="nearesttag_movie_rental";
	private static final String COLUMN_NEAREST_MOVIE_THEATER ="nearesttag_movie_theater";
	
	private static final String COLUMN_NEAREST_MOVING_COMPANY ="nearesttag_moving_company";
	private static final String COLUMN_NEAREST_MUSEUM ="nearesttag_museum";
	private static final String COLUMN_NEAREST_NIGHT_CLUB ="nearesttag_night_club";
	private static final String COLUMN_NEAREST_PAINTER ="nearesttag_painter";
	private static final String COLUMN_NEAREST_PARK ="nearesttag_park";
	private static final String COLUMN_NEAREST_PARKING ="nearesttag_parking";
	private static final String COLUMN_NEAREST_PET_STORE ="nearesttag_pet_store";
	private static final String COLUMN_NEAREST_PHARMACY ="nearesttag_pharmacy";
	private static final String COLUMN_NEAREST_PHYSIOTHERAPIST ="nearesttag_physiotherapist";
	private static final String COLUMN_NEAREST_PLACE_OF_WORSHIP ="nearesttag_place_of_worship";

	private static final String COLUMN_NEAREST_PLUMBER ="nearesttag_plumber";
	private static final String COLUMN_NEAREST_POLICE ="nearesttag_police";
	private static final String COLUMN_NEAREST_POST_OFFICE ="nearesttag_post_office";
	private static final String COLUMN_NEAREST_REAL_ESTATE_AGENCY ="nearesttag_real_estate_agency";
	private static final String COLUMN_NEAREST_RESTAURANT ="nearesttag_restaurant";
	private static final String COLUMN_NEAREST_ROOFING_CONTRACTOR ="nearesttag_roofing_contractor";
	private static final String COLUMN_NEAREST_RV_PARK ="nearesttag_rv_park";
	private static final String COLUMN_NEAREST_SCHOOL ="nearesttag_school";
	private static final String COLUMN_NEAREST_SHOE_STORE ="nearesttag_shoe_store";
	private static final String COLUMN_NEAREST_SHOPPING_MALL ="nearesttag_shopping_mall";

	private static final String COLUMN_NEAREST_SPA ="nearesttag_spa";
	private static final String COLUMN_NEAREST_STADIUM ="nearesttag_stadium";
	private static final String COLUMN_NEAREST_STORAGE ="nearesttag_storage";
	private static final String COLUMN_NEAREST_STORE ="nearesttag_store";
	private static final String COLUMN_NEAREST_SUBWAY_STATION ="nearesttag_subway_station";
	private static final String COLUMN_NEAREST_SYNAGOGUE ="nearesttag_synagogue";
	private static final String COLUMN_NEAREST_TAXI_STAND ="nearesttag_taxi_stand";
	private static final String COLUMN_NEAREST_TRAIN_STATION ="nearesttag_train_station";
	private static final String COLUMN_NEAREST_TRAVEL_AGENCY ="nearesttag_travel_agency";
	private static final String COLUMN_NEAREST_UNIVERSITY ="nearesttag_university";

	private static final String COLUMN_NEAREST_VETERINARY_CARE ="nearesttag_veterinary_care";
	private static final String COLUMN_NEAREST_ZOO ="nearesttag_zoo";

	private static final String COLUMN_MONDAY="Monday";
	private static final String COLUMN_TUESDAY="Tuesday";
	private static final String COLUMN_WEDNESDAY="Wednesday";
	private static final String COLUMN_THURSDAY="Thursday";
	private static final String COLUMN_FRIDAY="Friday";
	private static final String COLUMN_HOLIDAY="holiday";

	private static final String COLUMN_WALK="Walk";
	private static final String COLUMN_BUS_MODE="Bus";
	private static final String COLUMN_RAIL="Rail";
	private static final String COLUMN_BIKE="Bike";
	private static final String COLUMN_CAR="Car";

	private static final String COLUMN_ACTIVITY_DURATION="actdur";
	private static final String COLUMN_ARRIVAL_TIME="arrival_time";
	private static final String COLUMN_AIRLINE_DISTANCE="airline_dist";
	private static final String COLUMN_PREV_PURPOSE_HOME="prev_purpose_home";
	private static final String COLUMN_PREV_PURPOSE_WORK="prev_purpose_work";

	
	public final static String TABLE_ACTIVITY_PREDICTION_FEATURES_CREATE = "create table "
			+ TABLE_ACTIVITY_PREDICTION_FEATURES + "(" + SmartracSQLiteHelper.COLUMN_ID
			+ " integer primary key autoincrement, "
			+COLUMN_TIME+" text,"
			+COLUMN_CALENDAR_ID+" text,"
			+COLUMN_LATITUDE+" text,"
			+ COLUMN_LONGITUDE+ " text, "
			
			+ COLUMN_ACCOUNTING +" real,"
			+ COLUMN_AIRPORT +" real,"
			+COLUMN_AMUSEMENT +" real,"
			 +COLUMN_AQUARIUM +" real,"
			+ COLUMN_ART +" real,"
			 +COLUMN_ATM +" real,"
			 +COLUMN_BAKERY +" real,"
			+ COLUMN_BANK +" real,"
			 +COLUMN_BAR +" real,"
			 +COLUMN_BEAUTY +" real,"
			 
			 +	 COLUMN_BICYCLE +" real,"
				+ COLUMN_BOOK +" real,"
				 +COLUMN_BOWLING+" real,"
				 +COLUMN_BUS +" real,"
				 +COLUMN_CAFE +" real,"
				 +COLUMN_CAMPGROUND +" real,"
				 +COLUMN_CAR_DEALER +" real,"
				 +COLUMN_CAR_RENTAL +" real,"
				 +COLUMN_CAR_WASH +" real,"
				 +COLUMN_CAR_REPAIR +" real,"
			+
				 
			 COLUMN_CASINO +" real,"
			 +COLUMN_CEMETERY +" real,"
			 +COLUMN_CHURCH +" real,"
			 +COLUMN_CITY_HALL +" real,"
			 +COLUMN_CLOTHING_STORE +" real,"
			 +COLUMN_CONVENIENCE__STORE +" real,"
			 +COLUMN_COURTHOUSE+" real,"
			 +COLUMN_DENTIST +" real,"
			 +COLUMN_DEPARTMENT_STORE +" real,"
			 +COLUMN_DOCTOR +" real,"

			 +COLUMN_ELECTRICIAN +" real,"
			 +COLUMN_ELECTRONICS_STORE +" real,"
			 
			 +
			 
				 COLUMN_EMBASSY +" real,"
				 +COLUMN_ESTABLISHMENT +" real,"
				 +COLUMN_FINANCE +" real,"
				 +COLUMN_FIRE_STATION +" real,"
				 +COLUMN_FLORIST +" real,"
				 +COLUMN_FOOD +" real,"
				 +COLUMN_FUNERAL_HOME +" real,"
				 +COLUMN_FURNITURE_STORE +" real,"
				 +COLUMN_GAS_STATION +" real,"
				 +COLUMN_GENERAL_CONTRACTOR +" real,"

				 +COLUMN_GROCERY_SUPERMARKET +" real,"
				 +COLUMN_GYM +" real,"
				 +COLUMN_HAIR_CARE +" real,"
				 +COLUMN_HARDWARE_STORE +" real,"
				 +COLUMN_HEALTH +" real,"
				 +COLUMN_HINDU_TEMPLE +" real,"
				 +COLUMN_HOME_GOODS_STORE +" real,"
				 +COLUMN_HOSPITAL +" real,"
				 +COLUMN_INSURANCE_AGENCY +" real,"
				 +COLUMN_JEWELERY_STORE +" real,"
				
				 +COLUMN_LAUNDRY +" real,"
				 +COLUMN_LAWYER +" real,"
				 +COLUMN_LIBRARY +" real,"
				 +COLUMN_LIQUOR_STORE +" real,"
				 +COLUMN_LOCAL_GOVERNMENT_OFFICE +" real,"
				 +COLUMN_LOCKSMITH +" real,"
				 +COLUMN_LODGING +" real,"
				 +COLUMN_MEAL_DELIVERY +" real,"
				 +COLUMN_MEAL_TAKEAWAY +" real,"
				 +COLUMN_MOSQUE +" real,"

				 +COLUMN_MOVIE_RENTAL +" real,"
				 +COLUMN_MOVIE_THEATER +" real,"

	+COLUMN_MOVING_COMPANY +" real,"
	 +COLUMN_MUSEUM +" real,"
	 +COLUMN_NIGHT_CLUB +" real,"
	 +COLUMN_PAINTER +" real,"
	 +COLUMN_PARK +" real,"
	 +COLUMN_PARKING +" real,"
	 +COLUMN_PET_STORE +" real,"
	 +COLUMN_PHARMACY +" real,"
	 +COLUMN_PHYSIOTHERAPIST +" real,"
	 +COLUMN_PLACE_OF_WORSHIP +" real,"

	+ COLUMN_PLUMBER +" real,"
	+ COLUMN_POLICE +" real,"
	+ COLUMN_POST_OFFICE +" real,"
	+ COLUMN_REAL_ESTATE_AGENCY +" real,"
	+ COLUMN_RESTAURANT +" real,"
	+ COLUMN_ROOFING_CONTRACTOR +" real,"
	+ COLUMN_RV_PARK +" real,"
	+ COLUMN_SCHOOL +" real,"
	+ COLUMN_SHOE_STORE +" real,"
	+ COLUMN_SHOPPING_MALL +" real,"

	+ COLUMN_SPA +" real,"
	+ COLUMN_STADIUM +" real,"
	+ COLUMN_STORAGE +" real,"
	+ COLUMN_STORE +" real,"
	+ COLUMN_SUBWAY_STATION+" real,"
	+ COLUMN_SYNAGOGUE +" real,"
	+ COLUMN_TAXI_STAND +" real,"
	+ COLUMN_TRAIN_STATION +" real,"
	+ COLUMN_TRAVEL_AGENCY +" real,"
	+ COLUMN_UNIVERSITY +" real,"

	+ COLUMN_VETERINARY_CARE +" real,"
	+ COLUMN_ZOO +" real,"


				+ COLUMN_NEAREST_ACCOUNTING +" real,"
			+ COLUMN_NEAREST_AIRPORT +" real,"
			+COLUMN_NEAREST_AMUSEMENT +" real,"
			 +COLUMN_NEAREST_AQUARIUM +" real,"
			+ COLUMN_NEAREST_ART +" real,"
			 +COLUMN_NEAREST_ATM +" real,"
			 +COLUMN_NEAREST_BAKERY +" real,"
			+ COLUMN_NEAREST_BANK +" real,"
			 +COLUMN_NEAREST_BAR +" real,"
			 +COLUMN_NEAREST_BEAUTY +" real,"
			 
			 +	 COLUMN_NEAREST_BICYCLE +" real,"
				+ COLUMN_NEAREST_BOOK +" real,"
				 +COLUMN_NEAREST_BOWLING+" real,"
				 +COLUMN_NEAREST_BUS +" real,"
				 +COLUMN_NEAREST_CAFE +" real,"
				 +COLUMN_NEAREST_CAMPGROUND +" real,"
				 +COLUMN_NEAREST_CAR_DEALER +" real,"
				 +COLUMN_NEAREST_CAR_RENTAL +" real,"
				 +COLUMN_NEAREST_CAR_WASH +" real,"
				 +COLUMN_NEAREST_CAR_REPAIR +" real,"
			+
				 
			 COLUMN_NEAREST_CASINO +" real,"
			 +COLUMN_NEAREST_CEMETERY +" real,"
			 +COLUMN_NEAREST_CHURCH +" real,"
			 +COLUMN_NEAREST_CITY_HALL +" real,"
			 +COLUMN_NEAREST_CLOTHING_STORE +" real,"
			 +COLUMN_NEAREST_CONVENIENCE__STORE +" real,"
			 +COLUMN_NEAREST_COURTHOUSE+" real,"
			 +COLUMN_NEAREST_DENTIST +" real,"
			 +COLUMN_NEAREST_DEPARTMENT_STORE +" real,"
			 +COLUMN_NEAREST_DOCTOR +" real,"

			 +COLUMN_NEAREST_ELECTRICIAN +" real,"
			 +COLUMN_NEAREST_ELECTRONICS_STORE +" real,"
			 
			 +
			 
				 COLUMN_NEAREST_EMBASSY +" real,"
				 +COLUMN_NEAREST_ESTABLISHMENT +" real,"
				 +COLUMN_NEAREST_FINANCE +" real,"
				 +COLUMN_NEAREST_FIRE_STATION +" real,"
				 +COLUMN_NEAREST_FLORIST +" real,"
				 +COLUMN_NEAREST_FOOD +" real,"
				 +COLUMN_NEAREST_FUNERAL_HOME +" real,"
				 +COLUMN_NEAREST_FURNITURE_STORE +" real,"
				 +COLUMN_NEAREST_GAS_STATION +" real,"
				 +COLUMN_NEAREST_GENERAL_CONTRACTOR +" real,"

				 +COLUMN_NEAREST_GROCERY_SUPERMARKET +" real,"
				 +COLUMN_NEAREST_GYM +" real,"
				 +COLUMN_NEAREST_HAIR_CARE +" real,"
				 +COLUMN_NEAREST_HARDWARE_STORE +" real,"
				 +COLUMN_NEAREST_HEALTH +" real,"
				 +COLUMN_NEAREST_HINDU_TEMPLE +" real,"
				 +COLUMN_NEAREST_HOME_GOODS_STORE +" real,"
				 +COLUMN_NEAREST_HOSPITAL +" real,"
				 +COLUMN_NEAREST_INSURANCE_AGENCY +" real,"
				 +COLUMN_NEAREST_JEWELERY_STORE +" real,"
				
				 +COLUMN_NEAREST_LAUNDRY +" real,"
				 +COLUMN_NEAREST_LAWYER +" real,"
				 +COLUMN_NEAREST_LIBRARY +" real,"
				 +COLUMN_NEAREST_LIQUOR_STORE +" real,"
				 +COLUMN_NEAREST_LOCAL_GOVERNMENT_OFFICE +" real,"
				 +COLUMN_NEAREST_LOCKSMITH +" real,"
				 +COLUMN_NEAREST_LODGING +" real,"
				 +COLUMN_NEAREST_MEAL_DELIVERY +" real,"
				 +COLUMN_NEAREST_MEAL_TAKEAWAY +" real,"
				 +COLUMN_NEAREST_MOSQUE +" real,"

				 +COLUMN_NEAREST_MOVIE_RENTAL +" real,"
				 +COLUMN_NEAREST_MOVIE_THEATER +" real,"

				 	+COLUMN_NEAREST_MOVING_COMPANY +" real,"
	 +COLUMN_NEAREST_MUSEUM +" real,"
	 +COLUMN_NEAREST_NIGHT_CLUB +" real,"
	 +COLUMN_NEAREST_PAINTER +" real,"
	 +COLUMN_NEAREST_PARK +" real,"
	 +COLUMN_NEAREST_PARKING +" real,"
	 +COLUMN_NEAREST_PET_STORE +" real,"
	 +COLUMN_NEAREST_PHARMACY +" real,"
	 +COLUMN_NEAREST_PHYSIOTHERAPIST +" real,"
	 +COLUMN_NEAREST_PLACE_OF_WORSHIP +" real,"

	+ COLUMN_NEAREST_PLUMBER +" real,"
	+ COLUMN_NEAREST_POLICE +" real,"
	+ COLUMN_NEAREST_POST_OFFICE +" real,"
	+ COLUMN_NEAREST_REAL_ESTATE_AGENCY +" real,"
	+ COLUMN_NEAREST_RESTAURANT +" real,"
	+ COLUMN_NEAREST_ROOFING_CONTRACTOR +" real,"
	+ COLUMN_NEAREST_RV_PARK +" real,"
	+ COLUMN_NEAREST_SCHOOL +" real,"
	+ COLUMN_NEAREST_SHOE_STORE +" real,"
	+ COLUMN_NEAREST_SHOPPING_MALL +" real,"

	+ COLUMN_NEAREST_SPA +" real,"
	+ COLUMN_NEAREST_STADIUM +" real,"
	+ COLUMN_NEAREST_STORAGE +" real,"
	+ COLUMN_NEAREST_STORE +" real,"
	+ COLUMN_NEAREST_SUBWAY_STATION+" real,"
	+ COLUMN_NEAREST_SYNAGOGUE +" real,"
	+ COLUMN_NEAREST_TAXI_STAND +" real,"
	+ COLUMN_NEAREST_TRAIN_STATION +" real,"
	+ COLUMN_NEAREST_TRAVEL_AGENCY +" real,"
	+ COLUMN_NEAREST_UNIVERSITY +" real,"

	+ COLUMN_NEAREST_VETERINARY_CARE +" real,"
	+ COLUMN_NEAREST_ZOO +" real,"

	
	+ COLUMN_MONDAY+" text,"
	+ COLUMN_TUESDAY+" text,"
	+ COLUMN_WEDNESDAY+" text,"
	+ COLUMN_THURSDAY+" text,"
	+ COLUMN_FRIDAY+" text,"
	+ COLUMN_HOLIDAY+" text,"

	+ COLUMN_WALK+" text,"
	+ COLUMN_BUS_MODE+" text,"
	+ COLUMN_RAIL+" text,"
	+ COLUMN_BIKE+" text,"
	+ COLUMN_CAR+" text,"

	+ COLUMN_ACTIVITY_DURATION+" text,"
	+ COLUMN_ARRIVAL_TIME+" text,"
	+ COLUMN_AIRLINE_DISTANCE+" text,"
	+ COLUMN_PREV_PURPOSE_HOME+" text,"
	+ COLUMN_PREV_PURPOSE_WORK+" text);";



	public ActivityFeaturesDataSource(Context context) {
		dbHelper = SmartracSQLiteHelper.getInstance(context);
		database = dbHelper.getReadableDatabase();
	}


	public void writeActivityPredictionDB(Map<String, Double> features, ActivityCalendarItem item) {
		ContentValues googlePlacesValues = getGooglePlacesContentValues(features);
		ContentValues holidayValues = getHolidayContentValues(features);
		ContentValues dbFeatures = getDBFeatures(features);
		ContentValues itemValues = getItemContentValues(item);
		
		ContentValues values = new ContentValues();
		values.putAll(googlePlacesValues);
		values.putAll(holidayValues);
		values.putAll(dbFeatures);
		values.putAll(itemValues);

		database.insert(TABLE_ACTIVITY_PREDICTION_FEATURES, null, values);
	}


	private ContentValues getItemContentValues(ActivityCalendarItem item) {

		LatLng center = item.getWeightedCenter();
		double lat = center.latitude;
		double lng = center.longitude;

		Calendar c = Calendar.getInstance(); 
		String time = ISO8601FORMAT.format(c.getTime());

		ContentValues values = new ContentValues();
		values.put(COLUMN_LATITUDE, lat);
		values.put(COLUMN_LONGITUDE, lng);
		values.put(COLUMN_CALENDAR_ID, item.getId());
		values.put(COLUMN_TIME, time);
	
		return values;
	}


	private ContentValues getDBFeatures(Map<String, Double> features) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
		
		values.put(COLUMN_WALK, features.get(COLUMN_WALK));
		values.put(COLUMN_BUS_MODE, features.get(COLUMN_BUS_MODE));
		values.put(COLUMN_RAIL, features.get(COLUMN_RAIL));
		values.put(COLUMN_BIKE, features.get(COLUMN_BIKE));
		values.put(COLUMN_CAR, features.get(COLUMN_CAR));
		
		values.put(COLUMN_ACTIVITY_DURATION, features.get(COLUMN_ACTIVITY_DURATION));
		values.put(COLUMN_ARRIVAL_TIME, features.get(COLUMN_ARRIVAL_TIME));
		values.put(COLUMN_AIRLINE_DISTANCE, features.get(COLUMN_AIRLINE_DISTANCE));
		values.put(COLUMN_PREV_PURPOSE_HOME, features.get(COLUMN_PREV_PURPOSE_HOME));
		values.put(COLUMN_PREV_PURPOSE_WORK, features.get(COLUMN_PREV_PURPOSE_WORK));

		return values;
	}


	private ContentValues getHolidayContentValues(Map<String, Double> features) {
		ContentValues values = new ContentValues();
		
		values.put(COLUMN_MONDAY, features.get(COLUMN_MONDAY));
		values.put(COLUMN_TUESDAY, features.get(COLUMN_TUESDAY));
		values.put(COLUMN_WEDNESDAY, features.get(COLUMN_WEDNESDAY));
		values.put(COLUMN_THURSDAY, features.get(COLUMN_THURSDAY));
		values.put(COLUMN_FRIDAY, features.get(COLUMN_FRIDAY));
		values.put(COLUMN_HOLIDAY, features.get(COLUMN_HOLIDAY));

		return values;
	}


	private ContentValues getGooglePlacesContentValues(
			Map<String, Double> nearbyPlacesFeatures) {
		ContentValues values = new ContentValues();
		
		values.put( COLUMN_ACCOUNTING,nearbyPlacesFeatures.get(COLUMN_ACCOUNTING));
		values.put( COLUMN_AIRPORT ,nearbyPlacesFeatures.get(COLUMN_AIRPORT));
		values.put( COLUMN_AMUSEMENT ,nearbyPlacesFeatures.get(COLUMN_AMUSEMENT));
		values.put( COLUMN_AQUARIUM ,nearbyPlacesFeatures.get(COLUMN_AQUARIUM));
		values.put( COLUMN_ART ,nearbyPlacesFeatures.get(COLUMN_ART));
		values.put( COLUMN_ATM ,nearbyPlacesFeatures.get(COLUMN_ATM));
		values.put( COLUMN_BAKERY ,nearbyPlacesFeatures.get(COLUMN_BAKERY));
		values.put( COLUMN_BANK ,nearbyPlacesFeatures.get(COLUMN_BANK));
		values.put( COLUMN_BAR ,nearbyPlacesFeatures.get(COLUMN_BAR));
		values.put( COLUMN_BEAUTY ,nearbyPlacesFeatures.get(COLUMN_BEAUTY));

		values.put( COLUMN_BICYCLE ,nearbyPlacesFeatures.get(COLUMN_BICYCLE));
		values.put( COLUMN_BOOK ,nearbyPlacesFeatures.get(COLUMN_BOOK));
		values.put( COLUMN_BOWLING ,nearbyPlacesFeatures.get(COLUMN_BOWLING));
		values.put( COLUMN_BUS ,nearbyPlacesFeatures.get(COLUMN_BUS));
		values.put( COLUMN_CAFE ,nearbyPlacesFeatures.get(COLUMN_CAFE));
		values.put( COLUMN_CAMPGROUND ,nearbyPlacesFeatures.get(COLUMN_CAMPGROUND));
		values.put( COLUMN_CAR_DEALER ,nearbyPlacesFeatures.get(COLUMN_CAR_DEALER));
		values.put( COLUMN_CAR_RENTAL ,nearbyPlacesFeatures.get(COLUMN_CAR_RENTAL));
		values.put( COLUMN_CAR_WASH, nearbyPlacesFeatures.get(COLUMN_CAR_WASH));
		values.put( COLUMN_CAR_REPAIR ,nearbyPlacesFeatures.get(COLUMN_CAR_REPAIR));

		values.put( COLUMN_CASINO ,nearbyPlacesFeatures.get(COLUMN_CASINO));
		values.put( COLUMN_CEMETERY ,nearbyPlacesFeatures.get(COLUMN_CEMETERY));
		values.put( COLUMN_CHURCH ,nearbyPlacesFeatures.get(COLUMN_CHURCH));
		values.put( COLUMN_CITY_HALL ,nearbyPlacesFeatures.get(COLUMN_CITY_HALL));
		values.put( COLUMN_CLOTHING_STORE ,nearbyPlacesFeatures.get(COLUMN_CLOTHING_STORE));
		values.put( COLUMN_CONVENIENCE__STORE ,nearbyPlacesFeatures.get(COLUMN_CONVENIENCE__STORE));
		values.put( COLUMN_COURTHOUSE ,nearbyPlacesFeatures.get(COLUMN_COURTHOUSE));
		values.put( COLUMN_DENTIST ,nearbyPlacesFeatures.get(COLUMN_DENTIST));
		values.put( COLUMN_DEPARTMENT_STORE ,nearbyPlacesFeatures.get(COLUMN_DEPARTMENT_STORE));
		values.put( COLUMN_DOCTOR ,nearbyPlacesFeatures.get(COLUMN_DOCTOR));

		values.put( COLUMN_ELECTRICIAN ,nearbyPlacesFeatures.get(COLUMN_ELECTRICIAN));
		values.put( COLUMN_ELECTRONICS_STORE ,nearbyPlacesFeatures.get(COLUMN_ELECTRONICS_STORE));

		values.put( COLUMN_EMBASSY ,nearbyPlacesFeatures.get(COLUMN_EMBASSY));
		values.put( COLUMN_ESTABLISHMENT ,nearbyPlacesFeatures.get(COLUMN_ESTABLISHMENT));
		values.put( COLUMN_FINANCE ,nearbyPlacesFeatures.get(COLUMN_FINANCE));
		values.put( COLUMN_FIRE_STATION ,nearbyPlacesFeatures.get(COLUMN_FIRE_STATION));
		values.put( COLUMN_FLORIST ,nearbyPlacesFeatures.get(COLUMN_FLORIST));
		values.put( COLUMN_FOOD ,nearbyPlacesFeatures.get(COLUMN_FOOD));
		values.put( COLUMN_FUNERAL_HOME ,nearbyPlacesFeatures.get(COLUMN_FUNERAL_HOME));
		values.put( COLUMN_FURNITURE_STORE ,nearbyPlacesFeatures.get(COLUMN_FURNITURE_STORE));
		values.put( COLUMN_GAS_STATION ,nearbyPlacesFeatures.get(COLUMN_GAS_STATION));
		values.put( COLUMN_GENERAL_CONTRACTOR ,nearbyPlacesFeatures.get(COLUMN_GENERAL_CONTRACTOR));

		values.put( COLUMN_GROCERY_SUPERMARKET ,nearbyPlacesFeatures.get(COLUMN_GROCERY_SUPERMARKET));
		values.put( COLUMN_GYM ,nearbyPlacesFeatures.get(COLUMN_GYM));
		values.put( COLUMN_HAIR_CARE ,nearbyPlacesFeatures.get(COLUMN_HAIR_CARE));
		values.put( COLUMN_HARDWARE_STORE ,nearbyPlacesFeatures.get(COLUMN_HARDWARE_STORE));
		values.put( COLUMN_HEALTH ,nearbyPlacesFeatures.get(COLUMN_HEALTH));
		values.put( COLUMN_HINDU_TEMPLE ,nearbyPlacesFeatures.get(COLUMN_HINDU_TEMPLE));
		values.put( COLUMN_HOME_GOODS_STORE ,nearbyPlacesFeatures.get(COLUMN_HOME_GOODS_STORE));
		values.put( COLUMN_HOSPITAL ,nearbyPlacesFeatures.get(COLUMN_HOSPITAL));
		values.put( COLUMN_INSURANCE_AGENCY ,nearbyPlacesFeatures.get(COLUMN_INSURANCE_AGENCY));
		values.put( COLUMN_JEWELERY_STORE ,nearbyPlacesFeatures.get(COLUMN_JEWELERY_STORE));
		
		values.put( COLUMN_LAUNDRY ,nearbyPlacesFeatures.get(COLUMN_LAUNDRY));
		values.put( COLUMN_LAWYER ,nearbyPlacesFeatures.get(COLUMN_LAWYER));
		values.put( COLUMN_LIBRARY ,nearbyPlacesFeatures.get(COLUMN_LIBRARY));
		values.put( COLUMN_LIQUOR_STORE ,nearbyPlacesFeatures.get(COLUMN_LIQUOR_STORE));
		values.put( COLUMN_LOCAL_GOVERNMENT_OFFICE ,nearbyPlacesFeatures.get(COLUMN_LOCAL_GOVERNMENT_OFFICE));
		values.put( COLUMN_LOCKSMITH ,nearbyPlacesFeatures.get(COLUMN_LOCKSMITH));
		values.put( COLUMN_LODGING ,nearbyPlacesFeatures.get(COLUMN_LODGING));
		values.put( COLUMN_MEAL_DELIVERY ,nearbyPlacesFeatures.get(COLUMN_MEAL_DELIVERY));
		values.put( COLUMN_MEAL_TAKEAWAY ,nearbyPlacesFeatures.get(COLUMN_MEAL_TAKEAWAY));
		values.put( COLUMN_MOSQUE ,nearbyPlacesFeatures.get(COLUMN_MOSQUE));

		values.put( COLUMN_MOVIE_RENTAL ,nearbyPlacesFeatures.get(COLUMN_MOVIE_RENTAL));
		values.put( COLUMN_MOVIE_THEATER ,nearbyPlacesFeatures.get(COLUMN_MOVIE_THEATER));
		
		values.put( COLUMN_MOVING_COMPANY ,nearbyPlacesFeatures.get(COLUMN_MOVING_COMPANY));
		values.put( COLUMN_MUSEUM ,nearbyPlacesFeatures.get(COLUMN_MUSEUM));
		values.put( COLUMN_NIGHT_CLUB ,nearbyPlacesFeatures.get(COLUMN_NIGHT_CLUB));
		values.put( COLUMN_PAINTER ,nearbyPlacesFeatures.get(COLUMN_PAINTER));
		values.put( COLUMN_PARK ,nearbyPlacesFeatures.get(COLUMN_PARK));
		values.put( COLUMN_PARKING,nearbyPlacesFeatures.get(COLUMN_PARKING));
		values.put( COLUMN_PET_STORE ,nearbyPlacesFeatures.get(COLUMN_PET_STORE));
		values.put( COLUMN_PHARMACY ,nearbyPlacesFeatures.get(COLUMN_PHARMACY));
		values.put( COLUMN_PHYSIOTHERAPIST ,nearbyPlacesFeatures.get(COLUMN_PHYSIOTHERAPIST));
		values.put( COLUMN_PLACE_OF_WORSHIP ,nearbyPlacesFeatures.get(COLUMN_PLACE_OF_WORSHIP));

		values.put( COLUMN_PLUMBER ,nearbyPlacesFeatures.get(COLUMN_PLUMBER));
		values.put( COLUMN_POLICE ,nearbyPlacesFeatures.get(COLUMN_POLICE));
		values.put( COLUMN_POST_OFFICE ,nearbyPlacesFeatures.get(COLUMN_POST_OFFICE));
		values.put( COLUMN_REAL_ESTATE_AGENCY ,nearbyPlacesFeatures.get(COLUMN_REAL_ESTATE_AGENCY));
		values.put( COLUMN_RESTAURANT ,nearbyPlacesFeatures.get(COLUMN_RESTAURANT));
		values.put( COLUMN_ROOFING_CONTRACTOR ,nearbyPlacesFeatures.get(COLUMN_ROOFING_CONTRACTOR));
		values.put( COLUMN_RV_PARK ,nearbyPlacesFeatures.get(COLUMN_RV_PARK));
		values.put( COLUMN_SCHOOL ,nearbyPlacesFeatures.get(COLUMN_SCHOOL));
		values.put( COLUMN_SHOE_STORE ,nearbyPlacesFeatures.get(COLUMN_SHOE_STORE));
		values.put( COLUMN_SHOPPING_MALL ,nearbyPlacesFeatures.get(COLUMN_SHOPPING_MALL));

		values.put( COLUMN_SPA ,nearbyPlacesFeatures.get(COLUMN_SPA));
		values.put( COLUMN_STADIUM ,nearbyPlacesFeatures.get(COLUMN_STADIUM));
		values.put( COLUMN_STORAGE ,nearbyPlacesFeatures.get(COLUMN_STORAGE));
		values.put( COLUMN_STORE ,nearbyPlacesFeatures.get(COLUMN_STORE));
		values.put( COLUMN_SUBWAY_STATION ,nearbyPlacesFeatures.get(COLUMN_SUBWAY_STATION));
		values.put( COLUMN_SYNAGOGUE ,nearbyPlacesFeatures.get(COLUMN_SYNAGOGUE));
		values.put( COLUMN_TAXI_STAND ,nearbyPlacesFeatures.get(COLUMN_TAXI_STAND));
		values.put( COLUMN_TRAIN_STATION ,nearbyPlacesFeatures.get(COLUMN_TRAIN_STATION));
		values.put( COLUMN_TRAVEL_AGENCY ,nearbyPlacesFeatures.get(COLUMN_TRAVEL_AGENCY));
		values.put( COLUMN_UNIVERSITY ,nearbyPlacesFeatures.get(COLUMN_UNIVERSITY));

		values.put( COLUMN_VETERINARY_CARE ,nearbyPlacesFeatures.get(COLUMN_VETERINARY_CARE));
		values.put( COLUMN_ZOO ,nearbyPlacesFeatures.get(COLUMN_ZOO));

		
		
		values.put( COLUMN_NEAREST_ACCOUNTING,nearbyPlacesFeatures.get(COLUMN_NEAREST_ACCOUNTING.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_AIRPORT ,nearbyPlacesFeatures.get(COLUMN_NEAREST_AIRPORT.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_AMUSEMENT ,nearbyPlacesFeatures.get(COLUMN_NEAREST_AMUSEMENT.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_AQUARIUM ,nearbyPlacesFeatures.get(COLUMN_NEAREST_AQUARIUM.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_ART ,nearbyPlacesFeatures.get(COLUMN_NEAREST_ART.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_ATM ,nearbyPlacesFeatures.get(COLUMN_NEAREST_ATM.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_BAKERY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_BAKERY.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_BANK ,nearbyPlacesFeatures.get(COLUMN_NEAREST_BANK.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_BAR ,nearbyPlacesFeatures.get(COLUMN_NEAREST_BAR.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_BEAUTY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_BEAUTY.replaceFirst("_", ".")));

		values.put( COLUMN_NEAREST_BICYCLE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_BICYCLE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_BOOK ,nearbyPlacesFeatures.get(COLUMN_NEAREST_BOOK.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_BOWLING ,nearbyPlacesFeatures.get(COLUMN_NEAREST_BOWLING.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_BUS ,nearbyPlacesFeatures.get(COLUMN_NEAREST_BUS.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_CAFE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_CAFE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_CAMPGROUND ,nearbyPlacesFeatures.get(COLUMN_NEAREST_CAMPGROUND.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_CAR_DEALER ,nearbyPlacesFeatures.get(COLUMN_NEAREST_CAR_DEALER.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_CAR_RENTAL ,nearbyPlacesFeatures.get(COLUMN_NEAREST_CAR_RENTAL.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_CAR_WASH, nearbyPlacesFeatures.get(COLUMN_NEAREST_CAR_WASH.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_CAR_REPAIR ,nearbyPlacesFeatures.get(COLUMN_NEAREST_CAR_REPAIR.replaceFirst("_", ".")));

		values.put( COLUMN_NEAREST_CASINO ,nearbyPlacesFeatures.get(COLUMN_NEAREST_CASINO.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_CEMETERY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_CEMETERY.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_CHURCH ,nearbyPlacesFeatures.get(COLUMN_NEAREST_CHURCH.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_CITY_HALL ,nearbyPlacesFeatures.get(COLUMN_NEAREST_CITY_HALL.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_CLOTHING_STORE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_CLOTHING_STORE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_CONVENIENCE__STORE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_CONVENIENCE__STORE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_COURTHOUSE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_COURTHOUSE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_DENTIST ,nearbyPlacesFeatures.get(COLUMN_NEAREST_DENTIST.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_DEPARTMENT_STORE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_DEPARTMENT_STORE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_DOCTOR ,nearbyPlacesFeatures.get(COLUMN_NEAREST_DOCTOR.replaceFirst("_", ".")));

		values.put( COLUMN_NEAREST_ELECTRICIAN ,nearbyPlacesFeatures.get(COLUMN_NEAREST_ELECTRICIAN.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_ELECTRONICS_STORE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_ELECTRONICS_STORE.replaceFirst("_", ".")));

		values.put( COLUMN_NEAREST_EMBASSY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_EMBASSY.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_ESTABLISHMENT ,nearbyPlacesFeatures.get(COLUMN_NEAREST_ESTABLISHMENT.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_FINANCE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_FINANCE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_FIRE_STATION ,nearbyPlacesFeatures.get(COLUMN_NEAREST_FIRE_STATION.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_FLORIST ,nearbyPlacesFeatures.get(COLUMN_NEAREST_FLORIST.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_FOOD ,nearbyPlacesFeatures.get(COLUMN_NEAREST_FOOD.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_FUNERAL_HOME ,nearbyPlacesFeatures.get(COLUMN_NEAREST_FUNERAL_HOME.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_FURNITURE_STORE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_FURNITURE_STORE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_GAS_STATION ,nearbyPlacesFeatures.get(COLUMN_NEAREST_GAS_STATION.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_GENERAL_CONTRACTOR ,nearbyPlacesFeatures.get(COLUMN_NEAREST_GENERAL_CONTRACTOR.replaceFirst("_", ".")));

		values.put( COLUMN_NEAREST_GROCERY_SUPERMARKET ,nearbyPlacesFeatures.get(COLUMN_NEAREST_GROCERY_SUPERMARKET.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_GYM ,nearbyPlacesFeatures.get(COLUMN_NEAREST_GYM.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_HAIR_CARE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_HAIR_CARE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_HARDWARE_STORE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_HARDWARE_STORE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_HEALTH ,nearbyPlacesFeatures.get(COLUMN_NEAREST_HEALTH.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_HINDU_TEMPLE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_HINDU_TEMPLE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_HOME_GOODS_STORE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_HOME_GOODS_STORE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_HOSPITAL ,nearbyPlacesFeatures.get(COLUMN_NEAREST_HOSPITAL.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_INSURANCE_AGENCY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_INSURANCE_AGENCY.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_JEWELERY_STORE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_JEWELERY_STORE.replaceFirst("_", ".")));
		
		values.put( COLUMN_NEAREST_LAUNDRY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_LAUNDRY.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_LAWYER ,nearbyPlacesFeatures.get(COLUMN_NEAREST_LAWYER.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_LIBRARY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_LIBRARY.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_LIQUOR_STORE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_LIQUOR_STORE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_LOCAL_GOVERNMENT_OFFICE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_LOCAL_GOVERNMENT_OFFICE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_LOCKSMITH ,nearbyPlacesFeatures.get(COLUMN_NEAREST_LOCKSMITH.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_LODGING ,nearbyPlacesFeatures.get(COLUMN_NEAREST_LODGING.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_MEAL_DELIVERY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_MEAL_DELIVERY.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_MEAL_TAKEAWAY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_MEAL_TAKEAWAY.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_MOSQUE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_MOSQUE.replaceFirst("_", ".")));

		values.put( COLUMN_NEAREST_MOVIE_RENTAL ,nearbyPlacesFeatures.get(COLUMN_NEAREST_MOVIE_RENTAL.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_MOVIE_THEATER ,nearbyPlacesFeatures.get(COLUMN_NEAREST_MOVIE_THEATER.replaceFirst("_", ".")));
		
		values.put( COLUMN_NEAREST_MOVING_COMPANY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_MOVING_COMPANY.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_MUSEUM ,nearbyPlacesFeatures.get(COLUMN_NEAREST_MUSEUM.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_NIGHT_CLUB ,nearbyPlacesFeatures.get(COLUMN_NEAREST_NIGHT_CLUB.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_PAINTER ,nearbyPlacesFeatures.get(COLUMN_NEAREST_PAINTER.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_PARK ,nearbyPlacesFeatures.get(COLUMN_NEAREST_PARK.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_PARKING,nearbyPlacesFeatures.get(COLUMN_NEAREST_PARKING.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_PET_STORE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_PET_STORE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_PHARMACY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_PHARMACY.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_PHYSIOTHERAPIST ,nearbyPlacesFeatures.get(COLUMN_NEAREST_PHYSIOTHERAPIST.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_PLACE_OF_WORSHIP ,nearbyPlacesFeatures.get(COLUMN_NEAREST_PLACE_OF_WORSHIP.replaceFirst("_", ".")));

		values.put( COLUMN_NEAREST_PLUMBER ,nearbyPlacesFeatures.get(COLUMN_NEAREST_PLUMBER.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_POLICE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_POLICE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_POST_OFFICE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_POST_OFFICE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_REAL_ESTATE_AGENCY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_REAL_ESTATE_AGENCY.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_RESTAURANT ,nearbyPlacesFeatures.get(COLUMN_NEAREST_RESTAURANT.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_ROOFING_CONTRACTOR ,nearbyPlacesFeatures.get(COLUMN_NEAREST_ROOFING_CONTRACTOR.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_RV_PARK ,nearbyPlacesFeatures.get(COLUMN_NEAREST_RV_PARK.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_SCHOOL ,nearbyPlacesFeatures.get(COLUMN_NEAREST_SCHOOL.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_SHOE_STORE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_SHOE_STORE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_SHOPPING_MALL ,nearbyPlacesFeatures.get(COLUMN_NEAREST_SHOPPING_MALL.replaceFirst("_", ".")));

		values.put( COLUMN_NEAREST_SPA ,nearbyPlacesFeatures.get(COLUMN_NEAREST_SPA.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_STADIUM ,nearbyPlacesFeatures.get(COLUMN_NEAREST_STADIUM.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_STORAGE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_STORAGE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_STORE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_STORE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_SUBWAY_STATION ,nearbyPlacesFeatures.get(COLUMN_NEAREST_SUBWAY_STATION.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_SYNAGOGUE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_SYNAGOGUE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_TAXI_STAND ,nearbyPlacesFeatures.get(COLUMN_NEAREST_TAXI_STAND.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_TRAIN_STATION ,nearbyPlacesFeatures.get(COLUMN_NEAREST_TRAIN_STATION.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_TRAVEL_AGENCY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_TRAVEL_AGENCY.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_UNIVERSITY ,nearbyPlacesFeatures.get(COLUMN_NEAREST_UNIVERSITY.replaceFirst("_", ".")));

		values.put( COLUMN_NEAREST_VETERINARY_CARE ,nearbyPlacesFeatures.get(COLUMN_NEAREST_VETERINARY_CARE.replaceFirst("_", ".")));
		values.put( COLUMN_NEAREST_ZOO ,nearbyPlacesFeatures.get(COLUMN_NEAREST_ZOO.replaceFirst("_", ".")));

		return values;
	}
}
