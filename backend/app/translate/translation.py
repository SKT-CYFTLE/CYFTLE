import requests, uuid, json
# print(requests.__version__) v.2.28.1, v.1.30(uuid)

def translation(text, is_en_to_ko):
# Add your key and endpoint
    key = "{your api key}"
    endpoint = "https://api.cognitive.microsofttranslator.com"
    location = "{your region}"
    path = '/translate'
    constructed_url = endpoint + path
    
    params_en_to_ko = {
        'api-version': '3.0',
        'from': 'en',
        'to': 'ko'
    }
    params_ko_to_en = {
        'api-version': '3.0',
        'from': 'ko',
        'to': 'en'
    }

    headers = {
        'Ocp-Apim-Subscription-Key': key,
        'Ocp-Apim-Subscription-Region': location,
        'Content-type': 'application/json',
        'X-ClientTraceId': str(uuid.uuid4())
    }

    body_en_to_ko = [{
        'text': text
    }] # chatgpt로 생성된 동화(en -> ko)

    body_ko_to_en = [{
        'text': text
    }] # STT로 입력 받은 동화의 결말(ko -> en)

    if is_en_to_ko == True:
        ####### Translate the fairy tale created by chatgpt(using honorifics).
        request_tale_translation = requests.post(constructed_url, params=params_en_to_ko, headers=headers, json=body_en_to_ko)
        response_tale = request_tale_translation.json()
        # return json.dumps(response_tale, sort_keys=True, ensure_ascii=False, indent=4, separators=(',', ': '))
        return response_tale
        
    elif is_en_to_ko == False:
        ######## Translate the input received from the user by STT into English
        request_stt_translation = requests.post(constructed_url, params=params_ko_to_en, headers=headers, json=body_ko_to_en)
        response_ending_theme = request_stt_translation.json()
        # return json.dumps(response_ending_theme, sort_keys=True, ensure_ascii=False, indent=4, separators=(',', ': '))
        return response_ending_theme
    
    else:
        return "번역 모드 인자 전송이 잘못되었습니다."
        
# Usage
# print(translation("hi my name is prince song", True)[0]['translations'][0]['text']) # 영 -> 한
# print(translation("안녕하세요 제 이름은 송승우입니다.", False)[0]['translations'][0]['text']) # 한 -> 영