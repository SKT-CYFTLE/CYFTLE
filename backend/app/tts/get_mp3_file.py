import os
import zipfile
import requests
from .make_azure_tts import *

def get_mp3(text): # text - 동화 문자열
    url = making_tts_file(text)
    filename = 'result.zip'
    response = requests.get(url)
    path = './tts/downloads/' # result.zip을 다운 받을 경로
    if not os.path.exists(path):
        os.makedirs(path)
    with open(os.path.join(path, filename), 'wb') as f:
        f.write(response.content)
    extract_path = './tts/extracted/' # 압축 해제한 파일들이 존재하는 디렉토리, 위 path와 같은 경로를 사용하려면 밑의 extract_path[1]에서 index 1 말고 2로 바꿔야 함
    with zipfile.ZipFile(os.path.join(path, filename), 'r') as zip_ref:
        zip_ref.extractall(extract_path)
    tmp = os.path.join(extract_path, os.listdir(extract_path)[2])
    print(tmp)
    return tmp
    #return os.path.join(extract_path, os.listdir(extract_path)[2])