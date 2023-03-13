from fastapi import FastAPI, File, UploadFile
from typing import List, Optional
from pydantic import BaseModel
from fastapi.responses import FileResponse
from dalle import getImage
from revChatGPT.chatbot import chatgpt
from translate.translation import translation
from translate.tts_test import tts
import json, os
from datetime import datetime
from tts.get_mp3_file import get_mp3
app = FastAPI()
# for content
class Item(BaseModel):
    content: str
# for question making
class Story_id(BaseModel):
    p_id: str
    c_id: str

# for test
@app.get("/")
async def root():
    #file_path = get_mp3("시간아 멈춰 딱")
    #return FileResponse(file_path, media_type="audio/mp3")
    return {"message": "Hello World"}
# chat gpt api
# input: text
# output: JSON (korean story, image link)

@app.get("/chatgpttest")
async def chatgpt_test():
    response, c_id, p_id = chatgpt("tell me traveling lion fairytale " + "in 5 paragraphs")
    # summarize
    response_summarized, c_id, p_id = chatgpt("Summarize each paragraph briefly to use for an input of image generation model.", c_id, p_id)
    response_summarized_split = response_summarized.split('\n')

    return response_summarized_split[0][3:]

@app.get("/dalletest")
async def dalle_test():
    dalle_image = getImage("Lion traveling under the sea, fairytale style, dynamic, spirited")
    print(dalle_image['data'])
    return dalle_image

@app.get("/translation_test")
async def translation_test():
    
    EngToKor = translation("hi my name is prince song", True)
    KorToEng = translation("안녕하세요 제 이름은 송승우입니다.", False)
    #return EngToKor+KorToEng
    dicts = {'kor': EngToKor}
    return json.dumps(dicts, ensure_ascii = False)

@app.post("/make_story_parse_test/")
async def make_story_parse_test(item: Item):
    cur_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(cur_time + " : parse test called")
    #response = "Once upon a time, there was a lion named Leo who lived in the African savannah. Leo was always fascinated by the stars in the night sky and dreamed of traveling to space one day. One night, he saw a shooting star and made a wish to go to space.\n\nThe next morning, Leo was surprised to find a spaceship had crash-landed near his home. He cautiously approached the ship and found a group of friendly aliens inside. They had been traveling through space when their ship malfunctioned, and they needed Leo's help to fix it.\n\nLeo was excited to help the aliens and jumped into action, using his strong muscles and sharp claws to repair the ship's engine. In return, the aliens offered to take him on a journey through the stars. Leo eagerly accepted their offer and climbed aboard the ship.\n\nAs they traveled through space, Leo was in awe of the stunning views of the galaxies, planets, and stars. He saw colorful nebulas, massive black holes, and even witnessed a supernova. Leo was amazed by the beauty and vastness of the universe.\n\nFinally, after a thrilling adventure, Leo said goodbye to his new alien friends and returned to his home in the savannah. He now knew that his dreams of space travel were possible, and he felt grateful for the incredible journey he had experienced. Leo looked up at the stars in the night sky and smiled, knowing that he had been on an adventure that few lions could ever imagine."
    story_list = ["asd", "asda", "sdfsdf", "sdfsdf", "sdfsdf"] 
    p_id = "asd"
    c_id = "asd"

    item_dict = item.dict()
    input_text = item_dict['content']
    
    # chat gpt method call
    response, c_id, p_id = chatgpt(input_text + " in 5 paragraphs")
    print("response done")

    return json.dumps({"full": response, "summarized": story_list, "p_id": p_id, "c_id": c_id})


# eng 영한, kor 한영
@app.post("/translating/")
def translating(item: Item, lang="kor"):
    cur_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(cur_time + " : translation called")
    item_dict = item.dict()
    #translation call
    if lang == "kor":
        return translation(item_dict['content'], False)[0]['translations'][0]['text']
    elif lang == "eng":
        return translation(item_dict['content'], True)[0]['translations'][0]['text']
    else:
        return "language format error"

@app.post("/make_story/{target}")
def make_story(item: Item, target:str):
    cur_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(cur_time + " : make story called")
    item_dict = item.dict()
    input_text = item_dict['content']
    prompt = ""
    if target == "children":
        print("for children")
        prompt = ", for children to read"
    elif target == "adult":
        print("for adult")
        prompt = ""
    input_text = input_text.strip('"')
    print(input_text)
    # chat gpt method call
    response, c_id, p_id = chatgpt(input_text + ", exactly in 5 paragraphs" + prompt)
    print("response done")
    print(response)
    return json.dumps({"full": response, "p_id": p_id, "c_id": c_id})

@app.post("/story_summarize/")
def story_summarize(item: Story_id):
    cur_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(cur_time + " : story summarize called")
    item_dict = item.dict()
    p_id = item_dict['p_id']
    c_id = item_dict['c_id']
    
    # summarize
    response_summarized, c_id, p_id = chatgpt("Summarize each paragraph's most important sentence briefly to be used as an input of image generation model.", c_id, p_id)
    print("summarizing done")

    response_summarized_split = response_summarized.split('\n')

    print(response_summarized_split[1])
    story_list = []
    for i in response_summarized_split:
        i = i.replace("\"", "")
        story_list.append(i[3:])
    print(story_list[0])
    return json.dumps({"summarized": story_list, "p_id": p_id, "c_id": c_id})

