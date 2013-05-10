package android.support.v4.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

/*
 * Bug #37484 https://code.google.com/p/android/issues/detail?id=37484#c1
 */

public abstract class FixedFragmentStatePagerAdapter extends
		FragmentStatePagerAdapter {

	public FixedFragmentStatePagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment f = (Fragment) super.instantiateItem(container, position);
		Bundle savedFragmentState = f.mSavedFragmentState;
		if (savedFragmentState != null) {
			savedFragmentState.setClassLoader(f.getClass().getClassLoader());
		}
		return f;
	}
}
