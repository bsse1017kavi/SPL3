package android.support.v4.view;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowInsets;
/* loaded from: classes.dex */
class ViewCompatLollipop {
    private static ThreadLocal<Rect> sThreadLocalRect;

    ViewCompatLollipop() {
    }

    public static void setTransitionName(View view, String transitionName) {
        view.setTransitionName(transitionName);
    }

    public static String getTransitionName(View view) {
        return view.getTransitionName();
    }

    public static void requestApplyInsets(View view) {
        view.requestApplyInsets();
    }

    public static void setElevation(View view, float elevation) {
        view.setElevation(elevation);
    }

    public static float getElevation(View view) {
        return view.getElevation();
    }

    public static void setTranslationZ(View view, float translationZ) {
        view.setTranslationZ(translationZ);
    }

    public static float getTranslationZ(View view) {
        return view.getTranslationZ();
    }

    public static void setOnApplyWindowInsetsListener(View view, final OnApplyWindowInsetsListener listener) {
        if (listener == null) {
            view.setOnApplyWindowInsetsListener(null);
        } else {
            view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: android.support.v4.view.ViewCompatLollipop.1
                @Override // android.view.View.OnApplyWindowInsetsListener
                public WindowInsets onApplyWindowInsets(View view2, WindowInsets windowInsets) {
                    WindowInsetsCompatApi21 insets = new WindowInsetsCompatApi21(windowInsets);
                    return ((WindowInsetsCompatApi21) OnApplyWindowInsetsListener.this.onApplyWindowInsets(view2, insets)).unwrap();
                }
            });
        }
    }

    public static boolean isImportantForAccessibility(View view) {
        return view.isImportantForAccessibility();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static ColorStateList getBackgroundTintList(View view) {
        return view.getBackgroundTintList();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setBackgroundTintList(View view, ColorStateList tintList) {
        view.setBackgroundTintList(tintList);
        if (Build.VERSION.SDK_INT == 21) {
            Drawable background = view.getBackground();
            boolean hasTint = (view.getBackgroundTintList() == null || view.getBackgroundTintMode() == null) ? false : true;
            if (background != null && hasTint) {
                if (background.isStateful()) {
                    background.setState(view.getDrawableState());
                }
                view.setBackground(background);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static PorterDuff.Mode getBackgroundTintMode(View view) {
        return view.getBackgroundTintMode();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setBackgroundTintMode(View view, PorterDuff.Mode mode) {
        view.setBackgroundTintMode(mode);
        if (Build.VERSION.SDK_INT == 21) {
            Drawable background = view.getBackground();
            boolean hasTint = (view.getBackgroundTintList() == null || view.getBackgroundTintMode() == null) ? false : true;
            if (background != null && hasTint) {
                if (background.isStateful()) {
                    background.setState(view.getDrawableState());
                }
                view.setBackground(background);
            }
        }
    }

    public static WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
        WindowInsets unwrapped;
        WindowInsets result;
        if ((insets instanceof WindowInsetsCompatApi21) && (result = v.onApplyWindowInsets((unwrapped = ((WindowInsetsCompatApi21) insets).unwrap()))) != unwrapped) {
            return new WindowInsetsCompatApi21(result);
        }
        return insets;
    }

    public static WindowInsetsCompat dispatchApplyWindowInsets(View v, WindowInsetsCompat insets) {
        WindowInsets unwrapped;
        WindowInsets result;
        if ((insets instanceof WindowInsetsCompatApi21) && (result = v.dispatchApplyWindowInsets((unwrapped = ((WindowInsetsCompatApi21) insets).unwrap()))) != unwrapped) {
            return new WindowInsetsCompatApi21(result);
        }
        return insets;
    }

    public static void setNestedScrollingEnabled(View view, boolean enabled) {
        view.setNestedScrollingEnabled(enabled);
    }

    public static boolean isNestedScrollingEnabled(View view) {
        return view.isNestedScrollingEnabled();
    }

    public static boolean startNestedScroll(View view, int axes) {
        return view.startNestedScroll(axes);
    }

    public static void stopNestedScroll(View view) {
        view.stopNestedScroll();
    }

    public static boolean hasNestedScrollingParent(View view) {
        return view.hasNestedScrollingParent();
    }

    public static boolean dispatchNestedScroll(View view, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return view.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    public static boolean dispatchNestedPreScroll(View view, int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return view.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    public static boolean dispatchNestedFling(View view, float velocityX, float velocityY, boolean consumed) {
        return view.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public static boolean dispatchNestedPreFling(View view, float velocityX, float velocityY) {
        return view.dispatchNestedPreFling(velocityX, velocityY);
    }

    public static float getZ(View view) {
        return view.getZ();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void offsetTopAndBottom(View view, int offset) {
        Rect parentRect = getEmptyTempRect();
        boolean needInvalidateWorkaround = false;
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            View p = (View) parent;
            parentRect.set(p.getLeft(), p.getTop(), p.getRight(), p.getBottom());
            needInvalidateWorkaround = !parentRect.intersects(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        }
        ViewCompatHC.offsetTopAndBottom(view, offset);
        if (needInvalidateWorkaround && parentRect.intersect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom())) {
            ((View) parent).invalidate(parentRect);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void offsetLeftAndRight(View view, int offset) {
        Rect parentRect = getEmptyTempRect();
        boolean needInvalidateWorkaround = false;
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            View p = (View) parent;
            parentRect.set(p.getLeft(), p.getTop(), p.getRight(), p.getBottom());
            needInvalidateWorkaround = !parentRect.intersects(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        }
        ViewCompatHC.offsetLeftAndRight(view, offset);
        if (needInvalidateWorkaround && parentRect.intersect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom())) {
            ((View) parent).invalidate(parentRect);
        }
    }

    private static Rect getEmptyTempRect() {
        if (sThreadLocalRect == null) {
            sThreadLocalRect = new ThreadLocal<>();
        }
        Rect rect = sThreadLocalRect.get();
        if (rect == null) {
            rect = new Rect();
            sThreadLocalRect.set(rect);
        }
        rect.setEmpty();
        return rect;
    }
}
