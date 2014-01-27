<?php
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

// PHP 5.4.17 HMAC Authentication and WMS Pull List GET

// Define authentication parameters
$wskey = "";
$secret = "";
$principalID = "";
$principalIDNS = "";
$institutionId = "";
$branchId = "";
$startIndex = "1";
$itemsPerPage = "10";

// Declare variables
$url = "";
$queryparams = "";
$principalIDEncoded = "";
$principalIDNSEncoded = "";
$timestamp = "";
$nonce = "";
$bodyhash = "";
$method = "";
$normalizedRequest = "";
$signature = "";
$authorization = "";
$header = "";
$q = "\"";
$qc = "\",";
$uri = "";
$http = "";
$request = "";
$xmlresult = "";

$urlpattern = "https://circ.sd00.worldcat.org/pulllist/{branchId}?" +
    "inst={institutionId}" +
    "&principalID={principalIDEncoded}" +
    "&principalIDNS={principalIDNSEncoded}";

$principalIDEncoded = urlencode($principalID);
$principalIDNSEncoded = urlencode($principalIDNS);

// construct the parameter list
$queryparams = "inst=" . $institutionId . "\n" . "principalID=" . $principalIDEncoded . "\n" . "principalIDNS=" . $principalIDNSEncoded . "\n";

// set the method
$method = "GET";

// construct the url
$url = $urlpattern;
$url = str_replace("{branchId}", $branchId, $url);
$url = str_replace("{institutionId}", $institutionId, $url);
$url = str_replace("{principalIDEncoded}", $principalIDEncoded, $url);
$url = str_replace("{principalIDNSEncoded}", $principalIDNSEncoded, $url);

// create the timestamp, POSIX seconds since 1970 (aka Unix Time)
$timestamp = time();

// create the nonce, a random 8 digit hex string
$nonce = sprintf("%08x", mt_rand(0, 0x7fffffff));

// for this implementation, bodyhash is empty string
$bodyhash = "";

// create the normalized request
$normalizedRequest = $wskey . "\n" . $timestamp . "\n" . $nonce . "\n" . $bodyhash . "\n" . $method . "\n" . "www.oclc.org" . "\n" . "443" . "\n" . "/wskey" . "\n" . $queryparams;

// hash the normalized request
$signature = base64_encode(hash_hmac("sha256", $normalizedRequest, $secret, true));

// create the authorization header
$authorization = "http://www.worldcat.org/wskey/v2/hmac/v1 " . "clientId=" . $q . $wskey . $qc . "timestamp=" . $q . $timestamp . $qc . "nonce=" . $q . $nonce . $qc . "signature=" . $q . $signature . $q;

// Make the HTTP request
$headerArray[] = "Authorization: " . $authorization;

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_HTTPHEADER, $headerArray);
if ($method == "GET") {
    curl_setopt($ch, CURLOPT_HTTPGET, true);
} else if ($method == "POST") {
    curl_setopt($ch, CURLOPT_POSTFIELDS, $xmlrequest);
}
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$xmlresult = curl_exec($ch);
curl_close($ch);

echo $xmlresult;
echo "\n";

?>
