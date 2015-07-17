package com.example.securityscanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.R.string;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
	static TextView rootStatus;
	static TextView nfcStatus;
	static TextView bluetoothStatus;
	static TextView lockStatus;
	static TextView unknownStatus;
	static TextView encryptedStatus;
	static TextView locationStatus;
	static TextView simlockStatus;
	static TextView wifiHistory;
	List<String> data;
	List<String> wifi;
	
	public static final int SECURITY_NONE = 0;
	public static final int SECURITY_WEP = 1;
	public static final int SECURITY_PSK = 2;
	public static final int SECURITY_EAP = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		Log.i("datax", this.getApplicationContext().getPackageCodePath().toString());
		rootStatus = (TextView)findViewById(R.id.id_root_status);
		nfcStatus = (TextView)findViewById(R.id.id_nfc_status);
		bluetoothStatus = (TextView)findViewById(R.id.id_bluetooth_status);
		lockStatus = (TextView)findViewById(R.id.id_lock_status);
		unknownStatus = (TextView)findViewById(R.id.id_unknown_status);
		encryptedStatus = (TextView)findViewById(R.id.id_encrypted_status);
		locationStatus = (TextView)findViewById(R.id.id_location_status);
		simlockStatus = (TextView)findViewById(R.id.id_simlock_status);
		wifiHistory = (TextView) findViewById(R.id.id_wifi_history); 
		
		data = new ArrayList<String>();
		
		
		NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this.getApplicationContext());	
		if(nfcAdpt!=null)
		{
			
		if(nfcAdpt.isEnabled())
			{
				nfcStatus.setText("NFC status = true");
			}
			else
			{
				nfcStatus.setText("NFC status = false");
			}
		} else nfcStatus.setText("NFC status = no NFC adapter");
		
		boolean rootstatus2 = isRooted();
		rootStatus.setText("Root status= "+String.valueOf(rootstatus2));
		
		//add new line
		data.add("Root Status");
		data.add(String.valueOf(rootstatus2));
		
		// Check for available NFC Adapter
        /*PackageManager pm = getPackageManager();
        if(pm.hasSystemFeature(PackageManager.FEATURE_NFC) && NfcAdapter.getDefaultAdapter(this) != null) {
        	nfcStatus.setText("NFC status = true");
        } else {
        	nfcStatus.setText("NFC status = false");
        }*/
		
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//add new line
		data.add("Bluetooth Status");
		
		if (mBluetoothAdapter == null) {
			bluetoothStatus.setText("Bluetooth status = null");
		} else {
		    if (!mBluetoothAdapter.isEnabled()) {
		    	bluetoothStatus.setText("Bluetooth status = false");
		    	data.add("false");
		    }else{
		    	bluetoothStatus.setText("Bluetooth status = true");
		    	data.add("true");
		    }
		}
		
		int lockType = LockType.getCurrent(getContentResolver());
		String lockType2;
		switch(lockType) {
	    case 1:
	        lockType2 = "NONE or SLIDE";
	        break;
	    case 3:
	    	lockType2 = "FACE WITH PATTERN";
	        break;
	    case 4:
	    	lockType2 = "FACE WITH PIN";
	        break;
	    case 10:
	    	lockType2 = "PATTERN";
	        break;    
	    case 11:
	    	lockType2 = "PIN";
	        break;
	    case 12:
	    	lockType2 = "PASSWORD ALPHABETIC";
	        break;
	    case 13:
	    	lockType2 = "PASSWORD ALPHANUMERIC";
	        break;
	    default:
	    	lockType2 = "ERROR";
		}
		
		
		lockStatus.setText("Lock status= "+String.valueOf(lockType2));
		data.add("Lock screen status");
		data.add(String.valueOf(lockType2));
		
		boolean isNonPlayAppAllowed = isTrustUnknownSource();
		unknownStatus.setText("Unknown sources status= "+String.valueOf(isNonPlayAppAllowed));
		data.add("Unknown sources status");
		data.add(String.valueOf(isNonPlayAppAllowed));
		
		boolean encryptedStatus2 = isEncrypted(this.getApplicationContext());
		encryptedStatus.setText("Encrypted status= "+String.valueOf(encryptedStatus2));
		data.add("Phone Encryption status");
		data.add(String.valueOf(encryptedStatus2));
		
		boolean locationStatus2 = isGpsEnabled();
		locationStatus.setText("Location status= "+String.valueOf(locationStatus2));
		data.add("Location status");
		data.add(String.valueOf(locationStatus2));
		
		boolean simLockStatus2 = isSimPinRequired(getApplicationContext());
		simlockStatus.setText("Sim lock status= "+String.valueOf(simLockStatus2));
		data.add("Sim lock status");
		data.add(String.valueOf(simLockStatus2));
		
		/*WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> arraylist = wifiManager.getConfiguredNetworks();
		Log.i("WifiPreference","No of Networks "+wifiManager.getDhcpInfo().toString());//+arraylist.size());
		*/
		//wifi = new ArrayList<String>();
		/*
		WifiManager wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> networks=wifiManager.getConfiguredNetworks();
		for (WifiConfiguration config : networks) {
			Log.i("Wifi",config.SSID + " " +getSecurity(config));
			
		}*/
	
		
		StringBuilder strWifiStatus = new StringBuilder(); 
		List<String> dataWifi = new ArrayList<String>();
		WifiManager wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE); 
		List<WifiConfiguration> networks=wifiManager.getConfiguredNetworks(); 
		strWifiStatus.append("Wifi Status:\n"); 
		for (WifiConfiguration config : networks) 
		{
			//Log.i("Wifi",config.SSID + " " +getSecurity(config)); 
			strWifiStatus.append(config.SSID+" ("+getSecurity(config)+")\n");
			dataWifi.add(config.SSID);
			dataWifi.add(getSecurity(config));
		} 
		wifiHistory.setText(strWifiStatus);
		
		List<String> dataApplication = new ArrayList<String>(); 
		final PackageManager pm = getPackageManager(); 
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA); 
		for (ApplicationInfo appInfo : packages) 
		{
			String marketName = getMarket(appInfo.packageName);
			ApplicationInfo lApplicationInfo = null;
			try {
				lApplicationInfo = pm.getApplicationInfo(appInfo.packageName, 0);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String appName = (String) (lApplicationInfo != null ? pm.getApplicationLabel(lApplicationInfo) : "Unknown"); 			
			Log.i("PackageMarket", marketName); 
			dataApplication.add(marketName); 
			dataApplication.add(appName);
		}

		writeWifiCSV(dataWifi);
		writeMarketCSV(dataApplication);
	}
	
	public String getMarket(String packageName){ 
		String market = ""; 
		String installer = getPackageManager().getInstallerPackageName(packageName);
		if (installer == null) 
		{ // change to samsung app store link 
			if (packageName.contains("samsung")) 
			{ 
				market = "Samsung Vendor"; 
			} 
			else if (packageName.contains("lge")) 
			{ 
				market = "LG Vendor"; 
			}
			else if (packageName.contains("sony")) 
			{ 
				market = "Sony Vendor"; 
			}
			else if (packageName.contains("asus")) 
			{ 
				market = "Asus Vendor"; 
			}
			else if (packageName.contains("android")) 
			{ 
				market = "Android Default"; 
			}
			else 
			{
				market = "Unknown Sources";
			}
		}
		else if (installer.contains("android")) 
		{ // change to amazon app store link 
			market = "Google Play"; 
		} 
		else if (installer.contains("amazon")) 
		{ // change to amazon app store link 
			market = "Amazon App"; 
		} else
		{
			market = "Other Market";
		}
		
		return market; 		
	}
	
	public static String getSecurity(WifiConfiguration config) {
	    if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) 
	        return "WPA/WPA2 PSK";

	    if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) 
	    	return "WPA-EAP";

	    return (config.wepKeys[0] != null) ? "WEP" : "NONE";
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public  boolean isSimPinRequired(Context context){
	    TelephonyManager m = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	    if (m.getSimState() == TelephonyManager.SIM_STATE_PIN_REQUIRED) 
	    	{return true;} else
	    {return false;}
	}

	public boolean isGpsEnabled()
	{
	    LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
	    return service.isProviderEnabled(LocationManager.GPS_PROVIDER)&&service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
	
	private static boolean isRooted() {
	    return findBinary("su");
	}
	
	public  boolean isTrustUnknownSource(){
		boolean res = false;
		try {
			res = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return res;
	}
	
	@SuppressLint("NewApi")
    private boolean isEncrypted(Context context) {
		boolean res= false;
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context
                .getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            int status = devicePolicyManager.getStorageEncryptionStatus();
            if (DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE == status) {
                res= true;
            }
        }
        return res;
    }

	public static boolean findBinary(String binaryName) {
	    boolean found = false;
	    if (!found) {
	        String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
	                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
	        for (String where : places) {
	            if ( new File( where + binaryName ).exists() ) {
	            	found = true;
	                break;
	            }
	        }
	    }
	    
	    return found;
	}
	
	public static class LockType
	{
	    private final static String PASSWORD_TYPE_KEY = "lockscreen.password_type";

	    /**
	     * This constant means that android using some unlock method not described here.
	     * Possible new methods would be added in the future releases.
	     */
	    public final static int SOMETHING_ELSE = 0;

	    /**
	     * Android using "None" or "Slide" unlock method. It seems there is no way to determine which method exactly used.
	     * In both cases you'll get "PASSWORD_QUALITY_SOMETHING" and "LOCK_PATTERN_ENABLED" == 0.
	     */
	    public final static int NONE_OR_SLIDER = 1;

	    /**
	     * Android using "Face Unlock" with "Pattern" as additional unlock method. Android don't allow you to select
	     * "Face Unlock" without additional unlock method.
	     */
	    public final static int FACE_WITH_PATTERN = 3;

	    /**
	     * Android using "Face Unlock" with "PIN" as additional unlock method. Android don't allow you to select
	     * "Face Unlock" without additional unlock method.
	     */
	    public final static int FACE_WITH_PIN = 4;

	    /**
	     * Android using "Face Unlock" with some additional unlock method not described here.
	     * Possible new methods would be added in the future releases. Values from 5 to 8 reserved for this situation.
	     */
	    public final static int FACE_WITH_SOMETHING_ELSE = 9;

	    /**
	     * Android using "Pattern" unlock method.
	     */
	    public final static int PATTERN = 10;

	    /**
	     * Android using "PIN" unlock method.
	     */
	    public final static int PIN = 11;

	    /**
	     * Android using "Password" unlock method with password containing only letters.
	     */
	    public final static int PASSWORD_ALPHABETIC = 12;

	    /**
	     * Android using "Password" unlock method with password containing both letters and numbers.
	     */
	    public final static int PASSWORD_ALPHANUMERIC = 13;

	    /**
	     * Returns current unlock method as integer value. You can see all possible values above
	     * @param contentResolver we need to pass ContentResolver to Settings.Secure.getLong(...) and
	     *                        Settings.Secure.getInt(...)
	     * @return current unlock method as integer value
	     */
	    public static int getCurrent(ContentResolver contentResolver)
	    {
	        long mode = android.provider.Settings.Secure.getLong(contentResolver, PASSWORD_TYPE_KEY,
	                DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
	        if (mode == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING)
	        {
	            if (android.provider.Settings.Secure.getInt(contentResolver, Settings.Secure.LOCK_PATTERN_ENABLED, 0) == 1)
	            {
	                return LockType.PATTERN;
	            }
	            else return LockType.NONE_OR_SLIDER;
	        }
	        else if (mode == DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK)
	        {
	            String dataDirPath = Environment.getDataDirectory().getAbsolutePath();
	            if (nonEmptyFileExists(dataDirPath + "/system/gesture.key"))
	            {
	                return LockType.FACE_WITH_PATTERN;
	            }
	            else if (nonEmptyFileExists(dataDirPath + "/system/password.key"))
	            {
	                return LockType.FACE_WITH_PIN;
	            }
	            else return FACE_WITH_SOMETHING_ELSE;
	        }
	        else if (mode == DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC)
	        {
	            return LockType.PASSWORD_ALPHANUMERIC;
	        }
	        else if (mode == DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC)
	        {
	            return LockType.PASSWORD_ALPHABETIC;
	        }
	        else if (mode == DevicePolicyManager.PASSWORD_QUALITY_NUMERIC)
	        {
	            return LockType.PIN;
	        }
	        else return LockType.SOMETHING_ELSE;
	    }

	    private static boolean nonEmptyFileExists(String filename)
	    {
	        File file = new File(filename);
	        return file.exists() && file.length() > 0;
	    }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void writeWifiCSV(List<String> data){
		String columnString =   "\"SSID\",\"Security";
		StringBuilder strBuilder = new StringBuilder();
		for(int i=0;i<(data.size()-1);i=i+2){
			strBuilder.append("\""+data.get(i)+"\",\""+data.get(i+1)+"\n");
		}
		String combinedString = columnString+ "\n" + strBuilder;
		
		File file   = null;
		File root   = Environment.getExternalStorageDirectory();
		Log.i("datax", "writeCSV");
		if (root.canWrite()){
		    File dir    =   new File (root.getAbsolutePath());
		    Log.i("datax", root.getAbsolutePath().toString());
		     dir.mkdirs();
		     file   =   new File(dir, "DataWifi.csv");
		     FileOutputStream out   =   null;
		    try {
		        out = new FileOutputStream(file);
		        Log.i("datax", "FileOutputStream");
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }
		    
	        try {
	            out.write(combinedString.getBytes());
	            Log.i("datax", "columnString");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        try {
	            out.close();
	            Log.i("datax", "close");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	}
	
	public void writeMarketCSV(List<String> data){
		String columnString =   "\"App Source\",\"App Name";
		StringBuilder strBuilder = new StringBuilder();
		for(int i=0;i<(data.size()-1);i=i+2){
			strBuilder.append("\""+data.get(i)+"\",\""+data.get(i+1)+"\n");
		}
		String combinedString = columnString+ "\n" + strBuilder;
		
		File file   = null;
		File root   = Environment.getExternalStorageDirectory();
		Log.i("datax", "writeCSV");
		if (root.canWrite()){
		    File dir    =   new File (root.getAbsolutePath());
		    Log.i("datax", root.getAbsolutePath().toString());
		     dir.mkdirs();
		     file   =   new File(dir, "DataMarket.csv");
		     FileOutputStream out   =   null;
		    try {
		        out = new FileOutputStream(file);
		        Log.i("datax", "FileOutputStream");
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }
		    
	        try {
	            out.write(combinedString.getBytes());
	            Log.i("datax", "columnString");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        try {
	            out.close();
	            Log.i("datax", "close");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	}
	
	public void writeCSV(List<String> data){
		String columnString =   "\"Status\",\"Value";
		StringBuilder strBuilder = new StringBuilder();
		for(int i=0;i<(data.size()-1);i=i+2){
			strBuilder.append("\""+data.get(i)+"\",\""+data.get(i+1)+"\n");
		}
		String combinedString = columnString+ "\n" + strBuilder;
		
		File file   = null;
		File root   = Environment.getExternalStorageDirectory();
		Log.i("datax", "writeCSV");
		if (root.canWrite()){
		    File dir    =   new File (root.getAbsolutePath());
		    Log.i("datax", root.getAbsolutePath().toString());
		     dir.mkdirs();
		     file   =   new File(dir, "Data.csv");
		     FileOutputStream out   =   null;
		    try {
		        out = new FileOutputStream(file);
		        Log.i("datax", "FileOutputStream");
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }
		    
	        try {
	            out.write(combinedString.getBytes());
	            Log.i("datax", "columnString");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        try {
	            out.close();
	            Log.i("datax", "close");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	}
}
