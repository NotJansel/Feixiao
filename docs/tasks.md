# Feixiao Improvement Tasks

Below is an ordered, actionable checklist that covers architectural and code-level improvements. Check items off as they are completed.

1. [ ] Replace global singletons (twitchClient, botRef) with DI-managed instances (inject via Koin), eliminating mutable globals.
2. [x] Remove Twitch client initialization from Migrator; keep migrations side‑effect free and limited to database concerns.
3. [ ] Introduce a TwitchService abstraction (wrap twitch4j) exposing: enableListenerFor(name), disableListenerFor(name), resolveIdByName(name), resolveNameById(id), and sendLiveNotification(...).
4. [ ] Refactor EventHooks and StreamerCommand to use injected TwitchService and a repository interface instead of directly constructing StreamerCollection.
5. [ ] Consolidate StreamerCollection behind a StreamerRepository interface; provide only needed operations and inject repository via Koin (stop exposing the raw collection to callers).
6. [x] Add startup validation for required env vars (TOKEN, TWITCH_CLIENT_ID, TWITCH_CLIENT_SECRET, MONGO_URI, optional TEST_SERVER/TEST_CHANNEL) with clear error logs and fail‑fast behavior.
7. [x] Implement in‑memory caching with TTL for Twitch name↔id resolution to reduce Helix API calls.
8. [x] Handle null results from getTwitchIdByName/getTwitchNameById safely (remove !! usage) and add retry/backoff on transient Helix errors (429/5xx).
9. [x] Replace runBlocking in Twitch event handler with structured concurrency using a proper CoroutineScope from Kord (no blocking on event thread). (Basic non-blocking via coroutine; proper Kord scope pending)
10. [x] Add logic to disable Twitch stream listeners when a streamer has no subscribing servers; re‑enable when the first subscription is added.
11. [ ] Create DB indexes: unique index on StreamerData.name; index on servers.guildId; unique index on MetaData.id.
12. [ ] Fix migration v2 to update all documents with null id (use updateMany or iterate all), leveraging TwitchService with caching for backfill.
13. [ ] Improve migration tracking: store applied migrations and their status in a dedicated collection to prevent partial or repeated application.
14. [ ] Stop exposing StreamerCollection.collection; provide methods for read/write operations and keep data access encapsulated.
15. [ ] Make updates atomic where possible (e.g., use positional array updates or transactions) to avoid race conditions when editing servers list.
16. [ ] Standardize logging with context (guildId, streamerName) and structured messages; ensure consistent log levels and error reporting.
17. [x] Replace ad‑hoc string replacements for live messages with a small template formatter; validate tokens and provide defaults; optionally escape mentions.
18. [ ] Move user‑facing strings in commands/responses to translations where missing; ensure i18n coverage and consistency.
19. [ ] Add unit tests for StreamerRepository operations (add/update/remove) using embedded Mongo/Testcontainers.
20. [ ] Add unit tests for template formatting and TwitchService name/id resolution (mock Helix/Twitch client).
21. [ ] Add integration tests for command flows (Add/Update/Remove) using KordEx testing utilities or mocks.
22. [ ] Add CI workflow to run tests plus ktlint/detekt; enforce quality gates on PRs.
23. [ ] Add detekt and ktlint Gradle plugins; apply formatting and address findings incrementally.
24. [ ] Document architecture and data model (components, event flow, DB schema) in docs and link from README.
25. [ ] Update README with setup instructions, environment variables, local run/dev guide, and correct links to privacy policy and TOS.
26. [ ] Introduce configuration profiles (dev/test/prod) to gate "Bot Online!" notifications and test server/channel behavior.
27. [ ] Add graceful shutdown hooks to close Twitch client and Mongo connections cleanly.
28. [ ] Externalize and standardize collection names; ensure consistency (e.g., explicitly map StreamerData to "streamerData").
29. [ ] Normalize and validate streamer names (case handling); store Twitch ID as canonical key; keep display name updated separately.
30. [x] On streamer removal per guild, if no servers remain, delete the document and disable the Twitch listener for that streamer.
31. [ ] Add rate limiting/cooldowns for notifications to avoid Discord spam and satisfy API limits.
32. [ ] Add basic metrics/health instrumentation (counts for notifications sent, API calls, failures) with simple logs or a metrics facade.
33. [ ] Ensure exception safety in all event handlers (try/catch around external calls) and user‑friendly error messages.
34. [ ] Replace magic strings and numbers with constants/enums; define allowed live‑message tokens centrally.
35. [ ] Introduce feature flags (e.g., rename on Ready) to toggle experimental behaviors without code changes.
