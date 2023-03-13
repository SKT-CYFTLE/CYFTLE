from . import V1

def chatgpt(input_msg, conv_id=None, parent_id=None): 
    chatbot = V1.Chatbot(config={
    "email": "{your email}",
    "password": "{your password}"
    })

    prev_text = ""
    c_id = ""
    p_id = ""
    for data in chatbot.ask(
        input_msg, conv_id, parent_id#질문
    ):
        message = data["message"][len(prev_text) :]
        #print(message, end="", flush=True) #답변
        prev_text = data["message"]
        c_id = data["conversation_id"]
        p_id = data["parent_id"]
    return prev_text, c_id, p_id
