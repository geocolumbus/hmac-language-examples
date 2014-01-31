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

For example:
https://worldcat.org/bib/data/1039085?inst=128807&classificationScheme=LibraryOfCongress&holdingLibraryCode=MAIN
</pre>
The {parameters} need to be url encoded. For some OCLC API's (including this one, Worldcat Metadata), the principalID
and principalIDNS can sent as key value pairs in the request header and omitted from the url. However, I coded these
examples to include PrincipalID and PrincipalIDNS in the URL's. You can modify the code as an exercise.
</li>
<li>
Generate the normalized request.
<ul>
<li>
Create a string containing an <b>alphabetical</b> list of the parameters, <b>each terminated with a newline</b>:
<pre>
classificationScheme=LibraryOfCongress
holdingLibraryCode=MAIN
inst=128807
</pre>
</li>
<li>Set the method to GET</li>
<li>Set the timestamp to current posix time (a.k.a. unix time)</li>
<li>Create the nonce, a random 8 digit hex string</li>
<li>Set the body hash to empty string (not used in this example)</li>
<li>Build the normalized request from all of the above - note the newline characters after each parameter - and since
we added a newline on the end of the queryparameters above, we don't add one here.
<pre>
    $wskey + "\n"
    $timestamp + "\n"
    $nonce + "\n"
    $bodyhash + "\n"
    $method + "\n"
    "www.oclc.org" + "\n"
    "443" + "\n" .
    "/wskey" + "\n"
    $queryparams

For example:

