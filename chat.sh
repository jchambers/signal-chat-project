#!/bin/sh

# This is a trivial script for practical testing. It creates a chat between two
# users, sends a few (out-of-order) messages, and lists the messages from that
# chat.

curl http://localhost/chats?userId=51201
echo
curl http://localhost/chats?userId=28463
echo
curl -d '{"id":1,"participantIds":[51201,28463]}' -H "Content-Type: application/json" http://localhost:80/chats
echo
curl http://localhost/chats?userId=51201
echo
curl http://localhost/chats?userId=28463
echo
curl -d '{"id":"d9d5e7d7-4b7d-409a-b3a8-d9cd524061d7","timestamp":1580354844323,"message":"First message!","sourceUserId":51201,"destinationUserId":28463}' -H "Content-Type: application/json" http://localhost:80/chats/1/messages
curl -d '{"id":"d9d5e7d7-4b7d-409a-b3a8-d9cd524061d8","timestamp":1580354846323,"message":"Third message!","sourceUserId":51201,"destinationUserId":28463}' -H "Content-Type: application/json" http://localhost:80/chats/1/messages
curl -d '{"id":"d9d5e7d7-4b7d-409a-b3a8-d9cd524061d9","timestamp":1580354845323,"message":"Second message!","sourceUserId":51201,"destinationUserId":28463}' -H "Content-Type: application/json" http://localhost:80/chats/1/messages
curl http://localhost/chats/1/messages
echo
