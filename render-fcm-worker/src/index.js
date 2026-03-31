const admin = require("firebase-admin");
const http = require("http");
const crypto = require("crypto");
const { URL } = require("url");

const port = process.env.PORT || 3000;
const paymentBaseUrl = process.env.VNPAY_PAYMENT_URL || "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
const packagePriceMap = {
  BASIC: 42000,
  NITRO: 113000,
  PACKAGE_100_ORBS: 20000,
  PACKAGE_500_ORBS: 100000,
  PACKAGE_1000_ORBS: 200000,
};

http.createServer(async (req, res) => {
  try {
    const parsedUrl = new URL(req.url, `http://${req.headers.host || "localhost"}`);
    const { pathname } = parsedUrl;

    if (pathname === "/health") {
      res.writeHead(200, { "Content-Type": "text/plain" });
      res.end("ok");
      return;
    }

    if (pathname === "/vnpay/create-payment" && req.method === "POST") {
      const payload = await parseJsonBody(req);
      const result = await createVnpayPayment(payload, req);
      sendJson(res, 200, result);
      return;
    }

    if (pathname === "/vnpay/return" && req.method === "GET") {
      const result = await handleVnpayReturn(parsedUrl.searchParams);
      const deepLinkUrl = buildPaymentDeepLink(result);
      res.writeHead(302, { Location: deepLinkUrl });
      res.end();
      return;
    }

    if (pathname === "/vnpay/ipn" && req.method === "GET") {
      const result = await handleVnpayIpn(parsedUrl.searchParams);
      sendJson(res, 200, result);
      return;
    }

    res.writeHead(200, { "Content-Type": "text/plain" });
    res.end("FCM worker alive");
  } catch (error) {
    console.error("Request handling error", error);
    sendJson(res, 400, {
      success: false,
      message: error.message || "Bad request",
    });
  }
}).listen(port, "0.0.0.0", () => {
  console.log(`Health server listening on ${port}`);
});

function sendJson(res, statusCode, data) {
  res.writeHead(statusCode, { "Content-Type": "application/json" });
  res.end(JSON.stringify(data));
}

function parseJsonBody(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    req.on("data", (chunk) => chunks.push(chunk));
    req.on("end", () => {
      if (chunks.length === 0) {
        resolve({});
        return;
      }
      try {
        const body = Buffer.concat(chunks).toString("utf8");
        resolve(JSON.parse(body));
      } catch (error) {
        reject(new Error("Invalid JSON body"));
      }
    });
    req.on("error", reject);
  });
}

function encodeForVnpay(value) {
  return encodeURIComponent(String(value)).replace(/%20/g, "+");
}

function sortObjectByKeys(obj) {
  const sorted = {};
  Object.keys(obj)
    .sort()
    .forEach((key) => {
      sorted[key] = obj[key];
    });
  return sorted;
}

function buildVnpaySignData(params) {
  return Object.keys(params)
    .sort()
    .map((key) => `${encodeForVnpay(key)}=${encodeForVnpay(params[key])}`)
    .join("&");
}

function signVnpayPayload(params, hashSecret) {
  const signData = buildVnpaySignData(params);
  return crypto.createHmac("sha512", hashSecret).update(signData, "utf8").digest("hex");
}

function mapSearchParamsToObject(searchParams) {
  const output = {};
  for (const [key, value] of searchParams.entries()) {
    output[key] = value;
  }
  return output;
}

/**
 * VNPay yêu cầu vnp_CreateDate / vnp_ExpireDate theo **giờ Việt Nam (Asia/Ho_Chi_Minh)**,
 * định dạng yyyyMMddHHmmss. Nếu dùng getHours()/getDate() của Date theo múi giờ máy chủ (thường UTC
 * trên Render/Fly/Heroku), thời điểm gửi lệch ~7 giờ → sandbox báo "quá thời gian chờ" ngay.
 */
