package ui.lockscreen;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.ArrayList;
import java.util.List;

public class RNLockScreen extends ViewGroupManager<ViewGroup> {

    public static final String REACT_CLASS = "RNLockScreen";

    private ThemedReactContext context = null;
    private FrameLayout frameLayout = null;

    private PatternLockView patternLockView = null;

    private int lock = -1;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected FrameLayout createViewInstance(final ThemedReactContext reactContext) {
        context = reactContext;

        frameLayout = new FrameLayout(reactContext);
        patternLockView = new PatternLockView(context);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        patternLockView.setLayoutParams(params);
        patternLockView.setTactileFeedbackEnabled(true);
        patternLockView.addPatternLockListener(patternLockViewListener);

        frameLayout.addView(patternLockView);

        return frameLayout;
    }

    @ReactProp(name = "props")
    public void props(final FrameLayout frameLayout, ReadableMap props) {
        ReadableMapKeySetIterator keySetIterator = props.keySetIterator();
        while (keySetIterator.hasNextKey()) {
            String key = keySetIterator.nextKey();
            if (key.equals("width")) {
                patternLockView.setMinimumWidth(props.getInt("width") + 1000);
            }
            else if (key.equals("dotCount")) {
                patternLockView.setDotCount(props.getInt("dotCount") / 3);
            }
            else if (key.equals("dotNormalSize")) {
                patternLockView.setDotNormalSize(props.getInt("dotNormalSize"));
            }
            else if (key.equals("dotSelectedSize")) {
                patternLockView.setDotSelectedSize(props.getInt("dotSelectedSize"));
            }
            else if (key.equals("pathWidth")) {
                patternLockView.setPathWidth(props.getInt("pathWidth"));
            }
            else if (key.equals("aspectRatioEnabled")) {
                patternLockView.setAspectRatioEnabled(props.getBoolean("aspectRatioEnabled"));
            }
            else if (key.equals("aspectRatio")) {

                if (props.getString("aspectRatio").equalsIgnoreCase("SQUARE")) {
                    patternLockView.setAspectRatio(PatternLockView.AspectRatio.ASPECT_RATIO_SQUARE);
                } else if (props.getString("aspectRatio").equalsIgnoreCase("WIDTH_BIAS")) {
                    patternLockView.setAspectRatio(PatternLockView.AspectRatio.ASPECT_RATIO_WIDTH_BIAS);
                } else if (props.getString("aspectRatio").equalsIgnoreCase("HEIGHT_BIAS")) {
                    patternLockView.setAspectRatio(PatternLockView.AspectRatio.ASPECT_RATIO_HEIGHT_BIAS);
                }
            }
            else if (key.equals("normalStateColor")) {
                patternLockView.setNormalStateColor(Color.parseColor(props.getString("normalStateColor")));
            }
            else if (key.equals("correctStateColor")) {
                patternLockView.setCorrectStateColor(Color.parseColor(props.getString("correctStateColor")));
            }
            else if (key.equals("wrongStateColor")) {
                patternLockView.setWrongStateColor(Color.parseColor(props.getString("wrongStateColor")));
            }
            else if (key.equals("dotAnimationDuration")) {
                patternLockView.setDotAnimationDuration(props.getInt("dotAnimationDuration"));
            }
            else if (key.equals("pathEndAnimationDuration")) {
                patternLockView.setPathEndAnimationDuration(props.getInt("pathEndAnimationDuration"));
            }
        }

        if (props.getString("lock").length() != 0) {
            lock = Integer.parseInt(props.getString("lock"));
        }

        if (props.getBoolean("clear")) {
            patternLockView.clearPattern();
        }
    }

    private PatternLockViewListener patternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            context.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(
                    new LockEvent(frameLayout.getId(),"started", ""));
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> pattern) {
            context.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(
                    new LockEvent(frameLayout.getId(),"progress", PatternLockUtils.patternToString(patternLockView, pattern)));
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            PatternLockView lockView = (PatternLockView) frameLayout.getChildAt(0);

            if (lock != -1) {
                if (lock == Integer.parseInt(PatternLockUtils.patternToString(patternLockView, pattern))) {
                    lockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                } else {
                    lockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                }
            }

            context.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(
                    new LockEvent(frameLayout.getId(),"completed", PatternLockUtils.patternToString(patternLockView, pattern)));
        }

        @Override
        public void onCleared() {
            context.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(
                    new LockEvent(frameLayout.getId(),"cleared", ""));

        }
    };
}