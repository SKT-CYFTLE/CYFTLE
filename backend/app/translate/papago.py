from selenium import webdriver
from selenium.webdriver.common.by import By
import time

s = time.time()
options = webdriver.ChromeOptions() 
options.add_experimental_option('excludeSwitches', ['enable-logging'])

driver = webdriver.Chrome(executable_path="C:/Users/049/Downloads/chromedriver.exe", options=options)
URL = "https://papago.naver.com/"
driver.get(URL)

question = ["As the lion set out on his adventure, he was accompanied by his dear friends, a wise owl, a brave fox and a clever raccoon. They traveled through dense forests, crossed treacherous rivers and climbed steep mountains.", "Along the way, they encountered many challenges and obstacles. But the lion, with his courage and strength, led the way and helped his companions overcome each one. The wise owl used his intelligence to navigate through the unknown terrain, the brave fox used his agility to outsmart the predators and the clever raccoon used his resourcefulness to find food and shelter for the group.", "As they journeyed deeper into the unknown lands, they came across a mighty dragon who guarded a magical treasure. The lion, with the help of his companions, battled the dragon and emerged victorious. They found the treasure, a beautiful golden crown, and decided to bring it back to their kingdom and present it to the king.", "As they returned home, the kingdom was in awe of the lion and his companions' bravery and the king declared them as heroes. They were showered with honors and rewards for their deeds and the lion's reputation as a true leader and a loyal friend was solidified.", "From that day on, the lion and his companions went on many more adventures together and their friendship grew stronger with each one. They became known as the bravest and most loyal companions in the land."]

result_text = []
clicked = False
text_sended = False # 높임말 버튼 활성화 여부 - 텍스트가 보내지면 버튼이 생성됨
for q_text in question:
  while not text_sended:
    try:
      form = driver.find_element(By.CSS_SELECTOR, "textarea#txtSource")
      form.send_keys(q_text)
      text_sended = True
    except:
      time.sleep(0.5)

  button = driver.find_element(By.CSS_SELECTOR, "button#btnTranslate")
  button.click()
  # time.sleep(1)

  if not clicked:
    honorific = driver.find_element(By.XPATH, '//*[@id="root"]/div/div[1]/section/div/div[1]/div[2]/div/div[6]/div/button') # 높임말
    honorific.click()
    time.sleep(1.5)
    clicked = True

  result = driver.find_element(By.CSS_SELECTOR, "div#txtTarget")
  result_text.append(result.text)
  time.sleep(1) # 속도가 너무 빨라서 sleep을 안 넣어주면 다음 프롬프트가 읽히기 전에 전 프롬프트를 이미 읽어옴
  form.clear()
  text_sended = False

for tale in result_text:
  print(tale, "\n")

# while(True): # 계속 창 켜놓기
#   pass

print(time.time()-s)
# 1/3
# 기존 동화 내용을 요약해서 이 다음은
# 2/3
# 만들어진 내용을 여기서 번역 필요 1 (영 한)
# 3/3 
# 사용자한테 입력받은 stt 결과물을 한글 -> 영어로 번역해서 chatgpt에 넣어줌 번역 2 (한 영)
# # 그 결과물을 다시 번역 필요 3(영 한)

# 2번
# 크롬창 매번 띄우는 것이 아닌 한번 실행하면 내가 닫는 명령을 내리기 전까지 인풋을 계속 받을 준비를 하고 있게 대기시키기

# 1. time.sleep 없애기
# 2. requirement 만들기