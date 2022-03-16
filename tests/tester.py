# This is a sample Python script.

# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.
import random
import requests


url = 'http://127.0.0.1:8000'
url2 = 'http://127.0.0.1:8080'

headers = {
    'Content-type': 'application/json',
    'Accept': 'application/json'
}

def generate_string():
    chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
    string = ''
    for i in range(10):
        string = string + chars[random.randint(0, 61)]

    return string


def insert(test_email):
    response = requests.post(url2 + '/register', json={
        #for only one username
        "username": generate_string(),
        "password": "forgotten",
        "email": test_email,
        "friends": []
    }, headers=headers)
    print(response.status_code)


def askmail(test_email):
    response = requests.post(url + '/playground/email/', json={"email": test_email}, headers=headers)
    print(response.json())


if __name__ == '__main__':
    test_email_address = input("email address for testing > ")
    # Creates a new user with the given email address
    insert(test_email_address)
    # Asks for new password
    askmail(test_email_address)


