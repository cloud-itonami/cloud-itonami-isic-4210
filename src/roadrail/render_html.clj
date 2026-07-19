(ns roadrail.render-html
  "Build-time HTML renderer for docs/samples/operator-console.html.
  Closes flagship checklist item 2 (com-junkawasaki/root ADR-2607189300).
  Drives the REAL actor stack (roadrail.operation -> roadrail.governor ->
  roadrail.store -> roadrail.registry), with the real (mock, no-network)
  Notifier so the safety-concern-notice sends are real too (roadrail.
  notify/mock-notifier). No invented numbers, no timestamps, byte-
  identical across reruns. Scenario is adapted from `roadrail.sim`
  (verified against the real seed data / governor rules first, see
  `run-demo!` docstring below), NOT hand-typed HTML."
  (:require [clojure.string :as str]
            [roadrail.store :as store]
            [roadrail.operation :as op]
            [roadrail.phase :as phase]
            [roadrail.governor :as governor]
            [roadrail.notify :as notify]
            [langgraph.graph :as g]))

(def ^:private operator {:actor-id "op-1" :actor-role :site-supervisor :phase 3})

(defn- exec! [actor tid request]
  (g/run* actor {:request request :context operator} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}} {:thread-id tid :resume? true}))

(defn run-demo!
  "Drives the real OperationActor StateGraph through a scenario built
  directly from `roadrail.store/demo-data` and `roadrail.governor`'s
  actual checks -- adapted from `roadrail.sim` (verified real ids/ops
  against store.cljc + governor.cljc first, found trustworthy: every
  site id / op keyword below is exactly what `roadrail.store/demo-data`
  and `roadrail.governor/closed-op-allowlist` define). Exercises:
    (a) a phase-3 auto-commit op            -- :log-site-record site-1
    (b) always-escalate high-stakes ops,
        each followed by a human approve!   -- :schedule-construction-
                                                operation site-1,
                                                :flag-safety-concern
                                                site-1 (also a real,
                                                mock-notifier safety
                                                notice send),
                                                :order-supplies site-1
                                                above the cost threshold
    (c) a below-threshold :order-supplies auto-commit
    (d) six DISTINCT real HARD-hold rules, none of which ever reaches a
        human -- :no-legal-basis (site-2, ATL, uncovered jurisdiction),
        :site-not-verified + :utility-locate-incomplete together
        (site-3, JPN, neither ground-truth field set), :utility-locate-
        incomplete alone (site-4, JPN), :notification-lead-time-
        insufficient (site-5, JPN, 20h actual < 168h legal minimum),
        :unresolved-safety-concern (site-6, JPN), :unknown-op
        (:direct-equipment-command, outside the closed 4-op allowlist)
  Returns the resulting db + the notifier (so the render can show real
  sent-notice results)."
  []
  (let [db (store/seed-db)
        notifier (notify/mock-notifier)
        actor (op/build db {:notifier notifier})]
    ;; (a) phase-3 auto-commit -- pure data logging, no ground-truth booleans touched.
    (exec! actor "t1" {:op :log-site-record :subject "site-1"
                        :patch {:id "site-1" :grading-percent-complete 40}})

    ;; (b) always-escalate: construction-operation schedule proposal -> human approves.
    (exec! actor "t2" {:op :schedule-construction-operation :subject "site-1"
                        :window {:proposed-start-date "2026-08-01" :proposed-end-date "2026-08-15"}
                        :notes "路盤工先行、その後舗装工"})
    (approve! actor "t2")

    ;; (b) always-escalate: safety-concern flag -> human approves -> real (mock) notice send.
    (exec! actor "t3" {:op :flag-safety-concern :subject "site-1"
                        :concern-type :utility-strike
                        :concern-description "試掘溝で未表示のガス管を確認、埋設物損傷リスクの可能性。"})
    (approve! actor "t3")

    ;; (c) order-supplies below the cost threshold -- AUTO-COMMITS at phase 3.
    (exec! actor "t4" {:op :order-supplies :subject "site-1"
                        :items ["asphalt-mix-20t" "traffic-cones-100"]
                        :cost-usd 1200 :vendor "Local Paving Supply Co."})

    ;; (b) order-supplies above the cost threshold -- escalates, human approves.
    (exec! actor "t5" {:op :order-supplies :subject "site-1"
                        :items ["asphalt-paver-rental"] :cost-usd 18000 :vendor "Heavy Equip Rentals"})
    (approve! actor "t5")

    ;; (d) six distinct real HARD holds -- never reach a human.
    (exec! actor "t6" {:op :schedule-construction-operation :subject "site-2" :window {}})
    (exec! actor "t7" {:op :schedule-construction-operation :subject "site-3" :window {}})
    (exec! actor "t8" {:op :schedule-construction-operation :subject "site-4" :window {}})
    (exec! actor "t9" {:op :schedule-construction-operation :subject "site-5" :window {}})
    (exec! actor "t10" {:op :schedule-construction-operation :subject "site-6" :window {}})
    (exec! actor "t11" {:op :direct-equipment-command :subject "site-1"})

    {:db db :notifier notifier}))