{wskey - I can't show it here for security reasons. Like FabeuFmMwhHAA...}
1391177450
42203e11

GET
www.oclc.org
443
/wskey
classificationScheme=LibraryOfCongress
holdingLibraryCode=MAIN
inst=128807
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
<li>Construct the authorization header (note I broke it across multiple lines to make it more readable. In practice, it runs together on the same line - no "\n")
<pre>
    http://www.worldcat.org/wskey/v2/hmac/v1
    clientId="{wskey}"
    timestamp="{timestamp}"
    nonce="{nonce}"
    signature="{signature}"
    principalID="{principalID}"
    principalIDNS="{principalIDNS}"
    </pre>

    For example:
    http://www.worldcat.org/wskey/v2/hmac/v1 clientId="{wskey}",timestamp="1391177450",nonce="42203e11",signature="zk/q5vyHCOLulPf6Yu5pONy+pTsKby4RN+WJ1+TT1SQ=",principalID="{principalID}",principalIDNS="{principalIDNS}"</li>
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
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;entry xmlns="http://www.w3.org/2005/Atom"&gt;
  &lt;content type="application/xml"&gt;
    &lt;response xmlns="http://worldcat.org/rb" mimeType="application/vnd.oclc.marc21+xml"&gt;
      &lt;record xmlns="http://www.loc.gov/MARC21/slim"&gt;
        &lt;leader&gt;00000cam a2200000I  4500&lt;/leader&gt;
        &lt;controlfield tag="001"&gt;ocm01039085&lt;/controlfield&gt;
        &lt;controlfield tag="003"&gt;OCoLC&lt;/controlfield&gt;
        &lt;controlfield tag="005"&gt;20131216133216.8&lt;/controlfield&gt;
        &lt;controlfield tag="008"&gt;741011m19251927enk           000 0 eng  &lt;/controlfield&gt;
        &lt;datafield tag="010" ind1=" " ind2=" "&gt;
          &lt;subfield code="a"&gt;   25015133 &lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="040" ind1=" " ind2=" "&gt;
          &lt;subfield code="a"&gt;DLC&lt;/subfield&gt;
          &lt;subfield code="c"&gt;WEL&lt;/subfield&gt;
          &lt;subfield code="d"&gt;SER&lt;/subfield&gt;
          &lt;subfield code="d"&gt;OCL&lt;/subfield&gt;
          &lt;subfield code="d"&gt;MNU&lt;/subfield&gt;
          &lt;subfield code="d"&gt;OCL&lt;/subfield&gt;
          &lt;subfield code="d"&gt;MUB&lt;/subfield&gt;
          &lt;subfield code="d"&gt;OCLCG&lt;/subfield&gt;
          &lt;subfield code="d"&gt;CUY&lt;/subfield&gt;
          &lt;subfield code="d"&gt;UBY&lt;/subfield&gt;
          &lt;subfield code="d"&gt;OCLCF&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="019" ind1=" " ind2=" "&gt;
          &lt;subfield code="a"&gt;1393413&lt;/subfield&gt;
          &lt;subfield code="a"&gt;10452964&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="029" ind1="1" ind2=" "&gt;
          &lt;subfield code="a"&gt;NZ1&lt;/subfield&gt;
          &lt;subfield code="b"&gt;8177776&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="029" ind1="1" ind2=" "&gt;
          &lt;subfield code="a"&gt;AU@&lt;/subfield&gt;
          &lt;subfield code="b"&gt;000022012091&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="035" ind1=" " ind2=" "&gt;
          &lt;subfield code="a"&gt;(OCoLC)1039085&lt;/subfield&gt;
          &lt;subfield code="z"&gt;(OCoLC)1393413&lt;/subfield&gt;
          &lt;subfield code="z"&gt;(OCoLC)10452964&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="050" ind1="0" ind2="0"&gt;
          &lt;subfield code="a"&gt;QA9&lt;/subfield&gt;
          &lt;subfield code="b"&gt;.W5 1925&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="082" ind1=" " ind2="4"&gt;
          &lt;subfield code="a"&gt;500&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="049" ind1=" " ind2=" "&gt;
          &lt;subfield code="a"&gt;MAIN&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="100" ind1="1" ind2=" "&gt;
          &lt;subfield code="a"&gt;Whitehead, Alfred North,&lt;/subfield&gt;
          &lt;subfield code="d"&gt;1861-1947.&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="245" ind1="1" ind2="0"&gt;
          &lt;subfield code="a"&gt;Principia mathematica,&lt;/subfield&gt;
          &lt;subfield code="c"&gt;by Alfred North Whitehead ... and Bertrand Russell ...&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="250" ind1=" " ind2=" "&gt;
          &lt;subfield code="a"&gt;2d ed.&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="260" ind1=" " ind2=" "&gt;
          &lt;subfield code="a"&gt;Cambridge [Eng.]&lt;/subfield&gt;
          &lt;subfield code="b"&gt;The University Press,&lt;/subfield&gt;
          &lt;subfield code="c"&gt;1925-1927.&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="300" ind1=" " ind2=" "&gt;
          &lt;subfield code="a"&gt;3 v.&lt;/subfield&gt;
          &lt;subfield code="c"&gt;27 cm.&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="650" ind1=" " ind2="0"&gt;
          &lt;subfield code="a"&gt;Mathematics.&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="650" ind1=" " ind2="0"&gt;
          &lt;subfield code="a"&gt;Mathematics&lt;/subfield&gt;
          &lt;subfield code="x"&gt;Philosophy.&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="650" ind1=" " ind2="0"&gt;
          &lt;subfield code="a"&gt;Logic, Symbolic and mathematical.&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="650" ind1=" " ind2="7"&gt;
          &lt;subfield code="a"&gt;Logic, Symbolic and mathematical.&lt;/subfield&gt;
          &lt;subfield code="2"&gt;fast&lt;/subfield&gt;
          &lt;subfield code="0"&gt;(OCoLC)fst01002068&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="650" ind1=" " ind2="7"&gt;
          &lt;subfield code="a"&gt;Mathematics.&lt;/subfield&gt;
          &lt;subfield code="2"&gt;fast&lt;/subfield&gt;
          &lt;subfield code="0"&gt;(OCoLC)fst01012163&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="650" ind1=" " ind2="7"&gt;
          &lt;subfield code="a"&gt;Mathematics&lt;/subfield&gt;
          &lt;subfield code="x"&gt;Philosophy.&lt;/subfield&gt;
          &lt;subfield code="2"&gt;fast&lt;/subfield&gt;
          &lt;subfield code="0"&gt;(OCoLC)fst01012213&lt;/subfield&gt;
    &lt;/datafield&gt;
        &lt;datafield tag="700" ind1="1" ind2=" "&gt;
          &lt;subfield code="a"&gt;Russell, Bertrand,&lt;/subfield&gt;
          &lt;subfield code="d"&gt;1872-1970,&lt;/subfield&gt;
          &lt;subfield code="e"&gt;joint author.&lt;/subfield&gt;
    &lt;/datafield&gt;
  &lt;/record&gt;
    &lt;/response&gt;
  &lt;/content&gt;
  &lt;id&gt;http://worldcat.org/oclc/01039085&lt;/id&gt;
  &lt;link href="http://worldcat.org/oclc/01039085"&gt;&lt;/link&gt;
&lt;/entry&gt;
</pre>

## Notes

You'll notice that we did not include any HMAC hashing examples in popular client side languages, such as Javascript, Objective-C or Android Java. That is because HMAC Signature is an authentication technique that <b>should only be used on the server side</b>. Placing a "live" webservices key into a client side application means that application has full access to the database without requiring an individual to authenticate who they are first, and is an unacceptable security risk. For the case of client-side authentication direct to OCLC API's, say for a mobile application, you would use the token access authentication. Sample code for <a href="https://github.com/OCLC-Developer-Network/oclc-auth-ios-example">iOS</a> and <a href="https://github.com/OCLC-Developer-Network/oclc-auth-ios-example">Android</a> is available from OCLC's github account. Read more about the <a href="http://www.oclc.org/developer/platform/user-agent-or-mobile-pattern">User Agent / Mobile Pattern here</a>.

