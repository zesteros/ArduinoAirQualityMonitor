package angelo.itl.arduinoairqualitymonitor.activity.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import angelo.itl.arduinoairqualitymonitor.activity.airmonitor.AirMonitorValues;
import angelo.itl.arduinoairqualitymonitor.util.sensor.RatioGasComputer;


public class CustomSeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener, OnClickListener, AirMonitorValues{

    private static final String androidns="http://schemas.android.com/apk/res/android";

    private SeekBar mSeekBar;
    private TextView mSplashText,mValueText;
    private Context mContext;
    private float[] values;
    private RatioGasComputer ratio;

    private String mDialogMessage, mSuffix;
    private int mDefault;
    private int mMax;
    private int mValue = 0;

    public CustomSeekBarPreference(Context context, AttributeSet attrs) {

        super(context,attrs); 
        mContext = context;

        // Get string value for dialogMessage :
        int mDialogMessageId = attrs.getAttributeResourceValue(androidns, "dialogMessage", 0);
        if(mDialogMessageId == 0) mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
        else mDialogMessage = mContext.getString(mDialogMessageId);

        // Get string value for suffix (text attribute in xml file) :
        int mSuffixId = attrs.getAttributeResourceValue(androidns, "text", 0);
        if(mSuffixId == 0) mSuffix = attrs.getAttributeValue(androidns, "text");
        else mSuffix = mContext.getString(mSuffixId);

        // Get default and max seekbar values :
        mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
        setMax(attrs.getAttributeIntValue(androidns, "max", 100));
        ratio = new RatioGasComputer(context);
    }

    @Override 
    protected View onCreateDialogView() {

        LinearLayout.LayoutParams params;
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6,6,6,6);

        mSplashText = new TextView(mContext);
        mSplashText.setPadding(30, 10, 30, 10);
        if (mDialogMessage != null)
            mSplashText.setText(mDialogMessage);
        layout.addView(mSplashText);

        mValueText = new TextView(mContext);
        mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueText.setTextSize(15);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mValueText, params);

        mSeekBar = new SeekBar(mContext);
        mSeekBar.setOnSeekBarChangeListener(this);
        layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (shouldPersist())
            mValue = getPersistedInt(mDefault);

        mSeekBar.setMax(getMax());
        mSeekBar.setProgress(mValue);

        return layout;
    }

    @Override 
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        mSeekBar.setMax(getMax());
        mSeekBar.setProgress(mValue);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) 
            mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
            else 
                mValue = (Integer)defaultValue;
    }
    @Override
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        //String t = String.valueOf(value);
        //mValueText.setText(mSuffix == null ? t : t.concat(" " + mSuffix));
        //computeGasLimitsAccordingRatio(value, normalValues);
        mValueText.setText(ratio.getGasRatioAsString(value, getDefaultValues()));
    }


    @Override
    public void onStartTrackingTouch(SeekBar seek) {}
    @Override
    public void onStopTrackingTouch(SeekBar seek) {}

    public void setProgress(int progress) { 
        mValue = progress;
        if (mSeekBar != null)
            mSeekBar.setProgress(progress); 
    }

    public void setDefaultValues(float[] values){
        this.values = values;
    }

    public float[] getDefaultValues(){
        return this.values;
    }

    public int getProgress() { return mValue; }

    @Override
    public void showDialog(Bundle state) {

        super.showDialog(state);

        Button positiveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (shouldPersist()) {
            mValue = mSeekBar.getProgress();
            persistInt(mSeekBar.getProgress());
            callChangeListener(Integer.valueOf(mSeekBar.getProgress()));
        }
        ((AlertDialog) getDialog()).dismiss();
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int mMax) {
        this.mMax = mMax;
    }
}