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

function chunkArray(items, chunkSize) {
  const result = [];
  for (let i = 0; i < items.length; i += chunkSize) {
    result.push(items.slice(i, i + chunkSize));
  }
  return result;
}

async function collectRecipientTokens(serverId, senderId) {
  const membersSnapshot = await admin.database().ref(`server_members/${serverId}`).once("value");
  const members = membersSnapshot.val() || {};
  const memberIds = Object.keys(members).filter((uid) => uid && uid !== senderId);

  if (memberIds.length === 0) {
    return [];
  }

  const tokenSnapshots = await Promise.all(
    memberIds.map((uid) => admin.database().ref(`user_fcm_tokens/${uid}`).once("value"))
  );

  const uniqueTokens = new Set();
  tokenSnapshots.forEach((snapshot) => {
    const tokenMap = snapshot.val() || {};
    Object.keys(tokenMap).forEach((token) => {
      if (token && token.trim()) {
        uniqueTokens.add(token);
      }
    });
  });

  return Array.from(uniqueTokens);
}

async function handlePushEvent(snapshot) {
  const eventId = snapshot.key;
  const eventData = snapshot.val() || {};

  const serverId = eventData.serverId;
  const channelId = eventData.channelId;
  const channelName = eventData.channelName || "channel";
  const senderId = eventData.senderId || "";

  if (!serverId || !channelId) {
    console.warn(`[skip] invalid event payload id=${eventId}`);
    await snapshot.ref.remove();
    return;
  }

  const recipientTokens = await collectRecipientTokens(serverId, senderId);
  if (recipientTokens.length === 0) {
    console.log(`[skip] event=${eventId} has no recipient tokens`);
    await snapshot.ref.remove();
    return;
  }

  const notification = buildNotification(eventData);

  const messageTemplate = {
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
    const tokenBatches = chunkArray(recipientTokens, 500);
    let successCount = 0;

    for (const tokens of tokenBatches) {
      const response = await admin.messaging().sendEachForMulticast({
        ...messageTemplate,
        tokens,
      });

      successCount += response.successCount;

      response.responses.forEach((result, index) => {
        if (!result.success) {
          console.warn(
            `[failed-token] event=${eventId} token=${tokens[index]} error=${result.error?.code || result.error?.message || "unknown"}`
          );
        }
      });
    }

    console.log(
      `[sent] event=${eventId} recipients=${recipientTokens.length} delivered=${successCount}`
    );
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