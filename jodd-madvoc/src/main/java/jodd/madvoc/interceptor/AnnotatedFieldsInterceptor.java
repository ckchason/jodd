// Copyright (c) 2003-2013, Jodd Team (jodd.org). All Rights Reserved.

package jodd.madvoc.interceptor;

import jodd.introspector.FieldDescriptor;
import jodd.madvoc.ActionRequest;
import jodd.introspector.ClassDescriptor;
import jodd.introspector.ClassIntrospector;

import java.lang.reflect.Field;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Performs some operation on all annotated fields. Helpful with injection of
 * application context into action objects.
 */
public abstract class AnnotatedFieldsInterceptor extends BaseActionInterceptor {

	protected final Class<Annotation> fieldAnnotation;

	protected AnnotatedFieldsInterceptor(Class<Annotation> fieldAnnotation) {
		this.fieldAnnotation = fieldAnnotation;
	}

	public Object intercept(ActionRequest actionRequest) throws Exception {
		Object action = actionRequest.getAction();
		Class actionType = action.getClass();

		Field[] fields = lookupAnnotatedFields(actionType);
		for (Field field : fields) {
			onAnnotatedField(actionRequest, field);
		}
		return actionRequest.invoke();
	}

	/**
	 * Invoked on all annotated fields.
	 */
	protected abstract void onAnnotatedField(ActionRequest actionRequest, Field field);


	// ---------------------------------------------------------------- cache and lookup

	protected Map<Class<?>, Field[]> annotatedField = new HashMap<Class<?>, Field[]>();
	protected static final Field[] EMPTY_FIELD = new Field[0];

	/**
	 * Lookups for annotated fields. Caches all annotated fields on the first
	 * action class scan. 
	 */
	protected Field[] lookupAnnotatedFields(Class type) {
		Field[] fields = annotatedField.get(type);
		if (fields == null) {
			ClassDescriptor cd = ClassIntrospector.lookup(type);
			FieldDescriptor[] allFields = cd.getAllFieldDescriptors();

			List<Field> fieldList = new ArrayList<Field>();
			for (FieldDescriptor fieldDescriptor : allFields) {
				Field field = fieldDescriptor.getField();

				Annotation ann = field.getAnnotation(fieldAnnotation);
				if (ann != null) {
					fieldList.add(field);
				}
			}
			if (fieldList.isEmpty()) {
				fields = EMPTY_FIELD;
			} else {
				fields = fieldList.toArray(new Field[fieldList.size()]);
			}
			annotatedField.put(type, fields);
		}
		return fields;
	}

}