function formatVnpDateInVietnam(date) {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Ho_Chi_Minh",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).formatToParts(date);

  const map = {};
  for (const p of parts) {
    if (p.type !== "literal") map[p.type] = p.value;
  }
  return `${map.year}${map.month}${map.day}${map.hour}${map.minute}${map.second}`;
}

function randomTxnRef() {
  return `${Date.now()}${Math.floor(100000 + Math.random() * 900000)}`;
}

function resolveClientIp(req) {
  const forwarded = req.headers["x-forwarded-for"];
  if (forwarded) {
    return String(forwarded).split(",")[0].trim();
  }
  return req.socket?.remoteAddress || "127.0.0.1";
}

async function createVnpayPayment(payload, req) {
  const authUser = await verifyFirebaseAuth(req);
  const tmnCode = requiredEnv("VNPAY_TMN_CODE");
  const hashSecret = requiredEnv("VNPAY_HASH_SECRET");
  const returnUrl = requiredEnv("VNPAY_RETURN_URL");

  const uid = authUser.uid;
  const packageType = payload.packageType ? String(payload.packageType).trim().toUpperCase() : "";
  if (!packagePriceMap[packageType]) throw new Error("packageType must be BASIC, NITRO or an ORBS package");

  const amountVnd = packagePriceMap[packageType];
  const orderInfo = `${packageType} - ${uid}`;
  const txnRef = randomTxnRef();
  const createDate = formatVnpDateInVietnam(new Date());
  const expireDate = formatVnpDateInVietnam(new Date(Date.now() + 15 * 60 * 1000));

  const vnpParams = sortObjectByKeys({
    vnp_Version: "2.1.0",
    vnp_Command: "pay",
    vnp_TmnCode: tmnCode,
    vnp_Amount: String(amountVnd * 100),
    vnp_CurrCode: "VND",
    vnp_TxnRef: txnRef,
    vnp_OrderInfo: orderInfo,
    vnp_OrderType: "other",
    vnp_Locale: "vn",
    vnp_ReturnUrl: returnUrl,
    vnp_IpAddr: resolveClientIp(req),
    vnp_CreateDate: createDate,
    vnp_ExpireDate: expireDate,
  });

  const signature = signVnpayPayload(vnpParams, hashSecret);
  const vnpUrl = `${paymentBaseUrl}?${buildVnpaySignData({
    ...vnpParams,
    vnp_SecureHash: signature,
    vnp_SecureHashType: "HmacSHA512",
  })}`;

  await admin.database().ref(`nitro_payments/${txnRef}`).set({
    uid,
    packageType,
    amountVnd,
    status: "PENDING",
    createdAt: Date.now(),
    updatedAt: Date.now(),
  });

  return {
    success: true,
    txnRef,
    paymentUrl: vnpUrl,
  };
}

async function verifyFirebaseAuth(req) {
  const authHeader = req.headers.authorization || req.headers.Authorization;
  if (!authHeader || !String(authHeader).startsWith("Bearer ")) {
    throw new Error("Missing Authorization Bearer token");
  }

  const idToken = String(authHeader).slice("Bearer ".length).trim();
  if (!idToken) {
    throw new Error("Empty Firebase ID token");
  }

  const decoded = await admin.auth().verifyIdToken(idToken);
  if (!decoded || !decoded.uid) {
    throw new Error("Invalid Firebase ID token");
  }

  const roles = Array.isArray(decoded.roles) ? decoded.roles : [];
  const blockedRoles = new Set(["BANNED", "SUSPENDED"]);
  if (roles.some((role) => blockedRoles.has(String(role).toUpperCase()))) {
    throw new Error("Account is not allowed to purchase");
  }

  return { uid: decoded.uid, roles };
}

function verifyVnpaySignature(paramsObj) {
  const hashSecret = requiredEnv("VNPAY_HASH_SECRET");
  const secureHash = paramsObj.vnp_SecureHash;
  if (!secureHash) return false;

  const signPayload = { ...paramsObj };
  delete signPayload.vnp_SecureHash;
  delete signPayload.vnp_SecureHashType;
  const signed = signVnpayPayload(sortObjectByKeys(signPayload), hashSecret);
  return signed.toLowerCase() === String(secureHash).toLowerCase();
}

