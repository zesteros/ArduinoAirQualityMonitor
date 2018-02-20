package angelo.itl.arduinoairqualitymonitor.util.graphic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import angelo.itl.arduinoairqualitymonitor.R;

/**
 * Created by Angelo on 07/12/2016.
 */

public class LRUCache {
    /*Testing LRU Cache for gas indicator*/
    private LruCache<String, Bitmap> mMemoryCache;
    private Context context;
    private ImageView imageToProcess;
    private int height, weight;

    public LRUCache(Context context) {
        this.context = context;
    }

    public void setImageSize(int height, int weight){
        this.height = height;
        this.weight = weight;
    }
    public int getHeight(){
        return this.height;
    }
    public int getWeight(){
        return this.weight;
    }


    public void createLRUCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/16th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 16;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void loadBitmap(int resId, ImageView imageView) {
        final String imageKey = String.valueOf(resId);

        final Bitmap bitmap = getBitmapFromMemCache(imageKey);
        if (bitmap != null) {
            getImageToProcess().setImageBitmap(bitmap);
        } else {
            getImageToProcess().setImageResource(resId);
            BitmapWorkerTask task = new BitmapWorkerTask(getImageToProcess());
            task.execute(resId);
        }
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        //params[0] is the key of bitmap
        @Override
        protected Bitmap doInBackground(Integer... params) {
            final Bitmap bitmap = decodeSampledBitmapFromResource(
                    context.getResources(), params[0], getHeight(), getWeight());
            addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
            return bitmap;
        }
        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null)
                    imageView.setImageBitmap(bitmap);
            }
        }

    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    public ImageView getImageToProcess() {
        return imageToProcess;
    }

    public void setImageToProcess(ImageView imageToProcess) {
        this.imageToProcess = imageToProcess;
    }
}
