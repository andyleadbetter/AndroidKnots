package knots2.browser;

public class Profile {
	private String id;
	private String name;
	private String codec;
	private String bitrate;
	
	public synchronized String getId() {
		return id;
	}
	public synchronized void setId(String id) {
		this.id = id;
	}
	public synchronized String getName() {
		return name;
	}
	public synchronized void setName(String name) {
		this.name = name;
	}
	public synchronized String getCodec() {
		return codec;
	}
	public synchronized void setCodec(String codec) {
		this.codec = codec;
	}
	public synchronized String getBitrate() {
		return bitrate;
	}
	public synchronized void setBitrate(String bitrate) {
		this.bitrate = bitrate;
	}
	public synchronized int getIntegerId() {
		return Integer.valueOf( id );
	}
	
}