async function grantNitroAndMarkPayment(txnRef, responseCode, transactionNo) {
  const paymentRef = admin.database().ref(`nitro_payments/${txnRef}`);
  const snapshot = await paymentRef.once("value");
  const payment = snapshot.val();
  if (!payment) {
    return { ok: false, reason: "payment_not_found" };
  }

  if (payment.status === "PAID") {
    return { ok: true, reason: "already_paid", uid: payment.uid, packageType: payment.packageType };
  }

  if (responseCode !== "00") {
    await paymentRef.update({
      status: "FAILED",
      responseCode,
      transactionNo: transactionNo || null,
      updatedAt: Date.now(),
    });
    return { ok: false, reason: "payment_failed", uid: payment.uid, packageType: payment.packageType };
  }

  const now = Date.now();

  if (payment.packageType === "BASIC" || payment.packageType === "NITRO") {
    const expireAt = now + 30 * 24 * 60 * 60 * 1000;
    await admin.database().ref(`users/${payment.uid}/nitro`).update({
      plan: payment.packageType,
      isActive: true,
      activeSince: now,
      expiresAt: expireAt,
      updatedAt: now,
    });
  } else if (payment.packageType.startsWith("PACKAGE_") && payment.packageType.endsWith("_ORBS")) {
    const parts = payment.packageType.split("_");
    const orbsToAdd = parseInt(parts[1], 10);
    if (!isNaN(orbsToAdd)) {
      const userRef = admin.database().ref(`users/${payment.uid}`);
      await userRef.transaction((user) => {
        if (user) {
          user.orbs = (user.orbs || 0) + orbsToAdd;
        }
        return user;
      });
    }
  }

  await paymentRef.update({
    status: "PAID",
    responseCode,
    transactionNo: transactionNo || null,
    updatedAt: now,
  });

  return { ok: true, reason: "paid", uid: payment.uid, packageType: payment.packageType };
}

async function handleVnpayReturn(searchParams) {
  const paramsObj = mapSearchParamsToObject(searchParams);
  const txnRef = paramsObj.vnp_TxnRef || "";
  const responseCode = paramsObj.vnp_ResponseCode || "99";
  const transactionNo = paramsObj.vnp_TransactionNo || "";

  if (!verifyVnpaySignature(paramsObj)) {
    return { success: false, code: "97", txnRef, message: "invalid_signature" };
  }
  if (!txnRef) {
    return { success: false, code: "01", txnRef, message: "missing_txn_ref" };
  }

  const paidResult = await grantNitroAndMarkPayment(txnRef, responseCode, transactionNo);
  return {
    success: paidResult.ok,
    code: responseCode,
    txnRef,
    packageType: paidResult.packageType || "",
    message: paidResult.reason,
  };
}

async function handleVnpayIpn(searchParams) {
  const paramsObj = mapSearchParamsToObject(searchParams);
  const txnRef = paramsObj.vnp_TxnRef || "";
  const responseCode = paramsObj.vnp_ResponseCode || "99";
  const transactionNo = paramsObj.vnp_TransactionNo || "";

  if (!verifyVnpaySignature(paramsObj)) {
    return { RspCode: "97", Message: "Invalid signature" };
  }
  if (!txnRef) {
    return { RspCode: "01", Message: "Order not found" };
  }

  const paidResult = await grantNitroAndMarkPayment(txnRef, responseCode, transactionNo);
  if (paidResult.reason === "payment_not_found") return { RspCode: "01", Message: "Order not found" };
  if (responseCode !== "00") return { RspCode: "00", Message: "Confirm failed payment" };
  return { RspCode: "00", Message: "Confirm success" };
}

