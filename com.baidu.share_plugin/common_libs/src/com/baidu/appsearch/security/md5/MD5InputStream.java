package com.baidu.appsearch.security.md5;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.baidu.appsearch.logging.Log;

/** 
 * MD5InputStream, a subclass of FilterInputStream implementing MD5
 * functionality on a stream.
 * <p>
 * Originally written by Santeri Paavolainen, Helsinki Finland 1996 <br>
 * (c) Santeri Paavolainen, Helsinki Finland 1996 <br>
 * Some changes Copyright (c) 2002 Timothy W Macinta <br>
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * <p>
 * See http://www.twmacinta.com/myjava/fast_md5.php for more information
 * on this file.
 * <p>
 * Please note: I (Timothy Macinta) have put this code in the
 * com.twmacinta.util package only because it came without a package.  I
 * was not the the original author of the code, although I did
 * optimize it (substantially) and fix some bugs.
 *
 * @author  Santeri Paavolainen <santtu@cs.hut.fi>
 * @author  Timothy W Macinta (twm@alum.mit.edu) (added main() method)
 **/

public class MD5InputStream extends FilterInputStream {
  /**
   * MD5 context
   */
  private MD5 md5;
  
  /**
   * Creates a MD5InputStream
   * @param in The input stream
   */
    public MD5InputStream(InputStream in) {
    super(in);

    md5 = new MD5();
  }

    /**
     * Read a byte of data.
     * 
     * @see java.io.FilterInputStream
     */
    public int read() throws IOException { // SUPPRESS CHECKSTYLE
        int c = in.read();

        if (c == -1) {
            return -1;
        }

        if ((c & ~0xff) != 0) {// SUPPRESS CHECKSTYLE
            Log.i("MD5InputStream", "MD5InputStream.read() got character with (c & ~0xff) != 0)!");
        } else {
            md5.update(c);
        }

        return c;
    }

  /**
   * Reads into an array of bytes.
   *
   * @see java.io.FilterInputStream
   */
    public int read(byte bytes[], int offset, int length) throws IOException {// SUPPRESS CHECKSTYLE
        int r;

        if ((r = in.read(bytes, offset, length)) == -1) {// SUPPRESS CHECKSTYLE
            return r;
        }
        md5.update(bytes, offset, r);

        return r;
    }

  /**
   * Returns array of bytes representing hash of the stream as
   * finalized for the current state. 
   * @see MD5#Final
   */
    public byte[] hash() { // SUPPRESS CHECKSTYLE
    return md5.finalEncode();
  }

    /**
     * 获得已有的md5值
     * 
     * @return md5
     */
  public MD5 getMD5() {
    return md5;
  }

    /**
     * This method is here for testing purposes only - do not rely on it being
     * here.
     * 
     * @param arg
     *            参数列表
     **/
  public static void main(String[] arg) {
    try {

      ////////////////////////////////////////////////////////////////
      //
      // usage:  java com.twmacinta.util.MD5InputStream [--use-default-md5] [--no-native-lib] filename
      //
      /////////

      // determine the filename to use and the MD5 impelementation to use

      String filename = arg[arg.length-1]; // SUPPRESS CHECKSTYLE
      boolean use_default_md5 = false; // SUPPRESS CHECKSTYLE
      boolean use_native_lib = true; // SUPPRESS CHECKSTYLE
      for (int i = 0; i < arg.length-1; i++) { // SUPPRESS CHECKSTYLE
        if (arg[i].equals("--use-default-md5")) {
          use_default_md5 = true;
        } else if (arg[i].equals("--no-native-lib")) {
          use_native_lib = false;
        }
      }

      // initialize common variables

      byte[] buf = new byte[65536];// SUPPRESS CHECKSTYLE
      int num_read;// SUPPRESS CHECKSTYLE

      //   Use the default MD5 implementation that comes with Java

      if (use_default_md5) {
    InputStream in = new BufferedInputStream(new FileInputStream(filename));
    java.security.MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
    while ((num_read = in.read(buf)) != -1) {
      digest.update(buf, 0, num_read);
    }
    in.close();

    // Use the optimized MD5 implementation

      } else {

    //    disable the native library search, if requested

    if (!use_native_lib) {
      MD5.initNativeLibrary(true);
    }

    //    calculate the checksum

    MD5InputStream in = new MD5InputStream(new BufferedInputStream(new FileInputStream(filename)));
    while ((num_read = in.read(buf)) != -1); // SUPPRESS CHECKSTYLE
    in.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
