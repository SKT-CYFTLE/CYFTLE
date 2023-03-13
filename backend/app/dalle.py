import os
import openai
openai.api_key = '{your api key}'  # your api key


def getImage(text):
    return openai.Image.create(
        prompt=text,
        n=1,
        size="256x256"
    )
