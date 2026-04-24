const http = require("http");
const httpProxy = require("http-proxy");
const Redis = require("ioredis");

const redisUrl = process.env.REDIS_URL || "redis://redis-service:6379";

const redis = new Redis(redisUrl, {
  maxRetriesPerRequest: null,
  enableReadyCheck: false,
  retryStrategy(times) {
    const delay = Math.min(times * 50, 2000); // Backoff strategy
    console.log(`Redis connection failed. Retrying in ${delay}ms...`);
    return delay;
  },
});

redis.on("error", (err) => {
  console.error("Redis Client Error:", err.message);
});

redis.on("connect", () => {
  console.log("✅ Connected to Redis successfully");
});

const proxy = httpProxy.createProxyServer({
  ws: true,
  xfwd: true,
  changeOrigin: true,
});

async function getTarget(hostname) {
  try {
    const targetIp = await redis.get(`route:${hostname}`);
    if (targetIp) {
      return targetIp;
    }
  } catch (err) {
    console.error("Redis Error:", err);
  }
  return null;
}

// HELPER: Ensure target has the correct format
const getTargetUrl = (ip) => {
  return ip.includes(":") ? `http://${ip}` : `http://${ip}:5173`;
};

const server = http.createServer(async (req, res) => {
  const rawHost = req.headers.host || ""; // project-3.app.domain.com:8090
  const hostname = rawHost.split(":")[0];

  const targetIp = await getTarget(hostname);

  if (!targetIp) {
    res.writeHead(404, { "Content-Type": "text/plain" });
    return res.end(`Preview not found for ${hostname}.`);
  }

  const target = getTargetUrl(targetIp); // http://10.244.0.7:5173
  console.log(`HTTP Proxy: ${hostname} -> ${target}${req.url}`);

  proxy.web(req, res, { target }, (e) => {
    console.error(`Proxy Error (Web): ${hostname}`, e.message);
    if (!res.headersSent) {
      res.writeHead(502);
      res.end("Vite server unavailable...");
    }
  });
});

server.on("upgrade", async (req, socket, head) => {
  const rawHost = req.headers.host || "";
  const hostname = rawHost.split(":")[0];

  const targetIp = await getTarget(hostname);

  if (targetIp) {
    const target = getTargetUrl(targetIp);
    console.log(`WS Upgrade: ${hostname} -> ${target}`);

    proxy.ws(req, socket, head, { target }, (e) => {
      console.error(`Proxy Error (WS): ${hostname}`, e.message);
      socket.destroy();
    });
  } else {
    socket.destroy();
  }
});

server.listen(80, () => console.log("🚀 Wildcard Proxy Active on Port 80"));
