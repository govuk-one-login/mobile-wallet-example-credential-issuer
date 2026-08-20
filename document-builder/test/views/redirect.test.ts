import nunjucks from "nunjucks";
import path from "node:path";

describe("redirect.njk", () => {
  const env = nunjucks.configure(path.resolve("src/views"));

  it("should not HTML-escape ampersands in redirectUrl inside script tag", () => {
    const html = env.render("redirect.njk", {
      cspNonce: "test-nonce",
      redirectUrl:
        "https://oidc.example.com/authorize?client_id=abc&scope=openid&state=xyz",
    });

    const scriptContent = html.match(/<script[^>]*>([\s\S]*?)<\/script>/)?.[1];
    expect(scriptContent).toContain(
      'window.location.href = "https://oidc.example.com/authorize?client_id=abc&scope=openid&state=xyz"',
    );
    expect(scriptContent).not.toContain("&amp;");
  });

  it("should include a script tag with the provided nonce", () => {
    const html = env.render("redirect.njk", {
      cspNonce: "abc123nonce",
      redirectUrl: "/select-app?credentialType=test",
    });

    expect(html).toContain('<script nonce="abc123nonce">');
  });

  it("should include a noscript fallback link with the redirect URL", () => {
    const html = env.render("redirect.njk", {
      cspNonce: "test-nonce",
      redirectUrl: "/select-app?credentialType=test",
    });

    expect(html).toContain("<noscript>");
    expect(html).toContain('href="/select-app?credentialType=test"');
  });

  it("should render a valid HTML page", () => {
    const html = env.render("redirect.njk", {
      cspNonce: "test-nonce",
      redirectUrl: "/test",
    });

    expect(html).toContain("<!DOCTYPE html>");
    expect(html).toContain('<html lang="en">');
    expect(html).toContain("</html>");
  });
});
