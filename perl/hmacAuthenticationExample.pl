#!/usr/bin/perl

###############################################################################
# Copyright 2014 OCLC
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
###############################################################################

# Perl 5.12 HMAC Authentication and Worldcat Metadata Bibliographic Record GET

use Digest::SHA qw(hmac_sha256_base64);
use MIME::Base64;
use LWP; # port install p5.12-libwww-perl
use LWP::UserAgent;
$ENV{'PERL_LWP_SSL_VERIFY_HOSTNAME'} = 0;

sub urlencode { 
    my $s = shift; 
    $s =~ s/ /+/g; 
    $s =~ s/([^A-Za-z0-9+-])/sprintf("%%%02X", ord($1))/seg; 
    return $s; 
}

# Define constants

$wskey         = "";
$secret        = "";
$principalID   = ";
$principalIDNS = "";
$institutionId = "128807";
$classificationScheme = "LibraryOfCongress";
$holdingLibraryCode = "MAIN";
$oclcNumber = "1039085";

$startIndex    = "1";
$itemsPerPage  = "10";

# Quote and quote with a comma - used for constructing url later
$q = "\"";
$qc = "\",";

$urlpattern = "https://worldcat.org/bib/data/{oclcNumber}?" .
                  "inst={inst}" .
                  "&classificationScheme={classificationScheme}" .
                  "&holdingLibraryCode={holdingLibraryCode}";

# construct the parameter list
$queryparams = "".
"classificationScheme=" . $classificationScheme . "\n" .
"holdingLibraryCode=" . $holdingLibraryCode . "\n" .
"inst=".$institutionId."\n";

print "\nQuery Parameters:\n".$queryparams."\n\n";

# set the method
$method = "GET";

# construct the url
$url = $urlpattern;
$url =~ s/{classificationScheme}/$classificationScheme/;
$url =~ s/{holdingLibraryCode}/$holdingLibraryCode/;
$url =~ s/{oclcNumber}/$oclcNumber/;
$url =~ s/{inst}/$institutionId/;

print "URL:\n".$url."\n\n";

# create the timestamp, POSIX seconds since 1970 (aka Unix Time)
$timestamp = time;

# create the nonce, a random 8 digit hex string
$nonce = sprintf("%x", int(rand()*4026531839+268435456));

# for this implementation, bodyhash is empty string
$bodyhash = "";

# create the normalized request
$normalizedRequest = $wskey."\n".
$timestamp."\n".
$nonce."\n".
$bodyhash."\n".
$method."\n".
"www.oclc.org"."\n".
"443"."\n".
"/wskey"."\n".
$queryparams;

print "Normalized Request:\n".$normalizedRequest."\n\n";

# hash the normalized request
$signature = hmac_sha256_base64($normalizedRequest, $secret) . "=";

# create the authorization header
$authorization = "http://www.worldcat.org/wskey/v2/hmac/v1 "."clientId=".$q.$wskey.$qc.
"timestamp=".$q.$timestamp.$qc.
"nonce=".$q.$nonce.$qc.
"signature=".$q.$signature.$qc.
"principalID=".$q.$principalID.$qc.
"principalIDNS=".$q.$principalIDNS.$q;

print "Authorization Header:\n".$authorization."\n\n";

# Make the HTTP request
$ua = new LWP::UserAgent;
if ($method=='GET') {
    $req = new HTTP::Request GET => $url;
} elsif ($method=='POST') {
    $req = new HTTP::Request POST => $url;
    $req->content_type('application/x-www-form-urlencoded');
    $req->content('xml=<start></start>');
}
$req->header(Authorization => $authorization);
my $res = $ua->request($req);
$xmlresult = $res->content;

print "Result:\n".$xmlresult;
