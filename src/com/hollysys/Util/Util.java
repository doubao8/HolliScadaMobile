package com.hollysys.util;

import java.lang.reflect.Field;

import android.util.Log;

public final class Util {

	public static Object getPropertyValue(Class<?> cls, String property) {
		Field field;
		try {
			field = cls.getDeclaredField(property);
			return field.get(cls);
		} catch (Exception e) {
			Log.i("HollySys", e.getMessage());
			return null;
		}

	}

}
