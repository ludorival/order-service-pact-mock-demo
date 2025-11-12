# Pact Broker Webhook Configuration

This workflow is triggered by Pact Broker webhooks when contracts are published or updated.

## Workflow Setup

The workflow `pact-verification.yml` listens for `repository_dispatch` events with type `pact-verification`.

## Configuring Pact Broker Webhook

To configure Pact Broker to trigger this workflow:

### 1. Get GitHub Personal Access Token

1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Create a new token with `repo` scope
3. Copy the token (you'll need it for Pact Broker configuration)

### 2. Configure Webhook in Pact Broker

In your Pact Broker UI or API, create a webhook with the following configuration:

**Webhook URL:**
```
https://api.github.com/repos/YOUR_USERNAME/YOUR_REPOSITORY/dispatches
```

Replace:
- `YOUR_USERNAME`: Your GitHub username or organization
- `YOUR_REPOSITORY`: Your repository name (e.g., `order-service-demo`)

**Webhook Method:** `POST`

**Webhook Headers:**
```
Authorization: token YOUR_GITHUB_TOKEN
Accept: application/vnd.github.v3+json
Content-Type: application/json
```

**Webhook Body (JSON):**
```json
{
  "event_type": "pact-verification",
  "client_payload": {
    "pact_url": "${pactbroker.pactUrl}",
    "consumer_name": "${pactbroker.consumerName}",
    "consumer_version": "${pactbroker.consumerVersionNumber}",
    "provider_name": "${pactbroker.providerName}"
  }
}
```

**Webhook Events:** 
- `contract:published` - Trigger when a contract is published
- `contract:changed` - Trigger when a contract is changed

### 3. Example Webhook Configuration (Pact Broker API)

```bash
curl -X POST "${PACT_BROKER_BASE_URL}/pacts/provider/order-service/webhooks" \
  -H "Authorization: Bearer ${PACT_BROKER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Trigger GitHub Actions for order-service",
    "events": [{"name": "contract_published"}],
    "request": {
      "method": "POST",
      "url": "https://api.github.com/repos/YOUR_USERNAME/YOUR_REPOSITORY/dispatches",
      "headers": {
        "Authorization": "token YOUR_GITHUB_TOKEN",
        "Accept": "application/vnd.github.v3+json",
        "Content-Type": "application/json"
      },
      "body": {
        "event_type": "pact-verification",
        "client_payload": {
          "pact_url": "${pactbroker.pactUrl}",
          "consumer_name": "${pactbroker.consumerName}",
          "consumer_version": "${pactbroker.consumerVersionNumber}",
          "provider_name": "${pactbroker.providerName}"
        }
      }
    }
  }'
```

### 4. Using PactFlow (Pact Broker SaaS)

If you're using PactFlow:

1. Go to your PactFlow dashboard
2. Navigate to Settings → Webhooks
3. Click "Add webhook"
4. Configure:
   - **Name:** GitHub Actions - order-service
   - **Events:** Contract published, Contract changed
   - **URL:** `https://api.github.com/repos/YOUR_USERNAME/YOUR_REPOSITORY/dispatches`
   - **Method:** POST
   - **Headers:**
     - `Authorization: token YOUR_GITHUB_TOKEN`
     - `Accept: application/vnd.github.v3+json`
     - `Content-Type: application/json`
   - **Body:**
     ```json
     {
       "event_type": "pact-verification",
       "client_payload": {
         "pact_url": "${pactbroker.pactUrl}",
         "consumer_name": "${pactbroker.consumerName}",
         "consumer_version": "${pactbroker.consumerVersionNumber}",
         "provider_name": "${pactbroker.providerName}"
       }
     }
     ```

## How It Works

1. Consumer publishes a new or updated contract to Pact Broker
2. Pact Broker webhook triggers GitHub `repository_dispatch` event
3. GitHub Actions workflow runs automatically
4. Workflow runs Pact provider verification tests
5. Verification results are published back to Pact Broker (automatically when `CI=true`)

## Webhook Payload

The workflow can access webhook payload data via:
- `github.event.action`: The event type (`pact-verification`)
- `github.event.client_payload`: The custom payload sent by Pact Broker

Example payload structure:
```json
{
  "pact_url": "https://broker.pactflow.io/pacts/provider/order-service/consumer/shop-frontend/version/1.0.0",
  "consumer_name": "shop-frontend",
  "consumer_version": "1.0.0",
  "provider_name": "order-service"
}
```

## Security

- Store GitHub Personal Access Token securely (never commit it to the repository)
- Use repository secrets for sensitive data
- Consider using GitHub App tokens instead of Personal Access Tokens for better security
- Webhook should use HTTPS only

## Troubleshooting

### Webhook not triggering workflow

1. Check GitHub Personal Access Token has `repo` scope
2. Verify webhook URL is correct (check username and repository name)
3. Check webhook is configured for correct events
4. Verify webhook body format matches expected JSON structure
5. Check GitHub Actions logs for any errors

### Workflow runs but tests fail

1. Verify `PACT_BROKER_BASE_URL` and `PACT_BROKER_TOKEN` secrets are set
2. Check Pact Broker is accessible from GitHub Actions
3. Verify provider name matches in Pact Broker and test configuration

