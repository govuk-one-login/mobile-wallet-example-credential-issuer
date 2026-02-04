/*
 * Unlike the real One Login Auth server which redirects to Auth/Orchestration, this mock redirects to its own
 * redirect endpoint before returning to the Wallet. It might be simpler to redirect directly to the Wallet instead.
 */
def selfUrl = System.getenv('SELF_URL')
def location = "${selfUrl}/redirect?state=91446467-5127-4af4-a0ec-48e4b4347820&code=wMVoGvE7AY"

respond()
    .withHeader('Location', location)
    .withData("Redirecting to ${location}")
