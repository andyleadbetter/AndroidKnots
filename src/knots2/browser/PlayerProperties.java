package knots2.browser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.io.CharArrayWriter;
import org.xml.sax.helpers.DefaultHandler;

import android.net.Uri;


public class PlayerProperties  extends DefaultHandler {

		// ===========================================================
		// Fields
		// ===========================================================

		private String _address;

		// ===========================================================
		// Getter & Setter
		// ===========================================================
		
		private String _buffer;
		private String _currently_playing_filename;
	    private String _looped;
	    private String _media_id;
	    private String _mediatype;
	    private String _mux;
	    private String _password;
	    private String _playerId;
	    private String _playlist_length;
	    private String _playlistindex;
	    private String _port;
	    private String _seekable;
	    private String _stream;
	    private Uri    _streamUrl;
		private String _title;
	    private String _video_height;
	    private String _video_width;
	    // Buffer for collecting data from
		// the "characters" SAX event.
		private CharArrayWriter xmlContentArray = new CharArrayWriter();
	    int     _duration;
	    float    _position;
	    
	    
	    
	    /** Gets be called on the following structure: 
		 * <tag>characters</tag> */
		@Override
		public void characters(char ch[], int start, int length) {		
			xmlContentArray.write(ch, start, length);
		}

		@Override
		public void endDocument() throws SAXException {

			String url;
	        
			url = "rtsp://";
	        
	        if( _playerId != null )
	        {
	        	url += _playerId + ":" + _password + "@";
	        }
	        
	        url += "192.168.0.28:8080";
	        url += "/stream.sdp";
	        
	        _streamUrl = Uri.parse(url);
	        
		}

		/** Gets be called on closing tags like: 
		 * </tag> */
		@Override
		public void endElement(String namespaceURI, String localName, String qName)
		throws SAXException {

			String contents = xmlContentArray.toString();
			
			if( localName == "seekable" ) {
				_seekable = contents;
			} else if ( localName == "mediatype" ) {
				_mediatype = contents;
			} else if ( localName == "title" ) {
				_title = contents;
			} else if ( localName == "position" ) {
				_position = Float.parseFloat(contents);
			} else if ( localName == "media_id" ) {
				_media_id = contents;
			} else if ( localName == "playlistindex" ) {
				_playlistindex = contents;
			} else if ( localName == "playlist_length" ) {
				_playlist_length = contents;
			} else if ( localName == "currently_playing_filename" ) {
				_currently_playing_filename = contents;
			} else if ( localName == "video_width" ) {
				_video_width = contents;
			} else if ( localName == "video_height" ) {
				_video_height = contents;
			} else if ( localName == "mux" ) {
				_mux = contents;
			} else if ( localName == "stream" ) {
				_stream = contents;
			} else if ( localName == "port" ) {
				_port = contents;
			} else if ( localName == "buffer" ) {
				_buffer = contents;
			} else if ( localName == "address" ) {
				_address = contents;
			} else if ( localName == "looped" ) {
				_looped = contents;
			} else if ( localName == "duration") {
				_duration = Integer.parseInt(contents)*1000;
			}

		}

		/**
		 * @return the _address
		 */
		public String get_address() {
			return _address;
		}

		/**
		 * @return the _buffer
		 */
		public String get_buffer() {
			return _buffer;
		}

		/**
		 * @return the _currently_playing_filename
		 */
		public String get_currently_playing_filename() {
			return _currently_playing_filename;
		}

		/**
		 * @return the _duration
		 */
		public int get_duration() {
			return _duration;
		}

		/**
		 * @return the _looped
		 */
		public String get_looped() {
			return _looped;
		}

		/**
		 * @return the _media_id
		 */
		public String get_media_id() {
			return _media_id;
		}

		/**
		 * @return the _mediatype
		 */
		public String get_mediatype() {
			return _mediatype;
		}

		/**
		 * @return the _mux
		 */
		public String get_mux() {
			return _mux;
		}

		/**
		 * @return the _password
		 */
		public String get_password() {
			return _password;
		}

		/**
		 * @return the _playerId
		 */
		public String get_playerId() {
			return _playerId;
		}

