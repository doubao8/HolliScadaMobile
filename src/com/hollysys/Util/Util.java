package com.hollysys.Util;

import java.lang.reflect.Field;

public final class Util {

	public static Object getPropertyValue(Class<?> cls, String property) {
		Field field;
		try {
			field = cls.getDeclaredField(property);
			return field.get(cls);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

}