function buildPaymentDeepLink(result) {
  const status = result.success ? "success" : "failed";
  const isOrbs = result.packageType && result.packageType.includes("ORBS");
  const base = isOrbs ? "chatapp://orbs-payment" : "chatapp://nitro-payment";
  return `${base}?status=${encodeURIComponent(status)}&code=${encodeURIComponent(
    result.code || ""
  )}&txnRef=${encodeURIComponent(result.txnRef || "")}&packageType=${encodeURIComponent(
    result.packageType || ""
  )}`;
}

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
  const safeSenderId = String(senderId || "").trim();

  const membersSnapshot = await admin.database().ref(`server_members/${serverId}`).once("value");
  const members = membersSnapshot.val() || {};
  
  // 1. Lọc uid người gửi ra khỏi danh sách duyệt
  const memberIds = Object.keys(members).filter((uid) => {
    return uid && String(uid).trim() !== safeSenderId;
  });

  if (memberIds.length === 0) {
    return [];
  }

  // 2. Lấy danh sách token hiện tại của người gửi để phòng trường hợp token bị kẹt ở account cũ
  const senderTokensSnapshot = await admin.database().ref(`user_fcm_tokens/${safeSenderId}`).once("value");
  const senderTokensMap = senderTokensSnapshot.val() || {};
  const senderTokens = new Set(Object.keys(senderTokensMap).map(t => t.trim()));

  // 3. Lấy tokens của các thành viên khác
  const tokenSnapshots = await Promise.all(
    memberIds.map((uid) => admin.database().ref(`user_fcm_tokens/${uid}`).once("value"))
  );

  const uniqueTokens = new Set();
  tokenSnapshots.forEach((snapshot) => {
    const tokenMap = snapshot.val() || {};
    Object.keys(tokenMap).forEach((rawToken) => {
      const token = rawToken.trim();
      // Loại bỏ token nếu token đó cũng đang thuộc về người gửi hiện tại
      if (token && !senderTokens.has(token)) {
        uniqueTokens.add(token);
      }
    });
  });

  return Array.from(uniqueTokens);
}

async function collectUserTokens(uid) {
  if (!uid) return [];
  const tokenSnapshot = await admin.database().ref(`user_fcm_tokens/${uid}`).once("value");
  const tokenMap = tokenSnapshot.val() || {};
  return Object.keys(tokenMap).filter((token) => token && token.trim());
}

async function resolveCallerName(uid) {
  if (!uid) return "Friend";
  const userSnapshot = await admin.database().ref(`users/${uid}`).once("value");
  const user = userSnapshot.val() || {};
  return user.displayName || user.username || user.email || "Friend";
}

async function handleDmPushEvent(snapshot, eventData) {
  const eventId = snapshot.key;
  const calleeUid = eventData.calleeUid;
  const senderId = eventData.senderId || "";
  const senderName = eventData.senderName || "Unknown";

  if (!calleeUid) {
    console.warn(`[skip] invalid dm event payload id=${eventId}`);
    await snapshot.ref.remove();
    return;
  }

  const tokens = await collectUserTokens(calleeUid);

  if (tokens.length === 0) {
    console.log(`[skip] dm event=${eventId} callee has no token`);
    await snapshot.ref.remove();
    return;
  }

  const safeSenderId = String(senderId).trim();
  const senderTokensMap = (await admin.database().ref(`user_fcm_tokens/${safeSenderId}`).once("value")).val() || {};
  const senderTokens = new Set(Object.keys(senderTokensMap).map(t => t.trim()));

  const filteredTokens = tokens.filter(rawT => {
    const t = rawT.trim();
    return t && !senderTokens.has(t);
  });

  if (filteredTokens.length === 0) {
    await snapshot.ref.remove();
    return;
  }

  const title = senderName;
  const body = (eventData.content || "Bạn có tin nhắn mới").slice(0, 180);

  const messageTemplate = {
    notification: { title, body },
    android: {
      priority: "high",
      notification: {
        channelId: "chat_messages",
      },
    },
    data: {
      eventType: "dm",
      dmId: String(eventData.dmId || ""),
      senderId: String(senderId),
      senderName: String(senderName),
      title,
      body,
    },
  };

  try {
    const tokenBatches = chunkArray(filteredTokens, 500);
    let successCount = 0;

    for (const batch of tokenBatches) {
      const response = await admin.messaging().sendEachForMulticast({
        ...messageTemplate,
        tokens: batch,
      });
      successCount += response.successCount;
    }
    console.log(`[sent-dm] event=${eventId} recipients=${filteredTokens.length} delivered=${successCount}`);
  } catch (error) {
    console.error(`[failed-dm] event=${eventId}`, error);
  } finally {
    await snapshot.ref.remove();
  }
}

