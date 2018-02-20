package angelo.itl.arduinoairqualitymonitor.adapter.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import angelo.itl.arduinoairqualitymonitor.R;

public class CustomListAdapterSimple extends BaseAdapter {
    private Context context;
    private String[] nameProp;
    private String[] value;

    LayoutInflater inflater;

    public CustomListAdapterSimple(Context context, String[] nameProp, String[] value) {
        this.context = context;
        this.nameProp = nameProp;
        this.value = value;
    }

    @Override
    public int getCount() {
        return nameProp.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView title;
        TextView valueRead;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(R.layout.list_row_simple, parent, false);

        title = (TextView) itemView.findViewById(R.id.name_prop);
        valueRead = (TextView) itemView.findViewById(R.id.value_read);

        title.setText(nameProp[position]);
        title.setClickable(false);
        valueRead.setText(value[position]);

        return itemView;
    }
}
