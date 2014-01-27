#!/usr/bin/python

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

# Python 2.75 HMAC Authentication and WMS Pull List GET

import random
import math
import time
import urllib
import hmac
import hashlib
import base64
import urllib2

# Define constants
wskey         = ""
secret        = ""
principalID   = ""
principalIDNS = ""
institutionId = ""
branchId      = ""
startIndex    = "1"
itemsPerPage  = "10"

# Declare variables
url = ""
queryparams = ""
principalIDEncoded = ""
principalIDNSEncoded = ""
timestamp = ""
nonce = ""
bodyhash = ""
method = ""
normalizedRequest = ""
signature = ""
authorization = ""
header = ""
q = "\""
qc = "\","
uri = ""
http = ""
request = ""
xmlresult = ""

urlpattern = "https://circ.sd00.worldcat.org/pulllist/{branchId}?inst={institutionId}&principalID={principalIDEncoded}&principalIDNS={principalIDNSEncoded}"

principalIDEncoded = urllib.quote(principalID)
principalIDNSEncoded = urllib.quote(principalIDNS)

# construct the parameter list
queryparams = "inst=" + institutionId + "\n" + "principalID=" + principalIDEncoded + "\n" + "principalIDNS=" + principalIDNSEncoded + "\n"

# set the method
method = "GET"

# construct the url
url = urlpattern
url = url.replace("{branchId}",branchId)
url = url.replace("{institutionId}",institutionId)
url = url.replace("{principalIDEncoded}",principalIDEncoded)
url = url.replace("{principalIDNSEncoded}",principalIDNSEncoded)

# create the timestamp, POSIX seconds since 1970 (aka Unix Time)
timestamp = str(int(time.time()))

# create the nonce, a random 8 digit hex string
nonce = hex(int(math.floor(random.random()*4026531839+268435456)))

# for this implementation, bodyhash is empty string
bodyhash = ""

# create the normalized request
normalizedRequest = wskey + "\n" + timestamp + "\n" + nonce + "\n" + bodyhash + "\n" + method + "\n" + "www.oclc.org" + "\n" + "443" + "\n" + "/wskey" + "\n" + queryparams

# hash the normalized request
digest = hmac.new(secret, msg=normalizedRequest, digestmod=hashlib.sha256).digest()
signature = base64.b64encode(digest).decode()

# create the authorization header
authorization = "http://www.worldcat.org/wskey/v2/hmac/v1 " + "clientId=" + q + wskey + qc + "timestamp=" + q + timestamp + qc + "nonce=" + q + nonce + qc + "signature=" + q + signature + q

# Make the HTTP request
if method == 'GET':
    myRequest = urllib2.Request(url,None,{'Authorization':authorization})
else:
    myRequest = urllib2.Request(url,xmlrequest,{'Authorization':authorization})
xmlresult = urllib2.urlopen(myRequest).read()

print xmlresult
