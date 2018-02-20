package angelo.itl.arduinoairqualitymonitor.adapter.main;

import android.content.*;
import android.view.*;
import android.widget.*;

import angelo.itl.arduinoairqualitymonitor.*;

public class CustomListAdapter extends BaseAdapter {
    private Context context;
    private String[] nameProp;
    private String[] value;
    private String[] valueNormal;
    private String[] valueHazard;
    private String[] valueAvg;

    LayoutInflater inflater;

    public CustomListAdapter(Context context, String[] nameProp, String[] value,
                             String[] valueNormal, String[] valueHazard, String [] valueAvg) {
        this.context = context;
        this.nameProp = nameProp;
        this.value = value;
        this.valueNormal = valueNormal;
        this.valueHazard = valueHazard;
        this.valueAvg = valueAvg;
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
        TextView normalValue;
        TextView hazardValue;
        TextView avg;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(R.layout.list_row, parent, false);

        title = (TextView) itemView.findViewById(R.id.name_prop);
        valueRead = (TextView) itemView.findViewById(R.id.value_read);
        normalValue = (TextView) itemView.findViewById(R.id.value_limit_normal);
        hazardValue = (TextView) itemView.findViewById(R.id.value_limit_hazard);
        avg = (TextView) itemView.findViewById(R.id.average);


        title.setText(nameProp[position]);
        title.setClickable(false);
        valueRead.setText(value[position]+"");
        normalValue.setText(valueNormal[position]);
        hazardValue.setText(valueHazard[position]);
        avg.setText(valueAvg[position]);

        return itemView;
    }
}