;; ----------------------------- render helpers -----------------------------

(defn- esc [v]
  (-> (str v)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")
      (str/replace "\"" "&quot;")))

(defn- bool-cell [v]
  (cond (true? v) ["ok" "true"] (false? v) ["err" "false"] :else ["muted" "unknown"]))

(defn- row [cells]
  (str "<tr>" (apply str (map #(str "<td>" (esc %) "</td>") cells)) "</tr>\n"))

(defn- table [headers rows-html]
  (str "<table><thead><tr>"
       (apply str (map #(str "<th>" (esc %) "</th>") headers))
       "</tr></thead><tbody>\n"
       (apply str rows-html)
       "</tbody></table>\n"))

(defn- site-row [{site-name :name :keys [id jurisdiction site-verified? utility-locate-completed?
                                          safety-concern-unresolved? notification-lead-hours-actual status]}]
  (let [[vcls vtxt] (bool-cell site-verified?)
        [ucls utxt] (bool-cell utility-locate-completed?)
        [ccls _] (bool-cell (not safety-concern-unresolved?))]
    (str "<tr><td><code>" (esc id) "</code></td><td>" (esc site-name) "</td><td>" (esc jurisdiction) "</td>"
         "<td class=\"" vcls "\">" vtxt "</td>"
         "<td class=\"" ucls "\">" utxt "</td>"
         "<td class=\"" ccls "\">" (if safety-concern-unresolved? "unresolved" "clear") "</td>"
         "<td>" (esc (or notification-lead-hours-actual "n/a")) "</td>"
         "<td>" (esc (clojure.core/name status)) "</td></tr>\n")))

(defn- record-row [r]
  (row [(get r "record_id") (get r "kind") (get r "site_id") (get r "jurisdiction")]))

(defn- sent-row
  "One row per individual mock-transport send -- `notify/sent-log` is a
  FLAT log (one entry per -send-mail!/-send-phone-call! call across every
  contact), not grouped by contact -- see `roadrail.notify/MockNotifier`."
  [{:keys [status channel to subject message]}]
  (let [cls (if (= :sent status) "ok" "err")]
    (str "<tr><td>" (esc (clojure.core/name channel)) "</td><td>" (esc to) "</td>"
         "<td class=\"" cls "\">" (esc (clojure.core/name status)) "</td>"
         "<td>" (esc (or subject message)) "</td></tr>\n")))

(defn- ledger-status [{:keys [t basis]}]
  (cond
    (= :committed t)           ["ok" "committed"]
    (= :approval-granted t)    ["ok" "approval-granted"]
    (= :governor-hold t)       ["err" (str "governor-hold: " (str/join ", " (map name basis)))]
    (= :approval-rejected t)   ["err" "approval-rejected"]
    (= :approval-requested t)  ["warn" "approval-requested"]
    :else                      ["muted" "unknown"]))

(defn- ledger-row [{:keys [op subject disposition] :as fact}]
  (let [[cls label] (ledger-status fact)]
    (str "<tr><td>" (esc (name op)) "</td><td><code>" (esc subject) "</code></td>"
         "<td>" (esc (some-> disposition name)) "</td>"
         "<td class=\"" cls "\">" label "</td></tr>\n")))

(defn- phase-row [[n {:keys [label writes auto]}]]
  (row [(str "phase " n) label
        (str/join ", " (map name (sort writes)))
        (if (seq auto) (str/join ", " (map name (sort auto))) "(none -- always escalate/hold)")]))

(defn- high-stakes-row [op]
  (row [(name op) "always" "ALWAYS escalates to a human, every phase (see roadrail.governor ns docstring)"]))

(def ^:private css
  "table { width: 100%; border-collapse: collapse; font-size: 14px; }
.ok { color: #137a3f; }
body { font-family: system-ui,-apple-system,sans-serif; margin: 0; color: #1a1a1a; background: #fafafa; }
header.bar { display: flex; align-items: center; gap: 12px; padding: 12px 20px; background: #fff; border-bottom: 1px solid #e5e5e5; }
th, td { text-align: left; padding: 8px 10px; border-bottom: 1px solid #f0f0f0; }
h2 { margin-top: 0; font-size: 15px; }
.warn { color: #b25c00; background: #fff8e1; padding: 2px 6px; border-radius: 4px; }
main { max-width: 1080px; margin: 24px auto; padding: 0 20px 48px; }
header.bar h1 { font-size: 18px; margin: 0; font-weight: 600; }
.muted { color: #888; font-size: 13px; }
.critical { color: #fff; background: #b3261e; padding: 2px 6px; border-radius: 4px; font-weight: 600; }
.card { background: #fff; border: 1px solid #e5e5e5; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
.err { color: #b3261e; background: #fbe9e7; padding: 2px 6px; border-radius: 4px; }
th { font-weight: 600; color: #555; font-size: 12px; text-transform: uppercase; letter-spacing: 0.04em; }
header.bar .badge { margin-left: auto; font-size: 12px; color: #666; }
code { font-size: 12px; background: #f4f4f4; padding: 1px 4px; border-radius: 3px; }
p.note { color: #666; font-size: 13px; margin: 6px 0 14px; }")

(defn render [{:keys [db notifier]}]
  (let [sites (store/all-sites db)
        site-log (store/site-record-log-history db)
        schedules (store/schedule-proposal-history db)
        concerns (store/safety-concern-flag-history db)
        supply (store/supply-order-proposal-history db)
        sent (notify/sent-log notifier)
        ledger (store/ledger db)
        ph3 (get phase/phases phase/default-phase)]
    (str
     "<!doctype html>\n<html lang=\"ja\">\n<head>\n<meta charset=\"utf-8\">\n"
     "<title>roadrail.render-html -- Road &amp; Rail Construction Governor operator console</title>\n"
     "<style>\n" css "\n</style>\n</head>\n<body>\n"
     "<header class=\"bar\"><h1>Road &amp; Rail Construction Governor -- Operator Console</h1>"
     "<span class=\"badge\">ISIC 4210 &middot; phase " phase/default-phase
     " (" (:label ph3) ") &middot; coordination-only, effect always :propose</span></header>\n<main>\n"

     "<div class=\"card\"><h2>Site directory (real seed data, roadrail.store/demo-data)</h2>\n"
     (table ["site" "name" "jurisdiction" "site-verified?" "utility-locate-completed?"
             "safety-concern" "notification-lead-hours-actual" "status"]
            (map site-row sites))
     "</div>\n"

     "<div class=\"card\"><h2>Site-record-log (:log-site-record commits)</h2>\n"
     (table ["record_id" "kind" "site_id" "jurisdiction"] (map record-row site-log))
     "</div>\n"

     "<div class=\"card\"><h2>Schedule proposals (:schedule-construction-operation commits)</h2>\n"
     "<p class=\"note\">Always human-approved -- never auto-eligible at any phase (roadrail.phase ns docstring).</p>\n"
     (table ["record_id" "kind" "site_id" "jurisdiction"] (map record-row schedules))
     "</div>\n"

     "<div class=\"card\"><h2>Safety-concern flags (:flag-safety-concern commits)</h2>\n"
     "<p class=\"note\">Always human-approved -- unconditionally high-stakes, every phase.</p>\n"
     (table ["record_id" "kind" "site_id" "jurisdiction"] (map record-row concerns))
     "</div>\n"

     "<div class=\"card\"><h2>Supply-order proposals (:order-supplies commits)</h2>\n"
     (table ["record_id" "kind" "site_id" "jurisdiction"] (map record-row supply))
     "</div>\n"

     "<div class=\"card\"><h2>Safety-concern notices sent (real roadrail.notify, mock transport, no network)</h2>\n"
     (table ["channel" "to" "status" "subject / message"] (map sent-row sent))
     "</div>\n"

     "<div class=\"card\"><h2>Rollout phase gate (roadrail.phase/phases)</h2>\n"
     (table ["phase" "label" "writes allowed" "auto-commit eligible"]
            (map phase-row (sort-by key phase/phases)))
     "</div>\n"

     "<div class=\"card\"><h2>Permanent high-stakes ops (roadrail.governor/high-stakes)</h2>\n"
     "<p class=\"note\">Deliberately absent from every phase's :auto set -- a structural fact, not a rollout milestone (roadrail.phase ns docstring \"Actuation\").</p>\n"
     (table ["op" "escalates" "why"] (map high-stakes-row (sort governor/high-stakes)))
     "</div>\n"

     "<div class=\"card\"><h2>Audit ledger (roadrail.store/ledger, append-only, verbatim)</h2>\n"
     (table ["op" "subject" "disposition" "status / hard-hold basis"] (map ledger-row ledger))
     "</div>\n"

     "</main>\n</body></html>\n")))

(defn -main [& args]
  (let [out (or (first args) "docs/samples/operator-console.html")
        result (run-demo!)
        html (render result)]
    (spit out html)
    (println "wrote" out)))
