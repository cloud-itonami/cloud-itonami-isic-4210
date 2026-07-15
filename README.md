# cloud-itonami-4210

Open Business Blueprint for **ISIC Rev.5 4210**: construction of roads and railways.

This repository designs a forkable OSS business for road/railway-
construction-project operations coordination: run by a qualified operator
so a community keeps its own operating records instead of renting a
closed SaaS.

## Scope -- this is a COORDINATION-ONLY actor, not equipment control

This is a safety-critical domain: heavy equipment, traffic control near
public roads/rail, structural/civil engineering. **This actor does NOT
hold heavy-equipment-control authority, and it does NOT hold engineering-
design or grade-plan-finalization authority.** Both are the licensed
civil engineer / site supervisor's exclusive authority, always. The
Road-Rail Advisor (LLM) never issues an equipment-control command and
never finalizes an earthwork cross-section, track/road alignment or
pavement/ballast structural design; the independent **Road-Rail Governor**
HARD-blocks any proposal that even tries (un-overridable by any human
approval -- see `roadrail.governor` ns docstring). This actor coordinates
*potential* equipment dispatch (a proposed schedule window, a flagged
concern, a supply-order proposal) -- it never directly actuates.

Structurally, EVERY proposal this actor's advisor can produce carries
`:effect :propose`, and the Road-Rail Governor HARD-holds any proposal
that doesn't -- this is a permanent invariant distinguishing this actor
from `cloud-itonami-isic-4211` (the robotics-premise reference this actor
follows structurally), whose sibling actuation ops DO commit real-world
effects (mail dispatch, robot placement, structure handover).
`cloud-itonami-isic-4211`'s README robotics-premise framing therefore
does NOT apply verbatim here: this actor is deliberately narrower, the
same coordination-only shape as the sibling `cloud-itonami-isic-4311`
(demolition).

## Core Contract

```text
site/permit record + independent verification
        |
        v
Advisor -> Road-Rail Governor -> proceed (log/schedule/flag/order proposal), hold, or human approval
        |
        v
coordination artifacts (schedule proposal, safety-concern flag,
supply-order proposal) + audit ledger -- NEVER equipment dispatch,
NEVER a finalized engineering design or grade plan
```

No automated advice can propose a schedule the governor refuses, suppress
a safety-concern flag, or slip an equipment-control/design-finalization
marker past the governor -- and even a clean, governor-approved proposal
still always needs a human sign-off for scheduling and safety concerns
(see `Actuation` below).

## Capability layer

