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

## Use Case

When developing with an API that requires a complex security hashing algorithm, it is helpful to have a simple reference script that can successfully perform a GET. This way you can verify your keys are valid without wondering if it is your key or the hashing algorithm that is the problem.

## HMAC Signature Algorithm

The examples in all the languages follow this algorithm:

<ol>
<li>Establish the URL pattern. In this case, we are going to request the Bibliographic record for the book <i>Principia Mathematica</i> by Bertrand Russel and Alfred North Whitehead.

<pre>
https://worldcat.org/bib/data/{oclcNumber}?
inst={inst}
&classificationScheme={classificationScheme}
&holdingLibraryCode={holdingLibraryCode}
&principalID={principalIDEncoded}
&principalIDNS={principalIDNSEncoded}
</pre>
The {parameters} need to be url encoded. For some OCLC API's (including this one, Worldcat Metadata), the principalID
and principalIDNS can sent as key value pairs in the request header and omitted from the url. However, I coded these
examples to include PrincipalID and PrincipalIDNS in the URL's. You can modify the code as an exercise.
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

The hard work of building this repository was assembling the proper libraries and techniques for performing this hash
for different languages (PHP, Perl, Python and Java).

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

## Expected Result

If your WSKey is configured properly, you should get this result for OCLC #1039085:

<pre>
<?xml version="1.0" encoding="UTF-8"?>
<entry xmlns="http://www.w3.org/2005/Atom">
  <content type="application/xml">
    <response xmlns="http://worldcat.org/rb" mimeType="application/vnd.oclc.marc21+xml">
      <record xmlns="http://www.loc.gov/MARC21/slim">
        <leader>00000cam a2200000I  4500</leader>
        <controlfield tag="001">ocm01039085</controlfield>
        <controlfield tag="003">OCoLC</controlfield>
        <controlfield tag="005">20131216133216.8</controlfield>
        <controlfield tag="008">741011m19251927enk           000 0 eng  </controlfield>
        <datafield tag="010" ind1=" " ind2=" ">
          <subfield code="a">   25015133 </subfield>
    </datafield>
        <datafield tag="040" ind1=" " ind2=" ">
          <subfield code="a">DLC</subfield>
          <subfield code="c">WEL</subfield>
          <subfield code="d">SER</subfield>
          <subfield code="d">OCL</subfield>
          <subfield code="d">MNU</subfield>
          <subfield code="d">OCL</subfield>
          <subfield code="d">MUB</subfield>
          <subfield code="d">OCLCG</subfield>
          <subfield code="d">CUY</subfield>
          <subfield code="d">UBY</subfield>
          <subfield code="d">OCLCF</subfield>
    </datafield>
        <datafield tag="019" ind1=" " ind2=" ">
          <subfield code="a">1393413</subfield>
          <subfield code="a">10452964</subfield>
    </datafield>
        <datafield tag="029" ind1="1" ind2=" ">
          <subfield code="a">NZ1</subfield>
          <subfield code="b">8177776</subfield>
    </datafield>
        <datafield tag="029" ind1="1" ind2=" ">
          <subfield code="a">AU@</subfield>
          <subfield code="b">000022012091</subfield>
    </datafield>
        <datafield tag="035" ind1=" " ind2=" ">
          <subfield code="a">(OCoLC)1039085</subfield>
          <subfield code="z">(OCoLC)1393413</subfield>
          <subfield code="z">(OCoLC)10452964</subfield>
    </datafield>
        <datafield tag="050" ind1="0" ind2="0">
          <subfield code="a">QA9</subfield>
          <subfield code="b">.W5 1925</subfield>
    </datafield>
        <datafield tag="082" ind1=" " ind2="4">
          <subfield code="a">500</subfield>
    </datafield>
        <datafield tag="049" ind1=" " ind2=" ">
          <subfield code="a">MAIN</subfield>
    </datafield>
        <datafield tag="100" ind1="1" ind2=" ">
          <subfield code="a">Whitehead, Alfred North,</subfield>
          <subfield code="d">1861-1947.</subfield>
    </datafield>
        <datafield tag="245" ind1="1" ind2="0">
          <subfield code="a">Principia mathematica,</subfield>
          <subfield code="c">by Alfred North Whitehead ... and Bertrand Russell ...</subfield>
    </datafield>
        <datafield tag="250" ind1=" " ind2=" ">
          <subfield code="a">2d ed.</subfield>
    </datafield>
        <datafield tag="260" ind1=" " ind2=" ">
          <subfield code="a">Cambridge [Eng.]</subfield>
          <subfield code="b">The University Press,</subfield>
          <subfield code="c">1925-1927.</subfield>
    </datafield>
        <datafield tag="300" ind1=" " ind2=" ">
          <subfield code="a">3 v.</subfield>
          <subfield code="c">27 cm.</subfield>
    </datafield>
        <datafield tag="650" ind1=" " ind2="0">
          <subfield code="a">Mathematics.</subfield>
    </datafield>
        <datafield tag="650" ind1=" " ind2="0">
          <subfield code="a">Mathematics</subfield>
          <subfield code="x">Philosophy.</subfield>
    </datafield>
        <datafield tag="650" ind1=" " ind2="0">
          <subfield code="a">Logic, Symbolic and mathematical.</subfield>
    </datafield>
        <datafield tag="650" ind1=" " ind2="7">
          <subfield code="a">Logic, Symbolic and mathematical.</subfield>
          <subfield code="2">fast</subfield>
          <subfield code="0">(OCoLC)fst01002068</subfield>
    </datafield>
        <datafield tag="650" ind1=" " ind2="7">
          <subfield code="a">Mathematics.</subfield>
          <subfield code="2">fast</subfield>
          <subfield code="0">(OCoLC)fst01012163</subfield>
    </datafield>
        <datafield tag="650" ind1=" " ind2="7">
          <subfield code="a">Mathematics</subfield>
          <subfield code="x">Philosophy.</subfield>
          <subfield code="2">fast</subfield>
          <subfield code="0">(OCoLC)fst01012213</subfield>
    </datafield>
        <datafield tag="700" ind1="1" ind2=" ">
          <subfield code="a">Russell, Bertrand,</subfield>
          <subfield code="d">1872-1970,</subfield>
          <subfield code="e">joint author.</subfield>
    </datafield>
  </record>
    </response>
  </content>
  <id>http://worldcat.org/oclc/01039085</id>
  <link href="http://worldcat.org/oclc/01039085"></link>
</entry>
</pre>

## Notes

You'll notice that we did not include any HMAC hashing examples in popular client side languages, such as Javascript, Objective-C or Android Java. That is because HMAC Signature is an authentication technique that <b>should only be used on the server side</b>. Placing a "live" webservices key into a client side application means that application has full access to the database without requiring an individual to authenticate who they are first, and is an unacceptable security risk. For the case of client-side authentication direct to OCLC API's, say for a mobile application, you would use the token access authentication. Sample code for <a href="https://github.com/OCLC-Developer-Network/oclc-auth-ios-example">iOS</a> and <a href="https://github.com/OCLC-Developer-Network/oclc-auth-ios-example">Android</a> is available from OCLC's github account. Read more about the <a href="http://www.oclc.org/developer/platform/user-agent-or-mobile-pattern">User Agent / Mobile Pattern here</a>.

