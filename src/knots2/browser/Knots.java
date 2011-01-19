package knots2.browser;

import javax.security.auth.PrivateCredentialPermission;

import android.R.string;
import android.app.Application;

public class Knots extends Application {


	public final static String SEARCHID = "com.knots.browser.searchId";
	
	public final static String MEDIAID = "com.knots.browser.mediaId";

	public final static String ACTIONID = "com.knots.browser.action";
	
	public final static String PATHID = "com.knots.browser.action";
	

	
	private String password = "camer0n";
	private String username = "andyleadbetter";	
	private String apiKey = "32oc8ooo7ea2a";
	private String maxInactiveTime;
	private String orbVersion;
	private String sessionId;
	private String mWidth="800";
	private String mHeight="480";
	private String mSpeed="1200";
	
			
	public String getWidth() {
		return mWidth;
	}
	public String getHeight() {		
		return mHeight;
	}
	public String getBitrate() {
		return mSpeed;
	}

	/**
	 * @param mWidth the mWidth to set
	 */
	public void setWidth(String mWidth) {
		this.mWidth = mWidth;
	}
	/**
	 * @return the mHeight
	 */
	
	/**
	 * @param mHeight the mHeight to set
	 */
	public void setHeight(String mHeight) {
		this.mHeight = mHeight;
	}
	
	/**
	 * @param mSpeed the mSpeed to set
	 */
	public void setSpeed(String mSpeed) {
		this.mSpeed = mSpeed;
	}
	
	/**
	 * @return the maxInactiveTime
	 */
	public String getMaxInactiveTime() {
		return maxInactiveTime;
	}
	/**
	 * @param maxInactiveTime the maxInactiveTime to set
	 */
	public void setMaxInactiveTime(String maxInactiveTime) {
		this.maxInactiveTime = maxInactiveTime;
	}
	/**
	 * @return the orbVersion
	 */
	public String getOrbVersion() {
		return orbVersion;
	}
	/**
	 * @param orbVersion the orbVersion to set
	 */
	public void setOrbVersion(String orbVersion) {
		this.orbVersion = orbVersion;
	}
	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}
	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	/**
	 * @return the apikey
	 */
	public final String getApiKey() {
		return apiKey;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	
	
}