		/**
		 * @return the _playlist_length
		 */
		public String get_playlist_length() {
			return _playlist_length;
		}

		/**
		 * @return the _playlistindex
		 */
		public String get_playlistindex() {
			return _playlistindex;
		}

		/**
		 * @return the _port
		 */
		public String get_port() {
			return _port;
		}

		/**
		 * @return the _position
		 */
		public float get_position() {
			return _position;
		}

		/**
		 * @return the _seekable
		 */
		public String get_seekable() {
			return _seekable;
		}

		/**
		 * @return the _stream
		 */
		public String get_stream() {
			return _stream;
		}

		/**
		 * @return the _streamUrl
		 */
		public Uri get_streamUrl() {
			return _streamUrl;
		}

		/**
		 * @return the _title
		 */
		public String get_title() {
			return _title;
		}

		/**
		 * @return the _video_height
		 */
		public String get_video_height() {
			return _video_height;
		}

		/**
		 * @return the _video_width
		 */
		public String get_video_width() {
			return _video_width;
		}

		/**
		 * @param _address the _address to set
		 */
		public void set_address(String _address) {
			this._address = _address;
		}

		/**
		 * @param _buffer the _buffer to set
		 */
		public void set_buffer(String _buffer) {
			this._buffer = _buffer;
		}

		/**
		 * @param _currently_playing_filename the _currently_playing_filename to set
		 */
		public void set_currently_playing_filename(String _currently_playing_filename) {
			this._currently_playing_filename = _currently_playing_filename;
		}

		/**
		 * @param _duration the _duration to set
		 */
		public void set_duration(int _duration) {
			this._duration = _duration;
		}

		/**
		 * @param _looped the _looped to set
		 */
		public void set_looped(String _looped) {
			this._looped = _looped;
		}

		/**
		 * @param _media_id the _media_id to set
		 */
		public void set_media_id(String _media_id) {
			this._media_id = _media_id;
		}

		/**
		 * @param _mediatype the _mediatype to set
		 */
		public void set_mediatype(String _mediatype) {
			this._mediatype = _mediatype;
		}

		/**
		 * @param _mux the _mux to set
		 */
		public void set_mux(String _mux) {
			this._mux = _mux;
		}

		/**
		 * @param _password the _password to set
		 */
		public void set_password(String _password) {
			this._password = _password;
		}

		/**
		 * @param _playerId the _playerId to set
		 */
		public void set_playerId(String _playerId) {
			this._playerId = _playerId;
		}

		/**
		 * @param _playlist_length the _playlist_length to set
		 */
		public void set_playlist_length(String _playlist_length) {
			this._playlist_length = _playlist_length;
		}

		/**
		 * @param _playlistindex the _playlistindex to set
		 */
		public void set_playlistindex(String _playlistindex) {
			this._playlistindex = _playlistindex;
		}

		/**
		 * @param _port the _port to set
		 */
		public void set_port(String _port) {
			this._port = _port;
		}

		/**
		 * @param _position the _position to set
		 */
		public void set_position(float _position) {
			this._position = _position;
		}

		/**
		 * @param _seekable the _seekable to set
		 */
		public void set_seekable(String _seekable) {
			this._seekable = _seekable;
		}


		
		/**
		 * @param _stream the _stream to set
		 */
		public void set_stream(String _stream) {
			this._stream = _stream;
		}

		/**
		 * @param _title the _title to set
		 */
		public void set_title(String _title) {
			this._title = _title;
		}

		/**
		 * @param _video_height the _video_height to set
		 */
		public void set_video_height(String _video_height) {
			this._video_height = _video_height;
		}

		/**
		 * @param _video_width the _video_width to set
		 */
		public void set_video_width(String _video_width) {
			this._video_width = _video_width;
		}

		// ===========================================================
		// Methods
		// ===========================================================
		@Override
		public void startDocument() throws SAXException {
		}

		/** Gets be called on opening tags like: 
		 * <tag> 
		 * Can provide attribute(s), when xml was like:
		 * <tag attribute="attributeValue">*/
		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {

			xmlContentArray.reset();
		}
	}
