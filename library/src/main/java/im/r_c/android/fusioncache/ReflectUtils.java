/*
 * Copyright (c) 2016 Richard Chien
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package im.r_c.android.fusioncache;

import java.lang.reflect.Method;

/**
 * FusionCache
 * Created by richard on 6/13/16.
 */
public class ReflectUtils {
    public static Object invokeMethodIfExists(String methodName, boolean declared, Object target, Object... params) {
        Class<?> c = target.getClass();
        Method[] methods;
        if (declared) {
            methods = c.getDeclaredMethods();
        } else {
            methods = c.getMethods();
        }
        Method matchedMethod = null;
        Object result = null;
        for (Method m : methods) {
            if (!m.getName().equals(methodName)) {
                continue;
            }

            Class<?>[] types = m.getParameterTypes();

            if (types.length != params.length) {
                continue;
            }

            boolean matched = true;
            final int len = params.length;
            for (int i = 0; i < len; i++) {
                if (!types[i].isInstance(params[i])) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                matchedMethod = m;
                break;
            }
        }
        if (matchedMethod != null) {
            try {
                matchedMethod.setAccessible(true);
                result = matchedMethod.invoke(target, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
