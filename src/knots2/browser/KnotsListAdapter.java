package knots2.browser;

import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class KnotsListAdapter extends BaseAdapter {
	
	ImageDownloader mImageDownloader;
       
    private Activity activity;
    /**
	 * @return the activity
	 */
	public Activity getActivity() {
		return activity;
	}

	private Vector<KnotsItem> data;
    private static LayoutInflater inflater=null;
    
    public KnotsListAdapter( Activity a, ImageDownloader imageDownloader  ) {
        activity = a;
        mImageDownloader = imageDownloader;
        data = new Vector<KnotsItem>();        
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);        
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public void addItem( KnotsItem newItem ) {
    	data.add(newItem);
    	notifyDataSetChanged();
    }
    
    public void clear() {
    	data.clear();
    	notifyDataSetChanged();
    }
    
    public long getItemId(int position) {
        return position;
    }
    
    public static class ViewHolder{
        public TextView text;
        public ImageView image;
        public KnotsItem item;
    }
    
    @Override
    public boolean hasStableIds(){
    	return false;    	
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        ViewHolder holder;
        KnotsItem item = data.elementAt(position);
        
        if(convertView==null){
            vi = inflater.inflate(R.layout.item, null);
            holder=new ViewHolder();
            holder.text=(TextView)vi.findViewById(R.id.text);;
            holder.image=(ImageView)vi.findViewById(R.id.image);            
            vi.setTag(holder);
        }
        else
            holder=(ViewHolder)vi.getTag();

        if( item.getType() == KnotsItem.VIRTUAL )
        {
        	holder.image.setImageDrawable( activity.getResources().getDrawable(R.drawable.knots_dir));
        }
        else if( ( item.getType() == KnotsItem.ITEM ) && item.getItemImage() != null) {
        	holder.image.setTag(item.getItemImage());        	
            mImageDownloader.download(item.getItemImage(), (ImageView) holder.image);
        }
        
        holder.text.setText(item.getFields().get("title"));        
        holder.item = item;
                
        return vi;
    }
}