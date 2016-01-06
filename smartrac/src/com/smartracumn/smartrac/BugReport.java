package com.smartracumn.smartrac;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class BugReport extends Activity {

	private ToggleButton bugToggle;
	
	private boolean bugMode;
	
	private static int ACTIVITY_SELECT_IMAGE = 1;
	
	private ArrayList<Uri> list_uri = new ArrayList<Uri>();
	
	private ArrayList<Bitmap> list_images = new ArrayList<Bitmap>();
	
	private ImageAdapter imageAdapter;
	
	private GridView gridview;
	
	private SharedPreferences sharedPref;
	
	private SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bug_report);

		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setTitle("Send Bug Report");
		getActionBar().setDisplayHomeAsUpEnabled(true);

//		bugToggle = (ToggleButton) findViewById(R.id.toggleBug);
//
//		sharedPref = getSharedPreferences(
//				"com.smartracumn.smartrac", Context.MODE_PRIVATE);
//		 editor = sharedPref.edit();
//
//		if (sharedPref.getBoolean("BugMode", false)) {
//			bugToggle.setChecked(true);
//		}
//		bugToggle.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				bugMode = ((ToggleButton) v).isChecked();
//				if (bugMode) {
//					editor.putBoolean("BugMode", true);
//					editor.commit();
//				} else {
//					editor.putBoolean("BugMode", false);
//					editor.commit();
//				}
//			}
//		});

		Button selectImage = (Button) findViewById(R.id.button1);
		selectImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectImage();

			}
		});

		final String device_info = getDeviceInformation();

		Button sendEmail = (Button) findViewById(R.id.sendEmail);
		sendEmail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText details = (EditText) findViewById(R.id.editDetails);
				String text = details.getText().toString();
				Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL,
						new String[] { "smartrac@umn.edu" });
				i.putExtra(Intent.EXTRA_SUBJECT, "Bug Report");
				i.putExtra(Intent.EXTRA_TEXT, device_info + "\n\n" + text);
				i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, list_uri);
				try {
					startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(BugReport.this,
							"There are no email clients installed.",
							Toast.LENGTH_SHORT).show();
				}

			}
		});

		gridview = (GridView) findViewById(R.id.gridView1);
		imageAdapter = new ImageAdapter(this);
		gridview.setAdapter(imageAdapter);

	}

	private void selectImage() {
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, ACTIVITY_SELECT_IMAGE);

	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = imageReturnedIntent.getData();
				list_uri.add(selectedImage);

				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();

				Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
				list_images.add(yourSelectedImage);
				updateImages();
			}
		}
	}

	private void updateImages() {
		try {
			imageAdapter.notifyDataSetChanged();

		} catch (NullPointerException e) {
			imageAdapter = new ImageAdapter(this);
		}

	}

	private String getDeviceInformation() {
		String _OSVERSION = System.getProperty("os.version");
		String _RELEASE = android.os.Build.VERSION.RELEASE;
		String _DEVICE = android.os.Build.DEVICE;
		String _MODEL = android.os.Build.MODEL;
		String _PRODUCT = android.os.Build.PRODUCT;
		String _BRAND = android.os.Build.BRAND;
		String _DISPLAY = android.os.Build.DISPLAY;
		String _MANUFACTURER = android.os.Build.MANUFACTURER;
		StringBuilder sb = new StringBuilder();

		sb.append("_OSVersion: " + _OSVERSION);
		sb.append("\n_Release: " + _RELEASE);
		sb.append("\n_Device: " + _DEVICE);
		sb.append("\n_Model: " + _MODEL);
		sb.append("\n_PRODUCT: " + _PRODUCT);
		sb.append("\n_BRAND: " + _BRAND);
		sb.append("\n_DISPLAY: " + _DISPLAY);
		sb.append("\n_MANUFACTURER: " + _MANUFACTURER);

		return sb.toString();
	}

	// Image Adapter for the GridView
	public class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return list_images.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, initialize some
										// attributes
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);
			} else {
				imageView = (ImageView) convertView;
			}

			imageView.setImageBitmap(list_images.get(position));
			return imageView;
		}

	}

}
