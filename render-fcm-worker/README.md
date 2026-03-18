# Render FCM Worker

This worker replaces Firebase Cloud Functions for chat push notifications.

It listens on Realtime Database path:
- chat_push_events

For each child added event, it:
- reads server members from `server_members/{serverId}`
- excludes the `senderId` from recipients
- collects recipient device tokens from `user_fcm_tokens/{uid}`
- sends FCM multicast to those recipient tokens
- deletes the queue item

## Runtime
- Node.js 18+

## Environment variables
Use values from your Firebase service account and Realtime Database:
- FIREBASE_PROJECT_ID
- FIREBASE_CLIENT_EMAIL
- FIREBASE_PRIVATE_KEY
- FIREBASE_DATABASE_URL

See .env.example.

## Deploy to Render
1. Push this repo to GitHub.
2. Create a new Render Web Service.
3. Root directory: render-fcm-worker
4. Build command: npm install
5. Start command: npm start
6. Add all environment variables from .env.example
7. Deploy service.

## Security notes
- Keep service account key secret in Render environment variables.
- Restrict database rules to avoid unauthorized writes.

## Queue payload expected
The Android app writes queue events with fields:
- topic
- serverId
- channelId
- channelName
- senderId
- senderName
- content
- createdAt

Notes:
- `topic` is kept for backward compatibility in payload, but worker routing is based on `serverId` + `senderId` + user token map.

## Deep link payload sent to app
FCM data payload includes:
- serverId
- channelId
- channelName
- title
- body

Android app handles this and opens the right channel chat.
