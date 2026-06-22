const http = require("http");
const fs = require("fs");
const path = require("path");

const buildDirectory = path.join(__dirname, "build");
const port = Number(process.env.PORT) || 3000;

const contentTypes = {
  ".css": "text/css; charset=utf-8",
  ".gif": "image/gif",
  ".html": "text/html; charset=utf-8",
  ".ico": "image/x-icon",
  ".jpeg": "image/jpeg",
  ".jpg": "image/jpeg",
  ".js": "text/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".map": "application/json; charset=utf-8",
  ".mp4": "video/mp4",
  ".png": "image/png",
  ".svg": "image/svg+xml",
  ".txt": "text/plain; charset=utf-8",
  ".webp": "image/webp",
};

const sendFile = (response, filePath) => {
  fs.stat(filePath, (statError, stats) => {
    if (statError || !stats.isFile()) {
      const fallback = path.join(buildDirectory, "index.html");
      fs.createReadStream(fallback)
        .on("error", () => {
          response.writeHead(404);
          response.end("Not found");
        })
        .pipe(response);
      return;
    }

    response.writeHead(200, {
      "Content-Type":
        contentTypes[path.extname(filePath).toLowerCase()] ||
        "application/octet-stream",
    });
    fs.createReadStream(filePath).pipe(response);
  });
};

http
  .createServer((request, response) => {
    const requestPath = decodeURIComponent(
      new URL(request.url, `http://${request.headers.host}`).pathname,
    );
    const relativePath = requestPath === "/" ? "index.html" : requestPath.slice(1);
    const filePath = path.resolve(buildDirectory, relativePath);

    if (!filePath.startsWith(`${buildDirectory}${path.sep}`) && filePath !== buildDirectory) {
      response.writeHead(403);
      response.end("Forbidden");
      return;
    }

    sendFile(response, filePath);
  })
  .listen(port, "0.0.0.0", () => {
    console.log(`VirtueHire frontend listening on port ${port}`);
  });