Resolves via [`kotoba-lang/industry`](https://github.com/kotoba-lang/industry)
(ISIC `4210`). Required capabilities:

- `:identity`
- `:forms`
- `:audit-ledger`
- `:notifications`

## Implemented slice (`src/roadrail`)

`blueprint.edn` names the governor `:road-rail-governor` and is now
`:implemented`. This repo implements it end-to-end -- **Road-Rail
Advisor ⊣ Road-Rail Governor** -- following the SAME `.cljc` actor
pattern (langgraph-clj StateGraph, mock-by-default advisor, dual
MemStore/Datomic backend, 0→3 phase rollout) every prior
`cloud-itonami-isic-*` actor in this fleet uses, structured after
[`cloud-itonami-isic-4211`](https://github.com/cloud-itonami/cloud-itonami-isic-4211)
(the robotics-premise construction-domain reference) and
[`cloud-itonami-isic-4311`](https://github.com/cloud-itonami/cloud-itonami-isic-4311)
(the demolition sibling this repo mirrors most closely), narrowed to
coordination-only authority as described above.

### Closed op-allowlist (4 ops, all `:effect :propose`)

| Op | Ask | Implementation |
|---|---|---|
| `:log-site-record` | survey / grading / paving-progress data logging | Normalizes and commits a patch onto the site's ground-truth fields (`:site-verified?`, `:utility-locate-completed?`, `:notification-lead-hours-actual`, concern resolution, etc.) and appends an immutable site-record-log entry. No direct capital/safety risk -- MAY auto-commit at phase 3. |
| `:schedule-construction-operation` | earthwork/paving/track-laying scheduling proposal | Drafts a proposed schedule WINDOW (never a finalized engineering design or grade plan). ALWAYS escalates to a human at every phase -- coordinates potential heavy-equipment dispatch. |
| `:flag-safety-concern` | surface a structural / traffic-control / utility-strike concern | Drafts a safety-concern flag; ALWAYS escalates to a human, unconditionally. Once approved, `roadrail.notify` sends the notice (mail + phone) to the site's licensed-engineer/site-supervisor/utility-authority contact roster. |
| `:order-supplies` | aggregate/asphalt/rail/equipment procurement proposal | Drafts a supply-order proposal. Escalates above a cost threshold or below the confidence floor; may auto-commit at phase 3 otherwise. |

**Legal basis is data, not code** -- `src/roadrail/facts.cljc`'s
`catalog` is the per-jurisdiction EDN source-of-truth the governor checks
every `:schedule-construction-operation` proposal against (JPN/USA/DEU
seeded; DEU stands in for the EU, the same convention
`construction.facts`/`demolition.facts`/`aerospace.facts` use for EASA):

| Jurisdiction | Utility-locate legal basis | Traffic-control/notification legal basis |
|---|---|---|
| 🇯🇵 Japan | 労働安全衛生規則（昭和47年労働省令第32号）第355条 -- [e-Gov](https://laws.e-gov.go.jp/law/347M50002000032) | 道路交通法 第77条（道路使用許可）/ 建設リサイクル法第10条・施行令第2条（土木工作物、請負代金500万円以上、着手7日前=168時間までの届出）-- [e-Gov 道路交通法](https://laws.e-gov.go.jp/law/335AC0000000105) / [e-Gov 建設リサイクル法](https://laws.e-gov.go.jp/law/412AC0000000104) |
| 🇺🇸 USA | OSHA 29 CFR 1926.651(b) (utility-locate 24-hour response floor) -- [osha.gov](https://www.osha.gov/laws-regs/regulations/standardnumber/1926/1926.651) | 23 CFR 645.213 (utility use-and-occupancy permit) + 23 CFR 630 Subpart J / MUTCD Part 6 (work-zone traffic control plan) -- [eCFR](https://www.ecfr.gov/current/title-23/chapter-I/subchapter-G/part-645/subpart-B/section-645.213) |
| 🇪🇺 EU (DEU proxy) | Directive 92/57/EEC Art.3 (construction-site safety plan) -- [EUR-Lex](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:31992L0057) | StVO §45 Abs.6 (traffic-control order before work affecting road traffic, qualitative -- state/municipal level, no fixed EU-wide day count) -- [gesetze-im-internet.de](https://www.gesetze-im-internet.de/stvo_2013/__45.html) |

Japan (168 hours / 7 calendar days) and the USA (24 hours) have real
numeric lead-time floors; the EU deliberately does NOT --
`roadrail.facts/notification-lead-insufficient?` reports `:qualitative`
there rather than fabricating a number, and `:schedule-construction-
operation` always routes to a human regardless of jurisdiction anyway
(see `Actuation` below). USA's 24-hour floor is a modeling translation of
OSHA's utility-locate-response wait requirement into a minimum recorded
lead pause before scheduled work start -- see `roadrail.facts` ns
docstring for the full honesty discipline about that translation. This
R0 catalog's citations are grounded in the public-right-of-way legal
exposure common to BOTH road and railway construction (utility-strike
prevention, traffic-control/road-occupancy permitting); railway-specific
technical/track-safety-standard regimes (e.g. USA's FRA Track Safety
Standards, 49 CFR Part 213) are real but out of scope for this slice --
a documented future extension, never fabricated in.

**Governor -- eight HARD checks, ALL un-overridable by human approval:**
unknown op (outside the closed 4-op allowlist), `:effect` not `:propose`,
forbidden action class (equipment-control / direct-actuation /
engineering-design-finalization / grade-plan-finalization markers), site
not independently verified/registered, legal-basis missing, utility
locate incomplete, notification lead time insufficient (quantitative
jurisdictions only), unresolved safety concern on file. See `roadrail.
governor` ns docstring for the full enumeration, rationale and real-law
citations behind each.

## Actuation

This actor performs **no real-world actuation** -- every committed
record carries `:effect :propose` (see `roadrail.governor` ns
docstring). `:schedule-construction-operation` and `:flag-safety-concern`
NEVER auto-commit at any phase -- both always need a human sign-off, even
when the governor is completely clean (`roadrail.phase` ns docstring
'Actuation' section, `roadrail.governor`'s `high-stakes` set).
`:log-site-record` (pure data logging) and `:order-supplies` BELOW the
cost threshold (`roadrail.governor/supply-order-cost-threshold-usd`) MAY
auto-commit at phase 3 when the governor is clean.

```bash
clojure -M:dev:run    # demo: full coordination episode + every HARD hold
clojure -M:dev:test   # test suite
clojure -M:lint       # clj-kondo, errors fail
```

## License

AGPL-3.0-or-later.
