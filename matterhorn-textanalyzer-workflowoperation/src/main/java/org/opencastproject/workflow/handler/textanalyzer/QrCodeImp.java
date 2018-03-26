/**
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 *
 * The Apereo Foundation licenses this file to you under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at:
 *
 *   http://opensource.org/licenses/ecl2.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
 
package org.opencastproject.workflow.handler.textanalyzer;
 
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
 
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
 
import javax.imageio.ImageIO;
 
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
 
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
/**
 * Media analysis service that takes takes an image and returns text as extracted from that image.
 */
public class QrCodeImp {
 
  public static void insertDatabase(String qrcode, long timeStamp, String videoID) {
 
    URL aURL;
    String guid = null;
    if (qrcode == null || qrcode.isEmpty()) {
      return;
    }

    try {
      aURL = new URL(qrcode);
      guid = splitQuery(aURL).get("guid");
 
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return;
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return;
    }
 
    String url = "jdbc:mysql://localhost:3306/";
    String user = "opencast";
    String password = "opencast_password";
 
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      Connection con = DriverManager.getConnection(url, user, password);
 
      if (con == null) {
        System.out.println("Failed to make connection!");
      }
 
      Statement stt = con.createStatement();
      stt.execute("USE opencast"); 
      stt.execute("INSERT INTO datenbankRed (guid, timeStamp, videoID) SELECT '" + guid + "', '" + timeStamp + "', '" + videoID + "' WHERE NOT EXISTS(SELECT * FROM datenbankRed WHERE guid = '"+ guid +"')");

      stt.close();
      con.close();
 
    } catch (
 
    Exception e) {
      e.printStackTrace();
    }
  }
 
  public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    String query = url.getQuery();
    String[] pairs = query.split("&");
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
              URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
    }
    return query_pairs;
  }
 
  public static String readQRCode(String fileName) {
    File file = new File(fileName);
    BufferedImage image = null;
    BinaryBitmap bitmap = null;
    Result result = null;
 
    try {
      image = ImageIO.read(file);
      int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
      RGBLuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
      bitmap = new BinaryBitmap(new HybridBinarizer(source));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
 
    if (bitmap == null) {
      return null;
    }
 
    QRCodeReader reader = new QRCodeReader();
    try {
      result = reader.decode(bitmap);
      return result.getText();
    } catch (NotFoundException e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();
    } catch (ChecksumException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (FormatException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
 
    return null;
  }
 
}
