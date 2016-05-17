package com.example.arun.sampleapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Arun on 4/25/2016.
 */
public class InfoActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public SQLiteDatabase mSQLiteDataBase;
    String DB_PATH;

    protected static final String TAG = "main-activity";

    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";

    protected boolean mAddressRequested;
    protected String mAddressOutput;
    private AddressResultReceiver mResultReceiver;
    protected TextView mLocationAddress,mAddressText;
    Button mFetchAddress;


    String mCategory,mCategoryType;
    TextView mDisplayCategory,mSpeed,mAltitude,mBearing,mAccuracy,mProvider,mDate,mTime;
    LinearLayout mCheckboxes;
    LinearLayout mEditTexts,mTimingsLayout,mTimingsFromTo;
    Button mSave;
    private CheckBox mIdentifyLocs, mLease, mUniquePlace, mGames, mRestRoom, mCarParking, mMajorTemple, mParkBoating
            ,mThemePark,mUnderBridge,mCctv,mAirCheck,mFirstAid,mWashroom,mCreditCard,mSecurity,mVillage,mShopsNearby
            ,mFoodJoint,mDrinkingWater,mAutoLpg,mAtm,mRestArea,mCarWash,mDP,mTeaShop,mJuiceShop,mCocunut,mRestaurant,mAc,mNonveg,m24hrs
            ,mHotBeverages,mCoolDrinks,mFreshJuice,mSweets,mBakery,mFood,mTV,mTeaMaker,mLocker,mBar,mPool,mLift;
    private EditText mMobileNumber,mBrand,mPlace,mCharges,mName,mDept;
    private String columnString,dataString;
    private String combinedString = columnString + "\n" + dataString;
    private String appenddataString = "\n" +dataString;

    private FileWriter mFileWriter;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;
    private LocationServices mLocationServices;
    private TextView mCurrentLocation,mTimings,mFrom,mTo;

    private int mYear, mMonth, mDay, mHour, mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        mSQLiteDataBase=openOrCreateDatabase("CollectedData", Context.MODE_PRIVATE, null);

        mSQLiteDataBase.execSQL("CREATE TABLE IF NOT EXISTS CategoryType(CategotyType_Seq INTEGER Primary Key AUTOINCREMENT,CategoryType_Id VARCHAR Not Null," +
                "CategoryType_Name VARCHAR Not Null);");

        mSQLiteDataBase.execSQL("CREATE TABLE IF NOT EXISTS CategoryDetails(Category_Id Integer Primary Key AUTOINCREMENT,CategoryType_Id VARCHAR Not Null," +
                "Category_Date varchar,Categoty_Time varchar,Category_Location varchar, Categoty_Speed varchar, Categoty_Altitude varchar, Categoty_Bearing varchar," +
                "Categoty_Accuracy varchar, Categoty_Provider varchar, Category_Address varchar, Categoty_Name varchar, Categoty_Contact_Number varchar, Categoty_24X7 boolean," +
                "Categoty_Timings varchar, Categoty_Place varchar, Categoty_Charges boolean, Categoty_Dept boolean, Categoty_Identity_locs boolean, Categoty_Lease boolean, Categoty_Unique_Place boolean," +
                "Categoty_Games boolean, Categoty_RestRoom boolean, Categoty_CarParking boolean, Categoty_MajorTemple boolean, Categoty_ParkBoating boolean, Categoty_ThemePark boolean," +
                "Categoty_UnderBridge boolean, Categoty_cctv boolean, Categoty_FirstAid boolean, Categoty_AirCheck boolean, Categoty_WashRoom boolean,Category_RestArea boolean," +
                "Category_CreditCard boolean, Category_Security boolean, Category_Village boolean, Category_ShopsNearby boolean, Category_FoodJoint boolean,Categoty_Atm boolean," +
                "Category_DrinkingWater boolean, Category_AutoLpg boolean, Category_CarWash boolean, Category_DP boolean, Category_TeaShop boolean, Category_JuiceShop boolean," +
                "Category_Tender_Coconut boolean, Category_Restaurant boolean, Category_Ac boolean, Category_NonVeg boolean, Category_HotBeverages boolean, Category_CoolDrinks boolean," +
                "Category_FreshJuice boolean, Category_Sweets boolean, Category_Bakery boolean, Category_Food boolean, Category_Tv boolean,Category_TeaMaker boolean," +
                "Category_Locker boolean, Category_Bar boolean, Category_Pool boolean, Category_Lift boolean);");

        mResultReceiver = new AddressResultReceiver(new Handler());

        Bundle bdle = getIntent().getExtras();

        mCheckboxes = (LinearLayout) findViewById(R.id.check_boxes);
        mEditTexts = (LinearLayout) findViewById(R.id.edit_texts);
        mSave = (Button) findViewById(R.id.save);
        mFetchAddress = (Button) findViewById(R.id.get_address);
        mAddressText = (TextView) findViewById(R.id.address_text);

        mCurrentLocation = (TextView) findViewById(R.id.current_location);
        mSpeed = (TextView) findViewById(R.id.speed_info);
        mAltitude = (TextView) findViewById(R.id.altitude_info);
        mBearing = (TextView) findViewById(R.id.bearing_info);
        mAccuracy = (TextView) findViewById(R.id.accuracy_info);
        mProvider = (TextView) findViewById(R.id.provider_info);
        mTime = (TextView) findViewById(R.id.time_info);
        mDate = (TextView) findViewById(R.id.date_info);
        mLocationAddress = (TextView) findViewById(R.id.display_address);

        mDisplayCategory = (TextView) findViewById(R.id.display_category);
        mDisplayCategory.setText(bdle.getString("Category"));
        mCategory = (String) bdle.getString("Category");

        mAddressRequested = false;
        mAddressOutput = "";

        buildGoogleApiClient();

        if(mGoogleApiClient!= null){
            mGoogleApiClient.connect();
        }
        else
            Toast.makeText(this, "Not connected...", Toast.LENGTH_SHORT).show();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLocation != null) {
            updateUI();
            startLocationUpdates();
            if (mGoogleApiClient.isConnected() && mLocation != null) {
                startIntentService();
            }
            mAddressRequested = true;
        }


        if(mCategory.equals("Delivery Point")){
            mCategoryType = "DP";
            DeliveryPoint();
        }else if(mCategory.equals("Resting Place")){
            mCategoryType = "REST";
            RestingPlace();
        }else if(mCategory.equals("Tourist Attractions")){
            mCategoryType = "TOUR";
            TouristAttraction();
        }else if(mCategory.equals("ATM")){
            mCategoryType = "ATM";
            Atm();
        }else if(mCategory.equals("Pharmacy")){
            mCategoryType = "PHAR";
            Pharmacy();
        }else if(mCategory.equals("Hospital or Clinic")){
            mCategoryType = "HOSP";
            HospitalClinic();
        }else if(mCategory.equals("Fuel")){
            mCategoryType = "FUEL";
            Fuel();
        }else if(mCategory.equals("Puncture")){
            mCategoryType = "PUNC";
            Puncture();
        }else if(mCategory.equals("Mechanic")){
            mCategoryType = "MECH";
            Mechanic();
        }else if(mCategory.equals("Electric")){
            mCategoryType = "ELEC";
            Electric();
        }else if(mCategory.equals("Tyres Rebutton")){
            mCategoryType = "TYRE_R";
            TyresRebutton();
        }else if(mCategory.equals("Used Tyres")){
            mCategoryType = "TYRE_U";
            UsedTyres();
        }else if(mCategory.equals("Toll")){
            mCategoryType = "TOLL";
            Toll();
        }else if(mCategory.equals("State Permit")){
            mCategoryType = "S_PERM";
            StatePermit();
        }else if(mCategory.equals("FoodJoint")){
            mCategoryType = "FOOD";
            Restaurant();
        }else if(mCategory.equals("Tea Juice Snack Stall")){
            mCategoryType = "SNACK";
            TeaJuiceSnack();
        }else if(mCategory.equals("Lodge")){
            mCategoryType = "LODGE";
            Lodge();
        }else if(mCategory.equals("Bar")){
            mCategoryType = "BAR";
            Bar();
        }else if(mCategory.equals("Car Service")){
            mCategoryType = "SERV";
            CarService();
        }else if(mCategory.equals("Help Line")){
            mCategoryType = "HELP";
            Helpline();
        }else if(mCategory.equals("Road Repair")){
            mCategoryType = "ROAD";
            RoadRepair();
        }

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mDisplayCategory.getText().toString().equals("Delivery Point")){
                    DeliveryPointCsv();
                }else if(mDisplayCategory.getText().toString().equals("Resting Place")){
                    RestingPlaceCsv();
                }else if(mDisplayCategory.getText().toString().equals("FoodJoint")){
                    RestaurantCsv();
                }else if(mDisplayCategory.getText().toString().equals("Tea Juice Snack Stall")){
                    TeaJuiceSnackCsv();
                }else if(mDisplayCategory.getText().toString().equals("Lodge")){
                    LodgeCsv();
                }else if(mDisplayCategory.getText().toString().equals("Bar")){
                    BarCsv();
                }else if(mDisplayCategory.getText().toString().equals("Tourist Attractions")){
                    TouristAttractionCsv();
                }else if(mDisplayCategory.getText().toString().equals("ATM")){
                    AtmCsv();
                }else if(mDisplayCategory.getText().toString().equals("Pharmacy")){
                    PharmacyCsv();
                }else if(mDisplayCategory.getText().toString().equals("Hospital or Clinic")){
                    HospitalClinicCsv();
                }else if(mDisplayCategory.getText().toString().equals("Fuel")){
                    FuelCsv();
                }else if(mDisplayCategory.getText().toString().equals("Car Service")){
                    PharmacyCsv();
                }else if(mDisplayCategory.getText().toString().equals("Puncture")){
                    PharmacyCsv();
                }else if(mDisplayCategory.getText().toString().equals("Mechanic")){
                    PharmacyCsv();
                }else if(mDisplayCategory.getText().toString().equals("Electric")){
                    PharmacyCsv();
                }else if(mDisplayCategory.getText().toString().equals("Tyres Rebutton")){
                    PharmacyCsv();
                }else if(mDisplayCategory.getText().toString().equals("Used Tyres")){
                    UsedTyresCsv();
                }else if(mDisplayCategory.getText().toString().equals("Toll")){
                    TollCsv();
                }else if(mDisplayCategory.getText().toString().equals("State Permit")){
                    StatePermitCsv();
                }else if(mDisplayCategory.getText().toString().equals("Help Line")){
                    HelplineCsv();
                }else if(mDisplayCategory.getText().toString().equals("Road Repair")){
                    RoadRepairCsv();
                }
                saveToDB();
                //disableSpecialChar();
                combinedString = columnString + "\n" + dataString;
                appenddataString = "\n" +dataString;
                CsvWriter();
            }
        });

        mFetchAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient.isConnected() && mLocation != null) {
                    startIntentService();
                }
                mAddressRequested = true;
                updateUI();
            }
        });

        if(m24hrs!=null){
            m24hrs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(m24hrs.isChecked()){
                        mTimingsFromTo.setVisibility(View.INVISIBLE);
                        mFrom.setText("");
                        mTo.setText("");
                    }else{
                        mTimingsFromTo.setVisibility(View.VISIBLE);
                    }
                }
            });

            mFrom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimePickerDialog tpd = new TimePickerDialog(InfoActivity.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay,
                                                      int minute) {
                                    // Display Selected time in textbox
                                    mFrom.setText(String.format("%02d:%02d", hourOfDay, minute));
                                }
                            }, mHour, mMinute, false);
                    tpd.show();
                }
            });

            mTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimePickerDialog tpd = new TimePickerDialog(InfoActivity.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay,
                                                      int minute) {
                                    // Display Selected time in textbox
                                    mTo.setText(String.format("%02d:%02d", hourOfDay, minute));
                                }
                            }, mHour, mMinute, false);
                    tpd.show();
                }
            });
        }



        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void DeliveryPoint(){
        Name();
        MobileNumber();
        Timings();

        mIdentifyLocs = new CheckBox(getApplicationContext());
        mIdentifyLocs.setText("Identify Locs");
        mIdentifyLocs.setTextColor(Color.BLACK);
        mIdentifyLocs.setTextSize(16);
        mCheckboxes.addView(mIdentifyLocs);

        mLease = new CheckBox(getApplicationContext());
        mLease.setText("Lease");
        mLease.setTextColor(Color.BLACK);
        mLease.setTextSize(16);
        mCheckboxes.addView(mLease);

        mGames = new CheckBox(getApplicationContext());
        mGames.setText("Games");
        mGames.setTextColor(Color.BLACK);
        mGames.setTextSize(16);
        mCheckboxes.addView(mGames);

        CarParking();

        mUniquePlace = new CheckBox(getApplicationContext());
        mUniquePlace.setText("Unique Place");
        mUniquePlace.setTextColor(Color.BLACK);
        mUniquePlace.setTextSize(16);
        mCheckboxes.addView(mUniquePlace);
    }

    public void RestingPlace(){

        Name();
        MobileNumber();
        Timings();
        Security();
        RestRoom();

        mFoodJoint = new CheckBox(getApplicationContext());
        mFoodJoint.setText("FoodJoint");
        mFoodJoint.setTextColor(Color.BLACK);
        mFoodJoint.setTextSize(16);
        mCheckboxes.addView(mFoodJoint);

        UnderBridge();
        DP();
    }

    public void UnderBridge() {
        mUnderBridge = new CheckBox(getApplicationContext());
        mUnderBridge.setText("Under Bridge?");
        mUnderBridge.setTextColor(Color.BLACK);
        mUnderBridge.setTextSize(16);
        mCheckboxes.addView(mUnderBridge);
    }

    public void DP(){
        mDP = new CheckBox(getApplicationContext());
        mDP.setText("D.P");
        mDP.setTextColor(Color.BLACK);
        mDP.setTextSize(16);
        mCheckboxes.addView(mDP);
    }

    public void RestRoom(){
        mRestRoom = new CheckBox(getApplicationContext());
        mRestRoom.setText("Restroom");
        mRestRoom.setTextColor(Color.BLACK);
        mRestRoom.setTextSize(16);
        mCheckboxes.addView(mRestRoom);
    }

    public void AtmCheck(){
        mAtm = new CheckBox(getApplicationContext());
        mAtm.setText("ATM");
        mAtm.setTextColor(Color.BLACK);
        mAtm.setTextSize(16);
        mCheckboxes.addView(mAtm);
    }

    public void RestaurantCheck(){
        mRestaurant = new CheckBox(getApplicationContext());
        mRestaurant.setText("Restaurant nearby");
        mRestaurant.setTextColor(Color.BLACK);
        mRestaurant.setTextSize(16);
        mCheckboxes.addView(mRestaurant);

    }

    public void Ac(){
        mAc = new CheckBox(getApplicationContext());
        mAc.setText("AC");
        mAc.setTextColor(Color.BLACK);
        mAc.setTextSize(16);
        mCheckboxes.addView(mAc);
    }

    public void CarParking(){
        mCarParking = new CheckBox(getApplicationContext());
        mCarParking.setText("Car Parking");
        mCarParking.setTextColor(Color.BLACK);
        mCarParking.setTextSize(16);
        mCheckboxes.addView(mCarParking);

    }

    public void Security(){
        mSecurity = new CheckBox(getApplicationContext());
        mSecurity.setText("Security Guard");
        mSecurity.setTextColor(Color.BLACK);
        mSecurity.setTextSize(16);
        mCheckboxes.addView(mSecurity);
    }

    public void Name(){
        mName = new EditText(getApplicationContext());
        mName.setHint("Enter Name");
        mName.setHintTextColor(Color.GRAY);
        mName.setTextColor(Color.BLACK);
        mCheckboxes.addView(mName);
    }

    public void MobileNumber(){
        mMobileNumber = new EditText(getApplicationContext());
        mMobileNumber.setHint("Enter Mobile/Contact Number");
        mMobileNumber.setHintTextColor(Color.GRAY);
        mMobileNumber.setTextColor(Color.BLACK);
        setMargins(mMobileNumber,0,0,0,20);
        mMobileNumber.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        mCheckboxes.addView(mMobileNumber);
    }

    public void Timings(){
        mTimingsLayout = new LinearLayout(getApplicationContext());
        mTimingsLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams mParam1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTimingsLayout.setLayoutParams(mParam1);
        mCheckboxes.addView(mTimingsLayout);

        mTimings = new TextView(getApplicationContext());
        mTimings.setText("Enter Timings:");
        mTimings.setTextColor(Color.BLACK);
        mTimings.setTextSize(17);
        mTimingsLayout.addView(mTimings);

        m24hrs = new CheckBox(getApplicationContext());
        m24hrs.setText("24 hrs");
        m24hrs.setTextSize(16);
        m24hrs.setTextColor(Color.BLACK);
        mTimingsLayout.addView(m24hrs);

        mTimingsFromTo = new LinearLayout(getApplicationContext());
        mTimingsFromTo.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams mParam2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTimingsLayout.setLayoutParams(mParam2);
        mTimingsLayout.addView(mTimingsFromTo);

        mFrom = new TextView(getApplicationContext());
        mFrom.setHint("Set Start Time");
        mFrom.setHintTextColor(Color.GRAY);
        mFrom.setTextColor(Color.BLACK);
        mFrom.setBackgroundColor(Color.LTGRAY);
        mFrom.setPadding(5,10,5,10);
        mFrom.setTextSize(17);
        mFrom.setInputType(EditorInfo.TYPE_DATETIME_VARIATION_TIME);
        mTimingsFromTo.addView(mFrom);
        setMargins(mFrom,60,20,0,20);

        mTo = new TextView(getApplicationContext());
        mTo.setHint("Set End Time");
        mTo.setHintTextColor(Color.GRAY);
        mTo.setTextColor(Color.BLACK);
        mTo.setBackgroundColor(Color.LTGRAY);
        mTo.setPadding(5,10,5,10);
        mTo.setTextSize(17);
        mTo.setInputType(EditorInfo.TYPE_DATETIME_VARIATION_TIME);
        mTimingsFromTo.addView(mTo);
        setMargins(mTo,45,20,0,20);

    }

    public void Place(){
        mPlace = new EditText(getApplicationContext());
        mPlace.setHint("Enter Place");
        mPlace.setHintTextColor(Color.GRAY);
        mPlace.setTextColor(Color.BLACK);
        mCheckboxes.addView(mPlace);
    }

    public void Cctv(){
        mCctv = new CheckBox(getApplicationContext());
        mCctv.setText("CCTV");
        mCctv.setTextColor(Color.BLACK);
        mCctv.setTextSize(16);
        mCheckboxes.addView(mCctv);
    }

    public void TouristAttraction(){

        Name();
        MobileNumber();
        Timings();

        mMajorTemple = new CheckBox(getApplicationContext());
        mMajorTemple.setText("Major Temple");
        mMajorTemple.setTextColor(Color.BLACK);
        mMajorTemple.setTextSize(16);
        mCheckboxes.addView(mMajorTemple);

        mParkBoating = new CheckBox(getApplicationContext());
        mParkBoating.setText("Big Park/Boating");
        mParkBoating.setTextColor(Color.BLACK);
        mParkBoating.setTextSize(16);
        mCheckboxes.addView(mParkBoating);

        mThemePark = new CheckBox(getApplicationContext());
        mThemePark.setText("Theme Park");
        mThemePark.setTextColor(Color.BLACK);
        mThemePark.setTextSize(16);
        mCheckboxes.addView(mThemePark);

        UnderBridge();
        DP();
    }

    public void Atm(){
        Name();

        Timings();

        Cctv();

        Security();

        mShopsNearby = new CheckBox(getApplicationContext());
        mShopsNearby.setText("Shops Nearby?");
        mShopsNearby.setTextColor(Color.BLACK);
        mShopsNearby.setTextSize(16);
        mCheckboxes.addView(mShopsNearby);

        mVillage = new CheckBox(getApplicationContext());
        mVillage.setText("Village/Town Nearby?");
        mVillage.setTextColor(Color.BLACK);
        mVillage.setTextSize(16);
        mCheckboxes.addView(mVillage);
        UnderBridge();
        DP();
    }

    public void Pharmacy(){
        Name();
        MobileNumber();
        Timings();
        UnderBridge();
        DP();
    }

    public void HospitalClinic(){
        Name();
        MobileNumber();
        Timings();
        UnderBridge();
        DP();
    }

    public void Fuel(){
        mBrand = new EditText(getApplicationContext());
        mBrand.setHint("Enter Brand");
        mBrand.setHintTextColor(Color.GRAY);
        mBrand.setTextColor(Color.BLACK);
        mCheckboxes.addView(mBrand);

        Timings();

        Cctv();

        mAirCheck = new CheckBox(getApplicationContext());
        mAirCheck.setText("Air Check");
        mAirCheck.setTextColor(Color.BLACK);
        mAirCheck.setTextSize(16);
        mCheckboxes.addView(mAirCheck);

        mFirstAid = new CheckBox(getApplicationContext());
        mFirstAid.setText("First Aid");
        mFirstAid.setTextColor(Color.BLACK);
        mFirstAid.setTextSize(16);
        mCheckboxes.addView(mFirstAid);

        mWashroom = new CheckBox(getApplicationContext());
        mWashroom.setText("Wash Room");
        mWashroom.setTextColor(Color.BLACK);
        mWashroom.setTextSize(16);
        mCheckboxes.addView(mWashroom);

        mCreditCard = new CheckBox(getApplicationContext());
        mCreditCard.setText("Credit Card");
        mCreditCard.setTextColor(Color.BLACK);
        mCreditCard.setTextSize(16);
        mCheckboxes.addView(mCreditCard);

        mFoodJoint = new CheckBox(getApplicationContext());
        mFoodJoint.setText("FoodJoint");
        mFoodJoint.setTextColor(Color.BLACK);
        mFoodJoint.setTextSize(16);
        mCheckboxes.addView(mFoodJoint);

        mDrinkingWater = new CheckBox(getApplicationContext());
        mDrinkingWater.setText("Drinking Water");
        mDrinkingWater.setTextColor(Color.BLACK);
        mDrinkingWater.setTextSize(16);
        mCheckboxes.addView(mDrinkingWater);

        mAutoLpg = new CheckBox(getApplicationContext());
        mAutoLpg.setText("Auto LPG");
        mAutoLpg.setTextColor(Color.BLACK);
        mAutoLpg.setTextSize(16);
        mCheckboxes.addView(mAutoLpg);

        AtmCheck();

        mRestArea = new CheckBox(getApplicationContext());
        mRestArea.setText("Rest Area");
        mRestArea.setTextColor(Color.BLACK);
        mRestArea.setTextSize(16);
        mCheckboxes.addView(mRestArea);

        mCarWash = new CheckBox(getApplicationContext());
        mCarWash.setText("Car Wash");
        mCarWash.setTextColor(Color.BLACK);
        mCarWash.setTextSize(16);
        mCheckboxes.addView(mCarWash);

        UnderBridge();
        DP();
    }

    public void Puncture(){
        Name();
        MobileNumber();
        Timings();
        UnderBridge();
        DP();
    }

    public void Mechanic(){
        Name();
        MobileNumber();
        Timings();
        UnderBridge();
        DP();
    }

    public void Electric(){
        Name();
        MobileNumber();
        Timings();
        UnderBridge();
        DP();
    }

    public void TyresRebutton(){
        Name();
        MobileNumber();
        Timings();
        UnderBridge();
        DP();
    }

    public void UsedTyres(){
        Name();
        MobileNumber();
        Timings();
        DP();
    }

    public void Toll(){
        Place();

        mCharges = new EditText(getApplicationContext());
        mCharges.setHint("Enter Charges");
        mCharges.setHintTextColor(Color.GRAY);
        mCharges.setTextColor(Color.BLACK);
        mCheckboxes.addView(mCharges);

        RestRoom();
        RestaurantCheck();

        mTeaShop = new CheckBox(getApplicationContext());
        mTeaShop.setText("Tea Shop");
        mTeaShop.setTextColor(Color.BLACK);
        mTeaShop.setTextSize(16);
        mCheckboxes.addView(mTeaShop);

        mJuiceShop = new CheckBox(getApplicationContext());
        mJuiceShop.setText("Juice Shop");
        mJuiceShop.setTextColor(Color.BLACK);
        mJuiceShop.setTextSize(16);
        mCheckboxes.addView(mJuiceShop);

        mCocunut = new CheckBox(getApplicationContext());
        mCocunut.setText("Tender Cocunut");
        mCocunut.setTextColor(Color.BLACK);
        mCocunut.setTextSize(16);
        mCheckboxes.addView(mCocunut);

        DP();
    }

    public void StatePermit(){
        Place();

        mCharges = new EditText(getApplicationContext());
        mCharges.setHint("Enter Charges");
        mCharges.setHintTextColor(Color.GRAY);
        mCharges.setTextColor(Color.BLACK);
        mCheckboxes.addView(mCharges);

        UnderBridge();
        DP();
    }

    public void RoadRepair(){
        DP();

    }

    public void Helpline(){

        Place();

        mDept = new EditText(getApplicationContext());
        mDept.setHint("Enter Dept/Person Name");
        mDept.setHintTextColor(Color.GRAY);
        mDept.setTextColor(Color.BLACK);
        mCheckboxes.addView(mDept);

        MobileNumber();

        DP();
    }

    public void Restaurant(){
        Name();
        MobileNumber();
        Timings();

        mNonveg = new CheckBox(getApplicationContext());
        mNonveg.setText("Non Veg");
        mNonveg.setTextColor(Color.BLACK);
        mNonveg.setTextSize(16);
        mCheckboxes.addView(mNonveg);

        Ac();

        CarParking();
        UnderBridge();

        DP();

    }

    public void TeaJuiceSnack(){
        Name();
        MobileNumber();
        Timings();

        mHotBeverages = new CheckBox(getApplicationContext());
        mHotBeverages.setText("Hot Beverages(Tea/Coffee)");
        mHotBeverages.setTextColor(Color.BLACK);
        mHotBeverages.setTextSize(16);
        mCheckboxes.addView(mHotBeverages);

        mCoolDrinks = new CheckBox(getApplicationContext());
        mCoolDrinks.setText("Cool Drinks");
        mCoolDrinks.setTextColor(Color.BLACK);
        mCoolDrinks.setTextSize(16);
        mCheckboxes.addView(mCoolDrinks);

        mFreshJuice = new CheckBox(getApplicationContext());
        mFreshJuice.setText("Fresh Juice");
        mFreshJuice.setTextColor(Color.BLACK);
        mFreshJuice.setTextSize(16);
        mCheckboxes.addView(mFreshJuice);

        mSweets = new CheckBox(getApplicationContext());
        mSweets.setText("Sweets/Snacks");
        mSweets.setTextColor(Color.BLACK);
        mSweets.setTextSize(16);
        mCheckboxes.addView(mSweets);

        mBakery = new CheckBox(getApplicationContext());
        mBakery.setText("Bakery Items");
        mBakery.setTextColor(Color.BLACK);
        mBakery.setTextSize(16);
        mCheckboxes.addView(mBakery);

        CarParking();
        Ac();
        UnderBridge();
        DP();

    }

    public void Lodge(){
        Name();
        MobileNumber();
        Ac();

        mFood = new CheckBox(getApplicationContext());
        mFood.setText("Food");
        mFood.setTextColor(Color.BLACK);
        mFood.setTextSize(16);
        mCheckboxes.addView(mFood);

        mTV = new CheckBox(getApplicationContext());
        mTV.setText("TV");
        mTV.setTextColor(Color.BLACK);
        mTV.setTextSize(16);
        mCheckboxes.addView(mTV);

        mTeaMaker = new CheckBox(getApplicationContext());
        mTeaMaker.setText("Tea/Coffee Maker");
        mTeaMaker.setTextColor(Color.BLACK);
        mTeaMaker.setTextSize(16);
        mCheckboxes.addView(mTeaMaker);

        mLocker = new CheckBox(getApplicationContext());
        mLocker.setText("Locker");
        mLocker.setTextColor(Color.BLACK);
        mLocker.setTextSize(16);
        mCheckboxes.addView(mLocker);
        Security();

        mBar = new CheckBox(getApplicationContext());
        mBar.setText("Bar");
        mBar.setTextColor(Color.BLACK);
        mBar.setTextSize(16);
        mCheckboxes.addView(mBar);

        mPool = new CheckBox(getApplicationContext());
        mPool.setText("Pool");
        mPool.setTextColor(Color.BLACK);
        mPool.setTextSize(16);
        mCheckboxes.addView(mPool);

        mLift = new CheckBox(getApplicationContext());
        mLift.setText("Lift");
        mLift.setTextColor(Color.BLACK);
        mLift.setTextSize(16);
        mCheckboxes.addView(mLift);


        CarParking();
        UnderBridge();
        DP();
    }

    public void Bar(){
        Name();
        MobileNumber();
        Timings();
        CarParking();
        Ac();
        UnderBridge();
        DP();
    }

    public void CarService(){
        Name();
        MobileNumber();
        Timings();
        UnderBridge();
        DP();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLocation != null) {
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, "No geocoder available", Toast.LENGTH_LONG).show();
                return;
            }
            // It is possible that the user presses the button to get the address before the
            // GoogleApiClient object successfully connects. In such a case, mAddressRequested
            // is set to true, but no attempt is made to fetch the address (see
            // fetchAddressButtonHandler()) . Instead, we start the intent service here if the
            // user has requested an address, since we now have a connection to GoogleApiClient.
            if (mAddressRequested) {
                startIntentService();
            }
            updateUI();
        }
        startLocationUpdates();


    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        if (location != null) {
            updateUI();
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(getApplicationContext(), "Address Found", Toast.LENGTH_SHORT).show();
            }

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            mAddressRequested = false;
        }
    }

    protected void displayAddressOutput() {
        mLocationAddress.setText(mAddressOutput);
        mLocationAddress.setVisibility(View.VISIBLE);
    }



    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void CsvWriter(){
        File file   = null;
        File root   = Environment.getExternalStorageDirectory();
        if (root.canWrite()){
            File dir    =   new File(root.getAbsolutePath() + "/CsvDataCollection");
            dir.mkdirs();
            file   =   new File(dir, mDisplayCategory.getText().toString()+".csv");
            mFileWriter = null;
            try {
                mFileWriter = new FileWriter(file, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(file.length()< 1){
                    mFileWriter.append(combinedString);
                    mFileWriter.flush();
                }else{
                    mFileWriter.append(appenddataString);
                    mFileWriter.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mFileWriter.close();
                Toast.makeText(getApplicationContext(), "Saved Successfully", Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent(InfoActivity.this,MainActivity.class);
                //startActivity(intent);
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void DeliveryPointCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Name"+","+"Contact Number"+","+"24hrs"+","+"Timing"+","+"Identify Locs"+","+"Lease"+","+"Games"+","+"Car Parking"+","+"Unique Place";

        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mName.getText().toString().replace(",","\t")
                +","+mMobileNumber.getText().toString().replace(",","\t")+","+m24hrs.isChecked()+","+mFrom.getText().toString()+"-"+mTo.getText().toString()
                +","+mIdentifyLocs.isChecked()+","+mLease.isChecked() +","+mGames.isChecked()+","+mCarParking.isChecked()+","+mUniquePlace.isChecked();



        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Name,Categoty_Contact_Number,Categoty_24X7,Categoty_Timings,Categoty_Identity_locs," +
                "Categoty_Lease,Categoty_Unique_Place,Categoty_Games,Categoty_CarParking) Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
        "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mName.getText().toString()+"','"+mMobileNumber.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"'," +
                "'"+mIdentifyLocs.isChecked()+"','"+mLease.isChecked()+"','"+mUniquePlace.isChecked()+"','"+mGames.isChecked()+"','"+mCarParking.isChecked()+"');");
    }

    public void RestingPlaceCsv() {
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Name"+","+"Contact Number"+","+"24hrs"+","+"Timing"+","+"Security Guard"+","+"Restroom"+","+"Food Joint"+","+"Under Bridge"+","+"D.P";

        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mName.getText().toString().replace(",","\t")
                +","+mMobileNumber.getText().toString().replace(",","\t") +","+m24hrs.isChecked()+","+mFrom.getText().toString()+"-"+mTo.getText().toString()
                +","+mSecurity.isChecked()+","+mRestRoom.isChecked()+","+mFoodJoint.isChecked()+","+mUnderBridge.isChecked()+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Name,Categoty_Contact_Number,Categoty_24X7,Categoty_Timings,Category_Security," +
                "Categoty_RestRoom,Category_FoodJoint,Categoty_UnderBridge,Category_DP) Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mName.getText().toString()+"','"+mMobileNumber.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"'," +
                "'"+mSecurity.isChecked()+"','"+mRestRoom.isChecked()+"','"+mFoodJoint.isChecked()+"','"+mUnderBridge.isChecked()+"','"+mDP.isChecked()+"');");
    }

    public void RestaurantCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Name"+","+"Contact Number"+","+"24hrs"+","+"Timings"+","+"Non-Veg"+","+"AC"+","+"Car Parking"+","+"Under Bridge"+","+"D.P";

        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mName.getText().toString().replace(",","\t")
                +","+mMobileNumber.getText().toString().replace(",","\t") +","+m24hrs.isChecked()+","+mFrom.getText().toString()+"-"+mTo.getText().toString()
                +","+mNonveg.isChecked()+","+mAc.isChecked()+","+mCarParking.isChecked()+","+mUnderBridge.isChecked()+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Name,Categoty_Contact_Number,Categoty_24X7,Categoty_Timings,Category_NonVeg," +
                "Category_Ac,Categoty_CarParking,Categoty_UnderBridge,Category_DP) Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mName.getText().toString()+"','"+mMobileNumber.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"'," +
                "'"+mNonveg.isChecked()+"','"+mAc.isChecked()+"','"+mCarParking.isChecked()+"','"+mUnderBridge.isChecked()+"','"+mDP.isChecked()+"');");
    }

    public void TeaJuiceSnackCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Name"+","+"Contact Number"+","+"24hrs"+","+"Timings"+","+"Tea/Coffee"+","+"Cool Drinks"+","+"Fresh Juice"+","+"Sweets/Snacks"+","+"Bakery Items"+","+"Car Parking"+","
                +"AC"+","+"Under Bridge"+","+"D.P";

        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mName.getText().toString().replace(",","\t")
                +","+mMobileNumber.getText().toString().replace(",","\r\n")+","+m24hrs.isChecked()+","+mFrom.getText().toString()+"-"+mTo.getText().toString()
                +","+mHotBeverages.isChecked()+","+mCoolDrinks.isChecked()+","+mFreshJuice.isChecked() +","+mSweets.isChecked()+","+mBakery.isChecked()
                +","+mCarParking.isChecked()+","+mAc.isChecked()+","+mUnderBridge.isChecked()+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Name,Categoty_Contact_Number,Categoty_24X7,Categoty_Timings,Category_HotBeverages," +
                "Category_Ac,Categoty_CarParking,Categoty_UnderBridge,Category_DP,Category_CoolDrinks,Category_FreshJuice,Category_Sweets,Category_Bakery) " +
                "Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mName.getText().toString()+"','"+mMobileNumber.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"'," +
                "'"+mHotBeverages.isChecked()+"','"+mAc.isChecked()+"','"+mCarParking.isChecked()+"','"+mUnderBridge.isChecked()+"','"+mDP.isChecked()+"','"+mCoolDrinks.isChecked()+"'," +
                "'"+mFreshJuice.isChecked()+"','"+mSweets.isChecked()+"','"+mBakery.isChecked()+"');");
    }

    public void LodgeCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Name"+","+"Contact Number"+","+"AC"+","+"Food"+","+"TV"+","+"Tea/Coffee Maker"+","+"Locker"+","+"Security Guard"+","+"Bar"+","+"Pool"+","+"Lift"
                +","+"Car Parking"+","+"Under Bridge"+","+"D.P";

        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mName.getText().toString().replace(",","\t")
                +","+mMobileNumber.getText().toString().replace(",","\t") +","+mAc.isChecked()+","+mFood.isChecked()+","+mTV.isChecked()+","+mTeaMaker.isChecked()
                +","+mLocker.isChecked()+","+mSecurity.isChecked()+","+mBar.isChecked() +","+mPool.isChecked() +","+mLift.isChecked()+","+mCarParking.isChecked()
                +","+mUnderBridge.isChecked()+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Name,Categoty_Contact_Number,Category_Food,Category_Lift,"+
                "Category_Ac,Categoty_CarParking,Categoty_UnderBridge,Category_DP,Category_Tv,Category_TeaMaker,Category_Locker,Category_Security,Category_Bar,Category_Pool)"+
                "Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mName.getText().toString()+"','"+mMobileNumber.getText().toString()+"'," +
                "'"+mFood.isChecked()+"','"+mLift.isChecked()+"','"+mAc.isChecked()+"','"+mCarParking.isChecked()+"','"+mUnderBridge.isChecked()+"','"+mDP.isChecked()+"','"+mTV.isChecked()+"'," +
                "'"+mTeaMaker.isChecked()+"','"+mLocker.isChecked()+"','"+mSecurity.isChecked()+"','"+mBar.isChecked()+"','"+mPool.isChecked()+"');");
    }

    public void BarCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Name"+","+"24hrs"+","+"Timing"+","+"Contact Number"+","+"AC"+","+"Car Parking"+","+"Under Bridge"+","+"D.P";

        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mName.getText().toString().replace(",","\t")
                +","+mMobileNumber.getText().toString().replace(",","\t")+","+m24hrs.isChecked()+","+mFrom.getText().toString()+"-"+mTo.getText().toString()
                +","+mCarParking.isChecked()+","+mAc.isChecked()+","+mUnderBridge.isChecked()+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Name,Categoty_Contact_Number,Categoty_24X7,Categoty_Timings," +
                "Category_Ac,Categoty_CarParking,Categoty_UnderBridge,Category_DP) " +
                "Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mName.getText().toString()+"','"+mMobileNumber.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"'," +
                "'"+mAc.isChecked()+"','"+mCarParking.isChecked()+"','"+mUnderBridge.isChecked()+"','"+mDP.isChecked()+"');");
    }

    public void TouristAttractionCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Name"+","+"Contact Number"+","+"24hrs"+","+"Timing"+","+"Major Temple"+","+"Big Park/Boating"+","+"Theme Park"+","+"Under Bridge"+","+"D.P";

        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mName.getText().toString().replace(",","\t")
                +","+mMobileNumber.getText().toString().replace(",","\t")+","+m24hrs.isChecked()+","+mFrom.getText().toString()+"-"+mTo.getText().toString()
                +","+mMajorTemple.isChecked()+","+mParkBoating.isChecked()+","+mThemePark.isChecked()+","+mUnderBridge.isChecked()+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Name,Categoty_Contact_Number,Categoty_24X7,Categoty_Timings," +
                "Categoty_MajorTemple,Categoty_ParkBoating,Categoty_ThemePark,Categoty_UnderBridge,Category_DP)"+
                "Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mName.getText().toString()+"','"+mMobileNumber.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"',"+
                "'"+mMajorTemple.isChecked()+"','"+mParkBoating.isChecked()+"','"+mThemePark.isChecked()+"','"+mUnderBridge.isChecked()+"','"+mDP.isChecked()+"');");
    }

    public void AtmCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Name"+","+"24hrs"+","+"Timing"+","+"CCTV"+","+"Security Guard"+","+"Shops Nearby"+","+"Village/Town Nearby"+","+"Under Bridge"+","+"D.P";

        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mName.getText().toString().replace(",","\t")
                +","+m24hrs.isChecked()+","+mFrom.getText().toString()+"-"+mTo.getText().toString()+","+mCctv.isChecked()+","+mSecurity.isChecked()
                +","+mShopsNearby.isChecked()+","+mVillage.isChecked()+","+mUnderBridge.isChecked()+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Name,Categoty_24X7,Categoty_Timings," +
                "Categoty_cctv,Category_Security,Category_ShopsNearby,Category_Village,Categoty_UnderBridge,Category_DP)"+
                "Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mName.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"',"+
                "'"+mCctv.isChecked()+"','"+mSecurity.isChecked()+"','"+mShopsNearby.isChecked()+"','"+mVillage.isChecked()+"','"+mUnderBridge.isChecked()+"','"+mDP.isChecked()+"');");
    }

    public void PharmacyCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Name"+","+"Contact Number"+","+"24hrs"+","+"Timing"+","+"Under Bridge"+","+"D.P";

        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mName.getText().toString().replace(",","\t")+
                ","+mMobileNumber.getText().toString().replace(",","\t")+","+m24hrs.isChecked()+","+mFrom.getText().toString()+"-"+mTo.getText().toString()
                +","+mUnderBridge.isChecked()+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Name,Categoty_Contact_Number,Categoty_24X7,Categoty_Timings," +
                "Categoty_UnderBridge,Category_DP)"+
                "Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mName.getText().toString()+"','"+mMobileNumber.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"',"+
                "'"+mUnderBridge.isChecked()+"','"+mDP.isChecked()+"');");
    }

    public void HospitalClinicCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Name"+","+"Contact Number"+","+"24hrs"+","+"Timing"+","+"Under Bridge"+","+"D.P";

        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mName.getText().toString().replace(",","\t")
                +","+mMobileNumber.getText().toString().replace(",","\t")+","+m24hrs.isChecked()+","+mFrom.getText().toString()+"-"+mTo.getText().toString()
                +","+mUnderBridge.isChecked()+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Name,Categoty_Contact_Number,Categoty_24X7,Categoty_Timings," +
                "Categoty_UnderBridge,Category_DP)"+
                "Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mName.getText().toString()+"','"+mMobileNumber.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"',"+
                "'"+mUnderBridge.isChecked()+"','"+mDP.isChecked()+"');");
    }

    public void FuelCsv(){
        columnString =  "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Brand"+","+"24hrs"+","+"Timings"+"," +"CCTV" +","+"Air Check"+","+"First Aid"+","+"Washroom"+","+"Credit Card Accepted?" +","+"Food Joint"+","
                +"Drinking Water"+","+"Auto LPG"+","+"ATM"+","+"Rest Area"+","+"Car Wash"+","+"Under Bridge"+","+"D.P";

        dataString   =   mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mBrand.getText().toString() +","+m24hrs.isChecked()
                +","+mFrom.getText().toString()+"-"+mTo.getText().toString()+"," +mCctv.isChecked()+"," +mAirCheck.isChecked()+ "," + mFirstAid.isChecked()
                +"," +mWashroom.isChecked() +","+mCreditCard.isChecked()+"," +mFoodJoint.isChecked() +"," +mDrinkingWater.isChecked() +"," +mAutoLpg.isChecked()
                +"," +mAtm.isChecked()+"," +mRestArea.isChecked() +"," +mCarWash.isChecked() +"," +mUnderBridge.isChecked()+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Name,Categoty_24X7,Categoty_Timings,Categoty_cctv," +
                "Categoty_AirCheck,Categoty_FirstAid,Categoty_UnderBridge,Category_DP,Categoty_WashRoom,Category_CreditCard,Category_FoodJoint,Category_DrinkingWater, " +
                "Category_AutoLpg,Categoty_Atm,Category_RestArea,Category_CarWash)Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mBrand.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"'," +
                "'"+mCctv.isChecked()+"','"+mAirCheck.isChecked()+"','"+mFirstAid.isChecked()+"','"+mUnderBridge.isChecked()+"','"+mDP.isChecked()+"','"+mWashroom.isChecked()+"'," +
                "'"+mCreditCard.isChecked()+"','"+mFoodJoint.isChecked()+"','"+mDrinkingWater.isChecked()+"','"+mAutoLpg.isChecked()+"','"+mAtm.isChecked()+"'," +
                "'"+mRestArea.isChecked()+"','"+mCarWash.isChecked()+"');");
    }

    public void UsedTyresCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Name"+","+"Contact Number"+","+"24hrs"+","+"Timing"+","+"D.P";

        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mName.getText().toString().replace(",","\t")
                +","+mMobileNumber.getText().toString().replace(",","\t")+","+m24hrs.isChecked()+","+mFrom.getText().toString()+"-"+mTo.getText().toString()+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Name,Categoty_Contact_Number,Categoty_24X7,Categoty_Timings,Category_DP)" +
                "Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mName.getText().toString()+"','"+mMobileNumber.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"',"+
                "'"+mDP.isChecked()+"');");
    }

    public void TollCsv(){
        columnString =  "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Place"+","+"Charges"+","+"RestRoom" +","+"Restaurant Nearby"+","+"Tea Shop"+","+"Juice Shop"+","+"Tender Cocunut"+","+"D.P";

        dataString   =   mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mPlace.getText().toString().replace(",","\t")
                +","+mCharges.getText().toString().replace(",","\t")+","+mRestRoom.isChecked()+","+mRestaurant.isChecked()+","+mTeaShop.isChecked()
                +","+mJuiceShop.isChecked() +"," +mCocunut.isChecked()+","+mDP.isChecked();



        /*mSQLiteDataBase.execSQL("CREATE TABLE IF NOT EXISTS CategoryDetails(Category_Id Integer Primary Key AUTOINCREMENT,CategoryType_Id VARCHAR Not Null," +
                "Category_Date varchar,Categoty_Time varchar,Category_Location varchar, Categoty_Speed varchar, Categoty_Altitude varchar, Categoty_Bearing varchar," +
                "Categoty_Accuracy varchar, Categoty_Provider varchar, Category_Address varchar, Categoty_Name varchar, Categoty_Contact_Number varchar, Categoty_24X7 boolean," +
                "Categoty_Timings varchar, Categoty_Place varchar, Categoty_Charges boolean, Categoty_Dept boolean, Categoty_Identity_locs boolean, Categoty_Lease boolean, Categoty_Unique_Place boolean," +
                "Categoty_Games boolean, Categoty_RestRoom boolean, Categoty_CarParking boolean, Categoty_MajorTemple boolean, Categoty_ParkBoating boolean, Categoty_ThemePark boolean," +
                "Categoty_UnderBridge boolean, Categoty_cctv boolean, Categoty_FirstAid boolean, Categoty_AirCheck boolean, Categoty_WashRoom boolean,Category_RestArea boolean," +
                "Category_CreditCard boolean, Category_Security boolean, Category_Village boolean, Category_ShopsNearby boolean, Category_FoodJoint boolean,Categoty_Atm boolean," +
                "Category_DrinkingWater boolean, Category_AutoLpg boolean, Category_CarWash boolean, Category_DP boolean, Category_TeaShop boolean, Category_JuiceShop boolean," +
                "Category_Tender_Coconut boolean, Category_Restaurant boolean, Category_Ac boolean, Category_NonVeg boolean, Category_HotBeverages boolean, Category_CoolDrinks boolean," +
                "Category_FreshJuice boolean, Category_Sweets boolean, Category_Bakery boolean, Category_Food boolean, Category_Tv boolean,Category_TeaMaker boolean," +
                "Category_Locker boolean, Category_Bar boolean, Category_Pool boolean, Category_Lift boolean);");*/

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Place,Categoty_Charges,Categoty_RestRoom,Category_Restaurant,"+
                "Category_TeaShop,Category_JuiceShop,Category_Tender_Coconut,Category_DP)"+
                "Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mPlace.getText().toString()+"','"+mCharges.getText().toString()+"'," +
                "'"+mRestRoom.isChecked()+"','"+mRestaurant.isChecked()+"','"+mTeaShop.isChecked()+"','"+mJuiceShop.isChecked()+"','"+mCocunut.isChecked()+"','"+mDP.isChecked()+"');");
    }

    public void StatePermitCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Place"+","+"Charges"+","+"Under Bridge"+","+"D.P";
        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mPlace.getText().toString().replace(",","\t")
                +","+mCharges.getText().toString().replace(",","\t")+","+mUnderBridge.isChecked()+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Place,Categoty_Charges,Categoty_UnderBridge,Category_DP)"+
                "Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mPlace.getText().toString()+"','"+mCharges.getText().toString()+"','"+mUnderBridge.isChecked()+"','"+mDP.isChecked()+"');");
    }

    public void HelplineCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","
                +"Place"+","+"Dept/Person Name"+","+"Helpline Number"+","+"D.P";
        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mPlace.getText().toString().replace(",","\t")
                +","+mDept.getText().toString().replace(",","\t")+","+mMobileNumber.getText().toString().replace(",","\t")+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Categoty_Dept,Categoty_Contact_Number,Category_DP,Categoty_Place)"+
                "Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mDept.getText().toString()+"','"+mMobileNumber.getText().toString()+"','"+mDP.isChecked()+"','"+mPlace.getText().toString()+"');");
    }

    public void RoadRepairCsv(){
        columnString = "Date"+","+"Time"+","+"Latitude"+","+"Longitude"+","+"Speed"+","+"Altitude"+","+"Bearing"+","+"Accuracy"+","+"Provider"+","+"Address"+","+"D.P";
        dataString = mDate.getText().toString()+","+mTime.getText().toString()+","+mCurrentLocation.getText().toString()+","+mSpeed.getText().toString()
                +","+mAltitude.getText().toString()+","+mBearing.getText().toString()+","+mAccuracy.getText().toString()+","+mProvider.getText().toString()
                +","+mLocationAddress.getText().toString().replace(",","\t").replace("\n","\t")+","+mDP.isChecked();

        mSQLiteDataBase.execSQL("Insert into CategoryDetails (Category_Id,CategoryType_Id,Category_Date,Categoty_Time,Category_Location,Categoty_Speed,Categoty_Altitude," +
                "Categoty_Bearing,Categoty_Accuracy,Categoty_Provider,Category_Address,Category_DP)"+
                "Values(null,'"+mCategoryType+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mDP.isChecked()+"');");
    }



    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(300);
        mLocationRequest.setFastestInterval(100);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void updateUI() {
        mCurrentLocation.setText(String.valueOf(mLocation.getLatitude()+"," +String.valueOf(mLocation.getLongitude())));
        mSpeed.setText(String.valueOf(mLocation.getSpeed()));
        mAltitude.setText(String.valueOf(mLocation.getAltitude()));
        mBearing.setText(String.valueOf(mLocation.getBearing()));
        mAccuracy.setText(String.valueOf(mLocation.getAccuracy()));
        mProvider.setText(String.valueOf(mLocation.getProvider()));
        mTime.setText(String.valueOf(DateFormat.getTimeInstance().format(new Date())));
        mDate.setText(String.valueOf(DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(new Date())));
    }

    public void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    public void saveToDB(){
        //CategotyType_Seq
        mSQLiteDataBase.beginTransaction();

        mSQLiteDataBase.execSQL("CREATE TABLE IF NOT EXISTS CategoryType(CategotyType_Seq INTEGER Primary Key AUTOINCREMENT,CategoryType_Id VARCHAR Not Null," +
                "CategoryType_Name VARCHAR Not Null);");

        mSQLiteDataBase.execSQL("CREATE TABLE IF NOT EXISTS CategoryDetails(Category_Id Integer Primary Key AUTOINCREMENT,CategoryType_Id VARCHAR Not Null," +
                "Category_Date varchar,Categoty_Time varchar,Category_Location varchar, Categoty_Speed varchar, Categoty_Altitude varchar, Categoty_Bearing varchar," +
                "Categoty_Accuracy varchar, Categoty_Provider varchar, Category_Address varchar, Categoty_Name varchar, Categoty_Contact_Number varchar, Categoty_24X7 boolean," +
                "Categoty_Timings varchar, Categoty_Place varchar, Categoty_Charges boolean, Categoty_Dept boolean, Categoty_Identity_locs boolean, Categoty_Lease boolean, Categoty_Unique_Place boolean," +
                "Categoty_Games boolean, Categoty_RestRoom boolean, Categoty_CarParking boolean, Categoty_MajorTemple boolean, Categoty_ParkBoating boolean, Categoty_ThemePark boolean," +
                "Categoty_UnderBridge boolean, Categoty_cctv boolean, Categoty_FirstAid boolean, Categoty_AirCheck boolean, Categoty_WashRoom boolean," +
                "Category_CreditCard boolean, Category_Security boolean, Category_Village boolean, Category_ShopsNearby boolean, Category_FoodJoint boolean," +
                "Category_DrinkingWater boolean, Category_AutoLpg boolean, Category_CarWash boolean, Category_DP boolean, Category_TeaShop boolean, Category_JuiceShop boolean," +
                "Category_Tender_Coconut boolean, Category_Restaurant boolean, Category_Ac boolean, Category_NonVeg boolean, Category_HotBeverages boolean, Category_CoolDrinks boolean," +
                "Category_FreshJuice boolean, Category_Sweets boolean, Category_Bakery boolean, Category_Food boolean, Category_Tv boolean,Category_TeaMaker boolean," +
                "Category_Locker boolean, Category_Bar boolean, Category_Pool boolean, Category_Lift boolean);");

        mSQLiteDataBase.execSQL("INSERT INTO CategoryType VALUES(null,'"+mCategoryType+"','"+mDisplayCategory.getText().toString()+"');");

        /*try {
            mSQLiteDataBase.execSQL("INSERT INTO CategoryDetails VALUES(null,'"+mCategoryType+"','"+mDisplayCategory.getText().toString()+"','"+mDate.getText().toString()+"'," +
                    "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                    "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                    "'"+mName.getText().toString()+"','"+mMobileNumber.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"'," +
                    "'"+mPlace.getText().toString()+"','"+mCharges.getText().toString()+"','"+mDept.getText().toString()+"','"+mIdentifyLocs.isChecked()+"'," +
                    "'"+mLease.isChecked()+"','"+mUniquePlace.isChecked()+"','"+mGames.isChecked()+"','"+mRestRoom.isChecked()+"','"+mCarParking.isChecked()+"'," +
                    "'"+mMajorTemple.isChecked()+"','"+mParkBoating.isChecked()+"','"+mThemePark.isChecked()+"','"+mUnderBridge.isChecked()+"','"+mCctv.isChecked()+"'," +
                    "'"+mFirstAid.isChecked()+"','"+mAirCheck.isChecked()+"','"+mWashroom.isChecked()+"','"+mCreditCard.isChecked()+"','"+mSecurity.isChecked()+"'," +
                    "'"+mVillage.isChecked()+"','"+mShopsNearby.isChecked()+"','"+mFoodJoint.isChecked()+"','"+mDrinkingWater.isChecked()+"','"+mAutoLpg.isChecked()+"'," +
                    "'"+mCarWash.isChecked()+"','"+mDP.isChecked()+"','"+mTeaShop.isChecked()+"','"+mJuiceShop.isChecked()+"','"+mCocunut.isChecked()+"'," +
                    "'"+mRestaurant.isChecked()+"','"+mAc.isChecked()+"','"+mNonveg.isChecked()+"','"+mHotBeverages.isChecked()+"','"+mCoolDrinks.isChecked()+"'," +
                    "'"+mFreshJuice.isChecked()+"','"+mSweets.isChecked()+"','"+mBakery.isChecked()+"','"+mFood.isChecked()+"','"+mTV.isChecked()+"','"+mTeaMaker.isChecked()+"'," +
                    "'"+mLocker.isChecked()+"','"+mBar.isChecked()+"','"+mPool.isChecked()+"','"+mLift.isChecked()+"');");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }*/

        /*mSQLiteDataBase.execSQL("INSERT INTO CategoryDetails VALUES(null,'"+mCategoryType+"','"+mDisplayCategory.getText().toString()+"','"+mDate.getText().toString()+"'," +
                "'"+mTime.getText().toString()+"','"+mCurrentLocation.getText().toString()+"','"+mSpeed.getText().toString()+"','"+mAltitude.getText().toString()+"'," +
                "'"+mBearing.getText().toString()+"','"+mAccuracy.getText().toString()+"','"+mProvider.getText().toString()+"','"+mLocationAddress.getText().toString()+"'," +
                "'"+mName.getText().toString()+"','"+mNumber.getText().toString()+"','"+m24hrs.isChecked()+"','"+mFrom.getText().toString()+"-"+mTo.getText().toString()+"'," +
                "'"+mPlace.getText().toString()+"','"+mCharges.getText().toString()+"','"+mDept.getText().toString()+"','"+mIdentifyLocs.isChecked()+"'," +
                "'"+mLease.isChecked()+"','"+mUniquePlace.isChecked()+"','"+mGames.isChecked()+"','"+mRestRoom.isChecked()+"','"+mCarParking.isChecked()+"'," +
                "'"+mMajorTemple.isChecked()+"','"+mParkBoating.isChecked()+"','"+mThemePark.isChecked()+"','"+mUnderBridge.isChecked()+"','"+mCctv.isChecked()+"'," +
                "'"+mFirstAid.isChecked()+"','"+mAirCheck.isChecked()+"','"+mWashroom.isChecked()+"','"+mCreditCard.isChecked()+"','"+mSecurity.isChecked()+"'," +
                "'"+mVillage.isChecked()+"','"+mShopsNearby.isChecked()+"','"+mFoodJoint.isChecked()+"','"+mDrinkingWater.isChecked()+"','"+mAutoLpg.isChecked()+"'," +
                "'"+mCarWash.isChecked()+"','"+mDP.isChecked()+"','"+mTeaShop.isChecked()+"','"+mJuiceShop.isChecked()+"','"+mCocunut.isChecked()+"'," +
                "'"+mRestaurant.isChecked()+"','"+mAc.isChecked()+"','"+mNonveg.isChecked()+"','"+mHotBeverages.isChecked()+"','"+mCoolDrinks.isChecked()+"'," +
                "'"+mFreshJuice.isChecked()+"','"+mSweets.isChecked()+"','"+mBakery.isChecked()+"','"+mFood.isChecked()+"','"+mTV.isChecked()+"','"+mTeaMaker.isChecked()+"'," +
                "'"+mLocker.isChecked()+"','"+mBar.isChecked()+"','"+mPool.isChecked()+"','"+mLift.isChecked()+"');");*/

        //mSQLiteDataBase.execSQL("INSERT INTO CategoryType(CategoryType_Id, CategoryType_Name ) VALUES ('"+mCategoryType+"','"+mDisplayCategory.getText().toString()+"' );");
        mSQLiteDataBase.setTransactionSuccessful();
        mSQLiteDataBase.endTransaction();
        try {
            writeToSD();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeToSD() throws IOException {
        File sd = Environment.getExternalStorageDirectory();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DB_PATH = getApplicationContext().getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;
        }
        else {
            DB_PATH = getApplicationContext().getFilesDir().getPath() + getApplicationContext().getPackageName() + "/databases/";
        }

        if (sd.canWrite()) {
            //String currentDBPath = getBaseContext().getDatabasePath(DB_PATH).toString();
            String currentDBPath = getApplicationContext().getDatabasePath("CollectedData").toString();
            String backupDBPath = "backupdata.db";
            File currentDB = new File(currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
            Toast.makeText(getApplicationContext(),
                    "DB copied to external",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
