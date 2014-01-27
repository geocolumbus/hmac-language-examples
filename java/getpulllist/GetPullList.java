//  
// Copyright (c) 2012 OCLC, Inc. All Rights Reserved.
//  
// OCLC proprietary information: the enclosed materials contain
// proprietary information of OCLC, Inc. and shall not be disclosed in whole or in
// any part to any third party or used by any person for any purpose, without written
// consent of OCLC, Inc. Duplication of any portion of these materials shall include this notice.
//  
// Reference Code for Java for Dalvik
// Worldshare Platform Team
// Sat Sep 01 2012
// campbelg@oclc.org

// *** WMS Circulation Pull List ***

package getpulllist;

import javax.crypto.Mac;
import java.net.URLEncoder;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Encoder; // http://iharder.sourceforge.net/current/java/base64/
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.io.InputStream;

public class GetPullList {
  public static void main(String[] args) {


// Define constants

    String wskey         = "";
    String secret        = "";
    String principalID   = "";
    String principalIDNS = "";
    String institutionId = "";
    String branchId      = "";
    String startIndex    = "1";
    String itemsPerPage  = "10";

// Declare variables

    String url = "";
    String queryparams = "";
    String principalIDEncoded = "";
    String principalIDNSEncoded = "";
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

    String urlpattern = "https://circ.sd00.worldcat.org/pulllist/{branchId}?inst={institutionId}&principalID={principalIDEncoded}&principalIDNS={principalIDNSEncoded}";

    try {
        principalIDEncoded = URLEncoder.encode(principalID,"UTF-8");
    }
    catch(java.io.IOException e) {
        System.out.println("IO Error: " + e);
    }
    try {
        principalIDNSEncoded = URLEncoder.encode(principalIDNS,"UTF-8");
    }
    catch(java.io.IOException e) {
        System.out.println("IO Error: " + e);
    }

// construct the parameter list

    queryparams = "inst=" + institutionId + "\n" + "principalID=" + principalIDEncoded + "\n" + "principalIDNS=" + principalIDNSEncoded + "\n";

	System.out.println(queryparams);
	
// set the method

    method = "GET";

// construct the url

    url = urlpattern;
    url = url.replaceFirst("\\{branchId\\}",branchId);
    url = url.replaceFirst("\\{institutionId\\}",institutionId);
    url = url.replaceFirst("\\{principalIDEncoded\\}",principalIDEncoded);
    url = url.replaceFirst("\\{principalIDNSEncoded\\}",principalIDNSEncoded);

// create the timestamp, POSIX seconds since 1970 (aka Unix Time)

    timestamp = System.currentTimeMillis()/1000+"";
// timestamp = "12345678";

// create the nonce, a random 8 digit hex string

    nonce = String.format("%x",(long)(Math.random()*4026531839.0 + 268435456.0));
// nonce = "a0234f45";

// for this implementation, bodyhash is empty string

    bodyhash = "";

// create the normalized request

    normalizedRequest = wskey + "\n" + timestamp + "\n" + nonce + "\n" + bodyhash + "\n" + method + "\n" + "www.oclc.org" + "\n" + "443" + "\n" + "/wskey" + "\n" + queryparams;

// hash the normalized request

    try {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        try {
            sha256_HMAC.init(secret_key);
        }
        catch (java.security.InvalidKeyException e) {
            System.out.println("Error: " + e);
        }
        byte[] mac_data = sha256_HMAC.doFinal(normalizedRequest.getBytes());
        BASE64Encoder encoder = new BASE64Encoder();
        signature = encoder.encodeBuffer(mac_data);
        signature = signature.replaceFirst("\n","");
    }
    catch (java.security.NoSuchAlgorithmException e) {
        System.out.println("Error: " + e);
    }

// create the authorization header

    authorization = "http://www.worldcat.org/wskey/v2/hmac/v1 " + "clientId=" + q + wskey + qc + "timestamp=" + q + timestamp + qc + "nonce=" + q + nonce + qc + "signature=" + q + signature + q;
	System.out.println(authorization);

// Make the HTTP request

	System.out.println(url);
	
    try {
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Authorization", authorization);
        InputStream response = connection.getInputStream();
        int i;
        do {
            i =  response.read();
            if (i != -1) {
                xmlresult += (char) i;
            }
        } while(i != -1);
    } catch (UnsupportedEncodingException e) {
        System.out.println(e);
    } catch (MalformedURLException e) {
        System.out.println(e);
    } catch(IOException e) {
        System.out.println(e);
    }

    System.out.println(xmlresult);

  }
}

