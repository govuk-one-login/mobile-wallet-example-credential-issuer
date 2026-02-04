/*
 * Redirects to the Wallet redirect URI with code and state parameters. In the real service, code is a signed JWT
 * and state is the original state sent by the Wallet in the authorize request.
 */
def redirectUri = System.getenv('WALLET_REDIRECT_URI')
def location = "${redirectUri}?code=QNAtkgh6S5&state=1223"

respond()
        .withHeader('Location', location)
        .withData("Redirecting to ${location}")