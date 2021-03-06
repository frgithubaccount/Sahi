package net.sf.sahi.stream.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.sf.sahi.response.HttpResponse;
import org.apache.log4j.Logger;

/**
 * Sahi - Web Automation and Test Tool
 * <p/>
 * Copyright  2006  V Narayan Raman
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class ChunkedFilter extends StreamFilter {
  private static Logger logger = Logger.getLogger(ChunkedFilter.class);

  public ChunkedFilter() {
    super();
    logger.debug("In ChunkedFilter");
  }

  public byte[] modify(byte[] data) throws IOException {
    logger.debug("length = " + data.length);
    if (data == null || data.length == 0) return data;
    ByteArrayOutputStream ar = new ByteArrayOutputStream();
    ar.write((Integer.toHexString(data.length) + "\r\n").getBytes());
    ar.write(data);
    ar.write("\r\n".getBytes());
    return ar.toByteArray();
  }

  public void modifyHeaders(HttpResponse response) throws IOException {
    logger.debug("In modifyHeaders");
    response.removeHeader("Transfer-encoding");
    response.removeHeader("Content-Length");
    response.removeHeader("Content-length");
    response.setHeader("Transfer-Encoding", "chunked");
  }

  public byte[] getRemaining() {
    return (Integer.toHexString(0) + "\r\n\r\n").getBytes();
  }
}
