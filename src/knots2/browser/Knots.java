package knots2.browser;

import android.app.Application;

public class Knots extends Application {

    
	private String password = "camer0n";
	private String username = "andyleadbetter";
	
	private String apiKey = "32oc8ooo7ea2a";
	private String maxInactiveTime;
	private String orbVersion;
	private String sessionId;
			
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
