package com.bitfighters.maptool.maptoolinput.properties;

import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bitfighters.maptool.maptoolinput.MainTab;

import net.rptools.maptool.model.GUID;

/**
 * Created by admin on 11.03.2017.
 */

public abstract class PropertyViewFactory {

    public static PropertyViewFactory defaultFactory = new DefaultPropertyFactory();

    public static PropertyViewFactory getFactoryByName(String name){
        if(name == null)
            return  defaultFactory;
        else if(name.equals("simple"))
            return  defaultFactory;
        else
            return  defaultFactory;
    }



    public View createViewFor(MainTab parent, final String property, String currentValue, boolean showAsHidden, boolean showHideButton, GUID token, GUID zone){

        View child = createViewForInternal( parent, property, currentValue, showAsHidden, showHideButton, token, zone);
        if(child == null)
            return null;

        child.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        v);
                v.startDrag(null, shadowBuilder, v, 0);
                v.setVisibility(View.INVISIBLE);
                PropertySettings.getInstance().setDraggingProperty(property);
                return true;
            }
        });

        child.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                int action = event.getAction();
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // do nothing
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        //v.setBackgroundDrawable(enterShape);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        //v.setBackgroundDrawable(normalShape);
                        break;
                    case DragEvent.ACTION_DROP:
                        // Dropped, reassign View to ViewGroup
                        View view = (View) event.getLocalState();
                        ViewGroup owner = (ViewGroup) view.getParent();
                        owner.removeView(view);
                        LinearLayout container = (LinearLayout) owner;
                        int newPos = PropertySettings.getInstance().setDraggingPropertyAt(property);
                        container.addView(view,newPos);
                        view.setVisibility(View.VISIBLE);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        //v.setBackgroundDrawable(normalShape);
                    default:
                        break;
                }
                return true;
            }
        });

        return child;
    }
    View createViewForInternal(MainTab parent, String property, String currentValue, boolean showAsHidden, boolean showHideButton, GUID token, GUID zone){return null;}
    public abstract String getName();

}