@app.post("/make_question/")
def make_question(item: Story_id):
    cur_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(cur_time + " : make question called")
    item_dict = item.dict()
    p_id = item_dict['p_id']
    c_id = item_dict['c_id']

    # chat gpt method call
    response, c_id, p_id = chatgpt("Tell me 3 questions to think about this story.", c_id, p_id)
    response_split = response.split('\n')

    question_list = []

    for i in response_split:
        i = i.replace("\"", "")
        question_list.append(i[3:])

    return json.dumps({"question": question_list, "p_id": p_id, "c_id": c_id})
    
@app.post("/sentence_summarize/")
def sentence_summarize(item: Story_id):
    cur_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(cur_time + " : story summarize called")
    item_dict = item.dict()
    p_id = item_dict['p_id']
    c_id = item_dict['c_id']
    
    # summarize
    response_summarized, c_id, p_id = chatgpt("Summarize above answers for each to be used as an input of image generation model.", c_id, p_id)
    print("summarizing done")

    response_summarized_split = response_summarized.split('\n')

    print(response_summarized_split[1])
    story_list = []
    for i in response_summarized_split:
        i = i.replace("\"", "")
        story_list.append(i[3:])
    print(story_list[0])
    return json.dumps({"summarized": story_list, "p_id": p_id, "c_id": c_id})

@app.post("/make_answer/")
def make_answer(item: Story_id):
    cur_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(cur_time + " : make answer called")
    item_dict = item.dict()
    p_id = item_dict['p_id']
    c_id = item_dict['c_id']

    # chat gpt method call
    response, c_id, p_id = chatgpt("Give me your simple answers about these questions.", c_id, p_id)
    response_split = response.split('\n')

    answer_list = []

    for i in response_split:
        i = i.replace("\"", "")
        answer_list.append(i[3:])

    return json.dumps({"answer": answer_list, "p_id": p_id, "c_id": c_id})

@app.post("/make_image/{target}")
def make_image(item: Item, target:str):
    cur_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(cur_time + " : make image called")
    item_dict = item.dict()
    
    if target == "children":
        print("for children")
        prompt = ", digital art"
    elif target == "adult":
        print("for adult")
        prompt = ", realistic photo"

    image_link = getImage(item_dict['content']+prompt)['data']
    image_link = image_link[0].to_dict()
    return image_link
    
    #return {"url": "https://oaidalleapiprodscus.blob.core.windows.net/private/org-7g0FLkZzbkWMcyyz7RQ45NMK/user-eBWY4xUd9nKD2qOP3ozjeIBG/img-f6Uvlx24viYMYs8BvJyNND3X.png?st=2023-02-24T13%3A25%3A27Z&se=2023-02-24T15%3A25%3A27Z&sp=r&sv=2021-08-06&sr=b&rscd=inline&rsct=image/png&skoid=6aaadede-4fb3-4698-a8f6-684d7786b067&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2023-02-23T20%3A02%3A09Z&ske=2023-02-24T20%3A02%3A09Z&sks=b&skv=2021-08-06&sig=EmwOmPB94Zbs%2BTJjqiQuGeLFdf1jIVqbtP%2BrBXgqnJg%3D"}
 
@app.post("/tts_kakao/")
def text_sound_kakao(item: Item):
    cur_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(cur_time + " : tts called")
    item_dict = item.dict()
    tts(item_dict['content'])
    return "success"

@app.post("/tts_azure/")
def text_sound_azure(item: Item):
    cur_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(cur_time + " : tts azure called")
    item_dict = item.dict()
    file_path = get_mp3(item_dict['content'])
    return "success"

@app.get("/tts_result/{program_name}")
def text_sound_result(program_name:str):
    cur_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(cur_time + " : tts result called")
    
    if program_name == "azure":
        return FileResponse("./tts/extracted/0001.mp3", media_type="audio/mp3", headers={"Content-Disposition": "attachment"})
    elif program_name == "kakao":
        return FileResponse("./translate/tts/test.mp3", media_type="audio/mp3", headers={"Content-Disposition": "attachment"})
    else:
        return "no such program"

@app.post("/age_prediction/")
async def age_predict(file: UploadFile):
    # saving image file
    UPLOAD_DIR = "./images"
    content = await file.read()
    filename = f"age.jpg"
    with open(os.path.join(UPLOAD_DIR, filename), "wb") as fp:
        fp.write(content)  # 서버 로컬 스토리지에 이미지 저장 (쓰기)

    # deeplearning model process

    # return age label

@app.post("/items/")
async def generate_fairytale(item: Item, timeout=120):
    print("api called")
    item_dict = item.dict()
    #translation call
    input_text = translation(item_dict['content'], False)[0]['translations'][0]['text']

    # chat gpt method call
    response, c_id, p_id = chatgpt(input_text + "in 5 paragraphs")
    print("response done")
    # summarize
    response_summarized, c_id, p_id = chatgpt("Summarize each paragraph's most important sentence briefly to be used as an input of image generation model.", c_id, p_id)
    print("summarizing done")

    response_summarized_split = response_summarized.split('\n')
    img_list = [] 
    print("split done")

    for i in response_summarized_split:
        img_list.append(getImage(i[3:]+", fairytale style")['data'])

    print("dalle done")
    # dalle image generation
    # dalle_image = getImage(response_summarized)

    # translate into Kor
    response_kor = translation(response, True)[0]['translations'][0]['text']
    print("translation done")

    dict_for_return = {'story': response_kor, 'images': img_list}

    return json.dumps(dict_for_return, ensure_ascii = False)
