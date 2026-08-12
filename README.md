# Alpaca Options Premium Scanner

Java / Spring Boot application that pulls an Alpaca option chain, displays it in a two-sided **Calls | Strike | Puts** layout, and ranks Deep OTM **short strangle** candidates. It is an analytical scanner only. **It never places live trades.**

## Architecture

```
UI (Thymeleaf)
    -> REST / MVC controllers
        -> OptionChainService / StrangleService / HistoryService
            -> OptionFilterService, OptionCalculationService, StrangleRankingService
            -> AlpacaClient
                -> Alpaca Market Data + Trading APIs
```

| Layer | Classes |
| --- | --- |
| HTTP adapter | `AlpacaClient`, `EarningsClient` |
| Chain | `OptionChainService`, `OptionCalculationService`, `OptionFilterService` |
| Strategy | `StrangleService`, `StrangleRankingService`, `ExpectedMoveService` |
| Bonuses | `IvRankService`, `EarningsService`, `HistoryService` |
| API / UI | `OptionApiController`, `ScannerController`, `HistoryController` |

No order or trading endpoints exist.

## Setup

Requirements: **Java 17** and **Maven 3.9+**.

1. Create an [Alpaca paper trading](https://app.alpaca.markets/signup) account and generate API keys.
2. Export credentials (do not hard-code them):

```bash
export ALPACA_API_KEY=your_paper_key_id
export ALPACA_API_SECRET=your_paper_secret_key
```

3. Run:

```bash
mvn spring-boot:run
```

4. Open [http://localhost:8080](http://localhost:8080).

Copy `.env.example` if you prefer a local env file; Spring reads `ALPACA_API_KEY` and `ALPACA_API_SECRET` from the environment.

### Alpaca API configuration

| Setting | Default | Purpose |
| --- | --- | --- |
| `ALPACA_API_KEY` / `ALPACA_API_SECRET` | (required for live data) | Sent as `APCA-API-KEY-ID` and `APCA-API-SECRET-KEY` |
| `alpaca.data-base-url` | `https://data.alpaca.markets` | Option chain, stock snapshot, daily bars |
| `alpaca.trade-base-url` | `https://paper-api.alpaca.markets` | Option contracts (open interest) |
| `alpaca.chain-page-limit` | `1000` | Pagination page size; client follows `next_page_token` |

The `feed` query param is omitted so Alpaca uses OPRA when subscribed, otherwise the indicative feed.

Open interest is **not** on the snapshots endpoint. The app joins `GET /v2/options/contracts`. Volume comes from `dailyBar.v` when present.

## UI instructions

1. Enter an underlying (`SPY`, `QQQ`, `AAPL`, `NVDA`, `TSLA`, or any optionable symbol).
2. Adjust strategy parameters (all are configurable; YAML defaults are examples only).
3. Click **Load chain**.

**Section A** is the option chain: calls on the left (`Ask | Theta | Bid`), strike in the center, puts on the right (`Bid | Theta | Ask`). Ask is red, bid is green, theta is gray. Expirations are grouped with DTE and expand/collapse. Missing quotes/greeks render as `--`.

**Section B** is the ranked short-strangle table (sortable). Click a row for leg details, premium, breakevens, expected move, and a strike/breakeven visualization.

**History** (`/history`) stores each scan and later evaluates whether the underlying stayed between the strikes, theoretical P/L, and max adverse move.

## REST API

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/options/chain/{symbol}` | Grouped option chain + underlying price |
| `GET` | `/api/options/strangle/{symbol}` | Ranked short-strangle candidates |
| `GET` | `/api/options/strangle/{symbol}/{id}` | One candidate (`{callOcc}_{putOcc}`) |
| `GET` | `/api/options/history` | Saved scans with evaluated P/L |

Strangle query parameters (all optional; defaults from `application.yml`):

```
GET /api/options/strangle/SPY?minDelta=0.05&maxDelta=0.15&minTheta=0.015&minDte=7&maxDte=45&minPremium=0.20&maxSpread=0.20&minOpenInterest=500&minVolume=0
```

Error mapping: `400` invalid request/symbol, `401` missing/invalid credentials, `403` forbidden, `429` rate limit (retried a few times), `500` Alpaca server error. Missing greeks/quotes do not crash the chain view.

## Filtering methodology

Scanner selection (ITM contracts still appear on the raw chain):

1. Drop expired and **0DTE** contracts.
2. Keep DTE in `[minDte, maxDte]`.
3. **OTM only:** call strike > spot, put strike < spot.
4. Require delta and theta. Calls must have positive delta; puts negative.
5. Absolute delta in `[minDelta, maxDelta]` (puts use `abs(delta)`, so `-0.15 .. -0.05` when the UI says `0.05 .. 0.15`).
6. `abs(theta) >= minTheta`.
7. Positive bid and ask, mid `>= minPremium`, `ask - bid <= maxSpread`.
8. Volume and open interest floors. Missing OI is treated as `0` for filtering.

Per expiration, remaining OTM calls are paired with remaining OTM puts (cartesian, capped per expiry), then ranked globally (top 25).

## Ranking methodology

Each candidate gets a **0–100** score. Factors are min-max normalized across the current candidate set so one metric cannot dominate. Lowest delta does **not** automatically win.

| Factor | Weight | What it rewards |
| --- | --- | --- |
| Premium | 20% | Higher total mid |
| Liquidity | 18% | Tighter spread, higher volume and OI |
| Theta | 12% | Higher `abs(theta)` (time decay for the seller) |
| Delta | 12% | Further OTM (lower `abs(delta)`) *inside the user band* |
| Distance | 12% | About 5–20% from spot; too tight or too far is penalized |
| DTE | 8% | Middle of the user's min/max DTE window |
| IV | 10% | Richer implied vol (better for selling premium) |
| Expected-move coverage | 8% | Wings outside the implied expected move |

`score = 100 * Σ (weight_i * normalized_i)`

## Calculations

- **DTE:** calendar days from America/New_York today to expiration.
- **Weekly:** expiration is not the month's 3rd Friday.
- **Mid:** `(bid + ask) / 2`
- **Spread:** `ask - bid`
- **Total premium:** call mid + put mid
- **Premium / contract:** total premium × 100
- **Lower BE:** put strike − total premium
- **Upper BE:** call strike + total premium
- **Call distance %:** `(callStrike - spot) / spot`
- **Put distance %:** `(spot - putStrike) / spot`
- **Expected move:** ATM straddle mid when available, else `spot × ATM_IV × sqrt(DTE/365)`

Assignment fixture (spot `$650`, put `$625` mid `$0.30`, call `$680` mid `$0.375`):

- Total premium `$0.675` → `$67.50` per contract
- Lower BE `$624.325`, upper BE `$680.675`

### Theta

Alpaca theta is Black-Scholes **daily** change in the option *price* (typically negative for a long option). For a short strangle the scanner uses **`abs(theta)`**. `minTheta = 0.015` means `|theta| >= 0.015`. Higher theta is only one weighted input: a far-OTM contract with huge theta but no premium or liquidity will not automatically rank first.

### IV vs HV percentile (not IV Rank)

Alpaca snapshots do not provide a historical IV series, so true IV Rank/percentile is not available. The app builds 30-day **historical volatility** from one year of daily bars and ranks current ATM IV against that HV distribution. The UI labels this **IV vs HV percentile**.

### Earnings

Best-effort Yahoo `calendarEvents` lookup (no extra API key). Fail-open: if the call fails, the chain still loads. Expirations with earnings on or before expiry are flagged.

### Historical P/L

Each scan's top candidates are stored in a local H2 file (`./data/scanner`). History evaluates:

- Did the latest underlying stay between the strikes?
- Theoretical P/L: `(premium − call intrinsic − put intrinsic) × 100`
- Max adverse move: largest high/low excursion from the scan-time spot

This ignores commissions, assignment, and IV changes.

## Testing

No live Alpaca calls in CI. Tests use Mockito and MockWebServer.

```bash
mvn test
```

Coverage includes:

- Alpaca client: 200, 401, 400, 429 retries, pagination, empty chain, OCC parsing
- Processing: call/put, OTM, delta (including negative puts), theta, DTE, weekly vs monthly
- Strangle: pairing, `$0.675` premium, `$624.325` / `$680.675` breakevens, distance %
- Ranking: lowest-delta candidate does not automatically rank #1
- Pipeline integration: mocked SPY chain → expiration/OTM/delta/theta/liquidity filters → pairing → ranking
- REST: chain/strangle JSON and credential errors
- Bonuses: earnings JSON parse, IV vs HV math, P/L when spot stays between strikes vs ITM call

## Screenshots

After a live run against paper keys, capture:

1. Option chain
2. Expanded expiration
3. Filtered / scanned view
4. Short strangle candidates
5. Candidate details

Place files in [`screenshots/`](screenshots/README.md).

## Risk disclaimer

Short naked calls have theoretically unlimited loss. Short puts have substantial downside. Options can be assigned. Delta, gamma, and IV can change quickly, especially near expiration. This tool identifies *potential* premium-selling structures; it does not recommend trades or guarantee profitability.
