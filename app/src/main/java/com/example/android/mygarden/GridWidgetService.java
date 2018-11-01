package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;

/**
 * Created by Pawan Khandal on 10/31/18,16
 */
class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    Context mContext;
    Cursor mCursor;
    
    public GridRemoteViewsFactory(Context context) {
        mContext = context;
    }
    
    @Override
    public void onCreate() {
    
    }
    
    @Override
    public void onDataSetChanged() {
        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        if (mCursor !=null) mCursor.close();
        
        mCursor = mContext.getContentResolver().query(PLANT_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_CREATION_TIME);
        
    }
    
    @Override
    public void onDestroy() {
        mCursor.close();
    }
    
    @Override
    public int getCount() {
        if (mCursor.getCount()==0) return 0;
        return mCursor.getCount();
        
    }
    
    @Override
    public RemoteViews getViewAt(int position) {
        if(mCursor == null && mCursor.getCount() == 0) return null;
        mCursor.moveToPosition(position);
        int idIndex = mCursor.getColumnIndex(PlantContract.PlantEntry._ID);
        int creationTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
        int waterTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
        int plantTypeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
        
        long plantId = mCursor.getLong(idIndex);
        int plantType = mCursor.getInt(plantTypeIndex);
        long plantCreationTime = mCursor.getLong(creationTimeIndex);
        long plantWaterTime = mCursor.getLong(waterTimeIndex);
        long timeNow = System.currentTimeMillis();
        
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(),R.layout.plant_widget);

        //Update the plant Image
        int img = PlantUtils.getPlantImageRes(mContext,
                timeNow-plantCreationTime,timeNow-plantWaterTime,plantType);
        
        remoteViews.setImageViewResource(R.id.widget_plant_image,img);
        remoteViews.setTextViewText(R.id.widget_plant_name,String.valueOf(plantId));
        
        remoteViews.setViewVisibility(R.id.water_button, View.GONE);
    
        //set the data to pending intnet
        Bundle bundle = new Bundle();
        bundle.putLong(PlantDetailActivity.EXTRA_PLANT_ID,plantId);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(bundle);
        remoteViews.setOnClickFillInIntent(R.id.widget_plant_image,fillInIntent);
        return remoteViews;
    }
    
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }
    
    @Override
    public int getViewTypeCount() {
        return 0;
    }
    
    @Override
    public long getItemId(int position) {
        return 0;
    }
    
    @Override
    public boolean hasStableIds() {
        return false;
    }
}

public class GridWidgetService extends RemoteViewsService{
    
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}
