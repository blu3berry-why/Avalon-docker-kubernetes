import random
from django.http import HttpResponse
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
import json
import smtplib
import requests
import os

#------- secret app password ----------
login_password = os.environ.get("APP_PASSWORD_EMAIL")
login_email =  os.environ.get("EMAIL_ADDRESS",'avalon.f.pass@gmail.com')
#--------------------------------------

my_header = {
    'Content-type': 'application/json',
    'Accept': 'application/json'
}




d_from_address = login_email
d_subject = 'forgotten_password'


def get_database():
    import certifi
    ca = certifi.where()

    from pymongo import MongoClient
    import pymongo

    # Provide the mongodb atlas url to connect python to mongodb using pymongo
    CONNECTION_STRING = os.environ.get("CONNECTION_STRING")

    # Create a connection using MongoClient. You can import MongoClient or use pymongo.MongoClient
    from pymongo import MongoClient
    client = MongoClient(CONNECTION_STRING)

    # Create the database for our example (we will use the same database throughout the tutorial
    return client['Archimedes-Rest']


def send_direct_mail(from_address, to_address, subject, body):
    smtp_object = smtplib.SMTP('smtp.gmail.com')
    print(smtp_object.ehlo())
    print(smtp_object.starttls())
    print(smtp_object.login(login_email, login_password))
    msg = 'Subject: ' + subject + '\n' + body

    smtp_object.sendmail(from_address, to_address, msg)


def get_mail(request):
    body_unicode = request.body.decode('utf-8')
    body = json.loads(body_unicode)
    return body['email']


# Create your views here.
def say_hello(request):
    return HttpResponse("Hello World")


def generate_password():
    chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
    string = ''
    for i in range(10):
        string = string + chars[random.randint(0, 61)]

    return string


@csrf_exempt
def email(request):
    if request.method == "POST":
        data = get_mail(request)
        dbname = get_database()
        users = dbname.get_collection('user')
        u = users.find_one({"email": {"$eq": data}})
        if u is not None:
            new_password = generate_password()

            u['password'] = new_password

            requests.put(os.environ.get("KTOR_APP") + u['username'], headers=my_header, json=u)


            send_direct_mail(from_address=str(d_from_address),
                            to_address=str(u['email']), subject=d_subject, body=('Your new password is -> ' + new_password))
            return JsonResponse({"data": data})
        return JsonResponse({"data": "No data with the given address!"})
    return JsonResponse({"data": "Haven't reveived anything"})