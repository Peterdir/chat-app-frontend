const admin = require("firebase-admin");
const http = require("http");

const port = process.env.PORT || 3000;

http
  .createServer((req, res) => {
    if (req.url === "/health") {
      res.writeHead(200, { "Content-Type": "text/plain" });
      res.end("ok");
      return;
    }
    res.writeHead(200, { "Content-Type": "text/plain" });
    res.end("FCM worker alive");
  })
  .listen(port, "0.0.0.0", () => {
    console.log(`Health server listening on ${port}`);
  });

function requiredEnv(name) {
  const value = process.env[name];
  if (!value || !value.trim()) {
    throw new Error(`Missing required env var: ${name}`);
  }
  return value;
}

function initFirebaseAdmin() {
  const projectId = requiredEnv("FIREBASE_PROJECT_ID");
  const clientEmail = requiredEnv("FIREBASE_CLIENT_EMAIL");
  const privateKey = requiredEnv("FIREBASE_PRIVATE_KEY").replace(/\\n/g, "\n");
  const databaseURL = requiredEnv("FIREBASE_DATABASE_URL");

  admin.initializeApp({
    credential: admin.credential.cert({
      projectId,
      clientEmail,
      privateKey,
    }),
    databaseURL,
  });
}

function buildNotification(eventData) {
  const senderName = eventData.senderName || "Someone";
  const channelName = eventData.channelName || "channel";
  const title = `${senderName} trong #${channelName}`;
  const body = (eventData.content || "").slice(0, 180) || "Ban vua nhan duoc tin nhan moi";

  return { title, body };
}

async function handlePushEvent(snapshot) {
  const eventId = snapshot.key;
  const eventData = snapshot.val() || {};

  const topic = eventData.topic;
  const serverId = eventData.serverId;
  const channelId = eventData.channelId;
  const channelName = eventData.channelName || "channel";

  if (!topic || !serverId || !channelId) {
    console.warn(`[skip] invalid event payload id=${eventId}`);
    await snapshot.ref.remove();
    return;
  }

  const notification = buildNotification(eventData);

  const message = {
    topic,
    notification,
    android: {
      priority: "high",
      notification: {
        channelId: "chat_messages",
      },
    },
    data: {
      title: notification.title,
      body: notification.body,
      serverId: String(serverId),
      channelId: String(channelId),
      channelName: String(channelName),
    },
  };

  try {
    const response = await admin.messaging().send(message);
    console.log(`[sent] event=${eventId} topic=${topic} msg=${response}`);
  } catch (error) {
    console.error(`[failed] event=${eventId}`, error);
  } finally {
    await snapshot.ref.remove();
  }
}

async function main() {
  initFirebaseAdmin();

  const db = admin.database();
  const queueRef = db.ref("chat_push_events");

  console.log("Render FCM worker is running and listening chat_push_events...");

  queueRef.on("child_added", (snapshot) => {
    handlePushEvent(snapshot).catch((error) => {
      console.error("Unexpected worker error", error);
    });
  });

  process.on("SIGTERM", () => {
    console.log("SIGTERM received. Stopping worker.");
    queueRef.off();
    process.exit(0);
  });
}

main().catch((error) => {
  console.error("Worker boot failed", error);
  process.exit(1);
});
