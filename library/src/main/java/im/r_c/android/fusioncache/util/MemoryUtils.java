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

package im.r_c.android.fusioncache.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * FusionCache
 * Created by richard on 6/13/16.
 *
 * @author Richard Chien
 */
public class MemoryUtils {
    /**
     * Get the size of an object.
     *
     * @return Size in bytes of the object or -1 if the object is null.
     */
    public static int sizeOf(Object object) {
        if (object == null)
            return -1;

        if (object instanceof byte[]) {
            return ((byte[]) object).length;
        }

        // Special output stream use to write the content
        // of an output stream to an internal byte array.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Output stream that can write object
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();
        } catch (IOException ignored) {
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException ignored) {
                }
            }
        }

        // Get the byte array
        byte[] byteArray = baos.toByteArray();

        return byteArray == null ? 0 : byteArray.length;
    }
}
