import os
import requests

OAUTH_BASE_URL = "https://oauth.canto.global"

SITE_BASEURL = "https://XXXX.canto.global"

APP_ID = os.getenv("APP_ID")

APP_SECRET = os.getenv("APP_SECRET")


# Get access token
url = f"{OAUTH_BASE_URL}/oauth/api/oauth2/token?app_id={APP_ID}&app_secret={APP_SECRET}&grant_type=client_credentials"

r = requests.post(url)
data = r.json()

access_token = data["accessToken"]
token_type = data["tokenType"]

# Get upload settings
url = f"{SITE_BASEURL}/api/v1/upload/setting"

# Returns for example:
# {
#   "Policy": "eyAiZXhwaXXXXXXvbiI6ICIyMDIxLTAxLTA2VDIxOjQyOjM4LjkzN1oiLAogICJjb25kaXRpb25zIjogWwogICAgeyJidWNrZXQiOiAiZGVmYXVsdC11cGxvYWQtYnVja2V0LXN0YWdpbmctcHVibGljIn0sCiAgICB7ImFjbCI6ICJwcml2YXRlIn0sCiAgICBbInN0YXJ0cy13aXRoIiwgIiRrZXkiLCAiYmM0ZjllZjYtYzRkNS00MGQxLTlhM2QtYjA4MThmZWRlZDNkL2FrcmlzaG5hY2hhbmRyYW5AY2FudG8uY29tIl0sCiAgICBbInN0YXJ0cy13aXRoIiwgIiR4LWFtei1tZXRhLWZpbGVfbmFtZSIsICIiXSwKICAgIFsic3RhcnRzLXdpdGgiLCAiJHgtYW16LW1ldGEtdGFnIiwgIiJdLAogICAgWyJzdGFydHMtd2l0aCIsICIkeC1hbXotbWV0YS1zY2hlbWUiLCAiIl0sCiAgICBbInN0YXJ0cy13aXRoIiwgIiR4LWFtei1tZXRhLWlkIiwgIiJdLAogICAgWyJzdGFydHMtd2l0aCIsICIkeC1hbXotbWV0YS1hbGJ1bV9pZCIsICIiXQogIF0KfQ==",
#   "x-amz-meta-scheme": "",
#   "Signature": "EjqtXXXJDqg3Iw/QD28tJCA+LWw=",
#   "AWSAccessKeyId": "AKIAQXXX4S3M6TI2J4BI",
#   "x-amz-meta-album_id": "",
#   "acl": "private",
#   "x-amz-meta-tag": "",
#   "x-amz-meta-id": "",
#   "x-amz-meta-file_name": "${filename}",
#   "url": "https://default-upload-bucket-staging-public.s3-accelerate.amazonaws.com/",
#   "key": "bc4f9ef6-c4d5-40d1-9a3d-b0818feded3d/akrishnachandran@canto.com/${filename}"
# }
# Valid for 5 hours
r = requests.get(url, headers={"Authorization": f"{token_type} {access_token}"})
data = r.json()


# Upload file
image_url = "https://upload.wikimedia.org/wikipedia/commons/3/38/EE-logo-yellow.png"

url = data["url"]
print(url)

data = {
    "key": data["key"],
    "acl": data["acl"],
    "AWSAccessKeyId": data["AWSAccessKeyId"],
    "Policy": data["Policy"],
    "Signature": data["Signature"],
    "x-amz-meta-file_name": "EE-logo-yellow.png",
    "x-amz-meta-tag": "",
    "x-amz-meta-scheme": "",
    "x-amz-meta-id": "",
    "x-amz-meta-album_id": "G1BAM",

    # Invalid param unless you set parameter refer=true in the Get upload setting endpoint.
    # "x-amz-meta-refer_id": "",
}

r = requests.get(image_url)
files = {"file": r.content}

r = requests.post(
    url,
    data=data,
    files=files,

    # Doesn't need Authorization header. Credentials in form params.
    # headers={"Authorization": f"{token_type} {access_token}"},
)

print(r.status_code)
print(r.text)
