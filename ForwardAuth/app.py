import hashlib

import jwt
from flask import Flask, request
from pymongo import MongoClient
from hashlib import pbkdf2_hmac
from flask import Response
import os
from time import time
#import pyjwt
from werkzeug.datastructures import Headers

app = Flask(__name__)


def get_database():
    # Provide the mongodb atlas url to connect python to mongodb using pymongo
    connection_string = os.getenv("CONNECTION_STRING")

    # Create a connection using MongoClient. You can import MongoClient or use pymongo.MongoClient

    client = MongoClient(connection_string)

    # Create the database for our example (we will use the same database throughout the tutorial
    return client['Archimedes-Rest']


db = get_database()


def get_user(username):
    return db.users.find_one({"username": username})


@app.route('/')
def hello():
    return "im running :)"


@app.route('/auth')
def auth():
    if request.headers.get("Authorization"):
        jwtr = request.headers.get("Authorization").replace("Bearer ", "")
        data = jwt.decode(jwt=jwtr, key="secret", algorithms="HS256")
        if data["expire"] < time():
            d = Headers()
            d.add('Content-Type', 'application/json')
            return Response('{"message": "token expired"}', status=403, headers=d)
    else:
        d = Headers()
        d.add('Content-Type', 'application/json')
        return Response('{"message": "no token or wrong token"}', status=403, headers=d)
    return Response(status=200)


@app.route('/login', methods=["POST"])
def login():
    request_data = request.get_json()
    username = request_data["username"]
    password = request_data["password"]
    user = get_user(username)
    if user is None:
       return Response({"message": "No User found with this username!"}, status=404)

    if hashlib.pbkdf2_hmac('sha256', password=bytes(password, 'utf-8'), salt=user['salt'], iterations=1000) != user["password"]:
       return Response({"message": "Wrong password!"}, status=403)

    data = {
        "username": username,
        "expire": time() + 60*60*24
    }
    #os.getenv("JWT_SECRET")
    token = jwt.encode(data, "secret", algorithm="HS256")
    return {"username": username, "token": token}


@app.route('/register', methods=["POST"])
def register():
    request_data = request.get_json()
    username = request_data["username"]
    password = request_data["password"]
    email = request_data.get("email")
    friends = request_data.get("friends")

    h = Headers()
    h.add('Content-Type', 'application/json')
    if email is None:
        email = ""

    if friends is None:
        friends = []

    if get_user(username) is not None:
        return Response('{"message": "Username already in use!"}', status=409, headers=h)
    salt = os.urandom(16)
    user = {
        "username": username,
        "password": pbkdf2_hmac('sha256', password=bytes(password, 'utf-8'), salt=salt, iterations=1000),
        "salt": salt,
        "email": email,
        "friends": friends
    }

    db.users.insert_one(user)
    return Response(status=200)


if __name__ == '__main__':
    app.run()
