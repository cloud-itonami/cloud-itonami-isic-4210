# Business Model: Construction of Roads and Railways

> **Generated baseline.** This is an honest, registry- and blueprint-grounded
> business-model baseline for a fleet actor. The flagship landings (Meta Job
> Search / Talent Board / Placement Desk) carry hand-written, domain-deep
> business models; fleet actors carry this generated baseline. Unit-economics
> figures below are illustrative and **not yet measured at fleet scale** — a
> shape, not a reported metric. Regenerate with
> `nbb scripts/gen-actor-business-model.cljs <repo>` in `kotoba-lang/industry`.
>
> **Exception — `## Revenue` is no longer generated (2026-08-10).** That
> section was replaced by hand with directly measured competitor pricing and a
> live Stripe Payment Link. Regenerating this file wholesale would overwrite it
> with the unfounded ¥50k–150k/月 band it was written to correct; re-apply the
> Revenue section by hand if you regenerate.

## Classification
- Repository: `cloud-itonami-isic-4210` ([github.com/cloud-itonami/cloud-itonami-isic-4210](https://github.com/cloud-itonami/cloud-itonami-isic-4210))
- ISIC Rev.5: `4210` — Construction of Roads and Railways
- Domain: `:construction/roads-and-railways`
- Social impact: :worker-safety :traffic-safety :public-mobility-access
- Actor: `:road-rail-governor` — an independent Governor in the fleet's Sealed-LLM
  ⊣ Governor pattern (langgraph-clj StateGraph, append-only audit ledger,
  Phase 0→3 rollout). Robotics authority: none — a HARD permanent block; this actor holds NO field-equipment-control authority, every real-world act is human-carried.

## Customer
An operator running this vertical as an OSS business — :construction/roads-and-railways — who wants
a governed execution scaffold they own instead of renting a closed SaaS.

## Offer
The actor coordinates the :intake :design :permit :build :inspect :audit pipeline behind an independent Governor: the
advisor proposes only; the Governor HARD-blocks any proposal that fails a
spec-basis / evidence / actuation check; every real-world actuation is a
human sign-off (never autonomous, at any phase); every decision is recorded
in an append-only audit ledger. The full governor-check enumeration for this
vertical lives in `blueprint.edn`'s `:itonami.blueprint/implemented-slice`
and the `README.md`.

Capability stack (required): :identity :forms :audit-ledger :notifications.

## Revenue
Self-host is AGPL-3.0-or-later (free). Managed tenancy and compliance
packages are the revenue.

| Package | Customer | Price shape |
|---|---|---|
| Self-host | any qualified operator | AGPL-3.0-or-later, free |
| Managed Starter | one road/rail contractor running 1–3 concurrent sites (10–20 site-agent/engineer seats; typically 20–80 employees) | ¥30,000/月 flat |
| Compliance package / setup | as scoped | quoted separately, not part of the Starter tier |

**Replaces the earlier ¥50k–150k/月 figure (2026-08-10).** This document
previously carried a ¥50k–150k/月 band inherited from the generated fleet
baseline, which had copied it from the sibling flagships. That band was
anchored on HR/recruiting/CRM **per-seat** SaaS and has no evidenced
relationship to road/railway construction coordination; measuring this market
directly put the real prices an order of magnitude lower. The old number is
recorded here rather than silently deleted, because the mistake — carrying a
price across industries without evidence — is the exact failure this section
now exists to prevent.

**Market-anchored (2026-08-10)**: benchmarked against 10 real products.
**4 of the 10 publish a price**, and the split is the finding:

> The products that disclose are the SMB site-photo/日報 apps and Fieldwire.
> **The tier that actually matches heavy civil road/rail work —
> [HCSS](https://www.hcss.com/pricing/) (heavy-civil specialist),
> [Procore](https://www.procore.com/pricing), [ANDPAD](https://andpad.jp/),
> [Buildertrend](https://buildertrend.com/pricing/) and
> [Raken](https://www.rakenapp.com/pricing) — discloses nothing**, all five
> routing to a quote form. HCSS states it plainly ("Share your needs and
> we'll build the right quote"); Procore quotes off Annual Construction
> Volume; ANDPAD lists four plan names with 初期費用・月額・オプション all
> 要問い合わせ; Buildertrend *withdrew* its published pricing in 2026 and
> moved to ACV-based quotes. [KANNA](https://lp.kanna4u.com/price) also
> publishes nothing on its own site (third-party comparison sites carry a
> figure, but it is not vendor-published and is not used here).

Published anchors, converted at ~¥150/$ for the assumed contractor size above:

| Product | Published price (as printed) | ≈ JPY/月 at assumed size |
|---|---|---|
| [現場Plus](https://archi.fukuicompu.co.jp/products/genba/low_cost.html) (ダイテック／福井コンピュータアーキテクト) | スタンダード 月額10,000円（税抜）/11,000円（税込）・60ID・年額120,000円（税抜）。PLAN2（検査機能付）月額15,000円（税抜）。初期費用は月額1ヶ月分、ID追加30ID 5,000円/月 | ¥11,000（60ID が想定規模を包含） |
| [蔵衛門クラウド](https://www.kuraemon.com/plan/) (ルクレ) | フリー ¥0／プレミアム 6ライセンスパック 月額9,000円（税抜）、10ライセンスパック 月額12,000円（税抜・年額144,000円）／エンタープライズ 月額800円〜/人（11名以上） | ¥12,000（税抜・10ライセンス） |
| [ミライ工事](https://www.miraikoji.com/plan/) | 法人プラン 月額2,750円/人（税込、5人以上）。個人向けはベーシック990円/月、プロフェッショナル2,178円/月（税込） | ¥41,250（15人） |
| [Fieldwire by Hilti](https://www.fieldwire.com/pricing/) | Basic "$0 per user / month", Pro "$39 per user / month billed annually", Business "$64", Business Plus "$89"; custom contracts on request | ¥87,750 (Pro × 15 users) |

The measured band is therefore **¥11,000–¥87,750/月**, with the Japanese
products clustered at ¥11,000–¥41,250. **¥30,000/月 sits near that cluster's
midpoint**, and the reasoning for that position is the same in both
directions. Downward: this actor is narrower than every product listed — no
photo or drawing management, no chat, no 工程表, no 日報・出面, no 見積・請求.
It cannot win on feature count, so it is not priced against Fieldwire or the
opaque heavy-civil tier, neither of whose workflows it replaces. Upward: it is
not placed at 現場Plus's ¥11,000 flat-60ID floor either, because it carries
something none of the ten has — a per-jurisdiction legal-basis catalog
(労働安全衛生規則第355条／道路交通法第77条／建設リサイクル法の着手7日=168時間前
届出、OSHA 29 CFR 1926.651(b) の24時間) that HARD-blocks a schedule proposal
until utility-locate and traffic-control lead time are satisfied, plus a
safety-concern flag that can never auto-commit, plus an immutable ledger of
every hold. That gate, not the feature list, is the thing being bought, and
it addresses utility-strike and public-right-of-way exposure that a photo app
does not touch.

For the parallel reasoning in a sibling coordination-only vertical, see
`cloud-itonami-isic-851`, which placed ¥25,000/月 inside a ¥11,000–¥56,000
measured band on the same "narrower than every comparator, but structurally
un-overridable" argument. This vertical is set slightly higher for the legal
exposure that road and rail work carries.

**Subscribe (2026-08-10)**: a live Stripe Payment Link for the Managed
Starter tier (¥30,000/月 flat) is available now —
[**subscribe to Managed Starter**](https://buy.stripe.com/fZu00kgD58tqfdS0vweEo04).
This is a no-code Stripe-hosted checkout on Gftd Japan 株式会社's live
account; nothing in this repo's actor code changed, and managed-tenant setup
is manual fulfillment today with no automated onboarding. **No contractor has
claimed or subscribed to this tier yet — this is a working checkout with zero
paid tenants, not a claim of existing revenue.**

## Unit Economics (worked example, illustrative)
One managed tenant (:construction/roads-and-railways):
- infrastructure: actor runtime + store — runs at decision time, not per query
- LLM cost: proposals only at each operating step — bounded, because lookups
  never call a model
- human-approval labor: every real-world actuation is a human sign-off — the
  real cost driver
- support: budget a few hours/月 until feeds and jurisdiction catalogs stabilize

**These figures are illustrative and not yet measured at fleet scale.** Track
per operator: decisions/月, % proposals HARD-held (data-quality signal),
actuation-approval hours, churn.

## Open Participation
Anyone may fork, run the demo, self-host, submit patches, and publish
jurisdiction catalog entries (with official citations — never fabricated).
itonami.cloud certification is required before an operator is listed,
receives leads, or runs managed tenants under the platform brand.

## Operator Trust Levels
| Level | Capability |
|---|---|
| Contributor | patches, docs, jurisdiction catalog entries, examples |
| Self-host operator | runs their own instance, no platform endorsement |
| Certified operator | listed on itonami.cloud after review |
| Managed operator | may receive leads and operate customer tenants |
| Core maintainer | can approve changes to governor, security and governance |

## Trust Controls
- a proposal the governor refuses is never committed or actuated
- every real-world actuation is a human sign-off (never autonomous, at any phase)
- every decision (commit OR hold) is recorded in the append-only audit ledger
- sensitive operating and personal data stays outside Git

## Non-Negotiables
- Do not commit real customer / operating / personal data.
- Do not bypass the `:road-rail-governor` for production decisions.
- Do not market an uncertified deployment as an itonami.cloud certified operator.
- Any jurisdiction licence / registration this vertical requires is the
  operator's own legal duty; the software is the governed execution scaffold,
  not the licence.
