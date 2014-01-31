/*******************************************************************************
 * Copyright 2014 OCLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

// HMAC Authentication and Worldcat Metadata Bibliographic Record Request
//
// http://oclc.org/developer/documentation/worldcat-metadata-api/bibliographic-record-resource

package getbibrecord;

        import javax.crypto.Mac;
        import java.net.URLEncoder;
        import javax.crypto.spec.SecretKeySpec;
        import sun.misc.BASE64Encoder; // http://iharder.sourceforge.net/current/java/base64/
        import java.io.IOException;
        import java.io.UnsupportedEncodingException;
        import java.net.*;
        import java.io.InputStream;

public class GetBibRecord {
    public static void main(String[] args) {

// Authentication and request parameters

        String wskey = "";
        String secret = "";
        String principalID = "";
        String principalIDNS = "";
        String institutionId = "128807";
        String classificationScheme = "LibraryOfCongress";
        String holdingLibraryCode = "MAIN";
        String oclcNumber = "1039085";

        String startIndex = "1";
        String itemsPerPage = "10";

// Declare variables

        String url = "";
        String queryparams = "";
        String timestamp = "";
        String nonce = "";
        String bodyhash = "";
        String method = "";
        String normalizedRequest = "";
        String signature = "";
        String authorization = "";
        String header = "";
        String q = "\"";
        String qc = "\",";
        String uri = "";
        String http = "";
        String request = "";
        String xmlresult = "";

        String urlpattern = "https://worldcat.org/bib/data/{oclcNumber}?" +
                "inst={inst}" +
                "&classificationScheme={classificationScheme}" +
                "&holdingLibraryCode={holdingLibraryCode}";

// construct the parameter list
        queryparams = "" +
                "classificationScheme=" + classificationScheme + "\n" +
                "holdingLibraryCode=" + holdingLibraryCode + "\n" +
                "inst=" + institutionId + "\n";

        System.out.println("\nQuery Parameters:\n" + queryparams + "\n\n");

// set the method
        method = "GET";

// construct the url
        url = urlpattern;
        url = url.replaceFirst("\\{inst\\}", institutionId);
        url = url.replaceFirst("\\{classificationScheme\\}", classificationScheme);
        url = url.replaceFirst("\\{holdingLibraryCode\\}", holdingLibraryCode);
        url = url.replaceFirst("\\{oclcNumber\\}", oclcNumber);

        System.out.println("URL:\n" + url + "\n\n");

// create the timestamp, POSIX seconds since 1970 (aka Unix Time)
        timestamp = System.currentTimeMillis() / 1000 + "";

// create the nonce, a random 8 digit hex string
        nonce = String.format("%x", (long) (Math.random() * 4026531839.0 + 268435456.0));

// for this implementation, bodyhash is empty string
        bodyhash = "";

// create the normalized request
        normalizedRequest = wskey + "\n" +
                timestamp + "\n" +
                nonce + "\n" +
                bodyhash + "\n" +
                method + "\n" +
                "www.oclc.org" + "\n" +
                "443" + "\n" +
                "/wskey" + "\n" +
                queryparams;

        System.out.println("Normalized Request:\n" + normalizedRequest + "\n\n");

// hash the normalized request
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            try {
                sha256_HMAC.init(secret_key);
            } catch (java.security.InvalidKeyException e) {
                System.out.println("Error: " + e);
            }
            byte[] mac_data = sha256_HMAC.doFinal(normalizedRequest.getBytes());
            BASE64Encoder encoder = new BASE64Encoder();
            signature = encoder.encodeBuffer(mac_data);
            signature = signature.replaceFirst("\n", "");
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println("Error: " + e);
        }

// create the authorization header
        authorization = "http://www.worldcat.org/wskey/v2/hmac/v1 "
                + "clientId=" + q + wskey + qc +
                "timestamp=" + q + timestamp + qc +
                "nonce=" + q + nonce + qc +
                "signature=" + q + signature + qc +
                "principalID=" + q + principalID + qc +
                "principalIDNS=" + q + principalIDNS + q;

        System.out.println("Authorization Header:\n" + authorization + "\n\n");

// Make the HTTP request
        System.out.println(url);

        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Authorization", authorization);
            InputStream response = connection.getInputStream();
            int i;
            do {
                i = response.read();
                if (i != -1) {
                    xmlresult += (char) i;
                }
            } while (i != -1);
        } catch (UnsupportedEncodingException e) {
            System.out.println(e);
        } catch (MalformedURLException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }

        System.out.println("Result:\n"+xmlresult);

    }
}