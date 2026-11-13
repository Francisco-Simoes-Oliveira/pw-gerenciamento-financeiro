import assert from "node:assert/strict"
import { existsSync, readFileSync, readdirSync, statSync } from "node:fs"
import path from "node:path"
import test from "node:test"
import { fileURLToPath } from "node:url"

const projectDirectory = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..")
const outputDirectory = path.join(projectDirectory, "dist")

function readOutput(relativePath) {
  return readFileSync(path.join(outputDirectory, relativePath), "utf8")
}

function listFiles(directory, root = directory) {
  return readdirSync(directory).flatMap((name) => {
    const absolutePath = path.join(directory, name)
    if (statSync(absolutePath).isDirectory()) return listFiles(absolutePath, root)
    return [path.relative(root, absolutePath).replaceAll(path.sep, "/")]
  })
}

test("manifesto torna o FinShare instalável com ícones completos", () => {
  const manifest = JSON.parse(readOutput("manifest.webmanifest"))

  assert.equal(manifest.name, "FinShare — Gerenciamento Financeiro")
  assert.equal(manifest.short_name, "FinShare")
  assert.equal(manifest.display, "standalone")
  assert.equal(manifest.start_url, "/")
  assert.equal(manifest.scope, "/")
  assert.equal(manifest.theme_color, "#1e3a8a")
  assert.equal(manifest.background_color, "#f8fafc")

  const purposes = new Set(manifest.icons.map((icon) => icon.purpose))
  assert.deepEqual(purposes, new Set(["any", "maskable"]))
  for (const icon of manifest.icons) {
    const iconPath = path.join(outputDirectory, icon.src.replace(/^\//, ""))
    assert.ok(existsSync(iconPath), `ícone ausente: ${icon.src}`)
    const png = readFileSync(iconPath)
    const [expectedWidth, expectedHeight] = icon.sizes.split("x").map(Number)
    assert.equal(png.subarray(1, 4).toString(), "PNG")
    assert.equal(png.readUInt32BE(16), expectedWidth)
    assert.equal(png.readUInt32BE(20), expectedHeight)
  }
})

test("service worker limita o cache ao shell e aos assets públicos do build", () => {
  const serviceWorker = readOutput("sw.js")
  const precacheMatch = serviceWorker.match(/const PRECACHE_URLS = (\[[\s\S]*?\]);/)
  assert.ok(precacheMatch, "lista de precache não encontrada")
  const precacheUrls = JSON.parse(precacheMatch[1])

  const buildAssets = listFiles(path.join(outputDirectory, "assets"))
    .map((file) => `/assets/${file}`)
    .sort()
  assert.deepEqual(
    precacheUrls.filter((url) => url.startsWith("/assets/")).sort(),
    buildAssets
  )
  assert.ok(precacheUrls.includes("/index.html"))
  assert.ok(precacheUrls.includes("/manifest.webmanifest"))
  assert.ok(!precacheUrls.some((url) => url.startsWith("/api")))
  assert.match(serviceWorker, /NEVER_CACHE_PREFIXES = \["\/api", "\/ws", "\/socket"\]/)
  assert.match(serviceWorker, /request\.headers\.has\("authorization"\)/)
  assert.match(serviceWorker, /text\/event-stream/)
  assert.match(serviceWorker, /event\.data\?\.type === "SKIP_WAITING"/)
  assert.doesNotMatch(serviceWorker, /app-token|Bearer\s/)
})

test("HTML publicado referencia manifesto, identidade visual e tema", () => {
  const html = readOutput("index.html")

  assert.match(html, /rel="manifest"[^>]+href="\/manifest\.webmanifest"/)
  assert.match(html, /name="theme-color"[^>]+content="#1e3a8a"/)
  assert.match(html, /rel="apple-touch-icon"[^>]+finshare-180\.png/)
  assert.match(html, /<title>FinShare — Gerenciamento Financeiro<\/title>/)
})

