package angelo.itl.arduinoairqualitymonitor.adapter.table;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import angelo.itl.arduinoairqualitymonitor.R;
import angelo.itl.arduinoairqualitymonitor.activity.table.TableActivity;
import angelo.itl.arduinoairqualitymonitor.database.DataBaseController;
import angelo.itl.arduinoairqualitymonitor.util.graphic.TableFixHeaders;

public class MatrixTableAdapter<T> extends BaseTableAdapter {

    private final static int WIDTH_DIP = 110;
    private final static int HEIGHT_DIP = 40;

    private final Context context;

    private T[][] table;

    private final int width;
    private final int height;

    private int color;

    private int background;

    private boolean withBackground;
    private boolean allFeatures;

    public MatrixTableAdapter(Context context) {
        this(context, null, false);
    }

    public MatrixTableAdapter(Context context, T[][] table, boolean allFeatures) {
        this.context = context;
        Resources r = context.getResources();

        width = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, WIDTH_DIP, r.getDisplayMetrics()));
        height = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT_DIP, r.getDisplayMetrics()));

        setInformation(table);
        setTextColor(R.color.black);
        setBackground(R.drawable.grid_shape);
        setWithBackground(true);
        this.allFeatures = allFeatures;
    }

    public void setInformation(T[][] table) {
        this.table = table;
    }

    @Override
    public int getRowCount() {
        return table.length - 1;
    }

    @Override
    public int getColumnCount() {
        return table[0].length - 1;
    }

    public void setTextColor(int color) {
        this.color = color;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public int getTextColor() {
        return color;
    }

    public int getBackground() {
        return background;
    }

    @Override
    public View getView(final int row, final int column, View convertView, ViewGroup parent) {

        try {
            if (convertView == null) {
                convertView = new TextView(context);
                ((TextView) convertView).setTextColor(context.getResources().getColor(getTextColor()));
                if (isWithBackground())
                    convertView.setBackground(context.getResources().getDrawable(getBackground()));
                ((TextView) convertView).setGravity(Gravity.CENTER);
            }
            ((TextView) convertView).setText(table[row + 1][column + 1].toString());
            if (row + 1 != 0 && allFeatures)
                ((TextView) convertView).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showLongClickDialog(row);
                        return false;
                    }
                });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public int getHeight(int row) {
        return height;
    }

    @Override
    public int getWidth(int column) {
        return width;
    }

    @Override
    public int getItemViewType(int row, int column) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    public String getElementContent(int row, int column) {
        String element = "";
        try {
            element = table[row + 1][column + 1].toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return element;
    }

    public void showLongClickDialog(final int row) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(20);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogLight);
        builder.setTitle(table[row + 1][0] + "");
        builder.setItems(new CharSequence[]{
                        context.getString(R.string.delete_register),
                        context.getString(R.string.show_register_details)
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        switch (position) {
                            case 0:
                                ((TableActivity) context)
                                        .deleteRegister(table[row + 1][0] + "");
                                Snackbar
                                        .make(((TableActivity) context)
                                                        .findViewById(android.R.id.content),
                                                context.getString(R.string.register_deleted_succesfully),
                                                Snackbar.LENGTH_SHORT).show();
                                break;
                            case 1:
                                DataBaseController dbController = new DataBaseController(context);
                                dbController.open();
                                TableFixHeaders table = new TableFixHeaders(context);
                                MatrixTableAdapter adapter =
                                        new MatrixTableAdapter<T>(context,
                                                (T[][]) buildRegisterDetails(row, dbController), false);
                                adapter.setWithBackground(false);
                                table.setAdapter(adapter);
                                AlertDialog.Builder details = new AlertDialog.Builder(context, R.style.AlertDialogLight);
                                details
                                        .setTitle(R.string.register_details)
                                        .setView(table)
                                        .setPositiveButton(R.string.alert_dialog_accept, null)
                                        .show();
                                dbController.close();
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    public String[][] buildRegisterDetails(int row, DataBaseController dbController) {
        String[][] matrix = new String[10][5];
        String[] headers = context.getResources().getStringArray(R.array.details_dialog);
        for (int i = 0; i < 5; i++)
            matrix[0][i] = headers[i];
        int k = 1;
        for (int i = 0; i < 9; i++) {
            matrix[k][0] = table[0][i + 1] + "";
            matrix[k][1] = table[row + 1][i + 1] + "";
            matrix[k][2] = dbController.getAllAverageByDate(table[row + 1][0] + "")[i] + "";
            matrix[k][3] = dbController.getAllMaxMinByDate(table[row + 1][0] + "", true)[i] + "";
            matrix[k][4] = dbController.getAllMaxMinByDate(table[row + 1][0] + "", false)[i] + "";
            k++;
        }
        return matrix;
    }

    public boolean isWithBackground() {
        return withBackground;
    }

    public void setWithBackground(boolean withBackground) {
        this.withBackground = withBackground;
    }

    public boolean isAllFeatures() {
        return allFeatures;
    }

    public void setAllFeatures(boolean allFeatures) {
        this.allFeatures = allFeatures;
    }
}