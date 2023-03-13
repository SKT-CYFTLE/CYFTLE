import requests

api_key = "{your api key}"
endpoint_url = "{your endpoint url}"
def tts(text):
    data = '<speak><voice name="Kai">'+ text.encode('utf-8').decode('latin1') + '</voice></speak>'
    headers = {
        "x-api-key": api_key,
        "Content-Type": "application/xml",
        "X-TTS-Engine": "deep"
    }
    response = requests.post(endpoint_url, headers=headers, data=data)
    #return requests.post(endpoint_url, headers=headers, data=data)
    with open("./translate/tts/test.mp3", "wb") as f:
        f.write(response.content)