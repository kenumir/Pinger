package com.wt.pinger.proto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.wt.pinger.R;


public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };

    private static final int[] DISABLED_STATE_SET = {
        -android.R.attr.state_enabled
    };

    private boolean checked = false,
		    autoCheck = false,
		    populateEnabledState = true,
		    interceptTouchEvent = true;
	private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    @SuppressLint("NewApi")
    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CheckableRelativeLayout);
        checked = ta.getBoolean(R.styleable.CheckableRelativeLayout_marked, false);
	    autoCheck = ta.getBoolean(R.styleable.CheckableRelativeLayout_autocheck, false);
        ta.recycle();
	    init();
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CheckableRelativeLayout);
        checked = ta.getBoolean(R.styleable.CheckableRelativeLayout_marked, false);
	    autoCheck = ta.getBoolean(R.styleable.CheckableRelativeLayout_autocheck, false);
	    populateEnabledState = ta.getBoolean(R.styleable.CheckableRelativeLayout_populateEnabledState, true);
        ta.recycle();
	    init();
    }

	private void init() {
		if (autoCheck) {
			setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					toggle();
				}
			});
		}
	}

	// get all touch events
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return interceptTouchEvent;
	}

    @Override
    public void setEnabled(boolean enabled) {
    	super.setEnabled(enabled);
    	refreshDrawableState();
	    if (populateEnabledState) {
		    updateEnabled(this, enabled);
	    }
    }

	private void updateEnabled(ViewGroup vg, boolean enabled) {
		final int count = vg.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = vg.getChildAt(i);
			child.setEnabled(enabled);

			if (child instanceof ViewGroup)
				updateEnabled((ViewGroup) child, enabled);
		}
	}

    public CheckableRelativeLayout(Context context) {
        super(context);
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

	@Override
	public void setChecked(boolean checked) {
		setChecked(checked, false);
	}

    public void setChecked(boolean checked, boolean noListenerCall) {
        this.checked = checked;
        
        refreshDrawableState();
    
        //Propagate to child's
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if(child instanceof Checkable) {
                ((Checkable)child).setChecked(checked);
            } else {
	            if (child instanceof ViewGroup) {
		            int c = ((ViewGroup) child).getChildCount();
		            for (int ii = 0; ii < c; ii++) {
			            final View cc = ((ViewGroup) child).getChildAt(ii);
			            if(cc instanceof Checkable) {
				            if (noListenerCall && cc instanceof SwitchCompat) {
					            ((SwitchCompat) cc).setChecked(checked);
				            } else {
					            ((Checkable) cc).setChecked(checked);
				            }
			            }
		            }
	            }
            }
        }
	    
	    if (!noListenerCall && getOnCheckedChangeListener() != null)
		    getOnCheckedChangeListener().onCheckedChanged(null, this.checked);
    }
    
    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isEnabled()) {
	        if (isChecked()) {
	            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
	        }
        } else {
        	mergeDrawableStates(drawableState, DISABLED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public void toggle() {
        this.checked = !this.checked;
	    setChecked(this.checked);
    }

	public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener) {
		this.mOnCheckedChangeListener = mOnCheckedChangeListener;
	}

	public CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener() {
		return mOnCheckedChangeListener;
	}

	public void setPopulateEnabledState(boolean e) {
		populateEnabledState = e;
	}

	public boolean getPopulateEnabledState() {
		return populateEnabledState;
	}

	/**
	 * możliwość wyłączenie przejmowania eventu touch (możliwość na klikanie w kontrolki wewnątrz widoku)
	 * @param r true - cannot click, false - child can be clicked
	 */
	public void setInterceptTouchEvent(boolean r) {
		interceptTouchEvent = r;
	}

	public boolean getInterceptTouchEvent() {
		return interceptTouchEvent;
	}

}