async function handlePushEvent(snapshot) {
  const eventId = snapshot.key;
  const eventData = snapshot.val() || {};

  const eventType = eventData.type || "server";
  if (eventType === "dm") {
    return handleDmPushEvent(snapshot, eventData);
  }

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

async function handleCallSessionEvent(snapshot) {
  const callId = snapshot.key;
  const session = snapshot.val() || {};
  const status = String(session.status || "").toLowerCase();

  // Chỉ bắn thông báo khi có cuộc gọi mới.
  if (status !== "ringing") return;
  if (session.inviteSentAt) return;

  const calleeUid = session.calleeUid;
  const callerUid = session.callerUid;
  const callType = String(session.callType || "audio").toLowerCase() === "video" ? "video" : "audio";
  const channelName = session.channelName || `dm_call_${callId}`;

  if (!calleeUid || !callerUid) {
    console.warn(`[skip] invalid call session id=${callId}`);
    return;
  }

  const [tokens, callerName] = await Promise.all([
    collectUserTokens(calleeUid),
    resolveCallerName(callerUid),
  ]);

  if (tokens.length === 0) {
    console.log(`[skip] call=${callId} callee has no token`);
    return;
  }

  const notification = {
    title: callerName,
    body: callType === "video" ? "Cuộc gọi video đến" : "Cuộc gọi thoại đến",
  };

  const messageTemplate = {
    android: {
      priority: "high",
    },
    data: {
      eventType: "call_invite",
      callId: String(callId),
      callerUid: String(callerUid),
      callerName: String(callerName),
      calleeUid: String(calleeUid),
      callType,
      channelName: String(channelName),
      title: notification.title,
      body: notification.body,
    },
  };

  try {
    const tokenBatches = chunkArray(tokens, 500);
    let successCount = 0;
    for (const batch of tokenBatches) {
      const response = await admin.messaging().sendEachForMulticast({
        ...messageTemplate,
        tokens: batch,
      });
      successCount += response.successCount;
    }

    if (successCount > 0) {
      await snapshot.ref.update({
        inviteSentAt: Date.now(),
      });
    }
    console.log(`[sent-call] call=${callId} delivered=${successCount}/${tokens.length}`);
  } catch (error) {
    console.error(`[failed-call] call=${callId}`, error);
  }
}

async function main() {
  initFirebaseAdmin();

  const db = admin.database();
  const queueRef = db.ref("chat_push_events");
  const callSessionsRef = db.ref("call_sessions");

  console.log("Render FCM worker is running and listening chat_push_events...");

  queueRef.on("child_added", (snapshot) => {
    handlePushEvent(snapshot).catch((error) => {
      console.error("Unexpected worker error", error);
    });
  });

  // Lắng nghe cuộc gọi 1-1 từ Firebase signaling layer.
  callSessionsRef.on("child_added", (snapshot) => {
    handleCallSessionEvent(snapshot).catch((error) => {
      console.error("Unexpected call worker error", error);
    });
  });
  callSessionsRef.on("child_changed", (snapshot) => {
    handleCallSessionEvent(snapshot).catch((error) => {
      console.error("Unexpected call worker error", error);
    });
  });

  process.on("SIGTERM", () => {
    console.log("SIGTERM received. Stopping worker.");
    queueRef.off();
    callSessionsRef.off();
    process.exit(0);
  });
}

main().catch((error) => {
  console.error("Worker boot failed", error);
  process.exit(1);
});