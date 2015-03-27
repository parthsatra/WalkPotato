package csc495.potato.walk.walkpotato.UI.Widgets;

import android.content.Context;
import android.util.AttributeSet;

import com.gc.materialdesign.views.ButtonFloat;

public class FixButtonFloat extends ButtonFloat {
    public FixButtonFloat(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);

        setAttributes(attrs);
    }
}
