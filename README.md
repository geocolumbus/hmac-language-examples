# Generating an HMAC Signature to Access OCLC's APIs

APIs for developer level access to OCLC's APIs, such as Worldcat, requires generating an HMAC Signature using a public key and a private secret. This repo's code samples demonstrate how to perform a successful hash in a variety of languages not covered by code libraries at <a href="https://github.com/OCLC-Developer-Network">OCLC's Developer Network Github account</a>.

## Introduction

As of February, 2014, OCLC offers code libraries for <a href="">PHP</a> and <a href="https://github.com/OCLC-Developer-Network/oclc-auth-ruby">Ruby</a>. This repository offers hasing samples for PHP, Perl, Python and Java. They are formulated to request a Bibliographic Record from OCLC's WorldCat Metadata API.

To run the sample scripts, you must provide the following:

* WSKey - the public web services key
* secret - the private (secret) key
* principalID - the user identifier
* principalIDNS - the user domain identifier
* Institution Code - the OCLC code number for your specific institution

You can request these parameters from <a href="https://www.worldcat.org/config/">OCLC's Service Configuration</a> page.

Additional information about OCLC APIs is available from the <a href="http://oclc.org/developer/">OCLC Developer Network</a>. You will find specific information about <a href="http://oclc.org/developer/platform/authentication/hmac-signature">generating an HMAC Signature</a> and the <a href="http://oclc.org/developer/documentation/worldcat-metadata-api/bibliographic-record-resource">retrieving a bibliographic record from the Worldcat Metadata API</a> there as well.

## HMAC Signature Algorithm

The examples in all the languages follow this algorithm:

<ol>
<li>Establish the URL pattern. In this case, we are going to request the Bibliographic record for the book <i>Principia Mathematica</i> by Bertrand Russel and Alfred North Whitehead.

<pre>
https://worldcat.org/bib/data/{oclcNumber}?inst={inst}&classificationScheme={classificationScheme}
&holdingLibraryCode={holdingLibraryCode}&principalID={principalIDEncoded}
&principalIDNS={principalIDNSEncoded}
</pre>
Note that the {parameters} need to be url encoded.
</li>
<li>
Generate the normalized request.
<ul>
<li>
Create a string containing an <b>alphabetical</b> list of the parameters, each terminated with a newline:
<pre>
$queryParams = "classificationScheme=" . $classificationScheme . "\n" .
    "holdingLibraryCode=" . $holdingLibraryCode . "\n" .
    "inst=" . $institutionId . "\n" .
    "principalID=" . $principalIDEncoded . "\n" .
    "principalIDNS=" . $principalIDNSEncoded . "\n";
</pre>
</li>
<li>Set the method to GET</li>
<li>Set the timestamp to current posix time (a.k.a. unix time)</li>
<li>Create the nonce, a random 8 digit hex string</li>
<li>Set the body hash to empty string (not used in this example)</li>
<li>Build the normalized request from all of the above:
<pre>
$normalizedRequest = $wskey . "\n" .
    $timestamp . "\n" .
    $nonce . "\n" .
    $bodyhash . "\n" .
    $method . "\n" .
    "www.oclc.org" . "\n" .
    "443" . "\n" .
    "/wskey" . "\n" .
    $queryparams;
</pre>
</li>
</ul>
</li>
<li>Hash the normalized request using HAC256 and the secret, and then Base 64 encode it:

In PHP, it looks like this:

<pre>
$signature = base64_encode(hash_hmac("sha256", $normalizedRequest, $secret, true));
</pre>

</li>
<li>Construct the authorization header:
<pre>
$authorization = "http://www.worldcat.org/wskey/v2/hmac/v1 " .
    "clientId=" . $q . $wskey . $qc .
    "timestamp=" . $q . $timestamp . $qc .
    "nonce=" . $q . $nonce . $qc .
    "signature=" . $q . $signature . $q;
</pre></li>
<li>Finally, you are ready to make the request:
<ul>
<li>Set any additional headers per the documentation; for example "Accept: application/json"</li>
<li>Make the HTTP GET request</li>
</ul>
</li>
</ol>

## Notes

You'll notice that we did not include any HMAC hashing examples in popular client side languages, such as Javascript, Objective-C or Android Java. That is because HMAC Signature is an authentication technique that <b>should only be used on the server side</b>. Placing a "live" webservices key into a client side application means that application has full access to the database without requiring an individual to authenticate who they are first, and is an unacceptable security risk. For the case of client-side authentication direct to OCLC API's, say for a mobile application, you would use the token access authentication. Sample code for <a href="https://github.com/OCLC-Developer-Network/oclc-auth-ios-example">iOS</a> and <a href="https://github.com/OCLC-Developer-Network/oclc-auth-ios-example">Android</a> is available from OCLC's github account. Read more about the <a href="http://www.oclc.org/developer/platform/user-agent-or-mobile-pattern">User Agent / Mobile Pattern here</a>.
